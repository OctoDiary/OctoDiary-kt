package org.bxkr.octodiary.ui.fragments

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.util.Log
import android.view.View
import androidx.core.widget.doOnTextChanged
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
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.mam.MamManager.MamQueryArgs
import org.jivesoftware.smackx.mam.element.MamElements.MamResultExtension
import org.jivesoftware.smackx.message_correct.element.MessageCorrectExtension
import org.jxmpp.jid.impl.JidCreate
import java.util.Date


class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate) {
    private lateinit var mainActivity: MainActivity
    private lateinit var adapter: ChatAdapter

    override fun onResume() {
        super.onResume()
        mainActivity = activity as MainActivity
        mainActivity.binding.swipeRefresh.isRefreshing = false
        if (isVisible) {
            configureChats()
        }
    }

    fun configureChats() {
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
                        Thread {
                            ChatService.getConnection(
                                username = chatCredentials.jid,
                                password = chatCredentials.token,
                                hostname = chatContext.mongooseTCPHost
                            ).let { connectChats(it) }
                        }.start()
                    }
                )
            }
        )
    }

    private fun connectChats(connection: AbstractXMPPConnection) {
        val roster = Roster.getInstanceFor(connection)
        val setAdapter = { contacts: List<Contact> ->
            try {
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
                            adapter = ChatAdapter(mainActivity, contacts)
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
                            binding.recyclerView.adapter = adapter
                            binding.recyclerView.animate().alpha(1f).setDuration(300).start()
                            val chatManager = ChatManager.getInstanceFor(connection)
                            chatManager.addOutgoingListener { to, messageBuilder, _ ->
                                mainActivity.runOnUiThread {
                                    adapter.updateChatOutgo(to, messageBuilder.body)
                                }
                            }
                            chatManager.addIncomingListener { from, message, _ ->
                                mainActivity.runOnUiThread {
                                    adapter.updateChatIncome(from, message.body)
                                }
                            }
                            Thread {
                                Thread.sleep(1000)

                            }.start()
                            configureChips()
                            configureSearch()
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
                    val afterRosterGot = { rosterEntries: Set<RosterEntry> ->
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
                                val afterLongMamWork = {
                                    val mamByJid =
                                        mutableMapOf<String, MutableList<Pair<Pair<String, String>, Date>>>()
                                    bigArchive.forEach {
                                        val stanza = it.forwarded.forwardedStanza
                                        if (stanza.body != null) {
                                            val chat =
                                                (if (stanza.from.asBareJid() == connection.user.asBareJid()) stanza.to else stanza.from).asBareJid()
                                                    .toString()

                                            val delay = it.forwarded.delayInformation
                                            if (stanza.hasExtension(MessageCorrectExtension.QNAME)) {
                                                mamByJid.getOrPut(chat) { mutableListOf() }
                                                    .add(
                                                        it.forwarded.forwardedStanza.from.asBareJid()
                                                            .toString() to getString(R.string.deleted_message) to delay.stamp
                                                    )
                                            } else {
                                                mamByJid.getOrPut(chat) { mutableListOf() }
                                                    .add(
                                                        it.forwarded.forwardedStanza.from.asBareJid()
                                                            .toString() to it.forwarded.forwardedStanza.body to delay.stamp
                                                    )
                                            }
                                        }
                                    }
                                    val lastMessageStamps: MutableList<Pair<String, Long>> =
                                        mutableListOf()
                                    val enriched = enrichResponse.body()!!.jidList.map {
                                        if (mamByJid.containsKey(it.jid)) {
                                            val latestElement =
                                                mamByJid[it.jid]?.maxBy { it1 -> it1.second.time }
                                            if (latestElement != null) {
                                                if (latestElement.first.first == connection.user.asBareJid()
                                                        .toString()
                                                ) {
                                                    it.sender = getString(R.string.you)
                                                } else it.sender =
                                                    enrichResponse.body()!!.jidList.firstOrNull { it1 -> it1.jid == latestElement.first.first }?.shortName
                                                it.lastMessage = latestElement.first.second
                                                lastMessageStamps.add(it.jid to latestElement.second.time)
                                            }
                                        }
                                        it
                                    }
                                    lastMessageStamps.sortByDescending { it.second }
                                    val sortedEnriched = lastMessageStamps.map {
                                        enriched.first { it1 -> it1.jid == it.first }
                                    }
                                    val sendingList =
                                        sortedEnriched.plus(contacts.filter {
                                            !sortedEnriched.map { it1 -> it1.jid }.contains(it.jid)
                                        })
                                    mainActivity.runOnUiThread {
                                        setAdapter(sendingList)
                                    }
                                }
                                Thread {
                                    val ranJidList = mutableListOf<String>()
                                    val after = { archive: List<MamResultExtension> ->
                                        bigArchive.addAll(archive)
                                        if (enrichResponse.body()!!.jidList.size == ranJidList.size) {
                                            afterLongMamWork()
                                        }
                                    }
                                    enrichResponse.body()!!.jidList.forEach {
                                        Thread {
                                            val archive = mamManager.queryArchive(
                                                MamQueryArgs.Builder()
                                                    .limitResultsToJid(JidCreate.from(it.jid))
                                                    .setResultPageSize(100)
                                                    .queryLastPage()
                                                    .build()
                                            ).mamResultExtensions
                                            ranJidList.add(it.jid)
                                            after(archive)
                                        }.start()

                                    }
                                }.start()
                            }
                        ) {})
                    }
                    Thread {
                        val rosterEntries = roster.entries
                        afterRosterGot(rosterEntries)
                    }.start()
                }
            ) {})
        }
        thread.start()
    }


    private fun configureChips() {
        binding.chipGroup.animate().alpha(1f).setDuration(300).start()
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            adapter.filterBy {
                (if (checkedIds.contains(R.id.groups)) {
                    it.isGroupChat
                } else false) || (if (checkedIds.contains(R.id.personals)) {
                    if (checkedIds.contains(R.id.unknowns)) {
                        !it.isGroupChat
                    } else !it.isGroupChat && !it.unknown
                } else false)
            }
        }
    }

    private fun configureSearch() {
        binding.search.animate().alpha(1f).setDuration(300).start()
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            adapter.filterBy {
                if (!text.isNullOrBlank()) {
                    it.name.lowercase()
                        .matches(Regex("^.*${Regex.escape(text.toString().lowercase())}.*$"))
                } else true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.binding.bottomNavigationView.removeBadge(R.id.chatsPage)
    }
}