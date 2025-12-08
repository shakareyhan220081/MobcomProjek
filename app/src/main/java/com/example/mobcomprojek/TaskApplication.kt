package com.example.mobcomprojek

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.work.*
import com.example.mobcomprojek.data.DataRepository
import com.example.mobcomprojek.worker.SyncWorker
import java.util.concurrent.TimeUnit

class TaskApplication : Application() {

    lateinit var repository: DataRepository

    override fun onCreate() {
        super.onCreate()
        repository = DataRepository(this)

        createNotificationChannel()
        setupBackgroundSync()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val oldChannelId = "duecal_reminder_channel" // ID Lama (Hapus ini)
            val newChannelId = "duecal_reminder_channel_fix" // ID Baru (Fresh Start)

            val name = "DueCal Reminders"
            val descriptionText = "Notifications for tasks and notes reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH // High = Muncul Popup & Suara

            // Lokasi file suara: res/raw/custom_alert.mp3
            val soundUri = Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${packageName}/${R.raw.custom_alert}"
            )

            // Atribut Audio: Set sebagai ALARM agar suaranya kencang/prioritas
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. HAPUS CHANNEL LAMA (Pembersihan)
            notificationManager.deleteNotificationChannel(oldChannelId)
            notificationManager.deleteNotificationChannel("duecal_reminder_channel_v2")

            // 2. BUAT CHANNEL BARU
            val channel = NotificationChannel(newChannelId, name, importance).apply {
                description = descriptionText
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupBackgroundSync() {
        // ... (Kode sama seperti sebelumnya) ...
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DueCalSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}