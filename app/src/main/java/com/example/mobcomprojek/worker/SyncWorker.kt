package com.example.mobcomprojek.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mobcomprojek.TaskApplication

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Ambil Repository
        val repository = (applicationContext as TaskApplication).repository

        return try {
            // Panggil fungsi sinkronisasi (Pull dari Firebase)
            repository.syncAllData()
            // Note: Idealnya repository punya fungsi detail untuk upload pending data juga

            Result.success()
        } catch (e: Exception) {
            Result.retry() // Coba lagi nanti jika gagal
        }
    }
}