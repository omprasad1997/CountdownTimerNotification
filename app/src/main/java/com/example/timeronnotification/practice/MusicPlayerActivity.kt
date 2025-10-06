package com.example.timeronnotification.practice

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.timeronnotification.Constants
import com.example.timeronnotification.R
import com.example.timeronnotification.databinding.ActivityMusicPlayerBinding

class MusicPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_player)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            startService(serviceIntent)
        }

        binding.stopButton.setOnClickListener {
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
            startService(serviceIntent)
        }
    }
}