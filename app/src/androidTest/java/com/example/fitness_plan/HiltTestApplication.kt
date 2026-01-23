package com.example.fitness_plan

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltTestApplication : Application() {

    override fun getDefaultWorkerConfiguration(): Configuration {
        return super.getDefaultWorkerConfiguration()
            .setWorkerFactory(HiltWorkerFactory.getInstance(this))
    }
}
