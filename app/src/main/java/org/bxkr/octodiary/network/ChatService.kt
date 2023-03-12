package org.bxkr.octodiary.network

import android.content.Context
import android.view.View
import org.bxkr.octodiary.models.chat.ChatContext
import org.bxkr.octodiary.models.chat.ChatCredentials
import org.bxkr.octodiary.network.NetworkService.Server
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration


object ChatService {
    fun getContext(
        accessToken: String,
        server: Server,
        parentContext: Context,
        bindingRoot: View,
        after: (ChatContext) -> Unit
    ) {
        val call = NetworkService.api(server).chatContext(accessToken)
        call.enqueue(object : BaseCallback<ChatContext>(
            parentContext = parentContext,
            bindingRoot = bindingRoot,
            function = {
                after.invoke(it.body()!!)
            }
        ) {})
    }

    fun getCredentials(
        accessToken: String,
        server: Server,
        parentContext: Context,
        bindingRoot: View,
        after: (ChatCredentials) -> Unit
    ) {
        val call = NetworkService.api(server).chatCredentials(accessToken)
        call.enqueue(object : BaseCallback<ChatCredentials>(
            parentContext = parentContext,
            bindingRoot = bindingRoot,
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
        return XMPPTCPConnection(configBuilder.build())
    }
}