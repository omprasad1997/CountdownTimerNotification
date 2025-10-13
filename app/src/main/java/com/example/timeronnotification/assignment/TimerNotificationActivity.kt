package com.example.timeronnotification.assignment

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.timeronnotification.databinding.ActivityTimerNotificationBinding

class TimerNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerNotificationBinding
    private var timerService: TimerService? = null
    private var isBound = false

    /** Handles the bound connection with the TimerService */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true

            // Update UI instantly with current elapsed time
            binding.tvTime.text = formatTime(timerService?.currentElapsedTime ?: 0)

            // Then observe LiveData for further updates
            observeTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTimerNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener { startAndBindService() }
        binding.btnStop.setOnClickListener { stopAndUnbindService() }
    }

    /** Start ForegroundService + bind for UI updates */
    private fun startAndBindService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        startService(serviceIntent)  // Ensures service keeps running in background
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
    }

    /** Stop timer, unbind, and remove notification */
    private fun stopAndUnbindService() {
        if (isBound) {
            timerService?.stopServiceFromActivity()
            unbindService(connection)
            isBound = false
        }
    }

    /** Observe elapsed time LiveData from bound service */
    private fun observeTimer() {
        timerService?.elapsedTime?.observe(this, Observer { seconds ->
            binding.tvTime.text = formatTime(seconds)
        })
    }

    /** Format seconds → mm:ss */
    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onDestroy() {
        if (isBound) unbindService(connection)
        super.onDestroy()
    }
}
