package org.bxkr.octodiary

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.bxkr.octodiary.models.avatar.Avatar
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classmembers.OctoClassMembers
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.daysbalanceinfo.DaysBalanceInfo
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.govexams.GovExamsResponse
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.models.lesson2.LessonResponse
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklistdate.MarkListDate
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.mealsmenucomplexes.MealsMenuComplexes
import org.bxkr.octodiary.models.persondata.PersonData
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.profilesid.ProfilesId
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.models.schoolinfo.SchoolInfo
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.models.subjectranking.SubjectRanking
import org.bxkr.octodiary.models.visits.VisitsResponse
import org.bxkr.octodiary.network.MESLoginService.refreshToken
import org.bxkr.octodiary.network.NetworkService.externalApi
import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.utils.measurePerformance
import java.util.Calendar
import java.util.Date

object DataService {
    lateinit var subsystem: Diary
    lateinit var mainSchoolApi: MainSchoolAPI
    lateinit var dSchoolApi: DSchoolAPI
    lateinit var secondaryApi: SecondaryAPI
    lateinit var schoolSessionApi: SchoolSessionAPI

    var token: String = ""

    var userId: ProfilesId = ProfilesId()
    var hasUserId = true

    var sessionUser: SessionUser = SessionUser("mock")
    var hasSessionUser = true

    var eventCalendar: List<Event> = emptyList()
    var hasEventCalendar = true

    var eventsRange: List<Long> = emptyList()

    var ranking: List<RankingMember> = emptyList()
    var hasRanking = true

    var classMembers: List<ClassMember> = emptyList()
    var hasClassMembers = true

    var subjectRanking: List<SubjectRanking> = emptyList()
    var hasSubjectRanking = true

    var profile: ProfileResponse = ProfileResponse(
        children = listOf(
            org.bxkr.octodiary.models.profile.Children(
                birthDate = "", classLevelId = 0, className = "", classUnitId = 0, contingentGuid = "", contractId = 0,
                email = null, enrollmentDate = "", firstName = "Mock", groups = emptyList(), studentId = 0, isLegalRepresentative = false,
                lastName = "User", middleName = "", parallelCurriculumId = 0, phone = "", representatives = emptyList(),
                school = org.bxkr.octodiary.models.profile.School("", 0, 0, "", "", "", ""), sections = emptyList(), sex = "", snils = "",
                sudirAccountExists = false, sudirLogin = null, type = null, userId = 0
            )
        ),
        hash = "",
        profile = org.bxkr.octodiary.models.profile.Profile("", null, null, "Mock", 0, "User", "", "", "", "", "", 0)
    )
    var hasProfile = true

    var visits: VisitsResponse = VisitsResponse(emptyList())
    var hasVisits = true

    var marksDate: MarkListDate = MarkListDate(emptyList())
    var hasMarksDate = true

    var marksSubject: List<MarkListSubjectItem> = emptyList()
    var hasMarksSubject = true

    var homeworks: List<org.bxkr.octodiary.models.homeworks2.Homework> = emptyList()
    var hasHomeworks = true

    lateinit var mealBalance: MealBalance
    var hasMealBalance = false

    lateinit var schoolInfo: SchoolInfo
    var hasSchoolInfo = false

    var personData: PersonData = PersonData(emptyList())
    var hasPersonData = true

    var daysBalanceInfo: DaysBalanceInfo = DaysBalanceInfo(emptyList(), false)
    var hasDaysBalanceInfo = true
    var daysBalanceInfoCompleted = true

    var mealsMenuComplexes: MealsMenuComplexes = MealsMenuComplexes(emptyList())
    var hasMealsMenuComplexes = true

    var govExams: GovExamsResponse = GovExamsResponse(emptyList(), "")
    var hasGovExams = true

    var avatars: List<Avatar> = emptyList()
    var hasAvatars = true

    val states
        get() =
            listOfNotNull(
                ::hasUserId,
                ::hasSessionUser,
                ::hasEventCalendar,
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
                ::hasPersonData,
                ::hasDaysBalanceInfo.takeIf { subsystem == Diary.MES },
                ::hasMealsMenuComplexes.takeIf { subsystem == Diary.MES },
                ::hasSubjectRanking,
                ::hasGovExams,
                ::hasAvatars
            )

    val fields
        get() =
            listOfNotNull(
                ::userId,
                ::sessionUser,
                ::eventCalendar,
                ::eventsRange,
                ::ranking,
                ::classMembers,
                ::profile,
                ::visits.takeIf { subsystem == Diary.MES },
                ::marksDate,
                ::marksSubject,
                ::homeworks,
                ::mealBalance.takeIf { subsystem == Diary.MES },
                ::schoolInfo,
                ::personData,
                ::daysBalanceInfo.takeIf { subsystem == Diary.MES },
                ::mealsMenuComplexes.takeIf { subsystem == Diary.MES },
                ::subjectRanking,
                ::govExams,
                ::avatars
            )

    val mapOfDemoResourceIds = mapOf(
        ::userId to R.raw.demo_user_id,
        ::sessionUser to R.raw.demo_session_user,
        ::eventCalendar to R.raw.demo_event_calendar,
        ::eventsRange to R.raw.demo_events_range,
        ::ranking to R.raw.demo_ranking,
        ::classMembers to R.raw.demo_class_members,
        ::profile to R.raw.demo_profile,
        ::visits to R.raw.demo_visits,
        ::marksDate to R.raw.demo_marks_date,
        ::marksSubject to R.raw.demo_marks_subject,
        ::homeworks to R.raw.demo_homeworks,
        ::mealBalance to R.raw.demo_meal_balance,
        ::schoolInfo to R.raw.demo_school_info,
        ::personData to R.raw.demo_person_data,
        ::daysBalanceInfo to R.raw.demo_days_balance_info,
        ::mealsMenuComplexes to R.raw.demo_meals_menu_complexes,
        ::subjectRanking to R.raw.demo_subject_ranking,
        ::govExams to R.raw.demo_gov_exams,
        ::avatars to R.raw.demo_avatars
    ).mapKeys { it.key.name }

    val loadedEverything = mutableStateOf(false)

    var tokenExpirationHandler: (() -> Unit)? = null

    var onSingleItemInUpdateAllLoadedHandler: ((name: String, progress: Float) -> Unit)? = null

    var loadingStarted = false

    var currentProfile = 0

    fun initCacheManager(context: Context) {
        // Mock
    }

    fun clearAllCaches() {
        // Mock
    }

    fun clearCacheIfLowMemory(context: Context) {
        // Mock
    }

    /**
     * Resets all data to default states.
     * Useful for logout or hard reset.
     */
    fun clearData() {
        token = ""
        userId = ProfilesId()
        sessionUser = SessionUser("mock")
        eventCalendar = emptyList()
        eventsRange = emptyList()
        ranking = emptyList()
        classMembers = emptyList()
        subjectRanking = emptyList()
        visits = VisitsResponse(emptyList())
        marksDate = MarkListDate(emptyList())
        marksSubject = emptyList()
        homeworks = emptyList()
        personData = PersonData(emptyList())
        daysBalanceInfo = DaysBalanceInfo(emptyList(), false)
        mealsMenuComplexes = MealsMenuComplexes(emptyList())
        govExams = GovExamsResponse(emptyList(), "")
        avatars = emptyList()
        
        // Reset flags
        states.filterNotNull().forEach { it.set(false) }
        loadingStarted = false
        loadedEverything.value = false
    }

    fun updateUserId(onUpdated: () -> Unit) {
        if (this::dSchoolApi.isInitialized) {
            dSchoolApi.profilesId(token)
                .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
                    if (body.isEmpty()) {
                        tokenExpirationHandler?.invoke()
                    } else {
                        userId = body
                        hasUserId = true
                        onUpdated()
                    }
                }
        }
    }

    fun updateSessionUser(onUpdated: () -> Unit) {
        sessionUser = SessionUser("a")
        hasSessionUser = true
        onUpdated()
    }

    fun updateEventCalendar(weeksBefore: Int = 0, weeksAfter: Int = 0, onUpdated: () -> Unit) {
        val startDate = Calendar.getInstance().also {
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) - weeksBefore)
            it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val endDate = Calendar.getInstance().also {
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) + weeksAfter)
            it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        
        if (this::secondaryApi.isInitialized) {
            secondaryApi.events(
                "Bearer $token",
                personIds = profile.children[currentProfile].contingentGuid,
                beginDate = startDate.time.formatToDay(),
                endDate = endDate.time.formatToDay(),
                expandFields = "homework,marks"
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
                eventCalendar = body.response
                eventsRange = listOf(startDate.time.time, endDate.time.time)
                hasEventCalendar = true
                onUpdated()
            }
        } else {
            eventCalendar = emptyList()
            eventsRange = listOf(startDate.time.time, endDate.time.time)
            hasEventCalendar = true
            onUpdated()
        }
    }

    fun getEventWeek(date: Date, listener: (events: List<Event>, range: List<Long>) -> Unit) {
        val startDate = Calendar.getInstance().also {
            it.time = date
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR))
            it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val endDate = Calendar.getInstance().also {
            it.time = date
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR))
            it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        
        if (this::secondaryApi.isInitialized) {
            secondaryApi.events(
                "Bearer $token",
                personIds = profile.children[currentProfile].contingentGuid,
                beginDate = startDate.time.formatToDay(),
                endDate = endDate.time.formatToDay(),
                expandFields = "homework,marks"
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
                listener(body.response, listOf(startDate.time.time, endDate.time.time))
            }
        } else {
            listener(emptyList(), listOf(startDate.time.time, endDate.time.time))
        }
    }

    fun getMarkInfo(markId: Long, errorListener: (String) -> Unit, listener: (MarkInfo) -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.markInfo(
                token,
                markId = markId,
                studentId = profile.children[currentProfile].studentId
            ).baseEnqueue(errorListenerForMessage(errorListener)) { listener(it) }
        }
    }

    fun updateRanking(onUpdated: () -> Unit) {
        // Initialize independent states
        classMembers = emptyList()
        hasClassMembers = true
        
        if (!this::secondaryApi.isInitialized) {
            ranking = emptyList()
            hasRanking = true
            onUpdated()
            return
        }

        secondaryApi.classRanking(
            token,
            personId = profile.children[currentProfile].contingentGuid,
            date = Date().formatToDay()
        ).baseEnqueue({ errorBody: ResponseBody, httpCode: Int, className: String? ->
            val errorText = errorBody.string()
            if (errorText.contains("Рейтинг не доступен.")) {
                ranking = emptyList()
                hasRanking = true
                onUpdated()
            } else {
                baseErrorFunction(errorBody, httpCode, className)
            }
        }, ::baseInternalExceptionFunction) {
            ranking = it
            hasRanking = true
            onUpdated()
        }
    }

    fun updateCustomClassMembers(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.pullUserSettingsRaw(token, "od_class_members_assignments")
                .baseEnqueue({ _, _, _ -> onUpdated() }, ::baseInternalExceptionFunction) { unparsed ->
                    val parsed = unparsed.fromJson<OctoClassMembers>()
                    if (parsed != null) parsed.assignments.let { assignments ->
                        if (assignments != null) {
                            val assignmentsStudentIds =
                                assignments.map { assignment -> assignment.studentId }
                            val newClassMembers = classMembers.map {
                                if (it.studentId in assignmentsStudentIds) {
                                    it.copy(personId = assignments.first { assignment -> assignment.studentId == it.studentId }.personId)
                                } else it
                            }
                            classMembers = newClassMembers
                        }
                        onUpdated()
                    } else onUpdated()
                }
        } else {
            onUpdated()
        }
    }

    fun updateSubjectRanking(onUpdated: () -> Unit) {
        if (this::secondaryApi.isInitialized) {
            secondaryApi.subjectRanking(
                token,
                profile.children[currentProfile].contingentGuid,
                Date().formatToDay()
            ).baseEnqueue({ errorBody: ResponseBody, httpCode: Int, className: String? ->
                val errorText = errorBody.string()

                if (errorText.contains("Рейтинг не доступен.")) {
                    subjectRanking = emptyList()
                    hasSubjectRanking = true
                    onUpdated()
                } else {
                    baseErrorFunction(errorBody, httpCode, className)
                }
            }) {
                subjectRanking = it
                hasSubjectRanking = true
                onUpdated()
            }
        } else {
            subjectRanking = emptyList()
            hasSubjectRanking = true
            onUpdated()
        }
    }

    fun updateProfile(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.profile(token)
                .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                    profile = it
                    hasProfile = true
                    onUpdated()
                }
        } else {
            hasProfile = true
            onUpdated()
        }
    }

    fun updateVisits(onUpdated: () -> Unit) {
        if (subsystem == Diary.MES && this::mainSchoolApi.isInitialized) {
            mainSchoolApi.visits(
                token,
                profile.children[0].contractId,
                fromDate = Calendar.getInstance().apply {
                    time = Date()
                    set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 61)
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
        } else {
            hasVisits = true
            onUpdated()
        }
    }

    fun updateMarksDate(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.markList(
                token,
                studentId = profile.children[currentProfile].studentId,
                fromDate = Calendar.getInstance().run {
                    set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 4)
                    time
                }.formatToDay(),
                toDate = Date().formatToDay()
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                marksDate = it
                hasMarksDate = true
                onUpdated()
            }
        } else {
            hasMarksDate = true
            onUpdated()
        }
    }

    fun updateMarksSubject(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.subjectMarks(
                token,
                studentId = profile.children[currentProfile].studentId
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                marksSubject = it.payload
                hasMarksSubject = true
                onUpdated()
            }
        } else {
            hasMarksSubject = true
            onUpdated()
        }
    }

    fun updateHomeworks(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
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
        } else {
            hasHomeworks = true
            onUpdated()
        }
    }

    fun updateMealBalance(onUpdated: () -> Unit) {
        if (subsystem == Diary.MES && this::dSchoolApi.isInitialized) {
            dSchoolApi.mealBalance(
                token,
                contractId = profile.children[currentProfile].contractId
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                mealBalance = it
                hasMealBalance = true
                onUpdated()
            }
        } else {
            onUpdated()
        }
    }

    fun updateSchoolInfo(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.schoolInfo(
                token,
                schoolId = profile.children[currentProfile].school.id,
                classUnitId = profile.children[currentProfile].classUnitId
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                schoolInfo = it
                hasSchoolInfo = true
                onUpdated()
            }
        } else {
            onUpdated()
        }
    }


    fun updatePersonData(onUpdated: () -> Unit) {
        personData = PersonData(emptyList())
        hasPersonData = true
        onUpdated()
    }

    fun updateDaysBalanceInfo(onUpdated: () -> Unit) {
        daysBalanceInfo = DaysBalanceInfo(emptyList(), false)
        hasDaysBalanceInfo = true
        daysBalanceInfoCompleted = false
        onUpdated()

        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.daysBalanceInfo(
                accessToken = token,
                personId = profile.children[currentProfile].contingentGuid,
                from = "${Date().formatToDay()}T00:00:00.000Z",
                withPayments = true,
                limit = Int.MAX_VALUE
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                daysBalanceInfo = it
                daysBalanceInfoCompleted = true
                onSingleItemInUpdateAllLoadedHandler?.invoke("daysBalanceInfo", 100f)
            }
        } else {
            daysBalanceInfoCompleted = true
        }
    }

    fun updateMealsMenuComplexes(onUpdated: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.mealsMenuComplexes(
                accessToken = token,
                personId = profile.children[currentProfile].contingentGuid,
                onDate = Date().formatToDay()
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                mealsMenuComplexes = it
                hasMealsMenuComplexes = true
                onUpdated()
            }
        } else {
            hasMealsMenuComplexes = true
            onUpdated()
        }
    }

    fun updateGovExams(onUpdated: () -> Unit) {
        val onError = {
            govExams = GovExamsResponse(listOf(), "OK")
            hasGovExams = true
            onUpdated()
        }

        if (this::secondaryApi.isInitialized) {
            secondaryApi.govExams(
                "Bearer $token",
                profile.children[currentProfile].contingentGuid
            ).baseEnqueue({ _, _, _ -> onError() }, { _, _ -> onError() }) {
                govExams = it
                hasGovExams = true
                onUpdated()
            }
        } else {
            onError()
        }
    }

    fun updateAvatars(onUpdated: () -> Unit) {
        if (this::secondaryApi.isInitialized) {
            secondaryApi.avatars(
                "Bearer $token",
                profile.children[currentProfile].contingentGuid
            ).baseEnqueue({ _, _, _ ->
                avatars = emptyList()
                hasAvatars = true
                onUpdated()
            }) {
                avatars = it
                hasAvatars = true
                onUpdated()
            }
        } else {
            avatars = emptyList()
            hasAvatars = true
            onUpdated()
        }
    }

    fun getRankingForSubject(
        subjectId: Long,
        errorListener: (String) -> Unit,
        listener: (List<RankingForSubject>) -> Unit,
    ) {
        if (this::secondaryApi.isInitialized) {
            secondaryApi.rankingForSubject(
                token,
                profile.children[currentProfile].contingentGuid,
                profile.children[currentProfile].classUnitId,
                Date().formatToDay(),
                subjectId
            ).baseEnqueue(errorListenerForMessage(errorListener)) { listener(it) }
        }
    }

    fun refreshToken(context: Context, onUpdated: () -> Unit) {
        if (subsystem == Diary.MES) {
            context.refreshToken {
                onUpdated()
            }
        } else if (this::secondaryApi.isInitialized) {
            secondaryApi.refreshToken("Bearer $token")
                .baseEnqueue(::baseErrorFunction) {
                    token = it
                    updateUserId { onUpdated() }
                }
        }
    }

    fun setHomeworkDoneState(homeworkId: Long, state: Boolean, listener: () -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            if (state) {
                mainSchoolApi.doHomework(token, homeworkId)
                    .baseEnqueue(::baseErrorFunction) { listener() }
            } else {
                mainSchoolApi.undoHomework(token, homeworkId)
                    .baseEnqueue(::baseErrorFunction) { listener() }
            }
        }
    }

    fun getLessonInfo(
        lessonId: Long,
        errorListener: (String) -> Unit,
        listener: (LessonResponse) -> Unit,
    ) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.lessonSchedule(
                token,
                lessonId,
                profile.children[currentProfile].studentId
            ).baseEnqueue(errorListenerForMessage(errorListener)) {
                listener(it)
            }
        }
    }

    fun getLaunchUrl(homeworkId: Long, materialId: String, listener: (String) -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.launchMaterial(token, homeworkId, materialId)
                .baseEnqueue({ errorBody, httpCode, className ->
                    if (httpCode < 400) {
                        listener(errorBody.string())
                    } else {
                        baseErrorFunction(errorBody, httpCode, className)
                    }
                }) {}
        }
    }

    fun getMealsMenuComplexes(date: Date, listener: (MealsMenuComplexes) -> Unit) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.mealsMenuComplexes(
                accessToken = token,
                personId = profile.children[currentProfile].contingentGuid,
                onDate = date.formatToDay()
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                listener(it)
            }
        }
    }

    fun getBalanceHistory(
        fromDay: String = Date().formatToDay(),
        listener: (DaysBalanceInfo) -> Unit,
    ) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.daysBalanceInfo(
                accessToken = token,
                personId = profile.children[currentProfile].contingentGuid,
                from = "${fromDay}T00:00:00.000Z",
                withPayments = true,
                limit = Int.MAX_VALUE
            ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                listener(it)
            }
        }
    }

    fun sendStatistic(onUpdated: () -> Unit) {
        if (this::userId != null && externalApi().toString().isNotEmpty()) { // Mock check
             // skipped
        }
    }

    fun <Model> pushUserSettings(
        path: String,
        content: Model,
        onError: (String) -> Unit = {},
        onUpdated: () -> Unit,
    ) {
        if (this::mainSchoolApi.isInitialized) {
            mainSchoolApi.pushUserSettings(token, path, Gson().toJsonTree(content).asJsonObject)
                .baseEnqueueOrNull(
                    { errorBody, _, _ -> onError(errorBody.string()) },
                    { throwable, _ -> onError(throwable.message ?: "null throwable message") }) {
                    onUpdated()
                }
        }
    }

    fun updateAll(context: Context? = null, silent: Boolean = false) {
        if (loadingStarted) return else loadingStarted = true

        val onSingleItemLoad = { name: String ->
            val loadedCount = states.count { property ->
                try {
                    property.get()
                } catch (e: Exception) {
                    false
                }
            }
            val totalStates = states.size
            
            onSingleItemInUpdateAllLoadedHandler?.invoke(name, (loadedCount.toFloat()) / (totalStates.toFloat()))
            
            if (loadedCount == totalStates) {
                loadedEverything.value = true
            }
            println("$name response is loaded. Loaded: $loadedCount / $totalStates")
        }

        // Measure synchronization dispatch, actual network calls are async
        measurePerformance("DataService", "updateAll") {
            if (!silent) {
                states.filterNotNull().forEach { it.set(false) }
            }
            if (context != null) {
                refreshToken(context) {}
            }
            updateUserId {
                onSingleItemLoad(::userId.name)
                updateSessionUser {
                    onSingleItemLoad(::sessionUser.name)
                    updateProfile {
                        onSingleItemLoad(::profile.name)
                        // Trigger independent updates in parallel (async)
                        updateEventCalendar {
                            onSingleItemLoad(::eventCalendar.name)
                            onSingleItemLoad(::eventsRange.name)
                        }
                        updateMarksDate { onSingleItemLoad(::marksDate.name) }
                        updateMarksSubject { onSingleItemLoad(::marksSubject.name) }
                        updateHomeworks { onSingleItemLoad(::homeworks.name) }
                        updateRanking {
                            updateCustomClassMembers {
                                onSingleItemLoad(::classMembers.name)
                            }
                            onSingleItemLoad(::ranking.name)
                        }
                        updateGovExams { onSingleItemLoad(::govExams.name) }
                        updateSubjectRanking { onSingleItemLoad(::subjectRanking.name) }
                        if (subsystem == Diary.MES) updateVisits { onSingleItemLoad(::visits.name) }
                        if (subsystem == Diary.MES) updateMealBalance { onSingleItemLoad(::mealBalance.name) }
                        updateSchoolInfo { onSingleItemLoad(::schoolInfo.name) }
                        updateAvatars { onSingleItemLoad(::avatars.name) }
                        updatePersonData { onSingleItemLoad(::personData.name) }
                        if (subsystem == Diary.MES) updateDaysBalanceInfo { onSingleItemLoad(::daysBalanceInfo.name) }
                        if (subsystem == Diary.MES) updateMealsMenuComplexes { onSingleItemLoad(::mealsMenuComplexes.name) }
                    }
                }
            }
        }
    }
}
