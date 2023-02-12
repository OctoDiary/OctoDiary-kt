package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ActivityHomeworkModeBinding


class HomeworkModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkModeBinding
    private lateinit var audioManager: AudioManager
    private var pauseState = false
    private var preventDisappear = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lessonName.text = intent.getStringExtra("lesson_name")
        binding.lessonDate.text =
            getString(R.string.homework_for_date, intent.getStringExtra("lesson_date"))
        binding.homeworkText.text = intent.getStringExtra("homework_text")
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        checkMusic()
    }

    private fun controlMusic(control: AudioControlEvents) {
        val eventTime = SystemClock.uptimeMillis()
        val event =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, control.keyCode, 0)
        audioManager.dispatchMediaKeyEvent(event)
    }

    enum class AudioControlEvents(val keyCode: Int) {
        Play(KeyEvent.KEYCODE_MEDIA_PLAY),
        Pause(KeyEvent.KEYCODE_MEDIA_PAUSE),
        Next(KeyEvent.KEYCODE_MEDIA_NEXT),
        Previous(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun checkMusic() {
        if (audioManager.isMusicActive) {
            preventDisappear = true
            binding.previous.visibility = View.VISIBLE
            binding.playPause.visibility = View.VISIBLE
            binding.next.visibility = View.VISIBLE
            binding.previous.setOnClickListener { controlMusic(AudioControlEvents.Previous) }
            binding.playPause.setOnClickListener {
                pauseState = if (pauseState) {
                    controlMusic(AudioControlEvents.Play)
                    binding.playPause.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this,
                            R.drawable.ic_round_pause_24
                        )
                    )
                    false
                } else {
                    controlMusic(AudioControlEvents.Pause)
                    binding.playPause.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this,
                            R.drawable.ic_round_play_arrow_24
                        )
                    )
                    true
                }
            }
            binding.next.setOnClickListener { controlMusic(AudioControlEvents.Next) }
        } else if (preventDisappear) {
            pauseState = true
            binding.playPause.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_round_play_arrow_24
                )
            )
        } else {
            binding.previous.visibility = View.GONE
            binding.playPause.visibility = View.GONE
            binding.next.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        checkMusic()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        checkMusic()
    }
}