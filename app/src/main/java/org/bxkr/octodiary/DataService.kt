package org.bxkr.octodiary

import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.network.NetworkService
import java.util.Calendar

object DataService {
    lateinit var token: String
    lateinit var userId: Number
    val hasUserId get() = this::userId.isInitialized

    lateinit var sessionUser: SessionUser
    val hasSessionUser get() = this::sessionUser.isInitialized

    lateinit var eventCalendar: List<Event>
    val hasEventCalendar get() = this::eventCalendar.isInitialized
    val loadedEverything
        get() =
            ::token.isInitialized
                    && hasUserId
                    && hasSessionUser
                    && hasEventCalendar

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

    /** [loadedEverything] becomes true **/
    fun updateAll(onUpdated: () -> Unit) {
        updateUserId {
            updateSessionUser {
                updateEventCalendar { onUpdated() }
            }
        }
    }
}