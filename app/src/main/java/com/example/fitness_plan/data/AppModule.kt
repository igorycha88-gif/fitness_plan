package com.example.fitness_plan.data

import android.content.Context
import android.content.SharedPreferences
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.calculator.WorkoutDateCalculator
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.admin.AdminCredentialsRepository as AdminCredentialsDomainRepository
import com.example.fitness_plan.domain.usecase.AdminUseCase
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import com.example.fitness_plan.domain.usecase.WeightProgressionUseCase
import com.example.fitness_plan.data.CredentialsRepository
import com.example.fitness_plan.data.ReferenceDataRepositoryImpl
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.example.fitness_plan.domain.repository.MuscleGroupStatsRepository
import com.example.fitness_plan.domain.repository.ReferenceDataRepository
import com.example.fitness_plan.domain.repository.UserRepository as DomainUserRepository
import com.example.fitness_plan.domain.repository.WeightRepository as DomainWeightRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import com.example.fitness_plan.domain.repository.BodyParametersRepository as DomainBodyParametersRepository
import com.example.fitness_plan.domain.repository.WorkoutScheduleRepository as DomainWorkoutScheduleRepository
import com.example.fitness_plan.domain.usecase.AuthUseCase
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.ReferenceDataUseCase
import com.example.fitness_plan.domain.usecase.WeightUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import com.example.fitness_plan.domain.usecase.BodyParametersUseCase
import com.example.fitness_plan.domain.usecase.BodyParameterCalculator
import com.example.fitness_plan.domain.usecase.MeasurementValidator
import com.example.fitness_plan.presentation.viewmodel.BodyParametersViewModel
import com.example.fitness_plan.presentation.viewmodel.BodyParametersStatsViewModel
import com.google.gson.Gson
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
    fun provideContext(@ApplicationContext context: android.content.Context): android.content.Context {
        return context
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: android.content.Context): DomainUserRepository {
        return UserRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeightRepository(@ApplicationContext context: android.content.Context): DomainWeightRepository {
        return WeightRepository(context)
    }

    @Provides
    @Singleton
    fun provideExerciseStatsRepository(@ApplicationContext context: android.content.Context): ExerciseStatsRepository {
        return ExerciseStatsRepository(context)
    }

    @Provides
    @Singleton
    fun provideMuscleGroupStatsRepository(@ApplicationContext context: android.content.Context): MuscleGroupStatsRepository {
        return MuscleGroupStatsRepository(context)
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
    ): ICredentialsRepository {
        return CredentialsRepository(encryptedPrefs, passwordHasher)
    }

    @Provides
    @Singleton
    fun provideExerciseCompletionRepository(@ApplicationContext context: android.content.Context): ExerciseCompletionRepository {
        return ExerciseCompletionRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutScheduleRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): DomainWorkoutScheduleRepository {
        return WorkoutScheduleRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: android.content.Context): NotificationRepository {
        return NotificationRepository(context)
    }

    @Provides
    @Singleton
    fun provideCycleRepository(@ApplicationContext context: android.content.Context): CycleRepository {
        return CycleRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        @ApplicationContext context: Context,
        exerciseCompletionRepository: ExerciseCompletionRepository,
        workoutScheduleRepository: WorkoutScheduleRepository,
        weightCalculator: WeightCalculator,
        workoutDateCalculator: WorkoutDateCalculator,
        exerciseLibraryRepository: ExerciseLibraryRepository
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(context, exerciseCompletionRepository, workoutScheduleRepository, weightCalculator, workoutDateCalculator, exerciseLibraryRepository)
    }

    @Provides
    @Singleton
    fun provideAuthUseCase(
        credentialsRepository: com.example.fitness_plan.domain.repository.ICredentialsRepository,
        userRepository: DomainUserRepository
    ): AuthUseCase {
        return AuthUseCase(credentialsRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideAdminCredentialsRepository(
        encryptedPrefs: SharedPreferences
    ): AdminCredentialsDomainRepository {
        return com.example.fitness_plan.data.admin.AdminCredentialsRepository(encryptedPrefs)
    }

    @Provides
    @Singleton
    fun provideAdminUseCase(adminCredentialsRepository: AdminCredentialsDomainRepository): AdminUseCase {
        return AdminUseCase(adminCredentialsRepository)
    }

    @Provides
    @Singleton
    fun provideCycleUseCase(
        cycleRepository: CycleRepository,
        workoutRepository: WorkoutRepository,
        userRepository: DomainUserRepository,
        exerciseCompletionRepository: ExerciseCompletionRepository,
        weightProgressionUseCase: WeightProgressionUseCase
    ): CycleUseCase {
        return CycleUseCase(cycleRepository, workoutRepository, userRepository, exerciseCompletionRepository, weightProgressionUseCase)
    }

    @Provides
    @Singleton
    fun provideWeightProgressionUseCase(
        workoutRepository: WorkoutRepository,
        exerciseStatsRepository: ExerciseStatsRepository,
        userRepository: DomainUserRepository,
        weightCalculator: WeightCalculator
    ): WeightProgressionUseCase {
        return WeightProgressionUseCase(workoutRepository, exerciseStatsRepository, userRepository, weightCalculator)
    }

    @Provides
    @Singleton
    fun provideMuscleGroupStatsUseCase(
        muscleGroupStatsRepository: MuscleGroupStatsRepository,
        exerciseLibraryRepository: ExerciseLibraryRepository
    ): com.example.fitness_plan.domain.usecase.MuscleGroupStatsUseCase {
        return com.example.fitness_plan.domain.usecase.MuscleGroupStatsUseCase(muscleGroupStatsRepository, exerciseLibraryRepository)
    }

    @Provides
    @Singleton
    fun provideWorkoutUseCase(
        workoutRepository: WorkoutRepository,
        exerciseStatsRepository: ExerciseStatsRepository,
        exerciseCompletionRepository: ExerciseCompletionRepository,
        muscleGroupStatsUseCase: com.example.fitness_plan.domain.usecase.MuscleGroupStatsUseCase
    ): WorkoutUseCase {
        return WorkoutUseCase(workoutRepository, exerciseStatsRepository, exerciseCompletionRepository, muscleGroupStatsUseCase)
    }

    @Provides
    @Singleton
    fun provideWeightUseCase(weightRepository: DomainWeightRepository): WeightUseCase {
        return WeightUseCase(weightRepository)
    }

    @Provides
    @Singleton
    fun provideWeightCalculator(): WeightCalculator {
        return WeightCalculator()
    }

    @Provides
    @Singleton
    fun provideWorkoutDateCalculator(): WorkoutDateCalculator {
        return WorkoutDateCalculator()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideReferenceDataRepository(
        @ApplicationContext context: Context,
        gson: Gson
    ): ReferenceDataRepository {
        return ReferenceDataRepositoryImpl(context, gson)
    }

    @Provides
    @Singleton
    fun provideReferenceDataUseCase(
        referenceDataRepository: ReferenceDataRepository,
        userRepository: DomainUserRepository
    ): ReferenceDataUseCase {
        return ReferenceDataUseCase(referenceDataRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideExerciseLibraryRepository(
        @ApplicationContext context: Context,
        gson: Gson
    ): ExerciseLibraryRepository {
        return ExerciseLibraryRepositoryImpl(context, gson)
    }

    @Provides
    @Singleton
    fun provideExerciseLibraryUseCase(
        exerciseLibraryRepository: ExerciseLibraryRepository
    ): com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase {
        return com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase(exerciseLibraryRepository)
    }

    @Provides
    @Singleton
    fun provideExerciseLibraryViewModel(
        exerciseLibraryUseCase: ExerciseLibraryUseCase
    ): com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel {
        return com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel(exerciseLibraryUseCase)
    }

    @Provides
    @Singleton
    fun provideBodyParametersRepository(
        @ApplicationContext context: Context
    ): DomainBodyParametersRepository {
        return BodyParametersRepository(context)
    }

    @Provides
    @Singleton
    fun provideBodyParameterCalculator(): BodyParameterCalculator {
        return BodyParameterCalculator()
    }

    @Provides
    @Singleton
    fun provideMeasurementValidator(): MeasurementValidator {
        return MeasurementValidator()
    }

    @Provides
    @Singleton
    fun provideBodyParametersUseCase(
        bodyParametersRepository: DomainBodyParametersRepository,
        calculator: BodyParameterCalculator,
        validator: MeasurementValidator
    ): BodyParametersUseCase {
        return BodyParametersUseCase(bodyParametersRepository, calculator, validator)
    }

    @Provides
    @Singleton
    fun provideBodyParametersViewModel(
        bodyParametersUseCase: BodyParametersUseCase,
        userRepository: DomainUserRepository
    ): BodyParametersViewModel {
        return BodyParametersViewModel(bodyParametersUseCase, userRepository)
    }

    @Provides
    @Singleton
    fun provideBodyParametersStatsViewModel(
        bodyParametersUseCase: BodyParametersUseCase,
        credentialsRepository: ICredentialsRepository
    ): BodyParametersStatsViewModel {
        return BodyParametersStatsViewModel(bodyParametersUseCase, credentialsRepository)
    }
}
