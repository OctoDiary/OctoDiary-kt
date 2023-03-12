package org.bxkr.octodiary.ui.fragments

import android.os.Bundle
import android.view.View
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.FragmentChatListBinding
import org.bxkr.octodiary.ui.activities.MainActivity

class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::inflate) {
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity
        mainActivity.title = getString(R.string.chats)
    }
}