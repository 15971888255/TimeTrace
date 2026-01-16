package com.example.timetrace.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.timetrace.data.repository.MainRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RoutineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val routines = repository.getAllRoutines().first()
            routines.forEach { routine ->
                // This logic should be identical to the one in MainRepository,
                // but for now, we'll just call the repository method for simplicity.
                // In a more advanced scenario, this logic would live here.
                repository.addRoutineAndGenerateSchedules(routine)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
