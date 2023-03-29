package org.bxkr.octodiary.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var jid: String
    private lateinit var name: String
    private lateinit var avatar: String

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        jid = intent.getStringExtra("jid")!!
        name = intent.getStringExtra("name")!!
        avatar = intent.getStringExtra("avatar") ?: ""
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val params = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )
        supportActionBar?.setCustomView(
            layoutInflater.inflate(R.layout.action_bar_chat, null),
            params
        )
        val parent: Toolbar = supportActionBar?.customView?.parent as Toolbar
        parent.setPadding(0, 0, 0, 0)
        parent.setContentInsetsAbsolute(0, 0)
        supportActionBar?.customView?.post {
            val avatarView = findViewById<ShapeableImageView>(R.id.avatar)
            val chatNameView = findViewById<TextView>(R.id.chatName)
            if (this.avatar.isNotEmpty()) {
                Picasso.get().load(this.avatar).into(avatarView)
                avatarView.scaleType = ImageView.ScaleType.FIT_XY
            }
            chatNameView.text = name
            chatNameView.tooltipText = name
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}