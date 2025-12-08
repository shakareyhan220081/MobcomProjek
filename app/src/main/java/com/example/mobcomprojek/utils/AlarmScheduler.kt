package com.example.mobcomprojek.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mobcomprojek.data.NoteItem
import com.example.mobcomprojek.data.TaskParent

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Schedule untuk NOTE
    fun scheduleNote(note: NoteItem) {
        if (note.reminderTime == null) {
            cancel(note.id.hashCode())
            return
        }
        // Jangan schedule jika waktu sudah lewat
        if (note.reminderTime <= System.currentTimeMillis()) return

        scheduleAlarm(note.id.hashCode(), note.reminderTime, note.title, "Reminder for your note")
    }

    // Schedule untuk TASK
    fun scheduleTask(task: TaskParent) {
        if (task.reminderTime == null) {
            cancel(task.id.hashCode())
            return
        }
        if (task.reminderTime <= System.currentTimeMillis()) return

        scheduleAlarm(task.id.hashCode(), task.reminderTime, task.title, "Task deadline is approaching!")
    }

    // Fungsi Dasar Alarm
    private fun scheduleAlarm(id: Int, time: Long, title: String, message: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            putExtra("ID", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancel(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}