package com.example.timeronnotification.countdowntime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.timeronnotification.Constants
import com.example.timeronnotification.databinding.ActivityCountDownTimerBinding

class CountDownTimerActivity : AppCompatActivity() {
    lateinit var _binding : ActivityCountDownTimerBinding
    private val binding get() = _binding
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

        private fun updateUI(progress: Int, secondsLeft: Int) {
            progressBar.progress = progress
            tvTimer.text = "${secondsLeft}s"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCountDownTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        startBtn.setOnClickListener {
//            startCountdown()

            val intent = Intent(this, CountdownService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //startForegroundService(intent)
                intent.action = Constants.ACTION.STARTFOREGROUND_ACTION
                startService(intent)
            }
        }

//        // Register broadcast receiver
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            registerReceiver(
//                receiver,
//                IntentFilter("COUNTDOWN_UPDATE"),
//                Context.RECEIVER_NOT_EXPORTED
//            )
//        }

        // Immediately show latest progress if service was already running
        updateUI(CountdownService.latestProgress, CountdownService.latestSecondsLeft)


        // ✅ Add the flag here
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            registerReceiver(
//                receiver,
//                IntentFilter("COUNTDOWN_UPDATE"),
//                Context.RECEIVER_NOT_EXPORTED
//            )
//        }
    }

    private fun initViews() {
        progressBar = binding.progressBar
        tvTimer = binding.tvTimer
        startBtn = binding.startBtn
    }

    override fun onResume() {
        super.onResume()
        // Re-sync in case app reopened from notification
        updateUI(CountdownService.latestProgress, CountdownService.latestSecondsLeft)
    }

    private fun updateUI(progress: Int, secondsLeft: Int) {
        progressBar.progress = progress
        tvTimer.text = if (secondsLeft > 0) "${secondsLeft}s" else "Done!"
    }

    private fun startCountdown() {
        progressBar.progress = 100

        object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = (millisUntilFinished * 100 / totalTime).toInt()
                progressBar.progress = progress
                tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                progressBar.progress = 0
                tvTimer.text = "Done!"
            }
        }.start()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}