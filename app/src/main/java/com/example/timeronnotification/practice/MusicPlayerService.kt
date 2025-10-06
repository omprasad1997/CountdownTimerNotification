package com.example.timeronnotification.practice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.timeronnotification.Constants
import com.example.timeronnotification.Constants.DESCRIPTION
import com.example.timeronnotification.R

class MusicPlayerService : Service() {

    private val NOTIFICATION_ID = 1

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.song)
        mediaPlayer.isLooping = true

        // Create a notification channel (required for Android 8.0 and higher)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            startForeground(NOTIFICATION_ID, createNotification())
            playMusic()
            isPlaying = true
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            stopForeground(true)
            stopMusic()
            isPlaying = false
            stopSelf()
        }
        return START_NOT_STICKY
    }

    /**
     * Create a notification channel for devices running Android 8.0 or higher.
     * A channel groups notifications with similar behavior.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                DESCRIPTION,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true) // Turn on notification light
                lightColor = Color.GREEN
                enableVibration(true) // Allow vibration for notifications
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlaying) {
            stopMusic()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, MusicPlayerService::class.java)
        stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        val pendingStopIntent = PendingIntent.getService(this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE)

        // Intent that triggers when the notification is tapped
        val intent = Intent(this, MusicPlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing Music")
            .setSmallIcon(R.drawable.music_note)
            .addAction(R.drawable.music_stop, "Stop", pendingStopIntent)
            .setContentIntent(tapIntent)
            .setAutoCancel(false)

        return builder.build()
    }

    private fun playMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    private fun stopMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }
    }
}
