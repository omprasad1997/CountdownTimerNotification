package com.example.timeronnotification.assignment

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timeronnotification.R
import kotlinx.coroutines.*

class TimerService : Service() {

    private val binder = TimerBinder()
    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private var startTime = 0L
    private var timerJob: Job? = null

    private val CHANNEL_ID = "timer_channel_id"
    private val NOTIFICATION_ID = 1

    /** Expose current elapsed time for new bindings */
    val currentElapsedTime: Long
        get() = (System.currentTimeMillis() - startTime) / 1000

    /** Binder to let Activity bind to the Service */
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    //observe start id
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the foreground notification (required for Android 14/15)
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(0),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(0))
        }

        startTimer()
        return START_STICKY
    }

    /** Coroutine that counts seconds and updates LiveData + Notification */
    private fun startTimer() {
        if (timerJob != null) return  // prevent double start
        startTime = System.currentTimeMillis()

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _elapsedTime.postValue(elapsed)
                updateNotification(elapsed)
                delay(1000)
            }
        }
    }

    /** Stop coroutine + foreground service */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun stopServiceFromActivity() {
        stopTimer()
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    /** Build the ongoing timer notification */
    private fun buildNotification(seconds: Long): Notification {
        val openIntent = Intent(this, TimerNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timer Running")
            .setContentText("Elapsed time: ${formatTime(seconds)}")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setColor(Color.BLUE)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    /** Update the same foreground notification */
    private fun updateNotification(seconds: Long) {
        val notification = buildNotification(seconds)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    /** Create the notification channel (required for API 26+) */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows timer progress"
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /** Format time as mm:ss for both notification and UI */
    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}


