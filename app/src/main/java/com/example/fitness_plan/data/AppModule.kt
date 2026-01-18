package com.example.fitness_plan.data // Или com.example.fitness_plan.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Устанавливаем модуль в компонент уровня приложения
object AppModule {

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository {
        // Hilt автоматически предоставляет контекст приложения
        return UserRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeightRepository(@ApplicationContext context: Context): WeightRepository {
        return WeightRepository(context)
    }

    @Provides
    @Singleton
    fun provideExerciseStatsRepository(@ApplicationContext context: Context): ExerciseStatsRepository {
        return ExerciseStatsRepository(context)
    }

    @Provides
    @Singleton
    fun provideCredentialsRepository(@ApplicationContext context: Context): CredentialsRepository {
        return CredentialsRepository(context)
    }

    @Provides
    @Singleton
    fun provideExerciseCompletionRepository(@ApplicationContext context: Context): ExerciseCompletionRepository {
        return ExerciseCompletionRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutScheduleRepository(@ApplicationContext context: Context): WorkoutScheduleRepository {
        return WorkoutScheduleRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: Context): NotificationRepository {
        return NotificationRepository(context)
    }
}
