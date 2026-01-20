package com.example.fitness_plan.data

import android.content.Context
import android.content.SharedPreferences
import com.example.fitness_plan.domain.repository.CredentialsRepository
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository as DomainUserRepository
import com.example.fitness_plan.domain.repository.WeightRepository as DomainWeightRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import com.example.fitness_plan.domain.repository.WorkoutScheduleRepository as DomainWorkoutScheduleRepository
import com.example.fitness_plan.domain.usecase.AuthUseCase
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.WeightUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): DomainUserRepository {
        return UserRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeightRepository(@ApplicationContext context: Context): DomainWeightRepository {
        return WeightRepository(context)
    }

    @Provides
    @Singleton
    fun provideExerciseStatsRepository(@ApplicationContext context: Context): ExerciseStatsRepository {
        return ExerciseStatsRepository(context)
    }

    @Provides
    @Singleton
    fun providePasswordHasher(): PasswordHasher {
        return PasswordHasher()
    }

    @Provides
    @Singleton
    fun provideCredentialsRepository(
        encryptedPrefs: SharedPreferences,
        passwordHasher: PasswordHasher
    ): CredentialsRepository {
        return CredentialsRepository(encryptedPrefs, passwordHasher)
    }

    @Provides
    @Singleton
    fun provideExerciseCompletionRepository(@ApplicationContext context: Context): ExerciseCompletionRepository {
        return ExerciseCompletionRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutScheduleRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): DomainWorkoutScheduleRepository {
        return WorkoutScheduleRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: Context): NotificationRepository {
        return NotificationRepository(context)
    }

    @Provides
    @Singleton
    fun provideCycleRepository(@ApplicationContext context: Context): CycleRepository {
        return CycleRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        @ApplicationContext context: Context,
        exerciseCompletionRepository: ExerciseCompletionRepository,
        workoutScheduleRepository: WorkoutScheduleRepository
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(context, exerciseCompletionRepository, workoutScheduleRepository)
    }

    @Provides
    @Singleton
    fun provideAuthUseCase(
        credentialsRepository: CredentialsRepository,
        userRepository: DomainUserRepository
    ): AuthUseCase {
        return AuthUseCase(credentialsRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideCycleUseCase(
        cycleRepository: CycleRepository,
        workoutRepository: WorkoutRepository,
        userRepository: DomainUserRepository,
        exerciseCompletionRepository: ExerciseCompletionRepository
    ): CycleUseCase {
        return CycleUseCase(cycleRepository, workoutRepository, userRepository, exerciseCompletionRepository)
    }

    @Provides
    @Singleton
    fun provideWorkoutUseCase(
        workoutRepository: WorkoutRepository,
        exerciseStatsRepository: ExerciseStatsRepository,
        exerciseCompletionRepository: ExerciseCompletionRepository
    ): WorkoutUseCase {
        return WorkoutUseCase(workoutRepository, exerciseStatsRepository, exerciseCompletionRepository)
    }

    @Provides
    @Singleton
    fun provideWeightUseCase(weightRepository: DomainWeightRepository): WeightUseCase {
        return WeightUseCase(weightRepository)
    }
}
