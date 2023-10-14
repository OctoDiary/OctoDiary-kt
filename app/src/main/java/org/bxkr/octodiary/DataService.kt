package org.bxkr.octodiary

import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.network.NetworkService
import java.util.Calendar

object DataService {
    lateinit var token: String
    lateinit var userId: Number
    lateinit var sessionUser: SessionUser
    lateinit var eventCalendar: List<Event>
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
            }.time.formatToDay()
        ).baseEnqueue(::baseErrorFunction) { body ->
            eventCalendar = body.response
            onUpdated()
        }
    }
}