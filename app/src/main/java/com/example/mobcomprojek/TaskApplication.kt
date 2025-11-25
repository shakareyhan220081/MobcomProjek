package com.example.mobcomprojek

import android.app.Application
import com.example.mobcomprojek.data.AppDatabase
import com.example.mobcomprojek.data.TaskRepository

class TaskApplication : Application() {

    // Inisialisasi database dan repository secara lazy
    private val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}