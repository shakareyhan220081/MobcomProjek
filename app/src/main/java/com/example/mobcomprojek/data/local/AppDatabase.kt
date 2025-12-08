package com.example.mobcomprojek.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobcomprojek.data.*

@Database(
    entities = [NoteItem::class, Category::class, TaskParent::class, Subtask::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "duecal_local_db"
                )
                    .fallbackToDestructiveMigration() // Reset DB jika model berubah (aman utk development)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}