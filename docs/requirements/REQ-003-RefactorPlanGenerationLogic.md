# REQ-003: Рефакторинг логики формирования плана тренировок

**Status**: New
**Priority**: High
**Complexity**: Complex
**Created**: 2026-02-17
**Updated**: 2026-02-17
**Related**: REQ-001-FixExerciseRepetition.md, REQ-002-FixPlanPersistenceAndStability.md

---

## Description

Рефакторинг автоматического формирования плана тренировок с внедрением:
1. Гарантии отсутствия повторений силовых упражнений в рамках одного цикла
2. Логики формирования плана "сверху-вниз" по анатомическим группам мышц
3. Вставки дня кардио после полного прохождения всех групп мышц
4. Ротации пулов упражнений между циклами

---

## User Story

**As a** Пользователь приложения,
**I want** Чтобы план тренировок формировался логично (сверху-вниз по группам мышц), упражнения не повторялись в цикле, а между силовыми тренировками были дни кардио,
**So that** Я мог эффективно тренировать все группы мышц без перетренированности и следовать научно обоснованному подходу к тренировкам.

---

## Analysis: Current State

### Текущая реализация

**Location**: `WorkoutRepositoryImpl.kt`

**Метод**: `createWeightLossPlanBySplit()` (строки 475-624)

**Текущая логика:**
```
День 1: Ноги (4 упражнения)
День 2: Грудь (4 упражнения)
День 3: Спина (4 упражнения)
День 4: Плечи (4 упражнения)
День 5: Руки (4 упражнения)
День 6: Ноги (4 упражнения)  <-- ПОВТОР!
...
```

### Выявленные проблемы

#### Проблема 1: Упражнения повторяются в рамках цикла
**Current State**:
- Пул упражнений для каждой группы конечен (8-12 упражнений)
- При 10-дневном цикле и 4 упражнениях на день упражнения неизбежно повторяются
- Используется `ExercisePool`, но после исчерпания пула начинается повтор

**Impact**: Пользователь выполняет одни и те же упражнения несколько раз в цикле

#### Проблема 2: Нет логичного порядка следования групп мышц
**Current State**:
- Порядок: Ноги → Грудь → Спина → Плечи → Руки
- Нет анатомической логики "сверху-вниз"

**Impact**: Неудобное планирование тренировок

#### Проблема 3: Нет дня кардио как отдельной тренировки
**Current State**:
- Кардио добавляется в начало и конец КАЖДОГО дня
- Нет отдельного дня восстановления

**Impact**: Нет полноценного восстановления, отсутствие вариативности

#### Проблема 4: Упражнения повторяются между циклами
**Current State**:
- При создании нового цикла используется тот же пул упражнений
- Нет отслеживания использованных упражнений в предыдущих циклах

**Impact**: Монотонность тренировок в долгосрочной перспективе

---

## Analysis: Best Practices

### Научно обоснованные практики сочетания групп мышц

#### 1. Анатомический принцип "Сверху-Вниз"

**Логика**: Тренировка от верхних групп мышц к нижним

| Порядок | Группа мышц | Обоснование |
|---------|-------------|-------------|
| 1 | Плечи + Руки (Bi/Tri) | Верх тела, мелкие группы мышц |
| 2 | Грудь + Широчайшие | Крупные толкающие/тянущие группы |
| 3 | Спина (Трапеции + Поясница) | Средняя часть корпуса |
| 4 | Пресс + Core | Ядро тела |
| 5 | Ноги (Квадрицепсы + Бёдра + Икры) | Нижняя часть тела |

**Преимущества:**
- Логичная структура запоминания
- Чередование крупных и мелких групп
- Возможность суперсетов (антагонисты)

#### 2. Push/Pull/Legs (PPL) альтернатива

| День | Тип | Группы мышц |
|------|-----|-------------|
| Push | Толкающие | Грудь, Плечи, Трицепсы |
| Pull | Тянущие | Широчайшие, Трапеции, Бицепсы |
| Legs | Ноги | Квадрицепсы, Бёдра, Ягодицы, Икры |

**Преимущества:**
- Популярная и проверенная система
- Баланс между толкающими и тянущими движениями
- Эффективное восстановление

### Практики включения кардио

#### Для набора мышечной массы:
- **Частота**: 1-2 раза в неделю
- **Длительность**: 20-30 минут
- **Тип**: Низкоинтенсивное кардио (LISS)
- **Тайминг**: Отдельный день или после силовой

#### Для похудения:
- **Частота**: 2-3 раза в неделю
- **Длительность**: 30-45 минут
- **Тип**: Комбинация LISS и HIIT
- **Тайминг**: Отдельные дни или после силовой

#### Для поддержания формы:
- **Частота**: 2 раза в неделю
- **Длительность**: 30 минут
- **Тип**: Умеренное кардио
- **Тайминг**: Отдельные дни

### Рекомендуемая структура цикла

**Вариант A: 4+1 (4 силовых + 1 кардио)**
```
День 1: Плечи + Бицепсы + Трицепсы
День 2: Грудь + Широчайшие
День 3: Спина (Трапеции + Поясница) + Пресс
День 4: Ноги (Квадрицепсы + Бёдра + Ягодицы + Икры)
День 5: Кардио (полноценный день)
--- Повтор цикла ---
День 6: Плечи + Бицепсы + Трицепсы (другие упражнения)
...
```

**Вариант B: 3+1 Push/Pull/Legs**
```
День 1: Push (Грудь + Плечи + Трицепсы)
День 2: Pull (Широчайшие + Трапеции + Бицепсы)
День 3: Legs (Квадрицепсы + Бёдра + Ягодицы + Икры)
День 4: Кардио + Пресс
--- Повтор ---
День 5-8: То же с другими упражнениями
День 9-10: Добивка оставшихся групп
```

---

## Proposed Solution

### Новая логика формирования плана

#### Принцип 1: Анатомический порядок "Сверху-Вниз"

```kotlin
enum class MuscleGroupSequence(val displayName: String, val muscleGroups: List<MuscleGroup>) {
    SHOULDERS_ARMS("Плечи и Руки", listOf(SHOULDERS, BICEPS, TRICEPS)),
    CHEST_LATS("Грудь и Широчайшие", listOf(CHEST, LATS)),
    BACK_CORE("Спина и Пресс", listOf(TRAPS, LOWER_BACK, ABS)),
    LEGS("Ноги", listOf(QUADS, HAMSTRINGS, GLUTES, CALVES)),
    CARDIO("Кардио", emptyList())
}
```

#### Принцип 2: Неповторяемость упражнений в цикле

**Алгоритм:**
1. Загрузить все упражнения из ExerciseLibrary
2. Разделить по группам мышц согласно MuscleGroupSequence
3. Создать пул доступных упражнений для каждой группы
4. Последовательно выбирать уникальные упражнения
5. При исчерпании пула - перейти к следующему циклу

**Реализация:**
```kotlin
data class ExercisePoolTracker(
    val poolId: String,  // Уникальный ID пула
    val usedExercises: Set<String>,
    val availableExercises: List<ExerciseLibrary>
) {
    fun getNextExercises(count: Int): List<ExerciseLibrary>
    fun markExercisesUsed(exercises: List<ExerciseLibrary>): ExercisePoolTracker
}
```

#### Принцип 3: Вставка дня кардио

**Логика:**
- После прохождения всех 4 силовых дней → День кардио
- В 10-дневном цикле: 8 силовых + 2 кардио
- Или: 4 силовых + 1 кардио + 4 силовых + 1 кардио

**Структура 10-дневного цикла:**
```
День 1: Плечи + Руки (Pool A, упражнения 1-4)
День 2: Грудь + Широчайшие (Pool A, упражнения 1-4)
День 3: Спина + Пресс (Pool A, упражнения 1-4)
День 4: Ноги (Pool A, упражнения 1-4)
День 5: Кардио (Cardio Pool, упражнение 1-2)
День 6: Плечи + Руки (Pool A, упражнения 5-8)
День 7: Грудь + Широчайшие (Pool A, упражнения 5-8)
День 8: Спина + Пресс (Pool A, упражнения 5-8)
День 9: Ноги (Pool A, упражнения 5-8)
День 10: Кардио (Cardio Pool, упражнение 3-4)
```

#### Принцип 4: Ротация пулов между циклами

**Логика:**
- Cycle 1: Использует Pool A
- Cycle 2: Использует Pool B (альтернативные упражнения)
- Cycle 3: Использует Pool C
- Cycle 4: Возврат к Pool A

**Реализация:**
```kotlin
data class CycleExerciseHistory(
    val cycleNumber: Int,
    val usedExercisesByMuscleGroup: Map<MuscleGroupSequence, Set<String>>
)
```

---

## Acceptance Criteria

### AC1: Анатомический порядок групп мышц
- **Given** Пользователь создаёт новый план тренировок
- **When** Система формирует 10-дневный план
- **Then** Дни следуют в порядке: Плечи+Руки → Грудь+Широчайшие → Спина+Пресс → Ноги → Кардио
- **And** После кардио цикл повторяется
- **And** Названия дней отражают группы мышц (например, "День 1: Плечи и Руки")

### AC2: Уникальность упражнений в цикле
- **Given** Создаётся новый 10-дневный план
- **When** Система выбирает упражнения для каждого дня
- **Then** Каждое силовое упражнение встречается максимум 1 раз в цикле
- **And** Если в пуле недостаточно уникальных упражнений, используются ближайшие альтернативы
- **And** Кардио упражнения могут повторяться (отдельный пул)

### AC3: Вставка дней кардио
- **Given** Формируется 10-дневный план
- **When** Пройдены все 4 группы мышц (силовые дни 1-4)
- **Then** День 5 является днём кардио
- **And** День 10 также является днём кардио
- **And** Кардио день содержит 2-3 кардио упражнения по 20-30 минут

### AC4: Ротация упражнений между циклами
- **Given** Пользователь завершил цикл N и начинает цикл N+1
- **When** Система формирует новый план
- **Then** Упражнения в новом цикле отличаются от предыдущего минимум на 60%
- **And** Система отслеживает историю использованных упражнений
- **And** После 3-4 циклов пул сбрасывается и упражнения могут повторяться

### AC5: Адаптация под цель пользователя
- **Given** Пользователь выбрал цель "Похудение"
- **When** Формируется план
- **Then** Кардио дни содержат 3 упражнения по 30-40 минут
- **And** В силовых днях больше повторений (12-15)

- **Given** Пользователь выбрал цель "Наращивание мышечной массы"
- **When** Формируется план
- **Then** Кардио дни содержат 2 упражнения по 20 минут
- **And** В силовых днях меньше повторений (6-10)

### AC6: Адаптация под частоту тренировок
- **Given** Пользователь выбрал "3 раза в неделю"
- **When** Формируется 10-дневный план
- **Then** План растягивается на ~3 недели (21 день)
- **And** Структура сохраняется: 2 силовых + 1 кардио на неделю

---

## Technical Requirements

### Components Affected

| Component | Change Type | Description |
|-----------|-------------|-------------|
| `WorkoutRepositoryImpl.kt` | Major Refactor | Переработка всех методов создания планов |
| `CycleUseCase.kt` | Modify | Добавление логики ротации пулов |
| `MuscleGroupSequence.kt` | New | Новый enum для порядка групп мышц |
| `ExercisePoolManager.kt` | New | Новый класс для управления пулами |
| `CycleExerciseHistory.kt` | New | Новая модель для истории упражнений |
| `CycleRepository.kt` | Modify | Добавление хранения истории упражнений |
| `ExerciseLibraryRepository.kt` | Modify | Добавление методов фильтрации по группам |

### New Models

```kotlin
enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    SHOULDERS_ARMS("Плечи и Руки", listOf(SHOULDERS, BICEPS, TRICEPS), 4),
    CHEST_LATS("Грудь и Широчайшие", listOf(CHEST, LATS), 4),
    BACK_CORE("Спина и Пресс", listOf(TRAPS, LOWER_BACK, ABS), 4),
    LEGS("Ноги", listOf(QUADS, HAMSTRINGS, GLUTES, CALVES), 4),
    CARDIO("Кардио", emptyList(), 2)
}

data class ExercisePoolConfig(
    val poolId: String,
    val exercises: List<ExerciseLibrary>,
    val usedInCycle: MutableSet<String> = mutableSetOf()
)

data class CycleExerciseHistory(
    val cycleNumber: Int,
    val startDate: Long,
    val usedExercises: Map<MuscleGroupSequence, Set<String>>
)
```

### Data Store Schema Changes

**New Keys:**
- `{username}_exercise_history` - История использованных упражнений по циклам
- `{username}_current_pool_id` - ID текущего пула упражнений

### Architecture Impact

```
┌─────────────────────────────────────────────────────────────────┐
│                    NEW FLOW                                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  CycleUseCase.initializeCycleForUser()                           │
│        │                                                         │
│        ├──► Get CycleExerciseHistory from DataStore              │
│        │                                                         │
│        ├──► Determine current pool (A, B, C)                     │
│        │                                                         │
│        ▼                                                         │
│  ExercisePoolManager.getExercisesForCycle()                      │
│        │                                                         │
│        ├──► Filter exercises by MuscleGroupSequence              │
│        ├──► Exclude already used exercises                       │
│        ├──► Select unique exercises for each group               │
│        │                                                         │
│        ▼                                                         │
│  WorkoutRepositoryImpl.createPlanWithSequence()                  │
│        │                                                         │
│        ├──► Day 1: SHOULDERS_ARMS (4 unique exercises)           │
│        ├──► Day 2: CHEST_LATS (4 unique exercises)               │
│        ├──► Day 3: BACK_CORE (4 unique exercises)                │
│        ├──► Day 4: LEGS (4 unique exercises)                     │
│        ├──► Day 5: CARDIO (2 exercises)                          │
│        ├──► Day 6-9: Repeat with different exercises             │
│        └──► Day 10: CARDIO (2 exercises)                         │
│        │                                                         │
│        ▼                                                         │
│  Save CycleExerciseHistory to DataStore                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation Notes

### Phase 1: Создание моделей и менеджера пулов

**File**: `domain/model/MuscleGroupSequence.kt` (new)

```kotlin
package com.example.fitness_plan.domain.model

enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    SHOULDERS_ARMS("Плечи и Руки", listOf(MuscleGroup.SHOULDERS, MuscleGroup.BICEPS, MuscleGroup.TRICEPS), 4),
    CHEST_LATS("Грудь и Широчайшие", listOf(MuscleGroup.CHEST, MuscleGroup.LATS), 4),
    BACK_CORE("Спина и Пресс", listOf(MuscleGroup.TRAPS, MuscleGroup.LOWER_BACK, MuscleGroup.ABS), 4),
    LEGS("Ноги", listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES), 4),
    CARDIO("Кардио", emptyList(), 3);

    companion object {
        val STRENGTH_SEQUENCE = listOf(SHOULDERS_ARMS, CHEST_LATS, BACK_CORE, LEGS)
        val FULL_SEQUENCE = listOf(SHOULDERS_ARMS, CHEST_LATS, BACK_CORE, LEGS, CARDIO)
    }
}
```

**File**: `domain/model/CycleExerciseHistory.kt` (new)

```kotlin
package com.example.fitness_plan.domain.model

data class CycleExerciseHistory(
    val cycleNumber: Int,
    val startDate: Long,
    val usedExercises: Map<MuscleGroupSequence, Set<String>>,
    val poolId: String
) {
    fun getUsedExercisesForGroup(group: MuscleGroupSequence): Set<String> {
        return usedExercises[group] ?: emptySet()
    }
    
    fun getTotalUniqueExercisesUsed(): Int {
        return usedExercises.values.sumOf { it.size }
    }
}
```

**File**: `domain/usecase/ExercisePoolManager.kt` (new)

```kotlin
package com.example.fitness_plan.domain.usecase

class ExercisePoolManager @Inject constructor(
    private val exerciseLibraryRepository: ExerciseLibraryRepository
) {
    private val poolRotationSize = 3
    
    suspend fun getExercisesForSequence(
        sequence: MuscleGroupSequence,
        excludedExerciseNames: Set<String>,
        count: Int
    ): List<ExerciseLibrary> {
        val allExercises = exerciseLibraryRepository.getAllExercisesAsList()
        
        val filteredExercises = when (sequence) {
            MuscleGroupSequence.CARDIO -> {
                allExercises.filter { it.exerciseType == ExerciseType.CARDIO }
            }
            else -> {
                allExercises.filter { exercise ->
                    exercise.exerciseType == ExerciseType.STRENGTH &&
                    exercise.muscleGroups.any { it in sequence.muscleGroups } &&
                    exercise.name !in excludedExerciseNames
                }
            }
        }
        
        return filteredExercises.take(count)
    }
    
    fun determinePoolId(cycleNumber: Int): String {
        val poolIndex = ((cycleNumber - 1) % poolRotationSize)
        return "pool_${('A'.code + poolIndex).toChar()}"
    }
}
```

### Phase 2: Рефакторинг WorkoutRepositoryImpl

**File**: `data/WorkoutRepositoryImpl.kt`

Заменить метод `createWeightLossPlanBySplit()` на `createPlanWithSequence()`:

```kotlin
private suspend fun createPlanWithSequence(
    profile: UserProfile,
    excludedExercises: Map<MuscleGroupSequence, Set<String>> = emptyMap()
): WorkoutPlan {
    val exerciseLibrary = exerciseLibraryRepository.getAllExercisesAsList()
    val days = mutableListOf<WorkoutDay>()
    val usedExercises = mutableMapOf<MuscleGroupSequence, MutableSet<String>>()
    
    val (sets, reps) = getSetsAndRepsForLevel(profile.level)
    val cardioDuration = getCardioDurationForGoal(profile.goal)
    
    val sequence = MuscleGroupSequence.FULL_SEQUENCE
    var dayIndex = 0
    
    for (cycleRound in 0 until 2) {
        for (groupSequence in sequence) {
            val excludedForGroup = excludedExercises[groupSequence] ?: emptySet()
            val alreadyUsedInCycle = usedExercises[groupSequence] ?: mutableSetOf()
            val allExcluded = excludedForGroup + alreadyUsedInCycle
            
            val exercises = exercisePoolManager.getExercisesForSequence(
                sequence = groupSequence,
                excludedExerciseNames = allExcluded,
                count = groupSequence.exercisesPerDay
            )
            
            val dayExercises = exercises.mapIndexed { index, libExercise ->
                createExerciseFromLibrary(
                    id = "${dayIndex}_${index}",
                    library = libExercise,
                    sets = if (groupSequence == MuscleGroupSequence.CARDIO) 1 else sets,
                    reps = if (groupSequence == MuscleGroupSequence.CARDIO) cardioDuration else reps,
                    profile = profile
                )
            }
            
            usedExercises.getOrPut(groupSequence) { mutableSetOf() }
                .addAll(exercises.map { it.name })
            
            days.add(WorkoutDay(
                id = dayIndex,
                dayName = "День ${dayIndex + 1}: ${groupSequence.displayName}",
                exercises = dayExercises,
                muscleGroups = groupSequence.muscleGroups.map { it.displayName }
            ))
            
            dayIndex++
        }
    }
    
    return WorkoutPlan(
        id = "sequenced_plan_${profile.goal}_${profile.level}",
        name = "${profile.goal}: ${profile.level}",
        description = "10-дневный план с анатомическим порядком групп мышц",
        muscleGroups = MuscleGroupSequence.FULL_SEQUENCE.map { it.displayName },
        goal = profile.goal,
        level = profile.level,
        days = days
    )
}
```

### Phase 3: Интеграция с CycleUseCase

**File**: `domain/usecase/CycleUseCase.kt`

Добавить загрузку и сохранение истории упражнений:

```kotlin
suspend fun initializeCycleForUser(username: String, profile: UserProfile): CycleState {
    // ... existing logic ...
    
    val exerciseHistory = cycleRepository.getExerciseHistory(username).first()
    val excludedExercises = buildExcludedExercisesMap(exerciseHistory)
    
    val plan = loadWorkoutPlan(username, profile, cycle, shouldCreateNewPlan, excludedExercises)
    
    // Save history after plan creation
    if (shouldCreateNewPlan) {
        val newHistory = createCycleExerciseHistory(cycle, plan)
        cycleRepository.saveExerciseHistory(username, newHistory)
    }
    
    // ...
}

private fun buildExcludedExercisesMap(history: List<CycleExerciseHistory>): Map<MuscleGroupSequence, Set<String>> {
    return history.lastOrNull()?.usedExercises ?: emptyMap()
}
```

---

## Validation Plan

### Unit Tests

**File**: `app/src/test/java/com/example/fitness_plan/domain/usecase/ExercisePoolManagerTest.kt`

```kotlin
@Test
fun `getExercisesForSequence returns correct muscle groups`() = runTest {
    // Arrange
    val mockLibrary = listOf(
        ExerciseLibrary(name = "Жим лёжа", muscleGroups = listOf(CHEST, TRICEPS), exerciseType = STRENGTH),
        ExerciseLibrary(name = "Бицепс", muscleGroups = listOf(BICEPS), exerciseType = STRENGTH),
        ExerciseLibrary(name = "Бег", muscleGroups = emptyList(), exerciseType = CARDIO)
    )
    
    // Act
    val result = poolManager.getExercisesForSequence(SHOULDERS_ARMS, emptySet(), 2)
    
    // Assert
    assertTrue(result.all { it.muscleGroups.any { mg -> mg in listOf(SHOULDERS, BICEPS, TRICEPS) } })
}

@Test
fun `getExercisesForSequence excludes specified exercises`() = runTest {
    // Act
    val result = poolManager.getExercisesForSequence(
        SHOULDERS_ARMS,
        excludedExerciseNames = setOf("Жим лёжа"),
        count = 10
    )
    
    // Assert
    assertFalse(result.any { it.name == "Жим лёжа" })
}

@Test
fun `determinePoolId rotates through pools A B C`() {
    assertEquals("pool_A", poolManager.determinePoolId(1))
    assertEquals("pool_B", poolManager.determinePoolId(2))
    assertEquals("pool_C", poolManager.determinePoolId(3))
    assertEquals("pool_A", poolManager.determinePoolId(4))
}
```

**File**: `app/src/test/java/com/example/fitness_plan/data/WorkoutRepositoryImplSequenceTest.kt`

```kotlin
@Test
fun `createPlanWithSequence creates 10 days with correct order`() = runTest {
    // Act
    val plan = repository.createPlanWithSequence(profile, emptyMap())
    
    // Assert
    assertEquals(10, plan.days.size)
    assertEquals("День 1: Плечи и Руки", plan.days[0].dayName)
    assertEquals("День 2: Грудь и Широчайшие", plan.days[1].dayName)
    assertEquals("День 3: Спина и Пресс", plan.days[2].dayName)
    assertEquals("День 4: Ноги", plan.days[3].dayName)
    assertEquals("День 5: Кардио", plan.days[4].dayName)
}

@Test
fun `createPlanWithSequence has no duplicate exercises in cycle`() = runTest {
    // Act
    val plan = repository.createPlanWithSequence(profile, emptyMap())
    
    // Assert
    val allExerciseNames = plan.days.flatMap { day -> 
        day.exercises.filter { it.exerciseType == STRENGTH }.map { it.name }
    }
    val uniqueNames = allExerciseNames.toSet()
    
    // All strength exercises should be unique (or close to it)
    assertTrue(uniqueNames.size >= allExerciseNames.size * 0.8)
}

@Test
fun `createPlanWithSequence includes cardio on days 5 and 10`() = runTest {
    // Act
    val plan = repository.createPlanWithSequence(profile, emptyMap())
    
    // Assert
    assertTrue(plan.days[4].exercises.all { it.exerciseType == CARDIO })
    assertTrue(plan.days[9].exercises.all { it.exerciseType == CARDIO })
}
```

### Manual Testing

1. **Тест 1: Порядок групп мышц**
   - Создать новый план
   - Проверить названия дней: "Плечи и Руки", "Грудь и Широчайшие", etc.
   - Проверить упражнения в каждом дне соответствуют группе

2. **Тест 2: Уникальность упражнений**
   - Создать план
   - Выписать все упражнения
   - Проверить отсутствие дублей силовых

3. **Тест 3: Кардио дни**
   - Проверить что День 5 и День 10 содержат только кардио
   - Проверить длительность кардио по цели

4. **Тест 4: Ротация между циклами**
   - Завершить цикл 1
   - Начать цикл 2
   - Сравнить упражнения - должно быть минимум 60% разных

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Недостаточно упражнений в библиотеке | High | Medium | Добавить больше упражнений, разрешить повторы при нехватке |
| Нарушение обратной совместимости | Medium | High | Миграция существующих планов, fallback на старую логику |
| Сложность тестирования | Medium | Medium | Комплексные unit и integration тесты |
| Пользовательское непонимание новой структуры | Low | Low | Обновить UI, добавить подсказки |

---

## Dependencies

- **Depends on**: 
  - REQ-001 (уникальность упражнений - частично реализовано)
  - REQ-002 (сохранение плана - реализовано)
- **Blocks**: Нет
- **Related to**:
  - ExerciseLibraryRepository (нужны методы фильтрации)
  - CycleRepository (хранение истории упражнений)

---

## Metrics

### Success Metrics

1. **Уникальность упражнений**: 100% уникальных силовых упражнений в цикле (или 80%+ при нехватке пула)
2. **Ротация пулов**: 60%+ разных упражнений между циклами
3. **Кардио дни**: 2 кардио дня на 10-дневный цикл
4. **Пользовательское удовлетворение**: NPS > 8

### How to Verify

```kotlin
// Проверка в тестах:
// 1. plan.days.map { it.dayName } == ["День 1: Плечи и Руки", ...]
// 2. allExercises.distinct().size == allExercises.size
// 3. plan.days[4].exercises.all { it.exerciseType == CARDIO }
// 4. history[cycle2].usedExercises intersect history[cycle1].usedExercises < 40%
```

---

## Definition of Done

- [ ] Создан enum MuscleGroupSequence
- [ ] Создан класс ExercisePoolManager
- [ ] Создана модель CycleExerciseHistory
- [ ] Рефакторинг WorkoutRepositoryImpl.createPlanWithSequence()
- [ ] Интеграция с CycleUseCase
- [ ] Добавлены unit тесты (>90% coverage)
- [ ] Добавлены integration тесты
- [ ] Обновлена документация
- [ ] Manual тестирование выполнено
- [ ] Code review пройден

---

## Notes

### Обратная совместимость

При запуске с существующими пользователями:
1. Проверить наличие CycleExerciseHistory
2. Если нет - создать пустую историю
3. Использовать старый план до завершения цикла
4. Новый план создаётся по новой логике

### Альтернативные варианты

Если пользователь хочет старую структуру:
- Добавить настройку "Тип плана: Классический / Анатомический"
- Сохранять предпочтение в UserProfile

---

**Version History:**
- v1.0 (2026-02-17): Initial requirements document
