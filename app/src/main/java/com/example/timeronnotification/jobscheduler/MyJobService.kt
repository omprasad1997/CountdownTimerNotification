// MyJobService.kt
package com.example.timeronnotification.jobscheduler

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class MyJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "Job started!")

        // Simulate some background work (non-blocking)
        Thread {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            Log.d("MyJobService", "Job finished!")
            jobFinished(params, false) // Signal completion
        }.start()

        return true // true = work is still running on another thread
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "Job stopped before completion")
        return false
    }
}
