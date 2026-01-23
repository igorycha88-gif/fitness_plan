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

    private fun createExerciseWithAlternatives(
        id: String,
        name: String,
        sets: Int,
        reps: String,
        description: String? = null,
        recommendedWeight: Float? = null,
        recommendedRepsPerSet: String? = null
    ): Exercise {
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
            recommendedWeight = recommendedWeight,
            recommendedRepsPerSet = recommendedRepsPerSet
        )
    }

    private fun createWeightLossBeginnerPlan(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan {
        return WorkoutPlan(
            id = "weight_loss_beginner",
            name = "Похудение: Любитель 30 дней",
            description = "Программа на 30 дней с разнообразием упражнений для похудения",
            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки", "Core"),
            goal = profile.goal,
            level = profile.level,
            days = (1..30).map { dayIndex ->
                val cycle = (dayIndex - 1) / 10 + 1 // Цикл 1,2,3
                val progress = (cycle - 1) * 2.0f // +2 кг за цикл
                when {
                    dayIndex in listOf(1, 5) || (dayIndex in 11..12 && cycle == 2) || (dayIndex in 21..22 && cycle == 3) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Ноги + Кардио", listOf(
                        createExerciseWithAlternatives("1", "Приседания со штангой", 3, "12-15", "Ноги на ширине плеч, спина прямая, опускайся до параллели", 20.0f + progress, "12,13,14"),
                        createExerciseWithAlternatives("2", "Жим ногами в машине", 3, "12-15", "Ступни на платформе, колени не выходят за носки", 40.0f + progress, "12,13,14"),
                        createExerciseWithAlternatives("3", "Выпады с гантелями", 3, "10/ногу", "Шаг вперёд, колено не касается пола", 8.0f + progress / 2, "10,10,10"),
                        createExerciseWithAlternatives("4", "Подъёмы на носки", 3, "15", "На икровой машине или с гантелью", 10.0f + progress, "15,15,15"),
                        createExerciseWithAlternatives("5", "Кардио: Эллипс", 1, "15 мин", "Умеренный темп", null, null)
                    ), listOf("Ноги", "Кардио"))
                    dayIndex in listOf(3, 7) || (dayIndex in 13..14 && cycle == 2) || (dayIndex in 23..24 && cycle == 3) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Грудь + Спина + Кардио", listOf(
                        createExerciseWithAlternatives("6", "Жим штанги лёжа", 3, "12-15", "Гриф на уровне груди, локти под 45°", 25.0f + progress, "12,13,14"),
                        createExerciseWithAlternatives("7", "Тяга верхнего блока", 3, "12-15", "Тяни к груди, лопатки сведены", 30.0f + progress, "12,13,14"),
                        createExerciseWithAlternatives("8", "Разведение гантелей лёжа", 3, "12-15", "Разводи в стороны, локти слегка согнуты", 6.0f + progress / 2, "12,13,14"),
                        createExerciseWithAlternatives("9", "Тяга гантели одной рукой", 3, "10/руку", "В наклоне, тяни к поясу", 10.0f + progress / 2, "10,10,10"),
                        createExerciseWithAlternatives("10", "Кардио: Велотренажёр", 1, "15 мин", "Умеренный темп", null, null)
                    ), listOf("Грудь", "Спина", "Кардио"))
                    dayIndex in listOf(9, 19, 29) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Плечи + Руки + Core + Кардио", listOf(
                        createExerciseWithAlternatives("11", "Армейский жим с гантелями", 3, "12-15", "Жим вверх над головой", 8.0f + progress / 2, "12,13,14"),
                        createExerciseWithAlternatives("12", "Разведение гантелей в стороны", 3, "12-15", "Стоя, руки параллельно полу", 5.0f + progress / 2, "12,13,14"),
                        createExerciseWithAlternatives("13", "Сгибания рук с гантелями", 3, "12-15", "Бицепс, локти неподвижны", 6.0f + progress / 2, "12,13,14"),
                        createExerciseWithAlternatives("14", "Разгибания рук на блоке", 3, "12-15", "Трицепс, над головой", 15.0f + progress, "12,13,14"),
                        createExerciseWithAlternatives("15", "Планка", 3, "30-45 сек", "На предплечьях, тело прямое", null, null),
                        createExerciseWithAlternatives("16", "Кардио: Беговая дорожка", 1, "15 мин", "Умеренный темп", null, null)
                    ), listOf("Плечи", "Руки", "Core", "Кардио"))
                    dayIndex in listOf(15, 25) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Ноги (вариации) + Кардио", listOf(
                        createExerciseWithAlternatives("17", "Фронтальные приседания", 3, "10-12", "Штанга на плечах спереди", 18.0f + progress, "10,11,12"),
                        createExerciseWithAlternatives("18", "Румынская тяга", 3, "10-12", "Тяга штанги к коленям, спина прямая", 25.0f + progress, "10,11,12"),
                        createExerciseWithAlternatives("19", "Болгарские сплит-приседания", 3, "8-10/ногу", "Задняя нога на скамье", 7.0f + progress / 2, "8,9,10"),
                        createExerciseWithAlternatives("20", "Икры стоя", 3, "15", "С гантелью в руке", 12.0f + progress, "15,15,15"),
                        createExerciseWithAlternatives("21", "Кардио: Интервальный бег", 1, "20 мин", "1 мин бег, 1 мин ходьба", null, null)
                    ), listOf("Ноги", "Кардио"))
                    dayIndex in listOf(17, 27) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Грудь + Спина (вариации) + Кардио", listOf(
                        createExerciseWithAlternatives("22", "Жим гантелей на наклонной скамье", 3, "10-12", "Голова выше ног", 10.0f + progress, "10,11,12"),
                        createExerciseWithAlternatives("23", "Тяга штанги в наклоне", 3, "10-12", "Тяни к поясу", 28.0f + progress, "10,11,12"),
                        createExerciseWithAlternatives("24", "Пуловер с гантелью", 3, "10-12", "Лёжа, руки за голову", 12.0f + progress, "10,11,12"),
                        createExerciseWithAlternatives("25", "Подтягивания с резинкой", 3, "8-10", "Если нет — тяга нижнего блока", null, "8,9,10"),
                        createExerciseWithAlternatives("26", "Кардио: Велотренажёр с сопротивлением", 1, "20 мин", "Среднее сопротивление", null, null)
                    ), listOf("Грудь", "Спина", "Кардио"))
                    dayIndex in listOf(21) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Ноги (комбо) + Кардио", listOf(
                        createExerciseWithAlternatives("27", "Сумо-приседания", 4, "8-10", "Ноги широко, носки в стороны", 22.0f + progress, "8,9,10,10"),
                        createExerciseWithAlternatives("28", "Ягодичный мостик", 4, "10-12", "Лёжа, подъём таза", null, "10,11,12,12"),
                        createExerciseWithAlternatives("29", "Шагающий выпад с поворотом", 4, "8/ногу", "Шаг + поворот корпуса", 9.0f + progress / 2, "8,8,9,9"),
                        createExerciseWithAlternatives("30", "Осёл-качки", 4, "12", "Икры, в наклоне", 14.0f + progress, "12,12,13,13"),
                        createExerciseWithAlternatives("31", "Кардио: HIIT", 1, "20 мин", "30 сек спринт, 30 сек отдых", null, null)
                    ), listOf("Ноги", "Кардио"))
                    dayIndex in listOf(23) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Грудь + Спина (комбо) + Кардио", listOf(
                        createExerciseWithAlternatives("32", "Жим в хаммере", 4, "8-10", "Машина, грудь вперёд", 27.0f + progress, "8,9,10,10"),
                        createExerciseWithAlternatives("33", "Т-тяга с гантелью", 4, "8-10", "Одной рукой, упор на скамью", 13.0f + progress, "8,9,10,10"),
                        createExerciseWithAlternatives("34", "Кроссовер", 4, "10-12", "Блочный тренажёр, сведение рук", 18.0f + progress, "10,11,12,12"),
                        createExerciseWithAlternatives("35", "Тяга каната", 4, "10-12", "К груди, локти назад", 22.0f + progress, "10,11,12,12"),
                        createExerciseWithAlternatives("36", "Кардио: Гребной тренажёр", 1, "20 мин", "Полная амплитуда", null, null)
                    ), listOf("Грудь", "Спина", "Кардио"))
                    dayIndex in listOf(25) -> WorkoutDay(dayIndex - 1, "День $dayIndex: Плечи + Руки (комбо) + Core + Кардио", listOf(
                        createExerciseWithAlternatives("37", "Жим плечами в машине", 4, "8-10", "Сидя, хват сверху", 20.0f + progress, "8,9,10,10"),
                        createExerciseWithAlternatives("38", "Обратные разведения", 4, "10-12", "Задние дельты", 8.0f + progress / 2, "10,11,12,12"),
                        createExerciseWithAlternatives("39", "Концентрированные сгибания", 4, "10-12", "Бицепс, одной рукой", 7.0f + progress / 2, "10,11,12,12"),
                        createExerciseWithAlternatives("40", "Кикбэки", 4, "10-12", "Трицепс, на блоке", 18.0f + progress, "10,11,12,12"),
                        createExerciseWithAlternatives("41", "Велосипед", 4, "20", "Лёжа, скручивания", null, "20,20,20,20"),
                        createExerciseWithAlternatives("42", "Кардио: Комбо", 1, "20 мин", "10 мин бег + 10 мин велотренажёр", null, null)
                    ), listOf("Плечи", "Руки", "Core", "Кардио"))
                    else -> WorkoutDay(dayIndex - 1, "День $dayIndex: Отдых", emptyList(), emptyList())
                }
            }
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
