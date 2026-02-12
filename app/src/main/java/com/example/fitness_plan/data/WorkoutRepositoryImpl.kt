package com.example.fitness_plan.data

import android.util.Log
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.calculator.WorkoutDateCalculator
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WorkoutRepository"

private val Context.workoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_plans")

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val context: Context,
    private val exerciseCompletionRepository: ExerciseCompletionRepository,
    private val workoutScheduleRepository: WorkoutScheduleRepository,
    private val weightCalculator: WeightCalculator,
    private val workoutDateCalculator: WorkoutDateCalculator,
    private val exerciseLibraryRepository: com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
) : WorkoutRepository {

    private val gson = Gson()

    private fun getUserPlanKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_user_workout_plan")
    }

    private fun getSelectedPlanTypeKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_selected_plan_type")
    }

    private fun getDataStorePath(): String {
        return try {
            val dataDir = context.dataDir?.absolutePath ?: "N/A"
            val datastorePath = File(dataDir, "datastore/workout_plans.preferences_pb").absolutePath
            Log.d(TAG, "DataStore path: $datastorePath")
            Log.d(TAG, "DataStore exists: ${File(datastorePath).exists()}")
            datastorePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get DataStore path", e)
            "Error getting path: ${e.message}"
        }
    }

    init {
        Log.d(TAG, "=== WorkoutRepositoryImpl initialized ===")
        getDataStorePath()
    }

    private suspend fun findFavoriteExercise(
        favoriteExercises: Set<String>,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        targetMuscleGroups: List<String>
    ): com.example.fitness_plan.domain.model.ExerciseLibrary? {
        return exerciseLibrary.find { exercise ->
            exercise.name in favoriteExercises &&
            hasCommonMuscleGroup(targetMuscleGroups, exercise.muscleGroups.map { it.displayName })
        }
    }

    private fun hasCommonMuscleGroup(groups1: List<String>, groups2: List<String>): Boolean {
        return groups1.any { it in groups2 }
    }

    private suspend fun applyFavoriteExercisesToDay(
        day: WorkoutDay,
        favoriteExercises: Set<String>,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>
    ): WorkoutDay {
        val updatedExercises = day.exercises.mapIndexed { index, exercise ->
            val substitutionIndex = day.exercises.size / 2
            if (index < substitutionIndex) {
                val exerciseMuscleGroups = getMuscleGroupsForExercise(exercise.name)
                val favoriteExercise = findFavoriteExercise(favoriteExercises, exerciseLibrary, exerciseMuscleGroups)
                
                if (favoriteExercise != null && favoriteExercise.name != exercise.name) {
                    exercise.copy(
                        name = favoriteExercise.name,
                        description = favoriteExercise.description,
                        muscleGroups = favoriteExercise.muscleGroups,
                        equipment = favoriteExercise.equipment,
                        exerciseType = favoriteExercise.exerciseType,
                        stepByStepInstructions = favoriteExercise.stepByStepInstructions,
                        animationUrl = favoriteExercise.animationUrl,
                        imageRes = favoriteExercise.imageRes,
                        imageUrl = favoriteExercise.imageUrl,
                        isFavoriteSubstitution = true
                    )
                } else {
                    exercise
                }
            } else {
                exercise
            }
        }
        return day.copy(exercises = updatedExercises)
    }

    private suspend fun applyFavoriteExercises(
        plan: WorkoutPlan,
        favoriteExercises: Set<String>
    ): WorkoutPlan {
        if (favoriteExercises.isEmpty()) return plan
        
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()
        val updatedDays = plan.days.map { day ->
            applyFavoriteExercisesToDay(day, favoriteExercises, exerciseLibrary)
        }
        return plan.copy(days = updatedDays)
    }

    override suspend fun getWorkoutPlanForUser(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        Log.d(TAG, "getWorkoutPlanForUser: goal=${profile.goal}, level=${profile.level}, frequency=${profile.frequency}, favorites=${profile.favoriteExercises.size}")
        val plan = createWorkoutPlanWithNewLogic(profile)
        
        val planWithFavorites = applyFavoriteExercises(plan, profile.favoriteExercises)
        Log.d(TAG, "Plan created: ${planWithFavorites.name}, days=${planWithFavorites.days.size}")
        return planWithFavorites
    }

    override suspend fun getCycleWorkoutPlan(basePlan: WorkoutPlan, frequency: String): WorkoutPlan {
        val totalDays = 10

        val days = mutableListOf<WorkoutDay>()
        val exerciseNames = basePlan.days.flatMap { day -> day.exercises.map { it.name } }.distinct()
        var dayCounter = 1

        for (i in 0 until totalDays) {
            val dayIndex = i % basePlan.days.size
            val baseDay = basePlan.days[dayIndex]
            val newExercises = baseDay.exercises.map { exercise ->
                exercise.copy(id = "${i}_${exercise.name}")
            }
            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День $dayCounter",
                    exercises = newExercises,
                    muscleGroups = baseDay.muscleGroups
                )
            )
            dayCounter++
        }

        val planName = "10-дневный план"

        return basePlan.copy(
            name = planName,
            days = days
        )
    }

    override suspend fun generateCycleDates(startDate: Long, frequency: String): List<Long> {
        return workoutDateCalculator.generateDates(startDate, frequency, 10)
    }

    override suspend fun getWorkoutPlanWithDates(plan: WorkoutPlan, dates: List<Long>): WorkoutPlan {
        val daysWithDates = plan.days.mapIndexed { index, day ->
            day.copy(scheduledDate = dates.getOrNull(index))
        }
        return plan.copy(days = daysWithDates)
    }

    override suspend fun saveWorkoutPlan(username: String, plan: WorkoutPlan) {
        try {
            Log.d(TAG, "saveWorkoutPlan: START for username=$username, plan=${plan.name}")
            Log.d(TAG, "saveWorkoutPlan: days=${plan.days.size}, goal=${plan.goal}, level=${plan.level}")

            val json = gson.toJson(plan)
            Log.d(TAG, "saveWorkoutPlan: JSON size=${json.length} bytes")

            context.workoutDataStore.edit { preferences ->
                preferences[stringPreferencesKey("${username}_workout_plan")] = json
            }

            Log.d(TAG, "saveWorkoutPlan: SUCCESS - saved plan for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "saveWorkoutPlan: FAILED to save plan for username=$username", e)
            throw e
        }
    }

    private fun updateExerciseFromLibrary(
        exercise: Exercise,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>
    ): Exercise {
        val libraryExercise = exerciseLibrary.find { it.name == exercise.name }

        return if (libraryExercise != null) {
            exercise.copy(
                imageRes = if (exercise.imageRes == null) libraryExercise.imageRes else exercise.imageRes,
                imageUrl = if (exercise.imageUrl == null) libraryExercise.imageUrl else exercise.imageUrl
            )
        } else {
            exercise
        }
    }

    private suspend fun updateWorkoutPlanFromLibrary(plan: WorkoutPlan): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()
        val updatedDays = plan.days.map { day ->
            day.copy(exercises = day.exercises.map { exercise ->
                updateExerciseFromLibrary(exercise, exerciseLibrary)
            })
        }
        return plan.copy(days = updatedDays)
    }

    override fun getWorkoutPlan(username: String): Flow<WorkoutPlan?> {
        val key = stringPreferencesKey("${username}_workout_plan")
        Log.d(TAG, "getWorkoutPlan: requesting plan for username=$username")

        return context.workoutDataStore.data.map { preferences ->
            val json = preferences[key]
            if (json != null) {
                try {
                    val plan = gson.fromJson(json, WorkoutPlan::class.java)
                    if (plan.planType == null) {
                        plan.copy(planType = com.example.fitness_plan.domain.model.PlanType.AUTO)
                    } else {
                        plan
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getWorkoutPlan: FAILED to parse plan for username=$username", e)
                    null
                }
            } else {
                Log.d(TAG, "getWorkoutPlan: no plan found for username=$username")
                null
            }
        }
    }

    override suspend fun saveAdminWorkoutPlan(plan: WorkoutPlan) {
        val json = gson.toJson(plan)
        context.workoutDataStore.edit { preferences ->
            preferences[stringPreferencesKey("admin_workout_plan")] = json
        }
    }

    override fun getAdminWorkoutPlan(): Flow<WorkoutPlan?> {
        val key = stringPreferencesKey("admin_workout_plan")
        return context.workoutDataStore.data.map { preferences ->
            val json = preferences[key]
            if (json != null) {
                try {
                    val plan = gson.fromJson(json, WorkoutPlan::class.java)
                    if (plan.planType == null) {
                        plan.copy(planType = com.example.fitness_plan.domain.model.PlanType.ADMIN)
                    } else {
                        plan
                    }
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    override suspend fun saveUserWorkoutPlan(username: String, plan: WorkoutPlan) {
        try {
            Log.d(TAG, "saveUserWorkoutPlan: START for username=$username, plan=${plan.name}")
            val key = getUserPlanKey(username)
            val json = gson.toJson(plan)
            context.workoutDataStore.edit { preferences ->
                preferences[key] = json
            }
            Log.d(TAG, "saveUserWorkoutPlan: SUCCESS - saved user plan for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "saveUserWorkoutPlan: FAILED to save user plan for username=$username", e)
            throw e
        }
    }

    override fun getUserWorkoutPlan(username: String): Flow<WorkoutPlan?> {
        val key = getUserPlanKey(username)
        Log.d(TAG, "getUserWorkoutPlan: requesting user plan for username=$username")
        return context.workoutDataStore.data.map { preferences ->
            val json = preferences[key]
            if (json != null) {
                try {
                    val plan = gson.fromJson(json, WorkoutPlan::class.java)
                    if (plan.planType == null) {
                        plan.copy(planType = com.example.fitness_plan.domain.model.PlanType.USER)
                    } else {
                        plan
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getUserWorkoutPlan: FAILED to parse user plan for username=$username", e)
                    null
                }
            } else {
                Log.d(TAG, "getUserWorkoutPlan: no user plan found for username=$username")
                null
            }
        }
    }

    override suspend fun deleteUserWorkoutPlan(username: String) {
        try {
            Log.d(TAG, "deleteUserWorkoutPlan: START for username=$username")
            val key = getUserPlanKey(username)
            context.workoutDataStore.edit { preferences ->
                preferences.remove(key)
            }
            Log.d(TAG, "deleteUserWorkoutPlan: SUCCESS - deleted user plan for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "deleteUserWorkoutPlan: FAILED to delete user plan for username=$username", e)
            throw e
        }
    }

    override suspend fun updateUserPlan(username: String, plan: WorkoutPlan) {
        try {
            Log.d(TAG, "updateUserPlan: START for username=$username, plan=${plan.name}")
            val key = getUserPlanKey(username)
            val json = gson.toJson(plan)
            context.workoutDataStore.edit { preferences ->
                preferences[key] = json
            }
            Log.d(TAG, "updateUserPlan: SUCCESS - updated user plan for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "updateUserPlan: FAILED to update user plan for username=$username", e)
            throw e
        }
    }

    override suspend fun setSelectedPlanType(username: String, planType: com.example.fitness_plan.domain.repository.SelectedPlanType) {
        try {
            Log.d(TAG, "setSelectedPlanType: START for username=$username, planType=$planType")
            val key = getSelectedPlanTypeKey(username)
            context.workoutDataStore.edit { preferences ->
                preferences[key] = planType.name
            }
            Log.d(TAG, "setSelectedPlanType: SUCCESS - saved plan type for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "setSelectedPlanType: FAILED to save plan type for username=$username", e)
            throw e
        }
    }

    override fun getSelectedPlanType(username: String): Flow<com.example.fitness_plan.domain.repository.SelectedPlanType> {
        val key = getSelectedPlanTypeKey(username)
        return context.workoutDataStore.data.map { preferences ->
            val type = preferences[key] ?: com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO.name
            try {
                com.example.fitness_plan.domain.repository.SelectedPlanType.valueOf(type)
            } catch (e: Exception) {
                Log.e(TAG, "getSelectedPlanType: FAILED to parse plan type for username=$username, defaulting to AUTO", e)
                com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO
            }
        }
    }

    override suspend fun saveWorkoutSchedule(username: String, dates: List<Long>) {
        workoutScheduleRepository.saveWorkoutSchedule(username, dates)
    }

    private suspend fun getAlternatives(exerciseName: String): List<Exercise> {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()
        val currentExercise = exerciseLibrary.find { it.name == exerciseName }

        if (currentExercise == null || currentExercise.muscleGroups.isEmpty()) {
            return emptyList()
        }

        val alternativeLibraryExercises = exerciseLibraryRepository.getAlternativeExercises(
            currentExerciseName = exerciseName,
            currentMuscleGroups = currentExercise.muscleGroups,
            limit = 3
        )

        return alternativeLibraryExercises.map { libExercise ->
            Exercise(
                id = "alt_${libExercise.id}",
                name = libExercise.name,
                sets = 3,
                reps = "10-12",
                description = libExercise.description,
                muscleGroups = libExercise.muscleGroups,
                equipment = libExercise.equipment,
                exerciseType = libExercise.exerciseType,
                stepByStepInstructions = libExercise.stepByStepInstructions,
                animationUrl = libExercise.animationUrl,
                imageRes = libExercise.imageRes,
                imageUrl = libExercise.imageUrl
            )
        }
    }

    private fun getMuscleGroupsForExercise(name: String): List<String> {
        return when (name) {
            "Приседания", "Приседания со штангой", "Приседания с гантелями", "Фронтальные приседания", "Гакк-приседания", "Сумо-приседания", "Болгарские сплит-приседания", "Приседания на одной ноге" -> listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
            "Жим ногами", "Разведение ног", "Выпады", "Выпады назад", "Ягодичный мостик", "Подъёмы на носки стоя", "Подъёмы на носки сидя", "Сведение ног" -> listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
            "Жим лёжа", "Жим штанги лёжа", "Жим гантелей на наклонной скамье", "Жим на наклонной скамье", "Жим на наклонной скамье вниз", "Жим гантелей лёжа", "Разведение гантелей лёжа", "Пек-дек", "Отжимания на брусьях", "Отжимания узким хватом", "Отжимания" -> listOf("Грудь", "Трицепсы", "Плечи")
            "Становая тяга", "Румынская тяга", "Тяга штанги в наклоне", "Тяга гантели одной рукой", "Т-тяга с гантелью", "Тяга каната к лицу", "Пуловер с гантелью", "Гиперэкстензия", "Тяга верхнего блока", "Тяга верхнего блока узким хватом", "Подтягивания" -> listOf("Широчайшие", "Трапеции", "Бицепсы", "Предплечья", "Поясница")
            "Армейский жим", "Армейский жим с гантелями", "Жим гантелей сидя", "Жим на плечах в машине", "Жим Арнольда", "Разведение гантелей в стороны", "Обратные разведения", "Подъём штанги перед собой", "Махи гантелями перед собой" -> listOf("Плечи", "Трицепсы", "Трапеции")
            "Бицепс со штангой", "Бицепс", "Сгибания рук с гантелями", "Молотки", "Концентрированные сгибания" -> listOf("Бицепсы", "Предплечья", "Плечелучевая")
            "Трицепс", "Трицепс на блоке", "Французский жим", "Кикбэки", "Разгибания рук на блоке", "Разгибания на блоке из-за головы" -> listOf("Трицепсы", "Предплечья")
            "Пресс", "Скручивания", "Велосипед" -> listOf("Пресс")
            "Планка" -> listOf("Пресс", "Плечи", "Предплечья")
            "Бег", "Беговая дорожка", "Интервальный бег" -> listOf("Квадрицепсы", "Бёдра сзади", "Икры", "Ягодицы")
            "Велотренажёр", "Велотренажёр с сопротивлением", "Кардио: Комбо", "Эллипсоид", "Гребной тренажёр", "HIIT" -> listOf("Квадрицепсы", "Бёдра сзади", "Икры", "Ягодицы", "Широчайшие", "Бицепсы")
            else -> when {
                name.contains("Бицепс") || name.contains("Сгибания") -> listOf("Бицепсы", "Предплечья", "Плечелучевая")
                name.contains("Трицепс") || name.contains("Французский") || name.contains("Кикбэки") || name.contains("Разгибания") -> listOf("Трицепсы", "Предплечья")
                name.contains("Армейский") || name.contains("Жим гантелей") || name.contains("Разведение") || name.contains("Подъём") || name.contains("Махи") -> listOf("Плечи", "Трицепсы", "Трапеции")
                name.contains("Жим") && !name.contains("ног") -> listOf("Грудь", "Трицепсы", "Плечи")
                name.contains("Отжимания") || name.contains("Разведение") || name.contains("Пек-дек") -> listOf("Грудь", "Трицепсы", "Плечи")
                name.contains("Становая") || name.contains("Тяга") || name.contains("Гиперэкстензия") || name.contains("Подтягивания") || name.contains("Т-тяга") -> listOf("Широчайшие", "Трапеции", "Бицепсы", "Предплечья", "Поясница")
                name.contains("Приседания") || name.contains("Жим ног") || name.contains("Выпады") || name.contains("Ягодичный") || name.contains("Подъёмы на носки") || name.contains("Разведение ног") || name.contains("Сведение ног") -> listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
                else -> emptyList()
            }
        }
    }

    private fun getCardioDuration(level: String): String {
        return when (level) {
            "Новичок" -> "40 мин"
            "Любитель" -> "50 мин"
            "Профессионал" -> "60 мин"
            else -> "50 мин"
        }
    }

    private fun getMuscleGroupsFromNames(names: List<String>): List<com.example.fitness_plan.domain.model.MuscleGroup> {
        return names.mapNotNull { groupName ->
            com.example.fitness_plan.domain.model.MuscleGroup.values().find { it.displayName == groupName }
        }
    }

    private suspend fun createExerciseWithAlternatives(
        id: String,
        name: String,
        sets: Int,
        reps: String,
        description: String? = null,
        recommendedWeight: Float? = null,
        recommendedRepsPerSet: String? = null,
        profile: com.example.fitness_plan.domain.model.UserProfile? = null,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>? = null
    ): Exercise {
        val finalRecommendedWeight = if (recommendedWeight == null && profile != null && profile.weight > 0) {
            weightCalculator.calculateBaseWeight(
                bodyWeight = profile.weight.toFloat(),
                level = profile.level,
                goal = profile.goal,
                gender = profile.gender,
                exerciseType = weightCalculator.determineExerciseType(name)
            )
        } else {
            recommendedWeight
        }

        val finalRecommendedReps = if (recommendedRepsPerSet == null && profile != null) {
            weightCalculator.getRecommendedRepsString(profile.level)
        } else {
            recommendedRepsPerSet
        }

        val libExercise = exerciseLibrary?.find { it.name == name }

        val muscleGroups = if (libExercise != null && libExercise.muscleGroups.isNotEmpty()) {
            libExercise.muscleGroups
        } else {
            getMuscleGroupsFromNames(getMuscleGroupsForExercise(name))
        }

        return Exercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            weight = null,
            imageRes = libExercise?.imageRes,
            isCompleted = false,
            alternatives = emptyList(),
            description = libExercise?.description ?: description,
            recommendedWeight = finalRecommendedWeight,
            recommendedRepsPerSet = finalRecommendedReps,
            muscleGroups = muscleGroups,
            equipment = libExercise?.equipment ?: emptyList(),
            exerciseType = libExercise?.exerciseType ?: com.example.fitness_plan.domain.model.ExerciseType.STRENGTH,
            stepByStepInstructions = libExercise?.stepByStepInstructions,
            animationUrl = libExercise?.animationUrl,
            imageUrl = libExercise?.imageUrl
        )
    }

    private suspend fun createArmExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        sets: Int,
        reps: String,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("arm_1", "Бицепс со штангой", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_2", "Молотки", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_3", "Концентрированные сгибания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_4", "Сгибания рук с гантелями", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_5", "Трицепс на блоке", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_6", "Французский жим", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_7", "Кикбэки", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_8", "Разгибания на блоке из-за головы", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_9", "Сгибания рук с эспандером", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("arm_10", "Разгибания рук на блоке", sets, reps, profile = profile, exerciseLibrary = library)
        )
    }

    private suspend fun createShoulderExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        sets: Int,
        reps: String,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("shoulder_1", "Армейский жим", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_2", "Жим гантелей сидя", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_3", "Разведение гантелей в стороны", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_4", "Обратные разведения", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_5", "Подъём штанги перед собой", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_6", "Жим Арнольда", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_7", "Махи гантелями перед собой", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_8", "Жим на плечах в машине", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_9", "Тяга штанги к подбородку", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("shoulder_10", "Разведение гантелей в наклоне", sets, reps, profile = profile, exerciseLibrary = library)
        )
    }

    private suspend fun createChestExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        sets: Int,
        reps: String,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("chest_1", "Жим лёжа", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_2", "Жим на наклонной скамье", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_3", "Разведение гантелей лёжа", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_4", "Отжимания на брусьях", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_5", "Жим гантелей на наклонной скамье", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_6", "Пек-дек", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_7", "Жим гантелей лёжа", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_8", "Жим на наклонной скамье вниз", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_9", "Отжимания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("chest_10", "Разведение гантелей на наклонной скамье", sets, reps, profile = profile, exerciseLibrary = library)
        )
    }

    private suspend fun createBackExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        sets: Int,
        reps: String,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("back_1", "Становая тяга", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_2", "Тяга штанги в наклоне", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_3", "Тяга гантели одной рукой", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_4", "Тяга верхнего блока", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_5", "Тяга верхнего блока узким хватом", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_6", "Тяга каната к лицу", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_7", "Гиперэкстензия", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_8", "Подтягивания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_9", "Т-тяга с гантелью", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("back_10", "Пуловер с гантелью", sets, reps, profile = profile, exerciseLibrary = library)
        )
    }

    private suspend fun createLegExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        sets: Int,
        reps: String,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("leg_1", "Приседания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_2", "Жим ногами", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_3", "Выпады", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_4", "Приседания с гантелями", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_5", "Фронтальные приседания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_6", "Гакк-приседания", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_7", "Разведение ног", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_8", "Ягодичный мостик", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_9", "Выпады назад", sets, reps, profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("leg_10", "Подъёмы на носки стоя", sets, reps, profile = profile, exerciseLibrary = library)
        )
    }

    private suspend fun createCardioExercises(
        library: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<Exercise> {
        return listOf(
            createExerciseWithAlternatives("cardio_1", "Бег", 1, "", profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("cardio_2", "Велотренажёр", 1, "", profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("cardio_3", "Эллипсоид", 1, "", profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("cardio_4", "Гребной тренажёр", 1, "", profile = profile, exerciseLibrary = library),
            createExerciseWithAlternatives("cardio_5", "HIIT", 1, "", profile = profile, exerciseLibrary = library)
        ).map { it.copy(exerciseType = com.example.fitness_plan.domain.model.ExerciseType.CARDIO) }
    }

    private suspend fun createWorkoutPlanWithNewLogic(
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()
        
        val (sets, reps) = when (profile.level) {
            "Новичок" -> Pair(3, "12-15")
            "Любитель" -> Pair(3, "10-12")
            "Профессионал" -> Pair(4, "8-10")
            else -> Pair(3, "10-12")
        }
        
        val cardioDuration = getCardioDuration(profile.level)
        
        val armExercises = createArmExercises(exerciseLibrary, sets, reps, profile)
        val shoulderExercises = createShoulderExercises(exerciseLibrary, sets, reps, profile)
        val chestExercises = createChestExercises(exerciseLibrary, sets, reps, profile)
        val backExercises = createBackExercises(exerciseLibrary, sets, reps, profile)
        val legExercises = createLegExercises(exerciseLibrary, sets, reps, profile)
        val cardioExercises = createCardioExercises(exerciseLibrary, profile)
        
        val strengthPool = ExercisePool(
            armExercises + shoulderExercises + chestExercises + backExercises + legExercises
        )
        
        val cardioPool = ExercisePool(cardioExercises)
        
        data class DayConfiguration(
            val name: String,
            val muscleGroups: List<String>,
            val exerciseCount: Int,
            val isCardio: Boolean = false
        )
        
        val dayConfigurations = listOf(
            DayConfiguration("Руки", listOf("Бицепсы", "Трицепсы", "Предплечья", "Плечелучевая"), 6),
            DayConfiguration("Плечи", listOf("Плечи", "Трапеции"), 6),
            DayConfiguration("Грудь", listOf("Грудь", "Трицепсы"), 6),
            DayConfiguration("Кардио", listOf("Квадрицепсы", "Бёдра сзади", "Икры", "Ягодицы"), 0, isCardio = true),
            DayConfiguration("Спина", listOf("Широчайшие", "Трапеции", "Бицепсы", "Предплечья", "Поясница"), 6),
            DayConfiguration("Ноги", listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры"), 6),
            DayConfiguration("Руки", listOf("Бицепсы", "Трицепсы", "Предплечья", "Плечелучевая"), 6),
            DayConfiguration("Плечи", listOf("Плечи", "Трапеции"), 6),
            DayConfiguration("Кардио", listOf("Квадрицепсы", "Бёдра сзади", "Икры", "Ягодицы"), 0, isCardio = true),
            DayConfiguration("Грудь", listOf("Грудь", "Трицепсы"), 6)
        )
        
        val days = mutableListOf<WorkoutDay>()
        
        for ((index, config) in dayConfigurations.withIndex()) {
            val dayExercises = if (config.isCardio) {
                cardioPool.getAvailableExercises().shuffled().take(1).map {
                    it.copy(id = "${index}_${it.id}", reps = cardioDuration, sets = 1)
                }
            } else {
                getUniqueExercisesForDay(strengthPool, config.exerciseCount, config.muscleGroups)
                    .map { it.copy(id = "${index}_${it.id}") }
            }
            
            val muscleGroups = dayExercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()
            
            days.add(
                WorkoutDay(
                    id = index,
                    dayName = "День ${index + 1}: ${config.name}",
                    exercises = dayExercises,
                    muscleGroups = muscleGroups
                )
            )
        }
        
        return WorkoutPlan(
            id = "${profile.goal.lowercase()}_${profile.level.lowercase()}_new",
            name = "${profile.goal}: ${profile.level}",
            description = "План с уникальными упражнениями и кардио-днями (3 силовых + 1 кардио)",
            muscleGroups = listOf("Руки", "Плечи", "Грудь", "Спина", "Ноги", "Кардио"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createWeightLossPlanBySplit(
        profile: com.example.fitness_plan.domain.model.UserProfile,
        level: String
    ): WorkoutPlan {
        val (sets, reps) = when (level) {
            "Новичок" -> Pair(3, "12-15")
            "Любитель" -> Pair(3, "10-12")
            "Профессионал" -> Pair(4, "8-10")
            else -> Pair(3, "10-12")
        }

        val cardioSets = 1
        val cardioReps = "15-20 мин"

        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val legExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", sets, reps, "Базовое упражнение для ног", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Приседания с гантелями", sets, reps, "Приседания с отягощением", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Фронтальные приседания", sets, reps, "Акцент на квадрицепсы", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Гакк-приседания", sets, reps, "В тренажёре гакк-приседаний", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Жим ногами", sets, reps, "Изолированное упражнение для ног", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Разведение ног", sets, reps, "Для квадрицепсов", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Выпады", sets, reps, "Классические выпады", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Выпады назад", sets, reps, "Выпады назад", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Сумо-приседания", sets, reps, "Широкая постановка ног", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Болгарские сплит-приседания", sets, reps, "Приседания на одной ноге", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Ягодичный мостик", sets, reps, "Для ягодиц", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Подъёмы на носки стоя", sets, reps, "Для икроножных", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val chestExercises = listOf(
            createExerciseWithAlternatives("13", "Жим лёжа", sets, reps, "Базовое упражнение для груди", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("14", "Жим гантелей на наклонной скамье", sets, reps, "Для верхней части груди", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("15", "Жим на наклонной скамье", sets, reps, "Штанга на наклонной скамье", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("16", "Жим на наклонной скамье вниз", sets, reps, "Для нижней части груди", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("17", "Жим гантелей лёжа", sets, reps, "Гантели на горизонтальной скамье", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("18", "Разведение гантелей лёжа", sets, reps, "Изолированное упражнение", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("19", "Пек-дек", sets, reps, "На тренажёре пек-дек", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("20", "Отжимания на брусьях", sets, reps, "Для груди и трицепсов", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val backExercises = listOf(
            createExerciseWithAlternatives("21", "Становая тяга", sets, reps, "Базовое упражнение", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("22", "Румынская тяга", sets, reps, "Тяга на прямых ногах", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("23", "Тяга штанги в наклоне", sets, reps, "Для широчайших", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("24", "Тяга гантели одной рукой", sets, reps, "С упором на скамью", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("25", "Т-тяга с гантелью", sets, reps, "Тяга с углом", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("26", "Тяга каната к лицу", sets, reps, "Для задних дельт", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("27", "Пуловер с гантелью", sets, reps, "Для широчайших", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("28", "Гиперэкстензия", sets, reps, "Для поясницы", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("29", "Тяга верхнего блока узким хватом", sets, reps, "Узким хватом", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val shoulderExercises = listOf(
            createExerciseWithAlternatives("30", "Армейский жим", sets, reps, "Базовое упражнение", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("31", "Жим гантелей сидя", sets, reps, "Гантели сидя", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("32", "Жим Арнольда", sets, reps, "С поворотом", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("33", "Жим на плечах в машине", sets, reps, "На тренажёре", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("34", "Разведение гантелей в стороны", sets, reps, "Для средних дельт", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("35", "Обратные разведения", sets, reps, "Для задних дельт", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("36", "Подъём штанги перед собой", sets, reps, "Для передних дельт", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("37", "Махи гантелями перед собой", sets, reps, "Махи перед собой", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val armExercises = listOf(
            createExerciseWithAlternatives("38", "Бицепс со штангой", sets, reps, "Базовое упражнение", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("39", "Молотки", sets, reps, "Нейтральный хват", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("40", "Концентрированные сгибания", sets, reps, "Одной рукой", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("41", "Сгибания рук с гантелями", sets, reps, "Поочерёдно", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("42", "Французский жим", sets, reps, "Для трицепсов", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("43", "Трицепс на блоке", sets, reps, "Разгибания на блоке", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("44", "Кикбэки", sets, reps, "Разгибание в наклоне", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("45", "Разгибания на блоке из-за головы", sets, reps, "Из-за головы", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val cardioExercises = listOf(
            createExerciseWithAlternatives("46", "Бег", cardioSets, cardioReps, "Кардио", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("47", "Велотренажёр", cardioSets, cardioReps, "Кардио", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("48", "Эллипсоид", cardioSets, cardioReps, "Кардио", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("49", "Гребной тренажёр", cardioSets, cardioReps, "Кардио", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("50", "HIIT", cardioSets, cardioReps, "Интервальное кардио", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val dayNames = listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки")
        val exerciseGroups = listOf(legExercises, chestExercises, backExercises, shoulderExercises, armExercises)

        val days = mutableListOf<com.example.fitness_plan.domain.model.WorkoutDay>()

        val allStrengthExercises = legExercises + chestExercises + backExercises + shoulderExercises + armExercises
        val strengthPool = ExercisePool(allStrengthExercises)
        val cardioPoolStart = ExercisePool(cardioExercises)
        val cardioPoolEnd = ExercisePool(cardioExercises)

        val legMuscleGroups = listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
        val chestMuscleGroups = listOf("Грудь", "Трицепсы", "Плечи")
        val backMuscleGroups = listOf("Широчайшие", "Трапеции", "Бицепсы", "Предплечья", "Поясница")
        val shoulderMuscleGroups = listOf("Плечи", "Трицепсы", "Трапеции")
        val armMuscleGroups = listOf("Бицепсы", "Предплечья", "Плечелучевая", "Трицепсы")

        for (dayIndex in 0 until 10) {
            val groupIndex = dayIndex % 5
            val muscleGroupName = dayNames[groupIndex]

            val dayExercises = when (groupIndex) {
                0 -> getUniqueExercisesForDay(strengthPool, 4, legMuscleGroups)
                1 -> getUniqueExercisesForDay(strengthPool, 4, chestMuscleGroups)
                2 -> getUniqueExercisesForDay(strengthPool, 4, backMuscleGroups)
                3 -> getUniqueExercisesForDay(strengthPool, 4, shoulderMuscleGroups)
                else -> getUniqueExercisesForDay(strengthPool, 4, armMuscleGroups)
            }

            val cardioStartExercises = listOfNotNull(cardioExercises.shuffled().firstOrNull())
            val cardioEndExercises = listOfNotNull(cardioExercises.shuffled().firstOrNull())

            val cardioStart = cardioStartExercises.firstOrNull()?.copy(
                id = "${dayIndex}_cardio_start_${cardioStartExercises.firstOrNull()?.id}",
                recommendedWeight = null,
                recommendedRepsPerSet = cardioReps
            )
            val cardioEnd = cardioEndExercises.firstOrNull()?.copy(
                id = "${dayIndex}_cardio_end_${cardioEndExercises.firstOrNull()?.id}",
                recommendedWeight = null,
                recommendedRepsPerSet = cardioReps
            )

            val allDayExercises = listOfNotNull(cardioStart) + dayExercises.map { it.copy(id = "${dayIndex}_${it.id}") } + listOfNotNull(cardioEnd)

            val muscleGroups = allDayExercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                com.example.fitness_plan.domain.model.WorkoutDay(
                    id = dayIndex,
                    dayName = "День ${dayIndex + 1}: $muscleGroupName",
                    exercises = allDayExercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return WorkoutPlan(
            id = "weight_loss_${level.lowercase()}",
            name = "Похудение: $level",
            description = "10-дневный план по сплиту с уникальными упражнениями и кардио",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки", "Кардио"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private data class ExercisePool(
        val allExercises: List<Exercise>,
        val usedExerciseNames: MutableSet<String> = mutableSetOf()
    ) {
        fun getAvailableExercises(): List<Exercise> {
            return allExercises.filter { it.name !in usedExerciseNames }
        }

        fun markAsUsed(exercise: Exercise) {
            usedExerciseNames.add(exercise.name)
        }

        fun markAllAsUsed(exercises: List<Exercise>) {
            exercises.forEach { markAsUsed(it) }
        }

        fun reset() {
            usedExerciseNames.clear()
        }
    }

    private fun getExercisesForDay(
        exerciseList: List<Exercise>,
        startIndex: Int,
        count: Int
    ): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        for (i in 0 until count) {
            val index = (startIndex + i) % exerciseList.size
            exercises.add(exerciseList[index])
        }
        return exercises
    }

    private fun getUniqueExercisesForDay(
        pool: ExercisePool,
        count: Int,
        targetMuscleGroups: List<String>? = null
    ): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        var availableExercises = pool.getAvailableExercises()

        if (targetMuscleGroups != null) {
            val targetGroupExercises = availableExercises.filter { exercise ->
                val muscleGroups = getMuscleGroupsForExercise(exercise.name)
                targetMuscleGroups.any { it in muscleGroups }
            }

            val takeCount = minOf(count, targetGroupExercises.size)
            targetGroupExercises.shuffled().take(takeCount).forEach { exercise ->
                exercises.add(exercise)
                pool.markAsUsed(exercise)
            }

            availableExercises = pool.getAvailableExercises()
        }

        while (exercises.size < count && availableExercises.isNotEmpty()) {
            val exercise = availableExercises.first()
            exercises.add(exercise)
            pool.markAsUsed(exercise)
            availableExercises = pool.getAvailableExercises()
        }

        return exercises
    }

    private suspend fun createWeightLossBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 3, "12-15", "Ноги на ширине плеч, спина прямая", 15.0f, "12,13,14", profile, exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 3, "12-15", "Гриф на уровне груди, локти под 45°", 20.0f, "12,13,14", profile, exerciseLibrary),
            createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 3, "10-12", "Тяни к поясу", 25.0f, "10,11,12", profile, exerciseLibrary),
            createExerciseWithAlternatives("4", "Армейский жим", 3, "12-15", "Жим вверх над головой", 12.0f, "12,13,14", profile, exerciseLibrary),
            createExerciseWithAlternatives("5", "Отжимания", 3, "10-15", "Грудь к полу, корпус прямое", null, "10,12,14", profile, exerciseLibrary),
            createExerciseWithAlternatives("6", "Планка", 3, "30-45 сек", "На предплечьях, тело прямое", null, null, profile, exerciseLibrary),
            createExerciseWithAlternatives("7", "Скручивания", 3, "15-20", "Лёжа, поднимай плечи", null, null, profile, exerciseLibrary),
            createExerciseWithAlternatives("8", "Бег", 1, "15 мин", "Умеренный темп", null, null, profile, exerciseLibrary),
            createExerciseWithAlternatives("9", "Велотренажёр", 1, "15 мин", "Умеренный темп", null, null, profile, exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 2, 7),
                    listOf(3, 4, 6, 8),
                    listOf(0, 1, 2, 7),
                    listOf(3, 4, 6, 8),
                    listOf(0, 1, 2, 7),
                    listOf(3, 4, 6, 8),
                    listOf(0, 1, 2, 7),
                    listOf(3, 4, 6, 8),
                    listOf(0, 1, 2, 5, 7),
                    listOf(3, 4, 6, 8)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "weight_loss_beginner",
            name = "Похудение: Новичок",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Core", "Кардио"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createWeightLossIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Отжимания", 4, "12-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Трицепс на блоке", 3, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("13", "Скручивания", 3, "15-20", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("14", "Планка", 3, "45-60 сек", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("15", "Бег", 1, "20 мин", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 3, 10, 12),
                    listOf(2, 5, 6, 11, 13),
                    listOf(4, 7, 8, 14),
                    listOf(0, 1, 3, 10, 12),
                    listOf(2, 5, 6, 11, 13),
                    listOf(4, 7, 8, 14),
                    listOf(0, 1, 3, 10, 12),
                    listOf(2, 5, 6, 11, 13),
                    listOf(4, 7, 8, 14),
                    listOf(0, 1, 3, 10, 12, 15)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "weight_loss_intermediate",
            name = "Похудение: Любитель",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createMuscleGainBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Отжимания", 4, "10-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Скручивания", 3, "12-15", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 12),
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 12),
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 12),
                    listOf(0, 1, 3, 11)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "muscle_gain_beginner",
            name = "Масса: Новичок",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createMuscleGainIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 5, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 5, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Становая тяга", 5, "5-6", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 5, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Армейский жим", 5, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 5, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Подтягивания", 5, "макс", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Отжимания", 5, "12-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 5, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Жим на наклонной скамье", 5, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Бицепс со штангой", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("13", "Трицепс на блоке", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("14", "Скручивания", 4, "10-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("15", "Планка", 4, "45-60 сек", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 3, 11, 12, 14),
                    listOf(2, 5, 6, 13, 14),
                    listOf(4, 7, 8, 10, 14),
                    listOf(0, 1, 3, 11, 12, 14),
                    listOf(2, 5, 6, 13, 14),
                    listOf(4, 7, 8, 10, 14),
                    listOf(0, 1, 3, 11, 12, 14),
                    listOf(2, 5, 6, 13, 14),
                    listOf(4, 7, 8, 10, 14),
                    listOf(0, 1, 3, 11, 12, 14)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "muscle_gain_intermediate",
            name = "Масса: Любитель",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createMuscleGainAdvancedPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 6, "4-6", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 6, "4-6", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Становая тяга", 6, "3-5", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 6, "4-6", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Армейский жим", 6, "4-6", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 6, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Подтягивания", 6, "макс", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Отжимания", 6, "12-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Выпады", 5, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 6, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Жим на наклонной скамье", 6, "6-8", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Бицепс со штангой", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("13", "Трицепс на блоке", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("14", "Скручивания", 4, "10-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("15", "Планка", 4, "60 сек", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 3, 11, 12, 13, 14),
                    listOf(2, 5, 6, 12, 13, 14),
                    listOf(4, 7, 8, 10, 15),
                    listOf(0, 1, 3, 11, 12, 13, 14),
                    listOf(2, 5, 6, 12, 13, 14),
                    listOf(4, 7, 8, 10, 15),
                    listOf(0, 1, 3, 11, 12, 13, 14),
                    listOf(2, 5, 6, 12, 13, 14),
                    listOf(4, 7, 8, 10, 15),
                    listOf(0, 1, 3, 11, 12, 13, 14)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "muscle_gain_advanced",
            name = "Масса: Профессионал",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createMaintenancePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()

        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "8-10", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("8", "Отжимания", 4, "15-20", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("9", "Выпады", 4, "12-15", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("12", "Скручивания", 3, "15-20", profile = profile, exerciseLibrary = exerciseLibrary),
            createExerciseWithAlternatives("13", "Планка", 3, "45-60 сек", profile = profile, exerciseLibrary = exerciseLibrary)
        )

        val daysCount = 10

        val days = when (frequency) {
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount, exerciseLibrary, profile)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount, exerciseLibrary, profile)
            else -> createDaysWithUniqueExercises(
                listOf(
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 13),
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 13),
                    listOf(0, 1, 3, 11),
                    listOf(2, 5, 6, 12),
                    listOf(4, 7, 8, 13),
                    listOf(0, 1, 3, 11)
                ),
                baseExercises,
                exerciseLibrary,
                daysCount,
                profile
            )
        }

        return WorkoutPlan(
            id = "maintenance",
            name = "Поддержание формы",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = days,
            planType = com.example.fitness_plan.domain.model.PlanType.AUTO
        )
    }

    private suspend fun createFullBodyDays(
        baseExercises: List<Exercise>,
        totalCount: Int,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>? = null,
        profile: com.example.fitness_plan.domain.model.UserProfile? = null
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()

        val exercisesCount = Math.max(minOf(baseExercises.size, 8), 5)
        val pool = ExercisePool(baseExercises)

        val libraryExercises = if (exerciseLibrary != null && profile != null) {
            exerciseLibrary.map { lib ->
                runBlocking {
                    createExerciseWithAlternatives(
                        id = lib.id,
                        name = lib.name,
                        sets = 3,
                        reps = "10-12",
                        profile = profile,
                        exerciseLibrary = exerciseLibrary
                    )
                }
            }
        } else null

        val libraryPool = libraryExercises?.let { ExercisePool(it) }

        for (i in 0 until totalCount) {
            val pool = ExercisePool(baseExercises)
            var dayExercises = getUniqueExercisesForDay(pool, exercisesCount, null)

            if (dayExercises.size < exercisesCount && libraryPool != null) {
                val additionalExercises = getUniqueExercisesForDay(libraryPool, exercisesCount - dayExercises.size, null)
                dayExercises = dayExercises + additionalExercises
            }

            val muscleGroups = dayExercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День ${i + 1} (Full Body)",
                    exercises = dayExercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return days
    }

    private suspend fun createSplit3xDays(
        baseExercises: List<Exercise>,
        totalCount: Int,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>? = null,
        profile: com.example.fitness_plan.domain.model.UserProfile? = null
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()

        val pool = ExercisePool(baseExercises)

        val libraryExercises = if (exerciseLibrary != null && profile != null) {
            exerciseLibrary.map { lib ->
                runBlocking {
                    createExerciseWithAlternatives(
                        id = lib.id,
                        name = lib.name,
                        sets = 3,
                        reps = "10-12",
                        profile = profile,
                        exerciseLibrary = exerciseLibrary
                    )
                }
            }
        } else null

        val libraryPool = libraryExercises?.let { ExercisePool(it) }

        val legMuscleGroups = listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
        val upperMuscleGroups = listOf("Грудь", "Спина", "Плечи", "Трицепсы", "Бицепсы")

        for (i in 0 until totalCount) {
            val pool = ExercisePool(baseExercises)

            val cycleIndex = i % 3
            val (targetMuscleGroups, dayName) = when (cycleIndex) {
                0 -> Pair(legMuscleGroups, "Ноги")
                1 -> Pair(upperMuscleGroups, "Верх тела")
                else -> Pair(null, "Полный")
            }

            var exercises = getUniqueExercisesForDay(pool, 3, targetMuscleGroups)

            if (exercises.size < 3 && libraryPool != null) {
                val additionalExercises = getUniqueExercisesForDay(libraryPool, 3 - exercises.size, targetMuscleGroups)
                exercises = exercises + additionalExercises
            }

            val muscleGroups = exercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День ${i + 1} ($dayName)",
                    exercises = exercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return days
    }

    private suspend fun createSplit5xDays(
        baseExercises: List<Exercise>,
        totalCount: Int,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>? = null,
        profile: com.example.fitness_plan.domain.model.UserProfile? = null
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()
        val dayNames = listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки")

        val pool = ExercisePool(baseExercises)

        val libraryExercises = if (exerciseLibrary != null && profile != null) {
            exerciseLibrary.map { lib ->
                runBlocking {
                    createExerciseWithAlternatives(
                        id = lib.id,
                        name = lib.name,
                        sets = 3,
                        reps = "10-12",
                        profile = profile,
                        exerciseLibrary = exerciseLibrary
                    )
                }
            }
        } else null

        val libraryPool = libraryExercises?.let { ExercisePool(it) }

        val legMuscleGroups = listOf("Квадрицепсы", "Ягодицы", "Бёдра сзади", "Икры")
        val chestMuscleGroups = listOf("Грудь", "Трицепсы", "Плечи")
        val backMuscleGroups = listOf("Широчайшие", "Трапеции", "Бицепсы", "Предплечья", "Поясница")
        val shoulderMuscleGroups = listOf("Плечи", "Трицепсы", "Трапеции")
        val armMuscleGroups = listOf("Бицепсы", "Предплечья", "Плечелучевая", "Трицепсы")

        val muscleGroupsList = listOf(legMuscleGroups, chestMuscleGroups, backMuscleGroups, shoulderMuscleGroups, armMuscleGroups)

        for (i in 0 until totalCount) {
            val pool = ExercisePool(baseExercises)

            val cycleIndex = i % 5
            val targetMuscleGroups = muscleGroupsList[cycleIndex]
            val dayName = dayNames[cycleIndex]

            var exercises = getUniqueExercisesForDay(pool, 3, targetMuscleGroups)

            if (exercises.size < 3 && libraryPool != null) {
                val additionalExercises = getUniqueExercisesForDay(libraryPool, 3 - exercises.size, targetMuscleGroups)
                exercises = exercises + additionalExercises
            }

            val muscleGroups = exercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День ${i + 1} ($dayName)",
                    exercises = exercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return days
    }
 
    private suspend fun createDaysWithUniqueExercises(
        predefinedWorkouts: List<List<Int>>,
        baseExercises: List<Exercise>,
        exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
        totalCount: Int,
        profile: com.example.fitness_plan.domain.model.UserProfile
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()
        val pool = ExercisePool(baseExercises)
        val libraryPool = ExercisePool(exerciseLibrary.map { lib ->
            runBlocking {
                createExerciseWithAlternatives(
                    id = lib.id,
                    name = lib.name,
                    sets = 3,
                    reps = "10-12",
                    profile = profile,
                    exerciseLibrary = exerciseLibrary
                )
            }
        })

        for (i in 0 until minOf(totalCount, predefinedWorkouts.size)) {
            val exerciseIndices = predefinedWorkouts[i]
            val exercises = mutableListOf<Exercise>()

            for (idx in exerciseIndices) {
                if (idx < baseExercises.size) {
                    val exercise = baseExercises[idx]
                    if (exercise.name !in pool.usedExerciseNames) {
                        exercises.add(exercise.copy(id = "${i}_${exercise.id}"))
                        pool.markAsUsed(exercise)
                    } else {
                        val alternative = getUniqueExercisesForDay(libraryPool, 1, null).firstOrNull()
                        if (alternative != null) {
                            exercises.add(alternative.copy(id = "${i}_${alternative.id}"))
                        }
                    }
                }
            }

            val muscleGroups = exercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День ${i + 1}",
                    exercises = exercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return days
    }
}
