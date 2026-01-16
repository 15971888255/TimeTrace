package com.example.timetrace

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.timetrace.worker.RoutineWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class TimeTraceApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun setupRecurringWork() {
        val repeatingRequest = PeriodicWorkRequestBuilder<RoutineWorker>(
            repeatInterval = 7, 
            repeatIntervalTimeUnit = TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "routine-worker",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
