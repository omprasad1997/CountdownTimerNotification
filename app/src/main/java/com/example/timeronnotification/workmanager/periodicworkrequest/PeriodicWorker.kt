package com.example.timeronnotification.workmanager.periodicworkrequest

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PeriodicWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("PeriodicWorker", "Work executed at: ${System.currentTimeMillis()}")
        return Result.success()
    }
}