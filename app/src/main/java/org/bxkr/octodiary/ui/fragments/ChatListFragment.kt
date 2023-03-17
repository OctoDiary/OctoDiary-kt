package org.bxkr.octodiary.ui.fragments

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Bundle
import android.util.Log
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
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.mam.MamManager.MamQueryArgs
import org.jivesoftware.smackx.mam.element.MamElements.MamResultExtension
import org.jxmpp.jid.impl.JidCreate
import java.util.Date


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
            after = { chatContext ->
                ChatService.getCredentials(
                    server = NetworkService.Server.values()[mainActivity.server],
                    accessToken = mainActivity.token!!,
                    parentContext = mainActivity,
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
            try {
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
            } catch (_: NullPointerException) {
                Log.i(
                    "ChatListFragment.connectChats()",
                    "Fragment was destroyed, aborting connection..."
                )
            }
        }
        val thread = Thread {
            connection.connect().login()
            val call = NetworkService.api(NetworkService.Server.values()[mainActivity.server])
                .chatCloseContacts(mainActivity.token)
            call.enqueue(object : BaseCallback<ChatCloseContacts>(
                parentContext = mainActivity,
                function = { response ->
                    val contacts = response.body()!!.contacts
                    val rosterEntries = Roster.getInstanceFor(connection).entries
                    val enrich =
                        NetworkService.api(NetworkService.Server.values()[mainActivity.server])
                            .chatEnrich(
                                body = ChatEnrichBody(jids = rosterEntries.map { it.jid.toString() }),
                                accessToken = mainActivity.token
                            )
                    enrich.enqueue(object : BaseCallback<ChatEnrich>(
                        parentContext = mainActivity,
                        function = { enrichResponse ->
                            val mamManager = MamManager.getInstanceFor(connection)
                            mamManager.enableMamForAllMessages()
                            val bigArchive = mutableListOf<MamResultExtension>()
                            enrichResponse.body()!!.jidList.forEach {
                                val archive = mamManager.queryArchive(
                                    MamQueryArgs.Builder()
                                        .limitResultsToJid(JidCreate.from(it.jid))
                                        .setResultPageSize(100)
                                        .queryLastPage()
                                        .build()
                                ).mamResultExtensions
                                bigArchive.addAll(archive)
                            }
                            val mamByJid =
                                mutableMapOf<String, MutableList<Pair<Pair<String, String>, Date>>>()
                            bigArchive.forEach {
                                if (it.forwarded.forwardedStanza.body != null) {
                                    val chat =
                                        (if (it.forwarded.forwardedStanza.from.asBareJid() == connection.user.asBareJid()) it.forwarded.forwardedStanza.to else it.forwarded.forwardedStanza.from).asBareJid()
                                            .toString()

                                    val delay = it.forwarded.delayInformation
                                    if (delay != null) {
                                        mamByJid.getOrPut(chat) { mutableListOf() }
                                            .add(
                                                it.forwarded.forwardedStanza.from.asBareJid()
                                                    .toString() to it.forwarded.forwardedStanza.body to delay.stamp
                                            )
                                    }
                                }
                            }
                            val enriched = enrichResponse.body()!!.jidList.map {
                                if (mamByJid.containsKey(it.jid)) {
                                    val latestElement =
                                        mamByJid[it.jid]?.maxBy { it1 -> it1.second.time }
                                    if (latestElement?.first?.first == connection.user.asBareJid()
                                            .toString()
                                    ) {
                                        it.sender = getString(R.string.you)
                                    } else it.sender =
                                        enrichResponse.body()!!.jidList.firstOrNull { it1 -> it1.jid == latestElement?.first?.first }?.name
                                    it.lastMessage = latestElement?.first?.second
                                    println(it.name to it.lastMessage)
                                }
                                it
                            }
                            val sendingList =
                                enriched.plus(contacts.filter {
                                    !enriched.map { it1 -> it1.jid }.contains(it.jid)
                                })
                            mainActivity.runOnUiThread {
                                setAdapter(sendingList)
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