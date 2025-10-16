package com.example.timeronnotification.jobscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.timeronnotification.databinding.ActivityJobSchedulerBinding
import java.util.concurrent.TimeUnit

class JobSchedulerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobSchedulerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJobSchedulerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define which service to run
        val componentName = ComponentName(this, MyJobService::class.java)

        // Create job info
        val jobInfo = JobInfo.Builder(123, componentName)
            .setMinimumLatency(TimeUnit.SECONDS.toMillis(5)) // Run after 5 seconds
            .setOverrideDeadline(TimeUnit.SECONDS.toMillis(10)) // Must run by 10s
            .build()

        // Schedule it
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = jobScheduler.schedule(jobInfo)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            println("Job scheduled successfully ✅")
        } else {
            println("Job scheduling failed ❌")
        }

    }
}