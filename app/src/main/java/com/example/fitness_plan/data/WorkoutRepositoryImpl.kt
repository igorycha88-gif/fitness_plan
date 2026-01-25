package com.example.fitness_plan.data

import android.util.Log
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.calculator.WeightCalculator
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
    private val weightCalculator: WeightCalculator
) : WorkoutRepository {

    private val gson = Gson()

    override suspend fun getWorkoutPlanForUser(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        Log.d(TAG, "getWorkoutPlanForUser: goal=${profile.goal}, level=${profile.level}, frequency=${profile.frequency}")
        val plan = when {
            profile.goal == "Похудение" && profile.level == "Новичок" -> {
                Log.d(TAG, "Creating weight loss beginner plan")
                createWeightLossBeginnerPlan(profile, profile.frequency)
            }
            profile.goal == "Похудение" && profile.level == "Любитель" -> {
                Log.d(TAG, "Creating weight loss intermediate plan")
                createWeightLossIntermediatePlan(profile, profile.frequency)
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
        Log.d(TAG, "Plan created: ${plan.name}, days=${plan.days.size}")
        return plan
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
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val totalCount = 10

        when (frequency) {
            "1 раз в неделю" -> {
                for (i in 0 until totalCount) {
                    dates.add(calendar.timeInMillis)
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            "3 раза в неделю" -> {
                for (i in 0 until totalCount) {
                    dates.add(calendar.timeInMillis)
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    when (dayOfWeek) {
                        Calendar.MONDAY -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                        Calendar.WEDNESDAY -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                        Calendar.FRIDAY -> calendar.add(Calendar.DAY_OF_YEAR, 3)
                        else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }
            "5 раз в неделю" -> {
                for (i in 0 until totalCount) {
                    dates.add(calendar.timeInMillis)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        calendar.add(Calendar.DAY_OF_YEAR, 2)
                    }
                }
            }
            else -> {
                for (i in 0 until totalCount) {
                    dates.add(calendar.timeInMillis)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return dates
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

    private fun getAlternatives(exerciseName: String): List<Exercise> {
        return when (exerciseName) {
            "Приседания" -> listOf(
                Exercise(id = "alt1_1", name = "Приседания с гантелями", sets = 3, reps = "12-15"),
                Exercise(id = "alt1_2", name = "Гакк-приседания", sets = 3, reps = "10-12")
            )
            "Приседания со штангой" -> listOf(
                Exercise(id = "alt2_1", name = "Приседания в Смите", sets = 4, reps = "8-10", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt2_2", name = "Фронтальные приседания", sets = 4, reps = "8-10", isCompleted = false, alternatives = emptyList())
            )
            "Жим лёжа" -> listOf(
                Exercise(id = "alt3_1", name = "Жим на наклонной скамье", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt3_2", name = "Жим гантелей лёжа", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList())
            )
            "Жим гантелей сидя" -> listOf(
                Exercise(id = "alt4_1", name = "Жим Арнольда", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt4_2", name = "Подъём гантелей через стороны", sets = 4, reps = "12-15", isCompleted = false, alternatives = emptyList())
            )
            "Тяга штанги в наклоне" -> listOf(
                Exercise(id = "alt5_1", name = "Тяга гантели одной рукой", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt5_2", name = "Тяга верхнего блока", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList())
            )
            "Тяга верхнего блока" -> listOf(
                Exercise(id = "alt6_1", name = "Тяга штанги в наклоне", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt6_2", name = "Подтягивания", sets = 4, reps = "макс", isCompleted = false, alternatives = emptyList())
            )
            "Становая тяга" -> listOf(
                Exercise(id = "alt7_1", name = "Румынская тяга", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt7_2", name = "Тяга с плинтов", sets = 3, reps = "6-8", isCompleted = false, alternatives = emptyList())
            )
            "Подтягивания" -> listOf(
                Exercise(id = "alt8_1", name = "Тяга верхнего блока", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt8_2", name = "Подтягивания с assistance", sets = 3, reps = "макс", isCompleted = false, alternatives = emptyList())
            )
            "Отжимания" -> listOf(
                Exercise(id = "alt9_1", name = "Отжимания на брусьях", sets = 3, reps = "10-15", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt9_2", name = "Жим лёжа", sets = 4, reps = "10-12", isCompleted = false, alternatives = emptyList())
            )
            "Выпады" -> listOf(
                Exercise(id = "alt10_1", name = "Выпады с гантелями", sets = 3, reps = "12-15", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt10_2", name = "Сjump squat", sets = 3, reps = "15-20", isCompleted = false, alternatives = emptyList())
            )
            "Бицепс" -> listOf(
                Exercise(id = "alt11_1", name = "Подъём штанги на бицепс", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt11_2", name = "Молотки", sets = 3, reps = "12-15", isCompleted = false, alternatives = emptyList())
            )
            "Трицепс" -> listOf(
                Exercise(id = "alt12_1", name = "Французский жим", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
                Exercise(id = "alt12_2", name = "Отжимания на брусьях", sets = 3, reps = "10-15", isCompleted = false, alternatives = emptyList())
            )
            else -> emptyList()
        }
    }

    private fun getMuscleGroupsForExercise(name: String): List<String> {
        return when (name) {
            "Приседания", "Приседания со штангой" -> listOf("Ноги", "Грудь", "Спина", "Пресс")
            "Жим лёжа", "Жим штанги лёжа", "Жим гантелей на наклонной скамье", "Жим в хаммере", "Разведение гантелей лёжа", "Пуловер с гантелью", "Кроссовер" -> listOf("Ноги", "Грудь", "Спина", "Пресс")
            "Становая тяга", "Румынская тяга" -> listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")
            "Тяга штанги в наклоне", "Тяга гантели одной рукой", "Т-тяга с гантелью", "Тяга каната" -> listOf("Ноги", "Грудь", "Спина", "Пресс")
            "Армейский жим", "Армейский жим с гантелями", "Жим гантелей сидя", "Жим плечами в машине", "Разведение гантелей в стороны", "Обратные разведения" -> listOf("Спина", "Плечи", "Руки", "Пресс")
            "Подтягивания", "Подтягивания с резинкой", "Тяга верхнего блока" -> listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")
            "Отжимания" -> listOf("Ноги", "Грудь", "Спина", "Пресс")
            "Выпады", "Выпады с гантелями", "Болгарские сплит-приседания", "Сумо-приседания", "Фронтальные приседания", "Шагающий выпад с поворотом", "Жим ногами в машине", "Подъёмы на носки", "Икры стоя", "Осёл-качки", "Ягодичный мостик" -> listOf("Ноги", "Кардио")
            "Бицепс", "Бицепс со штангой", "Сгибания рук с гантелями", "Концентрированные сгибания" -> listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")
            "Трицепс", "Трицепс на блоке", "Разгибания рук на блоке", "Кикбэки", "Французский жим" -> listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")
            "Пресс", "Скручивания", "Велосипед" -> listOf("Ноги", "Грудь", "Спина", "Пресс")
            "Планка" -> listOf("Плечи", "Руки", "Core", "Кардио")
            "Бег", "Беговая дорожка", "Интервальный бег" -> listOf("Ноги", "Кардио")
            "Велотренажёр", "Велотренажёр с сопротивлением", "Кардио: Комбо" -> listOf("Грудь", "Спина", "Кардио")
            "Кардио: Эллипс", "HIIT", "Кардио: Гребной тренажёр" -> listOf("Ноги", "Кардио")
            else -> emptyList()
        }
    }

    private fun createExerciseWithAlternatives(
        id: String,
        name: String,
        sets: Int,
        reps: String,
        description: String? = null,
        recommendedWeight: Float? = null,
        recommendedRepsPerSet: String? = null,
        profile: com.example.fitness_plan.domain.model.UserProfile? = null
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
        
        return Exercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList(),
            description = description,
            recommendedWeight = finalRecommendedWeight,
            recommendedRepsPerSet = finalRecommendedReps
        )
    }

    private fun createWeightLossBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 3, "12-15", "Ноги на ширине плеч, спина прямая", 15.0f, "12,13,14"),
            createExerciseWithAlternatives("2", "Жим лёжа", 3, "12-15", "Гриф на уровне груди, локти под 45°", 20.0f, "12,13,14"),
            createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 3, "10-12", "Тяни к поясу", 25.0f, "10,11,12"),
            createExerciseWithAlternatives("4", "Армейский жим", 3, "12-15", "Жим вверх над головой", 12.0f, "12,13,14"),
            createExerciseWithAlternatives("5", "Отжимания", 3, "10-15", "Грудь к полу, корпус прямое", null, "10,12,14"),
            createExerciseWithAlternatives("6", "Планка", 3, "30-45 сек", "На предплечьях, тело прямое", null, null),
            createExerciseWithAlternatives("7", "Скручивания", 3, "15-20", "Лёжа, поднимай плечи", null, null),
            createExerciseWithAlternatives("8", "Бег", 1, "15 мин", "Умеренный темп", null, null),
            createExerciseWithAlternatives("9", "Велотренажёр", 1, "15 мин", "Умеренный темп", null, null)
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

    private fun createWeightLossIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "6-8", profile = profile),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile),
            createExerciseWithAlternatives("8", "Отжимания", 4, "12-15", profile = profile),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile),
            createExerciseWithAlternatives("12", "Трицепс на блоке", 3, "10-12", profile = profile),
            createExerciseWithAlternatives("13", "Скручивания", 3, "15-20", profile = profile),
            createExerciseWithAlternatives("14", "Планка", 3, "45-60 сек", profile = profile),
            createExerciseWithAlternatives("15", "Бег", 1, "20 мин", profile = profile)
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

    private fun createMuscleGainBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "6-8", profile = profile),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile),
            createExerciseWithAlternatives("8", "Отжимания", 4, "10-15", profile = profile),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile),
            createExerciseWithAlternatives("12", "Скручивания", 3, "12-15", profile = profile)
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

    private fun createMuscleGainIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 5, "6-8", profile = profile),
            createExerciseWithAlternatives("2", "Жим лёжа", 5, "6-8", profile = profile),
            createExerciseWithAlternatives("3", "Становая тяга", 5, "5-6", profile = profile),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 5, "6-8", profile = profile),
            createExerciseWithAlternatives("5", "Армейский жим", 5, "6-8", profile = profile),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 5, "8-10", profile = profile),
            createExerciseWithAlternatives("7", "Подтягивания", 5, "макс", profile = profile),
            createExerciseWithAlternatives("8", "Отжимания", 5, "12-15", profile = profile),
            createExerciseWithAlternatives("9", "Выпады", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 5, "8-10", profile = profile),
            createExerciseWithAlternatives("11", "Жим на наклонной скамье", 5, "8-10", profile = profile),
            createExerciseWithAlternatives("12", "Бицепс со штангой", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("13", "Трицепс на блоке", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("14", "Скручивания", 4, "10-15", profile = profile),
            createExerciseWithAlternatives("15", "Планка", 4, "45-60 сек", profile = profile)
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

    private fun createMuscleGainAdvancedPlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 6, "4-6", profile = profile),
            createExerciseWithAlternatives("2", "Жим лёжа", 6, "4-6", profile = profile),
            createExerciseWithAlternatives("3", "Становая тяга", 6, "3-5", profile = profile),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 6, "4-6", profile = profile),
            createExerciseWithAlternatives("5", "Армейский жим", 6, "4-6", profile = profile),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 6, "6-8", profile = profile),
            createExerciseWithAlternatives("7", "Подтягивания", 6, "макс", profile = profile),
            createExerciseWithAlternatives("8", "Отжимания", 6, "12-15", profile = profile),
            createExerciseWithAlternatives("9", "Выпады", 5, "8-10", profile = profile),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 6, "8-10", profile = profile),
            createExerciseWithAlternatives("11", "Жим на наклонной скамье", 6, "6-8", profile = profile),
            createExerciseWithAlternatives("12", "Бицепс со штангой", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("13", "Трицепс на блоке", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("14", "Скручивания", 4, "10-15", profile = profile),
            createExerciseWithAlternatives("15", "Планка", 4, "60 сек", profile = profile)
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

    private fun createMaintenancePlan(profile: com.example.fitness_plan.domain.model.UserProfile, frequency: String): WorkoutPlan {
        val baseExercises = listOf(
            createExerciseWithAlternatives("1", "Приседания", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("2", "Жим лёжа", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("3", "Становая тяга", 4, "8-10", profile = profile),
            createExerciseWithAlternatives("4", "Тяга штанги в наклоне", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("5", "Армейский жим", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("7", "Подтягивания", 4, "макс", profile = profile),
            createExerciseWithAlternatives("8", "Отжимания", 4, "15-20", profile = profile),
            createExerciseWithAlternatives("9", "Выпады", 4, "12-15", profile = profile),
            createExerciseWithAlternatives("10", "Тяга верхнего блока", 4, "10-12", profile = profile),
            createExerciseWithAlternatives("11", "Бицепс со штангой", 3, "10-12", profile = profile),
            createExerciseWithAlternatives("12", "Скручивания", 3, "15-20", profile = profile),
            createExerciseWithAlternatives("13", "Планка", 3, "45-60 сек", profile = profile)
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
