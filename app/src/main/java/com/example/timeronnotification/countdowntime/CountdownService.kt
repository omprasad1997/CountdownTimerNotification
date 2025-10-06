package com.example.timeronnotification.countdowntime

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.timeronnotification.Constants
import com.example.timeronnotification.R

class CountdownService : Service() {

    private val channelId = "countdown_channel"
    private val notificationId = 1
    private var countDownTimer: CountDownTimer? = null

    private val totalTime = 10_000L // 10 seconds
    private val interval = 1000L // update every 1s

    companion object {
        var latestProgress = 100
        var latestSecondsLeft = 10
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            startForeground(notificationId, buildNotification("Timer started"))
            startCountdown()
            //isPlaying = true
        } else if (intent?.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            stopForeground(true)
//            stopMusic()
//            isPlaying = false
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                latestProgress = (millisUntilFinished * 100 / totalTime).toInt()
                latestSecondsLeft = (millisUntilFinished / 1000).toInt()

                sendProgressToActivity()
                updateNotification("Time left: ${latestSecondsLeft}s")
            }

            override fun onFinish() {
                latestProgress = 0
                latestSecondsLeft = 0
                sendProgressToActivity()
                updateNotification("Timer finished!")
                stopSelf()
            }
        }.start()
    }

    private fun sendProgressToActivity() {
        val intent = Intent("COUNTDOWN_UPDATE")
        intent.putExtra("progress", latestProgress)
        intent.putExtra("secondsLeft", latestSecondsLeft)
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Countdown Timer Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true) // Turn on notification light
                lightColor = Color.GREEN
                enableVibration(true) // Allow vibration for notifications
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, CountDownTimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Countdown Timer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, latestProgress, false)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, buildNotification(content))
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
