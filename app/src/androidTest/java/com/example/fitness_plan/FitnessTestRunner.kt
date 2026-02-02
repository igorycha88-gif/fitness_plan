package com.example.fitness_plan

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class FitnessTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
        applyApi34Workarounds()
    }

    private fun applyApi34Workarounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                disableInputManagerInteraction()
                disableForceInjectPermissions()
            } catch (e: Exception) {
                // Silently ignore reflection failures
            }
        }
    }

    private fun disableInputManagerInteraction() {
        try {
            val inputManagerClass = Class.forName("android.hardware.input.InputManager")
            val getInstanceMethod = inputManagerClass.getMethod("getInstance")
            getInstanceMethod.isAccessible = true
        } catch (e: NoSuchMethodException) {
            // InputManager.getInstance() removed in API 34+, expected
        } catch (e: ClassNotFoundException) {
            // Class not found, ignore
        }
    }

    private fun disableForceInjectPermissions() {
        try {
            val espressoClass = Class.forName("androidx.test.espresso.Espresso")
            val forceInjectPermissionsField = espressoClass.getDeclaredField("forceInjectPermissions")
            forceInjectPermissionsField.isAccessible = true
            forceInjectPermissionsField.setBoolean(null, false)
        } catch (e: Exception) {
            // Field not found or reflection failed, ignore
        }
    }
}
