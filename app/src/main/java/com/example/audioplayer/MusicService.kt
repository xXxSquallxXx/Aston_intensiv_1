package com.example.audioplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackIndex = 0
    private val channelId = "music_channel"
    private val notificationId = 1

    companion object {
        const val ACTION_NEXT = "NEXT_TRACK"
        const val ACTION_PREVIOUS = "PREVIOUS_TRACK"
        const val ACTION_TOGGLE_PLAY_PAUSE = "TOGGLE_PLAY_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaPlayer = MediaPlayer.create(this, R.raw.track1)

        mediaPlayer?.setOnPreparedListener {
            it.start()
        }

        startForeground(notificationId, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_NEXT -> nextTrack()
            ACTION_PREVIOUS -> previousTrack()
            ACTION_TOGGLE_PLAY_PAUSE -> togglePlayPause()
        }
        updateNotification()
        return START_STICKY
    }

    private fun togglePlayPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    private fun nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % 3
        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer.create(this, when (currentTrackIndex) {
            0 -> R.raw.track1
            1 -> R.raw.track2
            else -> R.raw.track3
        })
        mediaPlayer?.start()
    }

    private fun previousTrack() {
        currentTrackIndex -= 1
        if (currentTrackIndex < 0) currentTrackIndex = 2
        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer.create(this, when (currentTrackIndex) {
            0 -> R.raw.track1
            1 -> R.raw.track2
            else -> R.raw.track3
        })
        mediaPlayer?.start()
    }

    private fun createNotification(): android.app.Notification {
        val playPauseIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_TOGGLE_PLAY_PAUSE
        }
        val nextIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_NEXT
        }
        val previousIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PREVIOUS
        }

        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPendingIntent = PendingIntent.getService(
            this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val previousPendingIntent = PendingIntent.getService(
            this, 2, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("HardcorePlayer")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_music_note)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_previous, "Previous", previousPendingIntent)
            .addAction(
                if (mediaPlayer?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play,
                "Play/Pause", playPausePendingIntent
            )
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, createNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for music playback"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
        stopSelf()
    }
}
