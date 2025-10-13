package com.example.timeronnotification.workmanager.onetimeworkrequest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.timeronnotification.R

class MyWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // 1️⃣ Get Input
        val userName = inputData.getString("USER_NAME") ?: "User"
        Log.d("MyWorker", "Received Input: $userName")

        // 2️⃣ Simulate some processing
        Thread.sleep(4000)

        // 3️⃣ Create output
        val greeting = "Hello, $userName! Work completed successfully 🎉"
        val outputData = Data.Builder()
            .putString("GREETING_MSG", greeting)
            .build()

        // Show notification
        showNotification("WorkManager Demo", "Hello $userName! Task completed ✅")
        Log.d("MyWorker", "Returning Output: $greeting")

        // 4️⃣ Return result
        return Result.success(outputData)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "work_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "WorkManager Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        // Custom layout for the notification content
        val contentView = RemoteViews(context.packageName, R.layout.activity_after_notification)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContent(contentView)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1, notification)
    }
}