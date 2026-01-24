package com.example.fitness_plan

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDex
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

private const val TAG = "FitnessPlan"

@HiltAndroidApp
class FitnessPlanApplication : Application() {

    @Inject
    lateinit var masterPassword: String

    @Inject
    lateinit var credentialsRepository: ICredentialsRepository

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        Log.d(TAG, "========================================")
        Log.d(TAG, "APP STARTING...")
        Log.d(TAG, "========================================")
        
        Log.d(TAG, "Master password loaded: ${masterPassword.take(8)}...")
        
        // Remove the reloadCredentials() call - AdminCredentialsRepository now initializes lazily
        // when first accessed or explicitly initialized
        
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "Version: ${packageManager.getPackageInfo(packageName, 0).versionName}")
        
        Log.d(TAG, "Security module ready")
        Log.d(TAG, "APP START COMPLETE")
        Log.d(TAG, "========================================")
    }
}
