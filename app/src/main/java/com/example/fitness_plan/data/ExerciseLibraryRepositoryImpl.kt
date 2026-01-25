package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.exerciseLibraryDataStore: DataStore<Preferences> by preferencesDataStore(name = "exercise_library")

@Singleton
class ExerciseLibraryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : ExerciseLibraryRepository {

    private val exercisesKey = stringPreferencesKey("exercises_list")

    override fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return context.exerciseLibraryDataStore.data.map { preferences ->
            val json = preferences[exercisesKey]
            if (json != null) {
                try {
                    gson.fromJson(json, object : TypeToken<List<ExerciseLibrary>>() {}.type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    override fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { it.exerciseType == type }
        }
    }

    override fun getExercisesByEquipment(equipment: List<EquipmentType>): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                equipment.any { equip -> exercise.equipment.contains(equip) }
            }
        }
    }

    override fun getExercisesByMuscleGroups(muscleGroups: List<MuscleGroup>): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                muscleGroups.any { muscle -> exercise.muscleGroups.contains(muscle) }
            }
        }
    }

    override fun searchExercises(query: String): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                exercise.name.lowercase().contains(query.lowercase()) ||
                exercise.description.lowercase().contains(query.lowercase())
            }
        }
    }

    override suspend fun addExercise(exercise: ExerciseLibrary) {
        val currentExercises = getAllExercises().first().toMutableList()
        currentExercises.add(exercise)

        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(currentExercises)
        }
    }

    override suspend fun deleteExercise(exerciseId: String) {
        val currentExercises = getAllExercises().first().toMutableList()
        currentExercises.removeAll { it.id == exerciseId }

        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(currentExercises)
        }
    }

    override suspend fun getExerciseById(id: String): ExerciseLibrary? {
        return getAllExercises().first().find { it.id == id }
    }

    override suspend fun initializeDefaultExercises() {
        val currentExercises = getAllExercises().first()
        if (currentExercises.isNotEmpty()) return

        val defaultExercises = getDefaultExerciseLibrary()
        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(defaultExercises)
        }
    }

    private fun getDefaultExerciseLibrary(): List<ExerciseLibrary> {
        return listOf(
            ExerciseLibrary(
                id = "squat",
                name = "Приседания",
                description = "Базовое упражнение для ног и ягодиц. Встаньте прямо, ноги на ширине плеч. Сгибайте колени, опускаясь вниз, пока бедра не станут параллельны полу.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, ноги на ширине плеч\n2. Спина прямая, грудь вперед\n3. Медленно опускайтесь, сгибая колени\n4. Опуститесь до параллели с полом\n5. Поднимитесь в исходное положение",
                tipsAndAdvice = "• Держите пятки на полу\n• Не скругляйте спину\n• Колени не должны выходить за носки\n• Держите грудь в расправленном состоянии\n• Дышите: вдох при опускании, выдох при подъеме",
                progressionAdvice = "1. Добавьте отягощение (гантели, штанга)\n2. Увеличьте глубину приседания\n3. Уменьшите отдых между подходами\n4. Попробуйте приседания на одной ноге\n5. Используйте темповые приседания"
            ),
            ExerciseLibrary(
                id = "bench_press",
                name = "Жим лёжа",
                description = "Базовое упражнение для грудных мышц. Лягте на скамью, возьмите штангу, опустите её к груди и поднимите вверх.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на скамью, стопы на полу\n2. Возьмите штангу хватом чуть шире плеч\n3. Опустите штангу к груди\n4. Выжмите штангу вверх",
                tipsAndAdvice = "• Лопатки сведены вместе\n• Поясница с небольшим прогибом\n• Контролируйте движение вниз\n• Полностью выпрямите руки вверху\n• Не отрывайте поясницу от скамьи",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Изменяйте ширину хвата\n3. Попробуйте жим с паузой внизу\n4. Используйте цепи или резинки\n5. Перейдите на жим гантелей"
            ),
            ExerciseLibrary(
                id = "deadlift",
                name = "Становая тяга",
                description = "Базовое упражнение для спины и ног. Наклонитесь вперед, возьмите штангу и поднимите её, выпрямляясь.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Встаньте перед штангой, ноги на ширине плеч\n2. Наклонитесь, взявшись за штангу\n3. Спина прямая, поднимите штангу\n4. Вернитесь в исходное положение",
                tipsAndAdvice = "• Спина должна быть прямой всё время\n• Пятки плотно прижаты к полу\n• Поднимайтесь за счёт ног, а не спины\n• Взгляд направлен вперёд\n• Не разгибайте спину в верхней точке",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте сумо-становую\n3. Работайте над хватом\n4. Используйте лямки при необходимости\n5. Добавьте паузу в верхней точке"
            ),
            ExerciseLibrary(
                id = "bent_over_row",
                name = "Тяга штанги в наклоне",
                description = "Упражнение для мышц спины. Наклоните корпус вперед и тяните штангу к низу груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Наклонитесь корпусом вперед\n2. Спина прямая, возьмите штангу\n3. Тяните штангу к нижней части груди\n4. Медленно опустите в исходное положение",
                tipsAndAdvice = "• Держите спину прямой\n• Локти направлены в стороны\n• Сжимайте лопатки в верхней точке\n• Держите корпус стабильным\n• Не поднимайте голову вверх",
                progressionAdvice = "1. Увеличьте рабочий вес\n2. Уменьшите паузу между подходами\n3. Используйте разнохват\n4. Попробуйте тягу гантели одной рукой\n5. Увеличьте количество повторений"
            ),
            ExerciseLibrary(
                id = "overhead_press",
                name = "Армейский жим",
                description = "Базовое упражнение для плеч. Жмите штангу или гантели вверх из положения стоя или сидя.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо или сядьте на скамью\n2. Возьмите штангу на уровне плеч\n3. Выжмите штангу вверх\n4. Опустите в исходное положение"
            ),
            ExerciseLibrary(
                id = "dumbbell_press",
                name = "Жим гантелей сидя",
                description = "Изолированное упражнение для плеч. Жмите гантели вверх из положения сидя.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на скамью, спина прямая\n2. Возьмите гантели на уровне плеч\n3. Выжмите гантели вверх\n4. Опустите в исходное положение"
            ),
            ExerciseLibrary(
                id = "pullups",
                name = "Подтягивания",
                description = "Базовое упражнение для спины и бицепсов. Подтягивайтесь к перекладине.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Повисните на турнике, хват чуть шире плеч\n2. Подтяните подбородок к перекладине\n3. Медленно опуститесь в исходное положение"
            ),
            ExerciseLibrary(
                id = "pushups",
                name = "Отжимания",
                description = "Базовое упражнение для груди, плеч и трицепсов. Отжимайтесь от пола.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS, MuscleGroup.ABS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Примите упор лежа, руки шире плеч\n2. Опуститесь к полу, сгибая локти\n3. Поднимитесь в исходное положение"
            ),
            ExerciseLibrary(
                id = "lunges",
                name = "Выпады",
                description = "Упражнение для ног и ягодиц. Делайте широкий шаг вперед и опускайте корпус.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT, EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сделайте широкий шаг вперед\n2. Опустите колено задней ноги к полу\n3. Вернитесь в исходное положение\n4. Повторите на другую ногу"
            ),
            ExerciseLibrary(
                id = "lat_pulldown",
                name = "Тяга верхнего блока",
                description = "Изолированное упражнение для спины. Тяните рукоятку к верхней части груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на тренажер, возьмите рукоятку\n2. Наклонитесь немного назад\n3. Тяните рукоятку к верхней части груди\n4. Вернитесь в исходное положение"
            ),
            ExerciseLibrary(
                id = "barbell_curl",
                name = "Бицепс со штангой",
                description = "Изолированное упражнение для бицепсов. Сгибайте руки со штангой.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.EZ_BARBELL),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите штангу прямым хватом\n2. Сгибайте руки к плечам\n3. Медленно разгибайте в исходное положение"
            ),
            ExerciseLibrary(
                id = "tricep_pushdown",
                name = "Трицепс на блоке",
                description = "Изолированное упражнение для трицепсов. Разгибайте руки на блоке.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите рукоятку прямым хватом\n2. Локти прижаты к корпусу\n3. Разгибайте руки вниз\n4. Медленно возвращайтесь в исходное положение"
            ),
            ExerciseLibrary(
                id = "crunches",
                name = "Скручивания",
                description = "Изолированное упражнение для пресса. Поднимайте плечи от пола, скручивая корпус.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.ABS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на спину, колени согнуты\n2. Руки за головой\n3. Поднимайте плечи, скручивая корпус\n4. Вернитесь в исходное положение"
            ),
            ExerciseLibrary(
                id = "plank",
                name = "Планка",
                description = "Изометрическое упражнение для кора. Держите тело в прямой линии на предплечьях.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Примите упор лежа на предплечьях\n2. Тело образует прямую линию\n3. Держите позицию заданное время"
            ),
            ExerciseLibrary(
                id = "running",
                name = "Бег",
                description = "Кардио упражнение. Бегайте с умеренной или высокой интенсивностью.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.TREADMILL),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Начните медленно для разминки\n2. Увеличьте темп до нужного уровня\n3. Дышите ритмично\n4. Завершите заминкой"
            ),
            ExerciseLibrary(
                id = "cycling",
                name = "Велотренажёр",
                description = "Кардио упражнение для ног. Крутите педали на велотренажере.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.ROWING_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Отрегулируйте сиденье и сопротивление\n2. Начните медленно для разминки\n3. Увеличьте темп и сопротивление\n4. Завершите заминкой"
            ),
            ExerciseLibrary(
                id = "incline_press",
                name = "Жим на наклонной скамье",
                description = "Изолированное упражнение для верхней части груди. Жмите гантели или штангу на наклонной скамье.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Установите скамью под углом 30-45 градусов\n2. Возьмите гантели или штангу\n3. Опустите вес к верхней части груди\n4. Выжмите вес вверх"
            )
        )
    }
}
