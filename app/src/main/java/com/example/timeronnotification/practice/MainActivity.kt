package com.example.timeronnotification.practice

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timeronnotification.R
import com.example.timeronnotification.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val channelId = "i.apps.notifications" // Unique channel ID for notifications
    private val description = "Test notification"  // Description for the notification channel
    private val notificationId = 1234 // Unique identifier for the notification

    private lateinit var  _binding : ActivityMainBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create a notification channel (required for Android 8.0 and higher)
        createNotificationChannel()

        binding.btn.setOnClickListener {
            // Request runtime permission for notifications on Android 13 and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                    return@setOnClickListener
                }
            }

            sendNotification() // Trigger the notification
        }
    }

    /**
     * Create a notification channel for devices running Android 8.0 or higher.
     * A channel groups notifications with similar behavior.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                description,
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

    /**
     * Build and send a notification with a custom layout and action.
     */
    @SuppressLint("MissingPermission")
    private fun sendNotification() {
        // Intent that triggers when the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Custom layout for the notification content
        val contentView = RemoteViews(packageName, R.layout.activity_after_notification)

        // Build the notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Notification icon
            .setContent(contentView) // Custom notification content
            .setContentTitle("Hello") // Title displayed in the notification
            .setContentText("Welcome to GeeksforGeeks!!") // Text displayed in the notification
            .setContentIntent(pendingIntent) // Pending intent triggered when tapped
            .setAutoCancel(true) // Dismiss notification when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Notification priority for better visibility

        // Display the notification
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

}