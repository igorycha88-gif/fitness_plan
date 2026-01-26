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
        val plan = when {
            profile.goal == "Похудение" -> {
                Log.d(TAG, "Creating weight loss ${profile.level} plan with split")
                createWeightLossPlanBySplit(profile, profile.level)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Новичок" -> {
                Log.d(TAG, "Creating muscle gain beginner plan")
                createMuscleGainBeginnerPlan(profile, profile.frequency)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Любитель" -> {
                Log.d(TAG, "Creating muscle gain intermediate plan")
                createMuscleGainIntermediatePlan(profile, profile.frequency)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Профессионал" -> {
                Log.d(TAG, "Creating muscle gain advanced plan")
                createMuscleGainAdvancedPlan(profile, profile.frequency)
            }
            else -> {
                Log.d(TAG, "Creating maintenance plan")
                createMaintenancePlan(profile, profile.frequency)
            }
        }
        
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
        val json = gson.toJson(plan)
        context.workoutDataStore.edit { preferences ->
            preferences[stringPreferencesKey("${username}_workout_plan")] = json
        }
    }

    override fun getWorkoutPlan(username: String): Flow<WorkoutPlan?> {
        val key = stringPreferencesKey("${username}_workout_plan")
        return context.workoutDataStore.data.map { preferences ->
            val json = preferences[key]
            if (json != null) {
                try {
                    gson.fromJson(json, WorkoutPlan::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
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
                    gson.fromJson(json, WorkoutPlan::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
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
                animationUrl = libExercise.animationUrl
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
            else -> emptyList()
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

        return Exercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList(),
            description = libExercise?.description ?: description,
            recommendedWeight = finalRecommendedWeight,
            recommendedRepsPerSet = finalRecommendedReps,
            muscleGroups = libExercise?.muscleGroups ?: emptyList(),
            equipment = libExercise?.equipment ?: emptyList(),
            exerciseType = libExercise?.exerciseType ?: com.example.fitness_plan.domain.model.ExerciseType.STRENGTH,
            stepByStepInstructions = libExercise?.stepByStepInstructions,
            animationUrl = libExercise?.animationUrl
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
        var legDayIndex = 0
        var chestDayIndex = 0
        var backDayIndex = 0
        var shoulderDayIndex = 0
        var armDayIndex = 0
        var cardioIndex = 0

        for (dayIndex in 0 until 10) {
            val groupIndex = dayIndex % 5
            val muscleGroupName = dayNames[groupIndex]
            val dayExercises = when (groupIndex) {
                0 -> getExercisesForDay(legExercises, legDayIndex, 4)
                1 -> getExercisesForDay(chestExercises, chestDayIndex, 4)
                2 -> getExercisesForDay(backExercises, backDayIndex, 4)
                3 -> getExercisesForDay(shoulderExercises, shoulderDayIndex, 4)
                else -> getExercisesForDay(armExercises, armDayIndex, 4)
            }

            when (groupIndex) {
                0 -> legDayIndex += 4
                1 -> chestDayIndex += 4
                2 -> backDayIndex += 4
                3 -> shoulderDayIndex += 4
                else -> armDayIndex += 4
            }

            val cardioStart = cardioExercises[cardioIndex].copy(
                id = "${dayIndex}_cardio_start_${cardioExercises[cardioIndex].id}",
                recommendedWeight = null,
                recommendedRepsPerSet = cardioReps
            )
            val cardioEnd = cardioExercises[(cardioIndex + 1) % cardioExercises.size].copy(
                id = "${dayIndex}_cardio_end_${cardioExercises[(cardioIndex + 1) % cardioExercises.size].id}",
                recommendedWeight = null,
                recommendedRepsPerSet = cardioReps
            )
            cardioIndex = (cardioIndex + 2) % cardioExercises.size

            val allDayExercises = listOf(cardioStart) + dayExercises.map { it.copy(id = "${dayIndex}_${it.id}") } + listOf(cardioEnd)

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
            days = days
        )
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "weight_loss_beginner",
            name = "Похудение: Новичок",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Core", "Кардио"),
            goal = profile.goal,
            level = profile.level,
            days = days
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "weight_loss_intermediate",
            name = "Похудение: Любитель",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "muscle_gain_beginner",
            name = "Масса: Новичок",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = days
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "muscle_gain_intermediate",
            name = "Масса: Любитель",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "muscle_gain_advanced",
            name = "Масса: Профессионал",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = days
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
            "1 раз в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "3 раза в неделю" -> createFullBodyDays(baseExercises, daysCount)
            "5 раз в неделю" -> createSplit5xDays(baseExercises, daysCount)
            else -> {
                val workouts = listOf(
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
                )
                workouts.mapIndexed { index, exerciseIndices ->
                    val exercises = exerciseIndices.map { i -> baseExercises[i].copy(id = "${index}_${baseExercises[i].id}") }
                    val muscleGroups = exercises.flatMap { exercise -> getMuscleGroupsForExercise(exercise.name) }.distinct()
                    WorkoutDay(
                        id = index,
                        dayName = "День ${index + 1}",
                        exercises = exercises,
                        muscleGroups = muscleGroups
                    )
                }
            }
        }

        return WorkoutPlan(
            id = "maintenance",
            name = "Поддержание формы",
            description = "Программа с адаптацией под частоту: $frequency",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = days
        )
    }

    private fun createFullBodyDays(
        baseExercises: List<Exercise>,
        totalCount: Int
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()

        val exercisesCount = Math.max(minOf(baseExercises.size, 8), 5)
        val exerciseIndices = (0 until exercisesCount).toList()

        for (i in 0 until totalCount) {
            val exercises = exerciseIndices.map { idx ->
                baseExercises[idx].copy(id = "${i}_${baseExercises[idx].id}")
            }

            val muscleGroups = exercises.flatMap { getMuscleGroupsForExercise(it.name) }.distinct()

            days.add(
                WorkoutDay(
                    id = i,
                    dayName = "День ${i + 1} (Full Body)",
                    exercises = exercises,
                    muscleGroups = muscleGroups
                )
            )
        }

        return days
    }

    private fun createSplit3xDays(
        baseExercises: List<Exercise>,
        totalCount: Int
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()

        val legsExercises = if (baseExercises.size > 0) listOf(0) else emptyList()
        val upperExercises = if (baseExercises.size > 1) listOf(1) else emptyList()
        val fullBodyExercises = if (baseExercises.size > 2) listOf(0, 1, 2).filter { it < baseExercises.size } else emptyList()

        for (i in 0 until totalCount) {
            val cycleIndex = i % 3
            val (exerciseIndices, dayName) = when (cycleIndex) {
                0 -> Pair(legsExercises, "Ноги")
                1 -> Pair(upperExercises, "Верх тела")
                else -> Pair(fullBodyExercises, "Полный")
            }

            val exercises = exerciseIndices.filter { it < baseExercises.size }.map { idx ->
                baseExercises[idx].copy(id = "${i}_${baseExercises[idx].id}")
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

    private fun createSplit5xDays(
        baseExercises: List<Exercise>,
        totalCount: Int
    ): List<WorkoutDay> {
        val days = mutableListOf<WorkoutDay>()
        val dayNames = listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки")

        val exercisesPerDay = Math.ceil(baseExercises.size.toDouble() / 5).toInt().coerceAtLeast(1)

        val splitCycles = mutableListOf<Pair<List<Int>, String>>()

        for (dayIndex in 0 until 5) {
            val startIndex = dayIndex * exercisesPerDay
            val endIndex = Math.min(startIndex + exercisesPerDay, baseExercises.size)
            val exerciseIndices = (startIndex until endIndex).toList()
            splitCycles.add(Pair(exerciseIndices, dayNames[dayIndex]))
        }

        for (i in 0 until totalCount) {
            val cycleIndex = i % 5
            val (exerciseIndices, dayName) = splitCycles[cycleIndex]

            val exercises = exerciseIndices.filter { it < baseExercises.size }.map { idx ->
                baseExercises[idx].copy(id = "${i}_${baseExercises[idx].id}")
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
}
