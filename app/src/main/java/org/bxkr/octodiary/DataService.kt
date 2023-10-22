package org.bxkr.octodiary

import androidx.compose.runtime.mutableStateOf
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklist.MarkList
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.models.visits.VisitsResponse
import org.bxkr.octodiary.network.NetworkService
import java.util.Calendar
import java.util.Date

object DataService {
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

    lateinit var mealBalance: MealBalance // FUTURE: REGIONAL_FEATURE
    val hasMealBalance get() = this::mealBalance.isInitialized

    val loadedEverything = mutableStateOf(false)

    var tokenExpirationHandler: (() -> Unit)? = null

    fun updateUserId(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        NetworkService.dnevnikApi().profilesId(token).baseEnqueue(::baseErrorFunction) { body ->
            userId = body[0].id // FUTURE: USES_FIRST_CHILD
            onUpdated()
        }
    }

    fun updateSessionUser(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)
        NetworkService.mesApi().sessionUser(SessionUser.Body(token)).baseEnqueue(
            ::baseErrorFunction
        ) { body ->
            sessionUser = body
            onUpdated()
        }
    }

    fun updateEventCalendar(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)
        NetworkService.mesApi().events(
            "Bearer $token",
            personIds = sessionUser.personId,
            beginDate = Calendar.getInstance().also {
                it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }.time.formatToDay(),
            endDate = Calendar.getInstance().also {
                it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }.time.formatToDay(),
            expandFields = "homework,marks"
        ).baseEnqueue(::baseErrorFunction) { body ->
            eventCalendar = body.response
            onUpdated()
        }
    }

    fun getMarkInfo(markId: Int, listener: (MarkInfo) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)
        NetworkService.mesApi().markInfo(
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
        NetworkService.mesApi().classRanking(
            token,
            personId = sessionUser.personId,
            date = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction) {
            ranking = it
            rankingFinished = true
            if (classMembersFinished) onUpdated()
        }

        // Class members request for matching names:
        NetworkService.dnevnikApi().classMembers(
            token,
            classUnitId = profile.children[0].classUnitId // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue {
            classMembers = it
            classMembersFinished = true
            if (rankingFinished) onUpdated()
        }
    }

    fun updateProfile(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        NetworkService.mesApi().profile(token).baseEnqueue(::baseErrorFunction) {
            profile = it
            onUpdated()
        }
    }

    fun updateVisits(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::sessionUser.isInitialized)

        NetworkService.mesApi().visits(
            token,
            profile.children[0].contractId,
            fromDate = Calendar.getInstance().apply {
                time = Date()
                set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 7)
            }.time.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction) { visitsResponse ->
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

        NetworkService.mesApi().markList(
            token,
            studentId = userId.toInt(),
            fromDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 1)
                time
            }.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction) {
            marks = it
            onUpdated()
        }
    }

    fun updateMealBalance(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        NetworkService.dnevnikApi().mealBalance(
            token,
            contractId = profile.children[0].contractId // FUTURE: USES_FIRST_CHILD
        ).baseEnqueue(::baseErrorFunction) {
            mealBalance = it
            onUpdated()
        }
    }

    fun updateAll() {
        val onSingleItemLoad = {
            if (
                !(listOf(
                    hasUserId,
                    hasSessionUser,
                    hasEventCalendar,
                    hasRanking,
                    hasClassMembers,
                    hasProfile,
                    hasVisits,
                    hasMarks,
                    hasMealBalance
                ).contains(false))
            ) {
                loadedEverything.value = true
            }
        }
        updateUserId {
            updateSessionUser {
                updateEventCalendar { onSingleItemLoad() }
                updateProfile {
                    updateRanking { onSingleItemLoad() }
                    updateVisits { onSingleItemLoad() }
                    updateMealBalance { onSingleItemLoad() }
                }
            }
            updateMarks { onSingleItemLoad() }
        }
    }
}