package com.example.timeronnotification.workmanager

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.timeronnotification.R
import com.example.timeronnotification.workmanager.onetimeworkrequest.MyWorker
import com.example.timeronnotification.workmanager.periodicworkrequest.PeriodicWorker
import java.util.concurrent.TimeUnit

class MainWorkManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_work_manager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

//        executeOneTimeWorkRequest() //OneTimeRequest
        executePeriodicWorkTimeRequest() //OneTimeRequest
    }

    private fun executePeriodicWorkTimeRequest() {
        // 1️⃣ Create Periodic Work Request (every 15 minutes)
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicWorker>(15, TimeUnit.MINUTES)
                .build()


        // 2️⃣ Enqueue work (unique to avoid duplicates)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyPeriodicWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun executeOneTimeWorkRequest() {

        // 1️⃣ Create Input Data
        val inputData = Data.Builder()
            .putString("USER_NAME", "Omi")
            .build()

        // 2️⃣ Build OneTimeWorkRequest with input
        val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
            .setInputData(inputData)
            .build()

        // 3️⃣ Enqueue Work
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(workRequest)

        // 4️⃣ Observe Output
//        workManager.getWorkInfoByIdLiveData(workRequest.id)
//            .observe(this) { workInfo ->
//                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
//                    val outputData = workInfo.outputData
//                    val message = outputData.getString("GREETING_MSG")
//                    Log.d("MainWorkManagerActivity", "Output from Worker: $message")
//                }
//            }
    }
}