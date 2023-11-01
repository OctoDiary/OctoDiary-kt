package org.bxkr.octodiary

import androidx.compose.runtime.mutableStateOf
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklist.MarkList
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.schoolinfo.SchoolInfo
import org.bxkr.octodiary.models.sessionuser.SessionUser
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
    lateinit var userId: Number
    val hasUserId get() = this::userId.isInitialized

    lateinit var sessionUser: SessionUser
    val hasSessionUser get() = this::sessionUser.isInitialized

    lateinit var eventCalendar: List<Event>
    val hasEventCalendar get() = this::eventCalendar.isInitialized

    lateinit var ranking: List<RankingMember>
    val hasRanking get() = this::ranking.isInitialized

    lateinit var classMembers: List<ClassMember>
    val hasClassMembers get() = this::classMembers.isInitialized

    lateinit var profile: ProfileResponse
    val hasProfile get() = this::profile.isInitialized

    lateinit var visits: VisitsResponse // FUTURE: REGIONAL_FEATURE
    val hasVisits get() = this::visits.isInitialized

    lateinit var marks: MarkList
    val hasMarks get() = this::marks.isInitialized

    lateinit var homeworks: List<Homework>
    val hasHomeworks get() = this::homeworks.isInitialized

    lateinit var mealBalance: MealBalance // FUTURE: REGIONAL_FEATURE
    val hasMealBalance get() = this::mealBalance.isInitialized

    lateinit var schoolInfo: SchoolInfo
    val hasSchoolInfo get() = this::schoolInfo.isInitialized

    val loadedEverything = mutableStateOf(false)

    var tokenExpirationHandler: (() -> Unit)? = null

    var onSingleItemInUpdateAllLoadedHandler: ((progress: Float) -> Unit)? = null

    var loadingStarted = false

    fun updateUserId(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        dSchoolApi.profilesId(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
            userId = body[0].id // FUTURE: USES_FIRST_CHILD
            onUpdated()
        }
    }

    fun updateSessionUser(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)
        schoolSessionApi.sessionUser(SessionUser.Body(token)).baseEnqueue(
            ::baseErrorFunction, ::baseInternalExceptionFunction
        ) { body ->
            sessionUser = body
            onUpdated()
        }
    }

    fun updateEventCalendar(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)
        secondaryApi.events(
            "Bearer $token",
            personIds = sessionUser.personId,
            beginDate = Calendar.getInstance().also {
                it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }.time.formatToDay(),
            endDate = Calendar.getInstance().also {
                it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }.time.formatToDay(),
            expandFields = "homework,marks"
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
            eventCalendar = body.response
            onUpdated()
        }
    }

    fun getMarkInfo(markId: Long, listener: (MarkInfo) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)
        mainSchoolApi.markInfo(
            token,
            markId = markId,
            studentId = sessionUser.profiles[0].id // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue(::baseErrorFunction) { listener(it) }
    }

    fun updateRanking(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)

        var rankingFinished = false
        var classMembersFinished = false

        // Ranking request:
        secondaryApi.classRanking(
            token,
            personId = sessionUser.personId,
            date = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            ranking = it
            rankingFinished = true
            if (classMembersFinished) onUpdated()
        }

        // Class members request for matching names:
        dSchoolApi.classMembers(
            token,
            classUnitId = profile.children[0].classUnitId // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            classMembers = it
            classMembersFinished = true
            if (rankingFinished) onUpdated()
        }
    }

    fun updateProfile(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        mainSchoolApi.profile(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            profile = it
            onUpdated()
        }
    }

    fun updateVisits(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)
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
            onUpdated()
        }
    }

    fun updateMarks(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)

        mainSchoolApi.markList(
            token,
            studentId = userId.toLong(),
            fromDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 1)
                time
            }.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            marks = it
            onUpdated()
        }
    }

    fun updateHomeworks(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)

        mainSchoolApi.homeworks(
            token,
            studentId = userId.toLong(),
            fromDate = Date().formatToDay(),
            toDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) + 1)
                time
            }.formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            homeworks = it.payload
            onUpdated()
        }
    }

    fun updateMealBalance(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        assert(subsystem == Diary.MES)

        dSchoolApi.mealBalance(
            token,
            contractId = profile.children[0].contractId // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            mealBalance = it
            onUpdated()
        }
    }

    fun updateSchoolInfo(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.schoolInfo(
            token,
            schoolId = profile.children[0].school.id, // FUTURE: USES_FIRST_CHILD
            classUnitId = profile.children[0].classUnitId // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            schoolInfo = it
            onUpdated()
        }
    }

    fun updateAll() {
        if (loadingStarted) return else loadingStarted = true
        val onSingleItemLoad = { name: String ->
            val allStates = listOf(
                hasUserId,
                hasSessionUser,
                hasEventCalendar,
                hasRanking,
                hasClassMembers,
                hasProfile,
                hasVisits.takeIf { subsystem == Diary.MES } ?: true,
                hasMarks,
                hasHomeworks,
                hasMealBalance.takeIf { subsystem == Diary.MES } ?: true,
                hasSchoolInfo
            )
            onSingleItemInUpdateAllLoadedHandler?.invoke((allStates.count { it }
                .toFloat()) / (allStates.size.toFloat()))
            if (!(allStates.contains(false))) {
                loadedEverything.value = true
            }
            println("$name response is loaded")
        }
        updateUserId {
            onSingleItemLoad(::userId.name)
            updateSessionUser {
                onSingleItemLoad(::sessionUser.name)
                updateEventCalendar { onSingleItemLoad(::eventCalendar.name) }
                updateProfile {
                    onSingleItemLoad(::profile.name)
                    updateRanking {
                        onSingleItemLoad(::classMembers.name)
                        onSingleItemLoad(::ranking.name)
                    }
                    if (subsystem == Diary.MES) updateVisits { onSingleItemLoad(::visits.name) }
                    if (subsystem == Diary.MES) updateMealBalance { onSingleItemLoad(::mealBalance.name) }
                    updateSchoolInfo { onSingleItemLoad(::schoolInfo.name) }
                }
            }
            updateMarks { onSingleItemLoad(::marks.name) }
            updateHomeworks { onSingleItemLoad(::homeworks.name) }
        }
    }
}