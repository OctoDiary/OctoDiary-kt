package org.bxkr.octodiary

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.models.lessonschedule.LessonSchedule
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklistdate.MarkListDate
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.profilesid.ProfilesId
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.models.schoolinfo.SchoolInfo
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.models.subjectranking.SubjectRanking
import org.bxkr.octodiary.models.visits.VisitsResponse
import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import java.util.Calendar
import java.util.Date

object DataService {
    lateinit var subsystem: Diary
    lateinit var mainSchoolApi: MainSchoolAPI
    lateinit var dSchoolApi: DSchoolAPI
    lateinit var secondaryApi: SecondaryAPI
    lateinit var schoolSessionApi: SchoolSessionAPI

    lateinit var token: String

    val listOfValues
        get() = listOfNotNull(
            ::userId,
            ::sessionUser,
            ::eventCalendar,
            ::ranking,
            ::classMembers,
            ::profile,
            ::visits.takeIf { subsystem == Diary.MES },
            ::marksDate,
            ::marksSubject,
            ::homeworks,
            ::mealBalance.takeIf { subsystem == Diary.MES },
            ::schoolInfo,
            ::subjectRanking
        )

    val listOfStates
        get() = listOfNotNull(
            ::hasUserId,
            ::hasSessionUser,
            ::hasEventCalendar,
            ::hasRanking,
            ::hasClassMembers,
            ::hasProfile,
            ::hasVisits.takeIf { subsystem == Diary.MES },
            ::hasMarksDate,
            ::hasMarksSubject,
            ::hasHomeworks,
            ::hasMealBalance.takeIf { subsystem == Diary.MES },
            ::hasSchoolInfo,
            ::hasSubjectRanking
        )

    val mapOfDemoResourceIds = mapOf(
        ::userId to R.raw.demo_user_id,
        ::sessionUser to R.raw.demo_session_user,
        ::eventCalendar to R.raw.demo_event_calendar,
        ::ranking to R.raw.demo_ranking,
        ::classMembers to R.raw.demo_class_members,
        ::profile to R.raw.demo_profile,
        ::visits to R.raw.demo_visits,
        ::marksDate to R.raw.demo_marks_date,
        ::marksSubject to R.raw.demo_marks_subject,
        ::homeworks to R.raw.demo_homeworks,
        ::mealBalance to R.raw.demo_meal_balance,
        ::schoolInfo to R.raw.demo_school_info,
        ::subjectRanking to R.raw.demo_subject_ranking
    ).mapKeys { it.key.name }

    lateinit var userId: ProfilesId
    var hasUserId = false

    lateinit var sessionUser: SessionUser
    var hasSessionUser = false

    lateinit var eventCalendar: List<Event>
    var hasEventCalendar = false

    lateinit var ranking: List<RankingMember>
    var hasRanking = false

    lateinit var classMembers: List<ClassMember>
    var hasClassMembers = false

    lateinit var subjectRanking: List<SubjectRanking>
    var hasSubjectRanking = false

    lateinit var profile: ProfileResponse
    var hasProfile = false

    lateinit var visits: VisitsResponse
    var hasVisits = false

    lateinit var marksDate: MarkListDate
    var hasMarksDate = false

    lateinit var marksSubject: List<MarkListSubjectItem>
    var hasMarksSubject = false

    lateinit var homeworks: List<Homework>
    var hasHomeworks = false

    lateinit var mealBalance: MealBalance
    var hasMealBalance = false

    lateinit var schoolInfo: SchoolInfo
    var hasSchoolInfo = false

    // ADD_NEW_FIELD_HERE
    // Don't forget to add demo cache data in res/raw folder, preferably with MES flavor

    val loadedEverything = mutableStateOf(false)

    var tokenExpirationHandler: (() -> Unit)? = null

    var onSingleItemInUpdateAllLoadedHandler: ((name: String, progress: Float) -> Unit)? = null

    var loadingStarted = false

    var currentProfile = 0

    fun updateUserId(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        dSchoolApi.profilesId(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
                if (body.size == 0) {
                    tokenExpirationHandler?.invoke()
                } else {
                    userId = body
                    hasUserId = true
                    onUpdated()
                }
            }
    }

    fun updateSessionUser(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)
        schoolSessionApi.sessionUser(SessionUser.Body(token)).baseEnqueue(
            ::baseErrorFunction, ::baseInternalExceptionFunction
        ) { body ->
            sessionUser = body
            hasSessionUser = true
            onUpdated()
        }
    }

    fun updateEventCalendar(weeksBefore: Int = 1, weeksAfter: Int = 1, onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        secondaryApi.events(
            "Bearer $token",
            personIds = profile.children[currentProfile].contingentGuid,
            beginDate = Calendar.getInstance().also {
                it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) - weeksBefore)
                it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }.time.formatToDay(),
            endDate = Calendar.getInstance().also {
                it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) + weeksAfter)
                it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }.time.formatToDay(),
            expandFields = "homework,marks"
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
            eventCalendar = body.response
            hasEventCalendar = true
            onUpdated()
        }
    }

    fun getMarkInfo(markId: Long, listener: (MarkInfo) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        mainSchoolApi.markInfo(
            token,
            markId = markId,
            studentId = profile.children[currentProfile].studentId
        ).baseEnqueue(::baseErrorFunction) { listener(it) }
    }

    fun updateRanking(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        var rankingFinished = false
        var classMembersFinished = false

        // Ranking request:
        secondaryApi.classRanking(
            token,
            personId = profile.children[currentProfile].contingentGuid,
            date = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            ranking = it
            hasRanking = true
            rankingFinished = true
            if (classMembersFinished) onUpdated()
        }

        // Class members request for matching names:
        dSchoolApi.classMembers(
            token,
            classUnitId = profile.children[currentProfile].classUnitId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            classMembers = it
            hasClassMembers = true
            classMembersFinished = true
            if (rankingFinished) onUpdated()
        }
    }

    fun updateSubjectRanking(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        secondaryApi.subjectRanking(
            token,
            profile.children[currentProfile].contingentGuid,
            Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction) {
            subjectRanking = it
            hasSubjectRanking = true
            onUpdated()
        }
    }

    fun updateProfile(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        mainSchoolApi.profile(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                profile = it
                hasProfile = true
                onUpdated()
            }
    }

    fun updateVisits(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        assert(subsystem == Diary.MES)

        mainSchoolApi.visits(
            token,
            profile.children[0].contractId,
            fromDate = Calendar.getInstance().apply {
                time = Date()
                set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 7)
            }.time.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { visitsResponse ->
            visits = VisitsResponse(
                payload = visitsResponse.payload.sortedByDescending {
                    it.date.parseFromDay().toInstant().toEpochMilli()
                }
            )
            hasVisits = true
            onUpdated()
        }
    }

    fun updateMarksDate(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.markList(
            token,
            studentId = profile.children[currentProfile].studentId,
            fromDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 1)
                time
            }.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            marksDate = it
            hasMarksDate = true
            onUpdated()
        }
    }

    fun updateMarksSubject(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.subjectMarks(
            token,
            studentId = profile.children[currentProfile].studentId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            marksSubject = it.payload
            hasMarksSubject = true
            onUpdated()
        }
    }

    fun updateHomeworks(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.homeworks(
            token,
            studentId = profile.children[currentProfile].studentId,
            fromDate = Date().formatToDay(),
            toDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) + 1)
                time
            }.formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            homeworks = it.payload
            hasHomeworks = true
            onUpdated()
        }
    }

    fun updateMealBalance(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        assert(subsystem == Diary.MES)

        dSchoolApi.mealBalance(
            token,
            contractId = profile.children[currentProfile].contractId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            mealBalance = it
            hasMealBalance = true
            onUpdated()
        }
    }

    fun updateSchoolInfo(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.schoolInfo(
            token,
            schoolId = profile.children[currentProfile].school.id,
            classUnitId = profile.children[currentProfile].classUnitId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            schoolInfo = it
            hasSchoolInfo = true
            onUpdated()
        }
    }

    fun getRankingForSubject(subjectId: Long, listener: (List<RankingForSubject>) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        secondaryApi.rankingForSubject(
            token,
            profile.children[currentProfile].contingentGuid,
            profile.children[currentProfile].classUnitId,
            Date().formatToDay(),
            subjectId
        ).baseEnqueue(::baseErrorFunction) { listener(it) }
    }

    fun refreshToken(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        secondaryApi.refreshToken("Bearer $token")
            .baseEnqueue(::baseErrorFunction) {
                token = it
                updateUserId { onUpdated() }
            }
    }

    fun setHomeworkDoneState(homeworkId: Long, state: Boolean, listener: () -> Unit) {
        assert(this::token.isInitialized)

        if (state) {
            mainSchoolApi.doHomework(token, homeworkId)
                .baseEnqueue(::baseErrorFunction) { listener() }
        } else {
            mainSchoolApi.undoHomework(token, homeworkId)
                .baseEnqueue(::baseErrorFunction) { listener() }
        }
    }

    fun getLessonInfo(lessonId: Long, listener: (LessonSchedule) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.lessonSchedule(
            token,
            lessonId,
            profile.children[currentProfile].studentId
        ).baseEnqueue(::baseErrorFunction) {
            listener(it)
        }
    }

    fun updateAll() {
        if (loadingStarted) return else loadingStarted = true
        listOfStates.forEach { it.set(false) }
        val onSingleItemLoad = { name: String ->
            val statesInit = listOfStates.map { it.get() }
            onSingleItemInUpdateAllLoadedHandler?.invoke(name, (statesInit.count { it }
                .toFloat()) / (statesInit.size.toFloat()))
            if (!(statesInit.contains(false))) {
                loadedEverything.value = true
            }
            println("$name response is loaded, $statesInit")
        }
        updateUserId {
            onSingleItemLoad(::userId.name)
            updateSessionUser {
                onSingleItemLoad(::sessionUser.name)
                updateProfile {
                    onSingleItemLoad(::profile.name)
                    updateEventCalendar { onSingleItemLoad(::eventCalendar.name) }
                    updateMarksDate { onSingleItemLoad(::marksDate.name) }
                    updateMarksSubject { onSingleItemLoad(::marksSubject.name) }
                    updateHomeworks { onSingleItemLoad(::homeworks.name) }
                    updateRanking {
                        onSingleItemLoad(::classMembers.name)
                        onSingleItemLoad(::ranking.name)
                    }
                    updateSubjectRanking { onSingleItemLoad(::subjectRanking.name) }
                    if (subsystem == Diary.MES) updateVisits { onSingleItemLoad(::visits.name) }
                    if (subsystem == Diary.MES) updateMealBalance { onSingleItemLoad(::mealBalance.name) }
                    updateSchoolInfo { onSingleItemLoad(::schoolInfo.name) }
                }
            }
            refreshToken {}
        }
    }

    fun loadFromCache(get: (String) -> String) {
        listOfValues.map { it.name }.forEach {
            javaClass.getDeclaredField(it)
                .set(this, Gson().fromJson(get(it), javaClass.getDeclaredField(it).genericType))
        }
    }

    fun Context.loadDemoCache() =
        DataService.loadFromCache {
            resources.openRawResource(
                DataService.mapOfDemoResourceIds.getValue(
                    it
                )
            ).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }

}