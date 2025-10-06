package com.example.timeronnotification.countdowntime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.timeronnotification.countdowntime.CountdownService.Companion.isRunning
import com.example.timeronnotification.countdowntime.CountdownService.Companion.latestProgress
import com.example.timeronnotification.countdowntime.CountdownService.Companion.latestSecondsLeft
import com.example.timeronnotification.databinding.ActivityCountDownTimerBinding

class CountDownTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountDownTimerBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTimer: TextView
    private lateinit var startBtn: TextView

    private val totalTime = 10_000L // 10 seconds
    private val interval = 100L // update every 100ms

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("progress", 100) ?: 100
            val secondsLeft = intent?.getIntExtra("secondsLeft", 0) ?: 0
            updateUI(progress, secondsLeft)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountDownTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        startBtn.setOnClickListener {
            val intent = Intent(this, CountdownService::class.java).apply {
                action = CountdownService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        registerCountdownReceiver()
        updateUI(CountdownService.latestProgress, CountdownService.latestSecondsLeft)

    }

    private fun initViews() {
        progressBar = binding.progressBar
        tvTimer = binding.tvTimer
        startBtn = binding.startBtn
    }

    private fun registerCountdownReceiver() {
        val filter = IntentFilter("COUNTDOWN_UPDATE")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                //registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI(CountdownService.latestProgress, CountdownService.latestSecondsLeft)
    }

    private fun updateUI(progress: Int, secondsLeft: Int) {
        progressBar.progress = progress
        tvTimer.text = if (secondsLeft > 0) "${secondsLeft}s" else "Done!"
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }
}
