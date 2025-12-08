package com.example.mobcomprojek.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager // Opsional untuk versi lama
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.mobcomprojek.MainActivity
import com.example.mobcomprojek.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITLE") ?: "Reminder"
        val message = intent.getStringExtra("MESSAGE") ?: "You have a task due!"
        val id = intent.getIntExtra("ID", 0)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // UPDATE: Gunakan Channel ID yang BARU
        val channelId = "duecal_reminder_channel_fix"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Ganti ke MAX
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Tambahkan Kategori Alarm
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}