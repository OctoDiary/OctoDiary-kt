package org.bxkr.octodiary.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.FragmentChatListBinding
import org.bxkr.octodiary.models.chat.ChatCloseContacts
import org.bxkr.octodiary.models.chat.Contact
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.ChatService
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.ChatAdapter
import org.jivesoftware.smack.AbstractXMPPConnection


class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate) {
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        mainActivity.title = getString(R.string.chats)
        configureChats()
    }

    private fun configureChats() {
        ChatService.getContext(
            server = NetworkService.Server.values()[mainActivity.server],
            accessToken = mainActivity.token!!,
            parentContext = mainActivity,
            bindingRoot = binding.root,
            after = { chatContext ->
                ChatService.getCredentials(
                    server = NetworkService.Server.values()[mainActivity.server],
                    accessToken = mainActivity.token!!,
                    parentContext = mainActivity,
                    bindingRoot = binding.root,
                    after = { chatCredentials ->
                        ChatService.getConnection(
                            username = chatCredentials.jid,
                            password = chatCredentials.token,
                            hostname = chatContext.mongooseTCPHost
                        ).let { connectChats(it) }
                    }
                )
            }
        )
    }

    private fun connectChats(connection: AbstractXMPPConnection) {
        val setAdapter = { it: List<Contact> ->
            binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
            binding.recyclerView.adapter = ChatAdapter(mainActivity, it)
        }
        val thread = Thread {
            connection.connect().login()
            val call = NetworkService.api(NetworkService.Server.values()[mainActivity.server])
                .chatCloseContacts(mainActivity.token)
            call.enqueue(object : BaseCallback<ChatCloseContacts>(
                parentContext = mainActivity,
                bindingRoot = binding.root,
                function = { response ->
                    mainActivity.runOnUiThread {
                        setAdapter(response.body()!!.contacts)
                    }
                }
            ) {})
        }
        thread.start()
//        ChatManager.getInstanceFor(connection).chatWith(JidCreate.entityBareFrom("user_1000006787970@xmpp.school.mosreg.ru"))
    }
}