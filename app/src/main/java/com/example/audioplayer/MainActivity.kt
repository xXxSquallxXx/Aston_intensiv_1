package com.example.audioplayer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var trackTitleTextView: TextView? = null

    private val trackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicService.ACTION_UPDATE_TRACK) {
                val trackName = intent.getStringExtra(MusicService.EXTRA_TRACK_NAME)
                trackTitleTextView?.text = trackName
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
                startNotificationService()
            } else {
                Toast.makeText(this, getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val trackName = trackTitleTextView?.text.toString()
        outState.putString("CURRENT_TRACK_NAME", trackName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        trackTitleTextView = findViewById(R.id.textViewTrackTitle)

        trackTitleTextView?.isSelected = true

        savedInstanceState?.let {
            val restoredTrackName = it.getString("CURRENT_TRACK_NAME")
            trackTitleTextView?.text = restoredTrackName
        }

        val filter = IntentFilter(MusicService.ACTION_UPDATE_TRACK)
        registerReceiver(trackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startNotificationService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startNotificationService()
        }

        val playPauseButton = findViewById<Button>(R.id.button_play_pause)
        playPauseButton.setOnClickListener {
            playPause()
        }

        val nextButton = findViewById<Button>(R.id.button_next)
        nextButton.setOnClickListener {
            nextTrack()
        }

        val previousButton = findViewById<Button>(R.id.button_previous)
        previousButton.setOnClickListener {
            previouslyTrack()
        }
    }

    private fun startNotificationService() {
        Toast.makeText(this, getString(R.string.notifications_ready), Toast.LENGTH_SHORT).show()
    }

    fun playPause() {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "TOGGLE_PLAY_PAUSE"
        startService(intent)
    }

    fun nextTrack() {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "NEXT_TRACK"
        startService(intent)
    }

    fun previouslyTrack() {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "PREVIOUS_TRACK"
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(trackReceiver)
    }
}
