package com.example.timeronnotification.countdowntime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.timeronnotification.R

class CountdownService : Service() {

    private val channelId = "countdown_channel"
    private val notificationId = 1
    private var countDownTimer: CountDownTimer? = null

    private val totalTime = 30_000L // 10 seconds
    private val interval = 1000L // 1s

    companion object {
        var latestProgress = 100
        var latestSecondsLeft = 10
        var isRunning = false
        const val ACTION_START = "ACTION_START_TIMER"
        const val ACTION_STOP = "ACTION_STOP_TIMER"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isRunning) {
                    startForeground(notificationId, buildNotification("Starting timer..."))
                    startCountdown()
                    isRunning = true
                }
            }
            ACTION_STOP -> {
                stopCountdown()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                latestProgress = (millisUntilFinished * 100 / totalTime).toInt()
                latestSecondsLeft = (millisUntilFinished / 1000).toInt()

                sendProgressToActivity()
                updateNotification("Time left: ${latestSecondsLeft}s", latestProgress)
            }

            override fun onFinish() {
                latestProgress = 0
                latestSecondsLeft = 0
                sendProgressToActivity()
                updateNotification("Timer finished!", 0)
                isRunning = false
                stopSelf()
            }
        }.start()
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
    }

    private fun sendProgressToActivity() {
        val updateIntent = Intent("COUNTDOWN_UPDATE").apply {
            putExtra("progress", latestProgress)
            putExtra("secondsLeft", latestSecondsLeft)
        }
        sendBroadcast(updateIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Countdown Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                lightColor = Color.GREEN
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val openActivityIntent = Intent(this, CountDownTimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CountdownService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Countdown Timer")
            .setContentText(content)
            .setProgress(100, latestProgress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPending)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(content: String, progress: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val updated = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Countdown Timer")
            .setContentText(content)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        manager.notify(notificationId, updated)
    }

    override fun onDestroy() {
        stopCountdown()
        super.onDestroy()
    }

}
