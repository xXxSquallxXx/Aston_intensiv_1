package com.example.audioplayer

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.view.View

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show()
                startNotificationService()
            } else {
                Toast.makeText(this, "Уведомления отключены", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun startNotificationService() {
        Toast.makeText(this, "Уведомления готовы к работе", Toast.LENGTH_SHORT).show()
    }

    fun playPause(view: View) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "TOGGLE_PLAY_PAUSE"
        startService(intent)
    }

    fun nextTrack(view: View) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "NEXT_TRACK"
        startService(intent)
    }

    fun previouslyTrack(view: View) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "PREVIOUS_TRACK"
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.clearApplicationUserData()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}
