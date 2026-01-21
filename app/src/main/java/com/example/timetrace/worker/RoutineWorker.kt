package com.example.timetrace.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.timetrace.data.repository.MainRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RoutineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.resetAllRoutines()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}