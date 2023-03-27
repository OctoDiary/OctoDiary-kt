package org.bxkr.octodiary.network

import android.content.Context
import org.bxkr.octodiary.models.chat.ChatContext
import org.bxkr.octodiary.models.chat.ChatCredentials
import org.bxkr.octodiary.network.NetworkService.Server
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ReconnectionManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.ping.PingManager


object ChatService {
    fun getContext(
        accessToken: String,
        server: Server,
        parentContext: Context,
        after: (ChatContext) -> Unit
    ) {
        val call = NetworkService.api(server).chatContext(accessToken)
        call.enqueue(object : BaseCallback<ChatContext>(
            parentContext = parentContext,
            function = {
                after.invoke(it.body()!!)
            }
        ) {})
    }

    fun getCredentials(
        accessToken: String,
        server: Server,
        parentContext: Context,
        after: (ChatCredentials) -> Unit
    ) {
        val call = NetworkService.api(server).chatCredentials(accessToken)
        call.enqueue(object : BaseCallback<ChatCredentials>(
            parentContext = parentContext,
            function = {
                after.invoke(it.body()!!)
            }
        ) {})
    }

    fun getConnection(
        username: String,
        password: String,
        hostname: String
    ): AbstractXMPPConnection {
        val configBuilder = XMPPTCPConnectionConfiguration.builder()
        configBuilder.setUsernameAndPassword(username, password)
        configBuilder.setHost(hostname.replace(Regex(":\\d+$"), ""))
        configBuilder.setPort(hostname.replace(Regex("^.+:"), "").toInt())
        configBuilder.setXmppDomain(username.replace(Regex("^.+@"), ""))
        configBuilder.setSendPresence(true)
        val connection = XMPPTCPConnection(configBuilder.build())
        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection()
        PingManager.getInstanceFor(connection).pingInterval = 60
        return connection
    }
}