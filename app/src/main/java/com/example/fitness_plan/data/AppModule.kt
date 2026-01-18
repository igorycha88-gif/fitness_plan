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
}
