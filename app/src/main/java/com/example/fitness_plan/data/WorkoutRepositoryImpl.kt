package com.example.fitness_plan.data

import android.util.Log
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
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
    private val workoutScheduleRepository: WorkoutScheduleRepository
) : WorkoutRepository {

    private val gson = Gson()

    override suspend fun getWorkoutPlanForUser(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        Log.d(TAG, "getWorkoutPlanForUser: goal=${profile.goal}, level=${profile.level}")
        val plan = when {
            profile.goal == "Похудение" && profile.level == "Новичок" -> {
                Log.d(TAG, "Creating weight loss beginner plan")
                createWeightLossBeginnerPlan(profile)
            }
            profile.goal == "Похудение" && profile.level == "Любитель" -> {
                Log.d(TAG, "Creating weight loss intermediate plan")
                createWeightLossIntermediatePlan(profile)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Новичок" -> {
                Log.d(TAG, "Creating muscle gain beginner plan")
                createMuscleGainBeginnerPlan(profile)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Любитель" -> {
                Log.d(TAG, "Creating muscle gain intermediate plan")
                createMuscleGainIntermediatePlan(profile)
            }
            profile.goal == "Наращивание мышечной массы" && profile.level == "Профессионал" -> {
                Log.d(TAG, "Creating muscle gain advanced plan")
                createMuscleGainAdvancedPlan(profile)
            }
            else -> {
                Log.d(TAG, "Creating maintenance plan")
                createMaintenancePlan(profile)
            }
        }
        Log.d(TAG, "Plan created: ${plan.name}, days=${plan.days.size}")
        return plan
    }

    override suspend fun get30DayWorkoutPlan(basePlan: WorkoutPlan): WorkoutPlan {
        val days = mutableListOf<WorkoutDay>()
        val exerciseNames = basePlan.days.flatMap { day -> day.exercises.map { it.name } }.distinct()
        var dayCounter = 1

        for (i in 0 until 30) {
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

        return basePlan.copy(
            name = "30-дневный план",
            days = days
        )
    }

    override suspend fun generate30DayDates(startDate: Long, frequency: String): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (frequency) {
            "1 раз в неделю" -> {
                for (i in 0 until 30) {
                    dates.add(calendar.timeInMillis)
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            "3 раза в неделю" -> {
                for (i in 0 until 30) {
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
                for (i in 0 until 30) {
                    dates.add(calendar.timeInMillis)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        calendar.add(Calendar.DAY_OF_YEAR, 2)
                    }
                }
            }
            else -> {
                for (i in 0 until 30) {
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

    override suspend fun saveWorkoutSchedule(username: String, dates: List<Long>) {
        workoutScheduleRepository.saveWorkoutSchedule(username, dates)
    }

    private fun getAlternatives(exerciseName: String): List<Exercise> {
        return when (exerciseName) {
            "Приседания" -> listOf(
                Exercise("alt1_1", "Приседания с гантелями", 3, "12-15"),
                Exercise("alt1_2", "Гакк-приседания", 3, "10-12")
            )
            "Приседания со штангой" -> listOf(
                Exercise("alt2_1", "Приседания в Смите", 4, "8-10"),
                Exercise("alt2_2", "Фронтальные приседания", 4, "8-10")
            )
            "Жим лёжа" -> listOf(
                Exercise("alt3_1", "Жим на наклонной скамье", 4, "10-12"),
                Exercise("alt3_2", "Жим гантелей лёжа", 4, "10-12")
            )
            "Жим гантелей сидя" -> listOf(
                Exercise("alt4_1", "Жим Арнольда", 4, "10-12"),
                Exercise("alt4_2", "Подъём гантелей через стороны", 4, "12-15")
            )
            "Тяга штанги в наклоне" -> listOf(
                Exercise("alt5_1", "Тяга гантели одной рукой", 4, "10-12"),
                Exercise("alt5_2", "Тяга верхнего блока", 4, "10-12")
            )
            "Тяга верхнего блока" -> listOf(
                Exercise("alt6_1", "Тяга штанги в наклоне", 4, "10-12"),
                Exercise("alt6_2", "Подтягивания", 4, "макс")
            )
            "Становая тяга" -> listOf(
                Exercise("alt7_1", "Румынская тяга", 4, "10-12"),
                Exercise("alt7_2", "Тяга с плинтов", 3, "6-8")
            )
            "Подтягивания" -> listOf(
                Exercise("alt8_1", "Тяга верхнего блока", 4, "10-12"),
                Exercise("alt8_2", "Подтягивания с assistance", 3, "макс")
            )
            "Отжимания" -> listOf(
                Exercise("alt9_1", "Отжимания на брусьях", 3, "10-15"),
                Exercise("alt9_2", "Жим лёжа", 4, "10-12")
            )
            "Выпады" -> listOf(
                Exercise("alt10_1", "Выпады с гантелями", 3, "12-15"),
                Exercise("alt10_2", "Сjump squat", 3, "15-20")
            )
            "Бицепс" -> listOf(
                Exercise("alt11_1", "Подъём штанги на бицепс", 3, "10-12"),
                Exercise("alt11_2", "Молотки", 3, "12-15")
            )
            "Трицепс" -> listOf(
                Exercise("alt12_1", "Французский жим", 3, "10-12"),
                Exercise("alt12_2", "Отжимания на брусьях", 3, "10-15")
            )
            else -> emptyList()
        }
    }

    private fun createExerciseWithAlternatives(id: String, name: String, sets: Int, reps: String): Exercise {
        return Exercise(id, name, sets, reps, null, false, getAlternatives(name))
    }

    private fun createWeightLossBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "weight_loss_beginner",
            name = "Похудение: Начальный",
            description = "Легкая тренировка для начинающих",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания", 3, "12-15"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 3, "10-12"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 3, "10-12"),
                    createExerciseWithAlternatives("4", "Пресс", 3, "15-20")
                ), listOf("Ноги", "Грудь", "Спина", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("5", "Выпады", 3, "12-15"),
                    createExerciseWithAlternatives("6", "Жим гантелей сидя", 3, "10-12"),
                    createExerciseWithAlternatives("7", "Подтягивания", 3, "макс"),
                    createExerciseWithAlternatives("8", "Пресс", 3, "15-20")
                ), listOf("Ноги", "Плечи", "Спина", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("9", "Становая тяга", 3, "8-10"),
                    createExerciseWithAlternatives("10", "Отжимания", 3, "10-15"),
                    createExerciseWithAlternatives("11", "Тяга верхнего блока", 3, "10-12"),
                    createExerciseWithAlternatives("12", "Пресс", 3, "15-20")
                ), listOf("Спина", "Грудь", "Пресс"))
            )
        )
    }

    private fun createWeightLossIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "weight_loss_intermediate",
            name = "Похудение: Продвинутый",
            description = "Интенсивная программа для похудения",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания со штангой", 4, "8-10"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 4, "8-10"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 4, "8-10"),
                    createExerciseWithAlternatives("4", "Бицепс", 3, "10-12"),
                    createExerciseWithAlternatives("5", "Пресс", 4, "15-20")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("6", "Становая тяга", 4, "6-8"),
                    createExerciseWithAlternatives("7", "Жим гантелей сидя", 4, "10-12"),
                    createExerciseWithAlternatives("8", "Подтягивания", 4, "макс"),
                    createExerciseWithAlternatives("9", "Трицепс", 3, "10-12"),
                    createExerciseWithAlternatives("10", "Пресс", 4, "15-20")
                ), listOf("Спина", "Плечи", "Руки", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("11", "Выпады с гантелями", 3, "12-15"),
                    createExerciseWithAlternatives("12", "Отжимания", 4, "15-20"),
                    createExerciseWithAlternatives("13", "Тяга верхнего блока", 4, "10-12"),
                    createExerciseWithAlternatives("14", "Бицепс", 3, "10-12"),
                    createExerciseWithAlternatives("15", "Пресс", 4, "15-20")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс"))
            )
        )
    }

    private fun createMuscleGainBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "muscle_gain_beginner",
            name = "Масса: Начальный",
            description = "Базовые упражнения для набора массы",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания", 4, "8-10"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 4, "8-10"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 4, "8-10"),
                    createExerciseWithAlternatives("4", "Пресс", 3, "12-15")
                ), listOf("Ноги", "Грудь", "Спина", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("5", "Становая тяга", 4, "6-8"),
                    createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12"),
                    createExerciseWithAlternatives("7", "Подтягивания", 4, "макс"),
                    createExerciseWithAlternatives("8", "Пресс", 3, "12-15")
                ), listOf("Спина", "Плечи", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("9", "Выпады", 4, "10-12"),
                    createExerciseWithAlternatives("10", "Отжимания", 4, "10-15"),
                    createExerciseWithAlternatives("11", "Тяга верхнего блока", 4, "10-12"),
                    createExerciseWithAlternatives("12", "Пресс", 3, "12-15")
                ), listOf("Ноги", "Грудь", "Спина", "Пресс"))
            )
        )
    }

    private fun createMuscleGainIntermediatePlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "muscle_gain_intermediate",
            name = "Масса: Продвинутый",
            description = "Программа для активного набора массы",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания со штангой", 5, "6-8"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 5, "6-8"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 5, "6-8"),
                    createExerciseWithAlternatives("4", "Бицепс", 4, "8-10"),
                    createExerciseWithAlternatives("5", "Пресс", 4, "10-15")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("6", "Становая тяга", 5, "5-6"),
                    createExerciseWithAlternatives("7", "Жим гантелей сидя", 5, "8-10"),
                    createExerciseWithAlternatives("8", "Подтягивания", 5, "макс"),
                    createExerciseWithAlternatives("9", "Трицепс", 4, "8-10"),
                    createExerciseWithAlternatives("10", "Пресс", 4, "10-15")
                ), listOf("Спина", "Плечи", "Руки", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("11", "Выпады с гантелями", 4, "10-12"),
                    createExerciseWithAlternatives("12", "Жим на наклонной скамье", 5, "8-10"),
                    createExerciseWithAlternatives("13", "Тяга верхнего блока", 5, "8-10"),
                    createExerciseWithAlternatives("14", "Бицепс", 4, "8-10"),
                    createExerciseWithAlternatives("15", "Пресс", 4, "10-15")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс"))
            )
        )
    }

    private fun createMuscleGainAdvancedPlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "muscle_gain_advanced",
            name = "Масса: Профессиональный",
            description = "Максимальная программа для набора массы",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Руки"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания со штангой", 6, "4-6"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 6, "4-6"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 6, "4-6"),
                    createExerciseWithAlternatives("4", "Бицепс", 4, "8-10"),
                    createExerciseWithAlternatives("5", "Трицепс", 4, "8-10"),
                    createExerciseWithAlternatives("6", "Пресс", 4, "10-15")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("7", "Становая тяга", 6, "3-5"),
                    createExerciseWithAlternatives("8", "Жим гантелей сидя", 6, "6-8"),
                    createExerciseWithAlternatives("9", "Подтягивания", 6, "макс"),
                    createExerciseWithAlternatives("10", "Бицепс", 4, "8-10"),
                    createExerciseWithAlternatives("11", "Трицепс", 4, "8-10"),
                    createExerciseWithAlternatives("12", "Пресс", 4, "10-15")
                ), listOf("Спина", "Плечи", "Руки", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("13", "Выпады с гантелями", 5, "8-10"),
                    createExerciseWithAlternatives("14", "Жим на наклонной скамье", 6, "6-8"),
                    createExerciseWithAlternatives("15", "Тяга верхнего блока", 6, "8-10"),
                    createExerciseWithAlternatives("16", "Бицепс", 4, "8-10"),
                    createExerciseWithAlternatives("17", "Трицепс", 4, "8-10"),
                    createExerciseWithAlternatives("18", "Пресс", 4, "10-15")
                ), listOf("Ноги", "Грудь", "Спина", "Руки", "Пресс"))
            )
        )
    }

    private fun createMaintenancePlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "maintenance",
            name = "Поддержание формы",
            description = "Сбалансированная программа для поддержания формы",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс"),
            goal = profile.goal,
            level = profile.level,
            days = listOf(
                WorkoutDay(0, "День 1", listOf(
                    createExerciseWithAlternatives("1", "Приседания", 4, "10-12"),
                    createExerciseWithAlternatives("2", "Жим лёжа", 4, "10-12"),
                    createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 4, "10-12"),
                    createExerciseWithAlternatives("4", "Пресс", 3, "15-20")
                ), listOf("Ноги", "Грудь", "Спина", "Пресс")),
                WorkoutDay(1, "День 2", listOf(
                    createExerciseWithAlternatives("5", "Становая тяга", 4, "8-10"),
                    createExerciseWithAlternatives("6", "Жим гантелей сидя", 4, "10-12"),
                    createExerciseWithAlternatives("7", "Подтягивания", 4, "макс"),
                    createExerciseWithAlternatives("8", "Пресс", 3, "15-20")
                ), listOf("Спина", "Плечи", "Пресс")),
                WorkoutDay(2, "День 3", listOf(
                    createExerciseWithAlternatives("9", "Выпады", 4, "12-15"),
                    createExerciseWithAlternatives("10", "Отжимания", 4, "15-20"),
                    createExerciseWithAlternatives("11", "Тяга верхнего блока", 4, "10-12"),
                    createExerciseWithAlternatives("12", "Пресс", 3, "15-20")
                ), listOf("Ноги", "Грудь", "Спина", "Пресс"))
            )
        )
    }
}
