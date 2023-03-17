package org.bxkr.octodiary.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemChatBinding
import org.bxkr.octodiary.models.chat.Contact
import org.bxkr.octodiary.ui.activities.MainActivity

class ChatAdapter(
    private val context: MainActivity,
    private val chats: List<Contact>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    class ChatViewHolder(
        val binding: ItemChatBinding, val context: MainActivity
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Contact) {
            binding.root.tag = chat.jid
            binding.chatName.text = chat.shortName
            binding.avatar.adjustViewBounds = chat.avatar != null
            if (!chat.lastMessage.isNullOrBlank()) {
                binding.lastMessage.text = context.getString(
                    R.string.last_message_template,
                    chat.sender,
                    chat.lastMessage
                )
                binding.lastMessage.visibility = View.VISIBLE
            } else {
                binding.lastMessage.text = ""
                binding.lastMessage.visibility = View.GONE
            }
            if (chat.avatar != null) {
                binding.avatar.scaleType = ImageView.ScaleType.FIT_START
                Picasso.get().load(chat.avatar).into(binding.avatar)
            } else {
                binding.avatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.avatar.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_round_person_24
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding =
            ItemChatBinding.inflate(LayoutInflater.from(context), parent, false)
        return ChatViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int = chats.size
}