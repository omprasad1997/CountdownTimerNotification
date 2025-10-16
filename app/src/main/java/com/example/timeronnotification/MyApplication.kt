package com.example.timeronnotification

import android.app.Application
import androidx.work.Configuration

class MyApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setJobSchedulerJobIdRange(1000, 2000) // ✅ Custom ID range
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}