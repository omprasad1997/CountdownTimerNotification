package com.example.timeronnotification.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timeronnotification.R
import com.example.timeronnotification.databinding.ActivityAlarmManagerBinding
import androidx.core.net.toUri


class AlarmManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetAlarm.setOnClickListener {
            setOneTimeAlarm()
        }
    }

    private fun setOneTimeAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 10_000 // 10 seconds later

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()

            if (!canScheduleExactAlarms) {
                // Prompt user to give permission
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = "package:$packageName".toUri()
                startActivity(intent)
            }
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}