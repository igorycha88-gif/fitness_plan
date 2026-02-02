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

    override suspend fun getAllExercisesAsList(): List<ExerciseLibrary> {
        return getAllExercises().first()
    }

    override suspend fun getAlternativeExercises(
        currentExerciseName: String,
        currentMuscleGroups: List<MuscleGroup>,
        limit: Int
    ): List<ExerciseLibrary> {
        val allExercises = getAllExercisesAsList()

        return allExercises
            .filter { it.name != currentExerciseName }
            .mapNotNull { exercise ->
                val commonMuscles = currentMuscleGroups.intersect(exercise.muscleGroups.toSet())
                if (commonMuscles.isNotEmpty()) {
                    Pair(exercise, commonMuscles.size)
                } else {
                    null
                }
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    override suspend fun initializeDefaultExercises() {
        val currentExercises = getAllExercises().first()
        if (currentExercises.isNotEmpty()) return

        val defaultExercises = getDefaultExerciseLibrary()
        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(defaultExercises)
        }
    }

    override suspend fun reloadDefaultExercises() {
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
                description = "Кардио упражнение для ног и ягодиц с высоким темпом выполнения. Встаньте прямо, ноги на ширине плеч. Сгибайте колени, опускаясь вниз, пока бедра не станут параллельны полу.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, ноги на ширине плеч\n2. Спина прямая, грудь вперед\n3. Медленно опускайтесь, сгибая колени\n4. Опуститесь до параллели с полом\n5. Поднимитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Держите пятки на полу\n• Не скругляйте спину\n• Колени не должны выходить за носки\n• Держите грудь в расправленном состоянии\n• Дышите: вдох при опускании, выдох при подъеме\n• Выполняйте с высоким темпом для кардио-эффекта",
                progressionAdvice = "1. Увеличьте количество повторений\n2. Увеличьте темп выполнения\n3. Уменьшите отдых между подходами\n4. Попробуйте приседания с выпрыгиванием\n5. Выполняйте в интервальном режиме"
            ),
            ExerciseLibrary(
                id = "goblet_squat",
                name = "Приседания с гантелями",
                description = "Вариация приседаний с отягощением перед грудью. Держите гантель у груди и выполняйте приседания.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите гантель и прижмите её к груди\n2. Ноги на ширине плеч\n3. Присядьте, держа спину прямо\n4. Поднимитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Держите гантель близко к груди\n• Спина прямая, грудь вперёд\n• Колени направлены вперёд\n• Держите пятки на полу\n• Контролируйте глубину приседания",
                progressionAdvice = "1. Увеличьте вес гантели\n2. Попробуйте приседания с двумя гантелями\n3. Уменьшите темп выполнения\n4. Добавьте паузу в нижней точке"
            ),
            ExerciseLibrary(
                id = "front_squat",
                name = "Фронтальные приседания",
                description = "Приседания со штангой на плечах. Акцент на квадрицепсы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Средний",
                stepByStepInstructions = "1. Положите штангу на передние дельты\n2. Локти направлены вперёд и вверх\n3. Присядьте, держа корпус вертикально\n4. Поднимитесь, разгибая ноги",
                imageUrl = null,
                tipsAndAdvice = "• Держите локти высоко\n• Корпус максимально вертикально\n• Пятки на полу\n• Взгляд направлен вперёд\n• Держите пресс напряжённым",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте приседания на ящик\n3. Уменьшите время отдыха\n4. Используйте цепи для сопротивления"
            ),
            ExerciseLibrary(
                id = "hack_squat",
                name = "Гакк-приседания",
                description = "Приседания в машине гакк-приседаний. Акцент на квадрицепсы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Расположитесь в машине гакк-приседаний\n2. Плечи под подушками\n3. Опуститесь, сгибая колени\n4. Поднимитесь, разгибая ноги",
                imageUrl = null,
                tipsAndAdvice = "• Не скругляйте поясницу\n• Полностью выпрямляйте ноги вверху\n• Контролируйте движение вниз\n• Пятки плотно на платформе\n• Не отрывайте плечи от подушек",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте разные положения ног\n3. Используйте темповые приседания\n4. Добавьте паузу в нижней точке"
            ),
            ExerciseLibrary(
                id = "leg_press",
                name = "Жим ногами",
                description = "Жим ногами в машине. Изолированное упражнение для ног.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в машину жима ногами\n2. Ноги на платформе на ширине плеч\n3. Опустите платформу к груди\n4. Выжмите платформу вверх",
                imageUrl = null,
                tipsAndAdvice = "• Не закрывайте ноги полностью\n• Не отрывайте поясницу от спинки\n• Контролируйте амплитуду\n• Пятки плотно на платформе\n• Не давите коленями внутрь",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте высокую постановку ног\n3. Попробуйте низкую постановку ног\n4. Измените ширину постановки ног"
            ),
            ExerciseLibrary(
                id = "leg_extension",
                name = "Разведение ног",
                description = "Разведение ног в тренажёре. Изолированное упражнение для квадрицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр\n2. Зафиксируйте голени за валиками\n3. Выпрямите ноги\n4. Медленно вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Не отрывайте бедра от сиденья\n• Полностью выпрямляйте ноги\n• Контролируйте негативную фазу\n• Держите корпус стабильным\n• Не перегружайте колени",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте поочерёдное выпрямление\n3. Уменьшите время отдыха\n4. Используйте изометрические паузы"
            ),
            ExerciseLibrary(
                id = "leg_curl",
                name = "Сведение ног",
                description = "Сведение ног в тренажёре. Изолированное упражнение для задней поверхности бедра.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.HAMSTRINGS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте в тренажёр на живот\n2. Зафиксируйте голени за валиками\n3. Согните ноги\n4. Медленно вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Не отрывайте таз от скамьи\n• Контролируйте движение вниз\n• Не делайте рывков\n• Держите корпус стабильным\n• Не перегружайте поясницу",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте поочерёдное сгибание\n3. Уменьшите время отдыха\n4. Используйте изометрические паузы"
            ),
            ExerciseLibrary(
                id = "reverse_lunge",
                name = "Выпады назад",
                description = "Выпады назад с гантелями. Акцент на ягодицы и квадрицепсы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, гантели в руках\n2. Сделайте шаг назад\n3. Опустите колено к полу\n4. Вернитесь в исходное положение\n5. Повторите на другую ногу",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая, корпус вертикально\n• Переднее колено под углом 90 градусов\n• Пятки на полу\n• Контролируйте движение\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте болгарские выпады\n3. Уменьшите время отдыха\n4. Попробуйте выпады в сторону"
            ),
            ExerciseLibrary(
                id = "sumo_squat",
                name = "Сумо-приседания",
                description = "Приседания с широкой постановкой ног. Акцент на внутреннюю поверхность бедра и ягодицы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.BARBELL, EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Расставьте ноги широко\n2. Носки развернуты в стороны\n3. Присядьте, сохраняя спину прямо\n4. Поднимитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Держите спину прямо\n• Колени направлены в сторону носков\n• Пятки плотно на полу\n• Контролируйте глубину приседания\n• Не сводите колени внутрь",
                progressionAdvice = "1. Увеличьте вес гантели\n2. Попробуйте приседания с паузой\n3. Уменьшите время отдыха\n4. Попробуйте плиометрический вариант"
            ),
            ExerciseLibrary(
                id = "bulgarian_split_squat",
                name = "Болгарские сплит-приседания",
                description = "Приседания на одной ноге с задней ногой на возвышении.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.BODYWEIGHT, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Положите заднюю ногу на скамью\n2. Передняя нога впереди\n3. Присядьте на передней ноге\n4. Поднимитесь в исходное положение\n5. Повторите на другую ногу",
                imageUrl = null,
                tipsAndAdvice = "• Держите спину прямо\n• Переднее колено над стопой\n• Не отрывайте пятку передней ноги\n• Контролируйте амплитуду\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте приседания без отягощения\n3. Уменьшите время отдыха\n4. Попробуйте статическую задержку"
            ),
            ExerciseLibrary(
                id = "single_leg_squat",
                name = "Приседания на одной ноге",
                description = "Приседания на одной ноге. Сложное упражнение для баланса и силы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Встаньте на одну ногу\n2. Другую ногу поднимите вперед\n3. Присядьте, сохраняя баланс\n4. Поднимитесь в исходное положение\n5. Повторите на другую ногу",
                imageUrl = null,
                tipsAndAdvice = "• Держите спину прямо\n• Контролируйте баланс\n• Не отрывайте пятку от пола\n• Начните с небольшой амплитуды\n• Используйте опору при необходимости",
                progressionAdvice = "1. Увеличьте глубину приседания\n2. Добавьте гантели\n3. Попробуйте приседания на ящик\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "hip_thrust",
                name = "Ягодичный мостик",
                description = "Упражнение для ягодиц и задней поверхности бедра.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.BARBELL, EquipmentType.WEIGHT_PLATES, EquipmentType.BODYWEIGHT, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на спину, ноги согнуты\n2. Поднимите таз вверх\n3. Сожмите ягодицы в верхней точке\n4. Медленно опуститесь",
                imageUrl = null,
                tipsAndAdvice = "• Поднимайте таз максимально высоко\n• Сжимайте ягодицы вверху\n• Спина прямая\n• Не отрывайте поясницу от пола\n• Контролируйте движение вниз",
                progressionAdvice = "1. Добавьте отягощение на таз\n2. Попробуйте односторонний вариант\n3. Уменьшите время отдыха\n4. Попробуйте на одной ноге"
            ),
            ExerciseLibrary(
                id = "calf_raises_standing",
                name = "Подъёмы на носки стоя",
                description = "Упражнение для икроножных мышц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES, EquipmentType.BODYWEIGHT, EquipmentType.SPECIAL_MACHINE),
                muscleGroups = listOf(MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте ровно, стопы на полу\n2. Поднимитесь на носки\n3. Задержитесь в верхней точке\n4. Медленно опуститесь",
                imageUrl = null,
                tipsAndAdvice = "• Держите корпус ровно\n• Полное растяжение внизу\n• Максимальное сокращение вверху\n• Не делайте рывков\n• Используйте полную амплитуду",
                progressionAdvice = "1. Добавьте гантели\n2. Попробуйте на одной ноге\n3. Увеличьте количество повторений\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "calf_raises_seated",
                name = "Подъёмы на носки сидя",
                description = "Упражнение для камбаловидных мышц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.SPECIAL_MACHINE, EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр или на скамью\n2. Положите отягощение на колени\n3. Поднимитесь на носки\n4. Медленно опуститесь",
                imageUrl = null,
                tipsAndAdvice = "• Полное растяжение внизу\n• Максимальное сокращение вверху\n• Не отрывайте пятки\n• Контролируйте движение\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Увеличьте количество повторений\n3. Попробуйте на одной ноге\n4. Уменьшите время отдыха"
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
                imageUrl = null,
                tipsAndAdvice = "• Лопатки сведены вместе\n• Поясница с небольшим прогибом\n• Контролируйте движение вниз\n• Полностью выпрямите руки вверху\n• Не отрывайте поясницу от скамьи",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Изменяйте ширину хвата\n3. Попробуйте жим с паузой внизу\n4. Используйте цепи или резинки\n5. Перейдите на жим гантелей"
            ),
            ExerciseLibrary(
                id = "incline_dumbbell_press",
                name = "Жим гантелей на наклонной скамье",
                description = "Жим гантелей на наклонной скамье для верхней части груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Установите скамью под углом 30-45 градусов\n2. Возьмите гантели\n3. Опустите гантели к груди\n4. Выжмите гантели вверх",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не разжимайте локти полностью вверху\n• Держите гантели на одной линии\n• Спина плотно прижата к скамье\n• Взгляд направлен вверх",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте нейтральный хват\n3. Попробуйте жим с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "decline_press",
                name = "Жим на наклонной скамье вниз",
                description = "Жим на наклонной скамье вниз для нижней части груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Установите скамью под углом -15-30 градусов\n2. Возьмите штангу или гантели\n3. Опустите вес к нижней части груди\n4. Выжмите вес вверх",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не отрывайте поясницу от скамьи\n• Держите локти под углом\n• Сжимайте грудь в верхней точке\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте жим гантелей\n3. Попробуйте жим с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "dumbbell_flyes",
                name = "Разведение гантелей лёжа",
                description = "Изолированное упражнение для грудных мышц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST),
                difficulty = "Средний",
                stepByStepInstructions = "1. Лягте на горизонтальную скамью\n2. Возьмите гантели над грудью\n3. Разведите гантели в стороны\n4. Сведите гантели вместе",
                imageUrl = null,
                tipsAndAdvice = "• Локти немного согнуты\n• Контролируйте движение вниз\n• Сжимайте грудь вверху\n• Не сгибайте сильно локти\n• Взгляд направлен вверх",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте на наклонной скамье\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "pec_deck",
                name = "Пек-дек",
                description = "Упражнение на тренажёре пек-дек для груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.CHEST),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр\n2. Возьмите рукоятки\n3. Сведите руки перед собой\n4. Медленно вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение назад\n• Сжимайте грудь в точке слияния\n• Не отрывайте спину от спинки\n• Локти на уровне плеч\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте одновременное сведение\n3. Попробуйте поочерёдное сведение\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "dips",
                name = "Отжимания на брусьях",
                description = "Упражнение для груди, плеч и трицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Повисните на брусьях\n2. Опуститесь вниз\n3. Поднимитесь вверх\n4. Выпрямите руки полностью",
                imageUrl = null,
                tipsAndAdvice = "• Не раскачивайтесь\n• Держите корпус ровно\n• Локти не уходят далеко назад\n• Контролируйте движение вниз\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте медленные повторения\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "close_grip_pushups",
                name = "Отжимания узким хватом",
                description = "Отжимания с узким хватом. Акцент на трицепсы и внутреннюю часть груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Примите упор лёжа\n2. Руки рядом друг с другом\n3. Опуститесь к полу\n4. Поднимитесь вверх",
                imageUrl = null,
                tipsAndAdvice = "• Локти близко к корпусу\n• Спина прямая\n• Контролируйте движение вниз\n• Не отрывайте таз\n• Держите корпус напряжённым",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте на fists\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "dumbbell_flat_press",
                name = "Жим гантелей лёжа",
                description = "Жим гантелей на горизонтальной скамье.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на горизонтальную скамью\n2. Возьмите гантели\n3. Опустите гантели к груди\n4. Выжмите гантели вверх",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не разжимайте локти полностью\n• Взгляд направлен вверх\n• Лопатки сведены\n• Держите пресс напряжённым",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте нейтральный хват\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
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
                imageUrl = null,
                tipsAndAdvice = "• Спина должна быть прямой всё время\n• Пятки плотно прижаты к полу\n• Поднимайтесь за счёт ног, а не спины\n• Взгляд направлен вперёд\n• Не разгибайте спину в верхней точке",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте сумо-становую\n3. Работайте над хватом\n4. Используйте лямки при необходимости\n5. Добавьте паузу в верхней точке"
            ),
            ExerciseLibrary(
                id = "romanian_deadlift",
                name = "Румынская тяга",
                description = "Тяга на прямых ногах. Акцент на заднюю поверхность бедра и ягодицы.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.LATS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Встаньте прямо, штанга в руках\n2. Наклонитесь вперёд, сгибая ноги чуть-чуть\n3. Ощутите растяжение задней поверхности\n4. Поднимитесь, сжимая ягодицы",
                imageUrl = null,
                tipsAndAdvice = "• Ноги чуть согнуты\n• Спина прямая\n• Гриф близко к ногам\n• Сжимайте ягодицы вверху\n• Контролируйте движение вниз",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с гантелями\n3. Уменьшите время отдыха\n4. Попробуйте на одной ноге"
            ),
            ExerciseLibrary(
                id = "bent_over_row",
                name = "Тяга штанги в наклоне",
                description = "Упражнение для мышц спины. Наклоните корпус вперед и тяните штангу к низу груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Наклонитесь корпусом вперед\n2. Спина прямая, возьмите штангу\n3. Тяните штангу к нижней части груди\n4. Медленно опустите в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Держите спину прямой\n• Локти направлены в стороны\n• Сжимайте лопатки в верхней точке\n• Держите корпус стабильным\n• Не поднимайте голову вверх",
                progressionAdvice = "1. Увеличьте рабочий вес\n2. Уменьшите паузу между подходами\n3. Используйте разнохват\n4. Попробуйте тягу гантели одной рукой\n5. Увеличьте количество повторений"
            ),
            ExerciseLibrary(
                id = "single_arm_dumbbell_row",
                name = "Тяга гантели одной рукой",
                description = "Тяга гантели одной рукой с упором на скамью.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Упритесь одной рукой и коленом в скамью\n2. Возьмите гантель другой рукой\n3. Тяните гантель к поясу\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая и параллельна полу\n• Локти близко к корпусу\n• Сжимайте лопатки вверху\n• Контролируйте движение вниз\n• Не раскачивайтесь",
                progressionAdvice = "1. Увеличьте вес гантели\n2. Попробуйте тягу с паузой\n3. Уменьшите время отдыха\n4. Попробуйте тянуть к плечу"
            ),
            ExerciseLibrary(
                id = "t_bar_row",
                name = "Т-тяга с гантелью",
                description = "Тяга гантели с углом для спины.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Расположите гантель в углу\n2. Наклонитесь, возьмите гантель\n3. Тяните гантель к груди\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Локти направлены в стороны\n• Сжимайте лопатки вверху\n• Контролируйте движение вниз\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте узкий хват\n3. Попробуйте широкий хват\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "face_pulls",
                name = "Тяга каната к лицу",
                description = "Упражнение для задних дельт и трапеций.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите канат верхнего блока\n2. Тяните к лицу, разводя локти\n3. Сожмите лопатки\n4. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Локти направлены в стороны и вверх\n• Сжимайте лопатки вверху\n• Контролируйте движение назад\n• Не отклоняйтесь назад\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Уменьшите время отдыха\n4. Попробуйте разные хваты"
            ),
            ExerciseLibrary(
                id = "dumbbell_pullover",
                name = "Пуловер с гантелью",
                description = "Упражнение для груди, широчайших и трицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.TRICEPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Лягте поперёк скамьи\n2. Возьмите гантель прямыми руками\n3. Опустите гантель за голову\n4. Поднимите гантель над грудью",
                imageUrl = null,
                tipsAndAdvice = "• Локти чуть согнуты\n• Не прогибайте поясницу\n• Контролируйте движение вниз\n• Сжимайте грудь вверху\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте на горизонтальной скамье\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "hyperextension",
                name = "Гиперэкстензия",
                description = "Упражнение для разгибателей позвоночника, ягодиц и задней поверхности бедра.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.SPECIAL_BENCH, EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Расположитесь в тренажёре\n2. Ноги зафиксированы\n3. Опустите корпус вниз\n4. Поднимитесь, разгибая спину",
                imageUrl = null,
                tipsAndAdvice = "• Не прогибайтесь слишком сильно\n• Сжимайте ягодицы вверху\n• Контролируйте движение вниз\n• Не делайте рывков\n• Держите корпус напряжённым",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте с паузой вверху\n3. Уменьшите время отдыха\n4. Попробуйте одновременное поднятие"
            ),
            ExerciseLibrary(
                id = "close_grip_lat_pulldown",
                name = "Тяга верхнего блока узким хватом",
                description = "Тяга верхнего блока узким прямым хватом.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на тренажёр\n2. Возьмите рукоятку узким хватом\n3. Тяните к верхней части груди\n4. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Локти направлены вниз\n• Сжимайте лопатки вверху\n• Контролируйте движение назад\n• Не отклоняйтесь сильно\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте нейтральный хват\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "dumbbell_press",
                name = "Жим гантелей сидя",
                description = "Изолированное упражнение для плеч. Жмите гантели вверх из положения сидя.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на скамью, спина прямая\n2. Возьмите гантели на уровне плеч\n3. Выжмите гантели вверх\n4. Опустите в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Спина плотно прижата к скамье\n• Локти чуть вперёд вверху\n• Взгляд направлен вперёд\n• Не прогибайте поясницу\n• Контролируйте движение вниз",
                progressionAdvice = "1. Увеличьте вес гантелей\n2. Попробуйте нейтральный хват\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "arnold_press",
                name = "Жим Арнольда",
                description = "Жим гантелей с поворотом для всех пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Возьмите гантели на уровне плеч\n2. Поднимайте и вращайте ладони\n3. Выжмите гантели вверх\n4. Верните гантели с обратным вращением",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте вращение\n• Локти чуть вперёд вверху\n• Взгляд направлен вперёд\n• Не делайте рывков\n• Спина плотно прижата",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Уменьшите время отдыха\n4. Попробуйте медленные повторения"
            ),
            ExerciseLibrary(
                id = "lateral_raises",
                name = "Разведение гантелей в стороны",
                description = "Упражнение для средних пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, гантели в руках\n2. Разведите руки в стороны до уровня плеч\n3. Задержитесь в верхней точке\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Руки чуть согнуты\n• Не поднимайте выше плеч\n• Контролируйте движение вниз\n• Не делайте рывков\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте поочерёдное разведение\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "reverse_flyes",
                name = "Обратные разведения",
                description = "Упражнение для задних пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Наклонитесь корпусом\n2. Разведите гантели в стороны\n3. Сожмите лопатки\n4. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Руки чуть согнуты\n• Сжимайте лопатки вверху\n• Контролируйте движение вниз\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Уменьшите время отдыха\n4. Попробуйте на тренажёре"
            ),
            ExerciseLibrary(
                id = "shoulder_press_machine",
                name = "Жим на плечах в машине",
                description = "Жим плеч в тренажёре.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.SPECIAL_MACHINE),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр\n2. Возьмите рукоятки\n3. Выжмите вверх\n4. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Спина плотно прижата\n• Локти чуть вперёд вверху\n• Контролируйте движение вниз\n• Не делайте рывков\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Уменьшите время отдыха\n4. Попробуйте разную высоту рукоятей"
            ),
            ExerciseLibrary(
                id = "front_raise",
                name = "Подъём штанги перед собой",
                description = "Упражнение для передних пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, штанга в руках\n2. Поднимите штангу перед собой\n3. Не поднимайте выше уровня плеч\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Руки чуть согнуты\n• Не раскачивайтесь\n• Контролируйте движение вниз\n• Не делайте рывков\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с гантелями\n3. Попробуйте с паузой\n4. Попробуйте поочерёдный подъём"
            ),
            ExerciseLibrary(
                id = "dumbbell_front_raise",
                name = "Махи гантелями перед собой",
                description = "Махи гантелями для передних пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, гантели в руках\n2. Поднимите гантели перед собой\n3. Не поднимайте выше плеч\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Руки чуть согнуты\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте поочерёдный подъём\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "pullups",
                name = "Подтягивания",
                description = "Базовое упражнение для спины и бицепсов. Подтягивайтесь к перекладине.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Повисните на турнике, хват чуть шире плеч\n2. Подтяните подбородок к перекладине\n3. Медленно опуститесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Полная амплитуда\n• Не раскачивайтесь\n• Лопатки сведите вверху\n• Контролируйте движение вниз\n• Взгляд направлен вверх",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Попробуйте эксцентрические подтягивания"
            ),
            ExerciseLibrary(
                id = "pushups",
                name = "Отжимания",
                description = "Базовое упражнение для груди, плеч и трицепсов. Отжимайтесь от пола.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS, MuscleGroup.ABS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Примите упор лежа, руки шире плеч\n2. Опуститесь к полу, сгибая локти\n3. Поднимитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Локти под углом 45 градусов\n• Контролируйте движение вниз\n• Не отрывайте таз\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Попробуйте на одной руке"
            ),
            ExerciseLibrary(
                id = "lunges",
                name = "Выпады",
                description = "Упражнение для ног и ягодиц. Делайте широкий шаг вперед и опускайте корпус.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT, EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сделайте широкий шаг вперед\n2. Опустите колено задней ноги к полу\n3. Вернитесь в исходное положение\n4. Повторите на другую ногу",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Переднее колено под 90 градусов\n• Пятки на полу\n• Контролируйте движение\n• Не делайте рывков",
                progressionAdvice = "1. Попробуйте с гантелями\n2. Попробуйте болгарские выпады\n3. Попробуйте выпады назад\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "lat_pulldown",
                name = "Тяга верхнего блока",
                description = "Изолированное упражнение для спины. Тяните рукоятку к верхней части груди.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на тренажер, возьмите рукоятку\n2. Наклонитесь немного назад\n3. Тяните рукоятку к верхней части груди\n4. Вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Локти направлены вниз\n• Сжимайте лопатки вверху\n• Контролируйте движение назад\n• Не отклоняйтесь сильно\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "barbell_curl",
                name = "Бицепс со штангой",
                description = "Изолированное упражнение для бицепсов. Сгибайте руки со штангой.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.EZ_BARBELL, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите штангу прямым хватом\n2. Сгибайте руки к плечам\n3. Медленно разгибайте в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Локти прижаты к корпусу\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте EZ-штангу\n3. Попробуйте разные хваты\n4. Попробуйте с паузой"
            ),
            ExerciseLibrary(
                id = "hammer_curl",
                name = "Молотки",
                description = "Сгибания рук с гантелями нейтральным хватом.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS, MuscleGroup.BRACHIALIS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите гантели нейтральным хватом\n2. Сгибайте руки к плечам\n3. Ладони обращены друг к другу\n4. Медленно разгибайте",
                imageUrl = null,
                tipsAndAdvice = "• Локти прижаты к корпусу\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте поочерёдные сгибания\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "concentration_curl",
                name = "Концентрированные сгибания",
                description = "Сгибание одной руки с упором локтя на бедро.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на скамью\n2. Упритесь локтем во внутреннюю часть бедра\n3. Сгибайте руку\n4. Медленно разгибайте\n5. Повторите на другую руку",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Не раскачивайтесь\n• Полная амплитуда\n• Контролируйте движение вниз\n• Взгляд направлен на бицепс",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте медленные повторения\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "dumbbell_curl",
                name = "Сгибания рук с гантелями",
                description = "Сгибания рук с гантелями поочерёдно.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите гантели\n2. Сгибайте одну руку\n3. Медленно разгибайте\n4. Повторите на другую руку",
                imageUrl = null,
                tipsAndAdvice = "• Локти прижаты к корпусу\n• Не раскачивайтесь\n• Контролируйте движение вниз\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте нейтральный хват\n3. Попробуйте с паузой\n4. Попробуйте одновременно"
            ),
            ExerciseLibrary(
                id = "french_press",
                name = "Французский жим",
                description = "Разгибания рук со штангой или гантелями.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.EZ_BARBELL, EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Возьмите отягощение над головой\n2. Сгибайте руки за голову\n3. Разгибайте руки вверх\n4. Локти зафиксированы",
                imageUrl = null,
                tipsAndAdvice = "• Локти зафиксированы\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с гантелями\n3. Попробуйте EZ-штангу\n4. Попробуйте с паузой"
            ),
            ExerciseLibrary(
                id = "tricep_pushdown",
                name = "Трицепс на блоке",
                description = "Изолированное упражнение для трицепсов. Разгибайте руки на блоке.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите рукоятку прямым хватом\n2. Локти прижаты к корпусу\n3. Разгибайте руки вниз\n4. Медленно возвращайтесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Локти прижаты к корпусу\n• Контролируйте движение вверх\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные рукоятки\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "tricep_kickbacks",
                name = "Кикбэки",
                description = "Разгибание руки с гантелью в наклоне.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Наклонитесь, упритесь рукой в скамью\n2. Согните руку с гантелью\n3. Разгибайте руку назад\n4. Медленно вернитесь\n5. Повторите на другую руку",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Локоть зафиксирован\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте медленные повторения\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "overhead_extension",
                name = "Разгибания на блоке из-за головы",
                description = "Разгибания рук на блоке из-за головы для трицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Возьмите рукоятку\n2. Повернитесь спиной к блоку\n3. Поднимите руки над головой\n4. Разгибайте руки вперёд",
                imageUrl = null,
                tipsAndAdvice = "• Локти зафиксированы\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные рукоятки\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "crunches",
                name = "Скручивания",
                description = "Изолированное упражнение для пресса. Поднимайте плечи от пола, скручивая корпус.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.ABS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на спину, колени согнуты\n2. Руки за головой\n3. Поднимайте плечи, скручивая корпус\n4. Вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Не тяните шею руками\n• Скручивайте корпус\n• Не отрывайте поясницу\n• Контролируйте движение вниз\n• Взгляд направлен вверх",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте на наклонной скамье\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "plank",
                name = "Планка",
                description = "Изометрическое упражнение для кора. Держите тело в прямой линии на предплечьях.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Примите упор лежа на предплечьях\n2. Тело образует прямую линию\n3. Держите позицию заданное время",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Напрягите пресс\n• Не прогибайте поясницу\n• Держите корпус напряжённым\n• Дышите ровно",
                progressionAdvice = "1. Увеличьте время\n2. Попробуйте планку на одной руке\n3. Попробуйте боковую планку\n4. Попробуйте с отягощением"
            ),
            ExerciseLibrary(
                id = "running",
                name = "Бег",
                description = "Кардио упражнение. Бегайте с умеренной или высокой интенсивностью.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.TREADMILL),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Начните медленно для разминки\n2. Увеличьте темп до нужного уровня\n3. Дышите ритмично\n4. Завершите заминкой",
                imageUrl = null,
                tipsAndAdvice = "• Держите правильную технику бега\n• Не сутультесь\n• Дышите ритмично\n• Не перегружайтесь\n• Пейте воду",
                progressionAdvice = "1. Увеличьте время\n2. Увеличьте скорость\n3. Попробуйте интервальный бег\n4. Попробуйте на повышенной наклонной"
            ),
            ExerciseLibrary(
                id = "cycling",
                name = "Велотренажёр",
                description = "Кардио упражнение для ног. Крутите педали на велотренажере.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.ROWING_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Отрегулируйте сиденье и сопротивление\n2. Начните медленно для разминки\n3. Увеличьте темп и сопротивление\n4. Завершите заминкой",
                imageUrl = null,
                tipsAndAdvice = "• Регулируйте сиденье по высоте\n• Держите правильную технику\n• Не раскачивайтесь\n• Дышите ритмично\n• Пейте воду",
                progressionAdvice = "1. Увеличьте время\n2. Увеличьте сопротивление\n3. Попробуйте интервальный режим\n4. Попробуйте стоя"
            ),
            ExerciseLibrary(
                id = "elliptical",
                name = "Эллипсоид",
                description = "Кардио упражнение на эллипсоиде. Низкая нагрузка на суставы.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.SPECIAL_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Встаньте на платформу\n2. Возьмитесь за рукоятки\n3. Начинайте движение\n4. Увеличьте темп и сопротивление",
                imageUrl = null,
                tipsAndAdvice = "• Держите правильную технику\n• Не перегружайтесь\n• Дышите ритмично\n• Не сутультесь\n• Пейте воду",
                progressionAdvice = "1. Увеличьте время\n2. Увеличьте сопротивление\n3. Попробуйте интервальный режим\n4. Попробуйте без рук"
            ),
            ExerciseLibrary(
                id = "rowing_machine",
                name = "Гребной тренажёр",
                description = "Кардио упражнение на гребном тренажёре. Работает всё тело.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.ROWING_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.GLUTES),
                difficulty = "Любой",
                stepByStepInstructions = "1. Сядьте на тренажёр\n2. Возьмитесь за рукоятку\n3. Оттолкнитесь ногами\n4. Тяните рукоятку к себе",
                imageUrl = null,
                tipsAndAdvice = "• Держите правильную технику\n• Не отклоняйтесь слишком сильно\n• Дышите ритмично\n• Не перегружайте спину\n• Пейте воду",
                progressionAdvice = "1. Увеличьте время\n2. Увеличьте сопротивление\n3. Попробуйте интервальный режим\n4. Попробуйте разный темп"
            ),
            ExerciseLibrary(
                id = "hiit",
                name = "HIIT",
                description = "Высокоинтенсивное интервальное тренировка. Чередование высокой и низкой интенсивности.",
                exerciseType = ExerciseType.CARDIO,
                equipment = listOf(EquipmentType.BODYWEIGHT, EquipmentType.TREADMILL, EquipmentType.ROWING_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.GLUTES),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Разминка 5 минут\n2. Высокая интенсивность 30 сек\n3. Низкая интенсивность 30 сек\n4. Повторите 10-15 раз\n5. Заминка 5 минут",
                imageUrl = null,
                tipsAndAdvice = "• Следите за пульсом\n• Не перегружайтесь\n• Дышите ритмично\n• Пейте воду\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте количество интервалов\n2. Увеличьте время высокой интенсивности\n3. Попробуйте разные упражнения\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "machine_chest_press",
                name = "Жим на тренажёре для груди",
                description = "Изолированное упражнение для грудных мышц на тренажёре.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр\n2. Возьмите рукоятки\n3. Выжмите вес вверх\n4. Вернитесь в исходное положение",
                imageUrl = null,
                imageRes = "chest_press_machine",
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не отрывайте спину\n• Сжимайте грудь вверху\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разную ширину хвата\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "chest_flyes",
                name = "Разведение на тренажёре",
                description = "Изолированное упражнение для грудных мышц на тренажёре.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.CHEST),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте в тренажёр\n2. Возьмите рукоятки\n3. Сведите руки вместе\n4. Вернитесь в исходное положение",
                imageUrl = null,
                imageRes = "chest_flyes",
                tipsAndAdvice = "• Контролируйте движение назад\n• Сжимайте грудь в точке слияния\n• Не отрывайте спину от спинки\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте поочерёдное сведение\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "cable_crossovers",
                name = "Кроссоверы на блоке",
                description = "Изолированное упражнение для грудных мышц на верхнем блоке.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.CHEST),
                difficulty = "Средний",
                stepByStepInstructions = "1. Возьмите рукоятки с верхних блоков\n2. Наклонитесь немного вперёд\n3. Сведите руки вниз\n4. Вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Небольшой наклон корпуса\n• Контролируйте движение назад\n• Сжимайте грудь в точке слияния\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой внизу\n3. Попробуйте разные углы\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "chin_ups",
                name = "Подтягивания обратным хватом",
                description = "Базовое упражнение для спины и бицепсов обратным хватом.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Повисните на турнике обратным хватом\n2. Подтяните подбородок к перекладине\n3. Медленно опуститесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Полная амплитуда\n• Не раскачивайтесь\n• Лопатки сведите вверху\n• Контролируйте движение вниз\n• Взгляд направлен вверх",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Попробуйте эксцентрические подтягивания"
            ),
            ExerciseLibrary(
                id = "wide_grip_lat_pulldown",
                name = "Тяга верхнего блока широким хватом",
                description = "Изолированное упражнение для широчайших мышц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на тренажёр\n2. Возьмите рукоятку широким хватом\n3. Тяните к верхней части груди\n4. Вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Локти направлены вниз\n• Сжимайте лопатки вверху\n• Контролируйте движение назад\n• Не отклоняйтесь сильно\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте нейтральный хват\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "incline_row",
                name = "Тяга штанги в наклоне на скамье",
                description = "Упражнение для спины с опорой на скамью.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.WEIGHT_PLATES, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Положите грудь на скамью под углом 45 градусов\n2. Возьмите штангу снизу\n3. Тяните штангу к поясу\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Грудь плотно прижата\n• Спина прямая\n• Сжимайте лопатки вверху\n• Контролируйте движение вниз\n• Взгляд направлен вперёд",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте узкий хват\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "upright_row",
                name = "Тяга штанги к подбородку",
                description = "Упражнение для плеч и трапеций.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.DUMBBELLS, EquipmentType.EZ_BARBELL, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Встаньте прямо, штанга в руках\n2. Поднимайте штангу к подбородку\n3. Локти направлены в стороны\n4. Медленно опустите",
                imageUrl = null,
                tipsAndAdvice = "• Локти выше рук\n• Контролируйте движение вниз\n• Не поднимайте слишком высоко\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с гантелями\n3. Попробуйте узкий хват\n4. Попробуйте с паузой"
            ),
            ExerciseLibrary(
                id = "rear_delt_flyes",
                name = "Махи гантелями в наклоне для задней дельты",
                description = "Изолированное упражнение для задних пучков дельт.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRAPS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Наклонитесь корпусом к полу\n2. Разведите гантели в стороны\n3. Сожмите лопатки\n4. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая и параллельна полу\n• Руки чуть согнуты\n• Сжимайте лопатки вверху\n• Контролируйте движение вниз\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Уменьшите время отдыха\n4. Попробуйте на тренажёре"
            ),
            ExerciseLibrary(
                id = "behind_neck_press",
                name = "Жим из-за головы",
                description = "Жим штанги из-за головы для плеч.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BARBELL, EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.TRAPS),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Сядьте на скамью\n2. Возьмите штангу на уровне плеч\n3. Выжмите штангу вверх\n4. Опустите за голову\n5. Вернитесь в исходное положение",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не прогибайте поясницу\n• Локти направлены вперёд\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте жим гантелей\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "preacher_curl",
                name = "Сгибания на скамье Скотта",
                description = "Изолированное упражнение для бицепсов на наклонной скамье.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.EZ_BARBELL, EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Сядьте на скамью Скотта\n2. Возьмите отягощение\n3. Сгибайте руки к плечам\n4. Медленно разгибайте",
                imageUrl = null,
                tipsAndAdvice = "• Руки плотно на подушке\n• Локти зафиксированы\n• Контролируйте движение вниз\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "resistance_band_curl",
                name = "Сгибания с эспандером",
                description = "Упражнение для бицепсов с эспандером.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.EXPANDER),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Наступите на эспандер\n2. Возьмите концы в руки\n3. Сгибайте руки к плечам\n4. Медленно разгибайте",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Держите напряжение\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте сопротивление\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "standing_concentration_curl",
                name = "Концентрированные сгибания стоя",
                description = "Изолированное упражнение для бицепсов стоя.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Встаньте прямо\n2. Упритесь локтем в сторону тела\n3. Сгибайте руку\n4. Медленно разгибайте\n5. Повторите на другую руку",
                imageUrl = null,
                tipsAndAdvice = "• Спина прямая\n• Не раскачивайтесь\n• Полная амплитуда\n• Контролируйте движение вниз\n• Взгляд направлен на бицепс",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой\n3. Попробуйте медленные повторения\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "rope_pushdown",
                name = "Разгибания на блоке канаты",
                description = "Изолированное упражнение для трицепсов с канатной рукояткой.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.CABLE_MACHINE),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Возьмите канатную рукоятку\n2. Локти прижаты к корпусу\n3. Разгибайте руки вниз\n4. Разводите концы в стороны\n5. Медленно вернитесь",
                imageUrl = null,
                tipsAndAdvice = "• Локти прижаты к корпусу\n• Разводите концы внизу\n• Контролируйте движение вверх\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с паузой внизу\n3. Попробуйте разные рукоятки\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "seated_french_press",
                name = "Французский жим сидя",
                description = "Разгибания рук сидя для трицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.EZ_BARBELL, EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH),
                muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Сядьте на скамью\n2. Возьмите отягощение над головой\n3. Сгибайте руки за голову\n4. Разгибайте руки вверх\n5. Локти зафиксированы",
                imageUrl = null,
                tipsAndAdvice = "• Локти зафиксированы\n• Контролируйте движение вниз\n• Не раскачивайтесь\n• Взгляд направлен вперёд\n• Не делайте рывков",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте с гантелями\n3. Попробуйте EZ-штангу\n4. Попробуйте с паузой"
            ),
            ExerciseLibrary(
                id = "inclined_dips",
                name = "Отжимания на брусьях с наклоном",
                description = "Упражнение для нижней части груди и трицепсов.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Повисните на брусьях\n2. Наклоните корпус вперёд\n3. Опуститесь вниз\n4. Поднимитесь вверх\n5. Выпрямите руки полностью",
                imageUrl = null,
                tipsAndAdvice = "• Корпус наклонён вперёд\n• Локти направлены в стороны\n• Контролируйте движение вниз\n• Взгляд направлен вперёд\n• Не раскачивайтесь",
                progressionAdvice = "1. Попробуйте с отягощением\n2. Попробуйте медленные повторения\n3. Попробуйте с паузой внизу\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "jump_squat",
                name = "Приседания с выпрыгиванием",
                description = "Плиометрическое упражнение для ног и ягодиц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Встаньте прямо, ноги на ширине плеч\n2. Присядьте до параллели с полом\n3. Выпрыгните вверх\n4. Приземлитесь мягко на пол\n5. Немедленно переходите к следующему повторению",
                imageUrl = null,
                tipsAndAdvice = "• Мягко приземляйтесь\n• Держите спину прямо\n• Используйте руки для инерции\n• Контролируйте приземление\n• Не делайте рывков",
                progressionAdvice = "1. Добавьте вес в руках\n2. Увеличьте высоту прыжка\n3. Попробуйте на одной ноге\n4. Увеличьте количество повторений"
            ),
            ExerciseLibrary(
                id = "single_leg_press",
                name = "Жим ногами на одной ноге",
                description = "Изолированное упражнение для ног на одной ноге.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.LEVER_MACHINE),
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
                difficulty = "Продвинутый",
                stepByStepInstructions = "1. Сядьте в машину жима ногами\n2. Ногу поставьте на платформу\n3. Опустите платформу к груди\n4. Выжмите платформу вверх\n5. Повторите на другую ногу",
                imageUrl = null,
                tipsAndAdvice = "• Другая нога расслаблена\n• Не закрывайте ногу полностью\n• Контролируйте амплитуду\n• Пятка плотно на платформе\n• Не давите коленом внутрь",
                progressionAdvice = "1. Увеличивайте вес постепенно\n2. Попробуйте высокую постановку\n3. Попробуйте низкую постановку\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "standing_romanian_deadlift",
                name = "Румынская тяга с гантелями стоя",
                description = "Упражнение для задней поверхности бедра и ягодиц.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS),
                muscleGroups = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.LATS, MuscleGroup.FOREARMS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Встаньте прямо, гантели в руках\n2. Наклонитесь вперёд, сгибая ноги чуть-чуть\n3. Ощутите растяжение задней поверхности\n4. Поднимитесь, сжимая ягодицы",
                imageUrl = null,
                tipsAndAdvice = "• Ноги чуть согнуты\n• Спина прямая\n• Гантели близко к ногам\n• Сжимайте ягодицы вверху\n• Контролируйте движение вниз",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные хваты\n3. Уменьшите время отдыха\n4. Попробуйте на одной ноге"
            ),
            ExerciseLibrary(
                id = "weighted_crunches",
                name = "Скручивания с отягощением",
                description = "Упражнение для пресса с дополнительным весом.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.ABS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Лягте на спину, ноги согнуты\n2. Возьмите вес к груди\n3. Поднимите корпус вверх\n4. Сожмите пресс\n5. Медленно опуститесь",
                imageUrl = null,
                tipsAndAdvice = "• Держите вес близко к груди\n• Поднимайтесь только за счёт пресса\n• Не тяните шеей\n• Дышите ритмично\n• Контролируйте движение вниз",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте на наклонной скамье\n3. Попробуйте скручивания в стороны\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "bicycle_crunches",
                name = "Велосипед",
                description = "Упражнение для пресса с имитацией велосипеда.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.BODYWEIGHT),
                muscleGroups = listOf(MuscleGroup.ABS),
                difficulty = "Начальный",
                stepByStepInstructions = "1. Лягте на спину\n2. Поднимите ноги и согните колени\n3. Одновременно коснитесь локтем противоположного колена\n4. Продолжайте чередовать стороны",
                imageUrl = null,
                tipsAndAdvice = "• Не отрывайте поясницу от пола\n• Контролируйте движение\n• Дышите ритмично\n• Не делайте рывков\n• Поднимайте плечи, а не шею",
                progressionAdvice = "1. Увеличьте количество повторений\n2. Попробуйте с замедлением\n3. Попробуйте задержку в верхней точке\n4. Уменьшите время отдыха"
            ),
            ExerciseLibrary(
                id = "incline_press",
                name = "Жим на наклонной скамье",
                description = "Изолированное упражнение для верхней части груди. Жмите гантели или штангу на наклонной скамье.",
                exerciseType = ExerciseType.STRENGTH,
                equipment = listOf(EquipmentType.DUMBBELLS, EquipmentType.SPECIAL_BENCH, EquipmentType.BARBELL, EquipmentType.WEIGHT_PLATES),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                difficulty = "Средний",
                stepByStepInstructions = "1. Установите скамью под углом 30-45 градусов\n2. Возьмите гантели или штангу\n3. Опустите вес к верхней части груди\n4. Выжмите вес вверх",
                imageUrl = null,
                tipsAndAdvice = "• Контролируйте движение вниз\n• Не разжимайте локти полностью\n• Взгляд направлен вверх\n• Лопатки сведены\n• Держите пресс напряжённым",
                progressionAdvice = "1. Увеличьте вес\n2. Попробуйте разные хваты\n3. Попробуйте с паузой\n4. Уменьшите время отдыха"
            )
        )
    }
}
