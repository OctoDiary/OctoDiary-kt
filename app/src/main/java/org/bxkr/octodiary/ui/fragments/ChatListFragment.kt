package org.bxkr.octodiary.ui.fragments

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.FragmentChatListBinding
import org.bxkr.octodiary.models.chat.ChatCloseContacts
import org.bxkr.octodiary.models.chat.ChatEnrich
import org.bxkr.octodiary.models.chat.ChatEnrichBody
import org.bxkr.octodiary.models.chat.Contact
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.ChatService
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.ChatAdapter
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.packet.Message.Body
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.mam.MamManager


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
        val setAdapter = { contacts: List<Contact> ->
            mainActivity.binding.bottomNavigationView.getOrCreateBadge(R.id.chatsPage).backgroundColor =
                mainActivity.getColor(R.color.green_connected)
            binding.progress.visibility = View.GONE
            binding.connecting.text = getString(R.string.connected)
            binding.connecting.animate().alpha(0f).setDuration(300)
                .setListener(object : AnimatorListener {
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
                        binding.recyclerView.adapter = ChatAdapter(mainActivity, contacts)
                        binding.recyclerView.animate().alpha(1f).setDuration(300).start()
                    }
                }).start()
        }
        val thread = Thread {
            connection.connect().login()
            val call = NetworkService.api(NetworkService.Server.values()[mainActivity.server])
                .chatCloseContacts(mainActivity.token)
            call.enqueue(object : BaseCallback<ChatCloseContacts>(
                parentContext = mainActivity,
                bindingRoot = binding.root,
                function = { response ->
                    val rosterEntries = Roster.getInstanceFor(connection).entries
                    val enrich =
                        NetworkService.api(NetworkService.Server.values()[mainActivity.server])
                            .chatEnrich(
                                body = ChatEnrichBody(jids = rosterEntries.map { it.jid.toString() }),
                                accessToken = mainActivity.token
                            )
                    enrich.enqueue(object : BaseCallback<ChatEnrich>(
                        parentContext = mainActivity,
                        bindingRoot = binding.root,
                        function = { enrichResponse ->
                            val mamElements = rosterEntries.map {
                                val lastMessage =
                                    MamManager.getInstanceFor(connection).queryMostRecentPage(
                                        it.jid,
                                        10
                                    ).messages.last { it1 -> it1.hasExtension(Body.QNAME) }
                                it.jid to lastMessage.body to lastMessage.from
                            }
                            val enriched = enrichResponse.body()!!.jidList.map {
                                if (mamElements.map { it1 -> it1.first.first.toString() }
                                        .contains(it.jid)) {
                                    val mamEl =
                                        mamElements.first { it1 -> it1.first.first.toString() == it.jid }
                                    it.lastMessage = mamEl.first.second
                                    it.sender =
                                        mamElements.first { it1 -> it1.first.first.toString() == it.jid }.first.first.toString()
                                            .let { jid ->
                                                if (response.body()!!.contacts.map { it1 -> it1.jid }
                                                        .contains(jid)) {
                                                    return@let response.body()!!.contacts.first { it1 -> it1.jid == jid }.shortName
                                                } else if (mamEl.second == connection.user.asBareJid()) {
                                                    return@let getString(R.string.you)
                                                }
                                                ""
                                            }
                                }
                                it
                            }
                            mainActivity.runOnUiThread {
                                setAdapter(
                                    enriched.plus(response.body()!!.contacts.filter {
                                        !enriched.map { it1 -> it1.jid }.contains(it.jid)
                                    })
                                )
                            }
                        }
                    ) {})
                }
            ) {})
        }
        thread.start()
//        ChatManager.getInstanceFor(connection).chatWith(JidCreate.entityBareFrom("user_1000006787970@xmpp.school.mosreg.ru"))
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.binding.bottomNavigationView.removeBadge(R.id.chatsPage)
    }
}