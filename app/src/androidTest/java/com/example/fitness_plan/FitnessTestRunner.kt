package com.example.fitness_plan

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class FitnessTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
        try {
            val inputManagerClass = Class.forName("android.hardware.input.InputManager")
            try {
                val getInstanceMethod = inputManagerClass.getMethod("getInstance")
                getInstanceMethod.isAccessible = true
            } catch (e: NoSuchMethodException) {
                // Method doesn't exist in API 34+, ignore
            }
        } catch (e: Exception) {
            // Class not found, ignore
        }
    }
}
