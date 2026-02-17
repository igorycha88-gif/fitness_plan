# REQ-002: Исправление сохранения и стабильности плана тренировок

**Status**: New
**Priority**: Critical
**Complexity**: Medium
**Created**: 2026-02-17
**Related**: REQ-001-FixExerciseRepetition.md

---

## Description

Исправление критической проблемы: упражнения в дне тренировки меняются при повторном входе в тот же день. План тренировок не сохраняется при создании и не загружается при повторном входе в приложение.

---

## User Story

**As a** Пользователь приложения,
**I want** Чтобы упражнения в дне тренировки оставались неизменными при повторном входе,
**So that** Я мог следовать плану тренировок и отслеживать свой прогресс.

---

## Current State Analysis

### Выявленные проблемы

#### Проблема 1: План не сохраняется при создании
**Location**: `CycleUseCase.kt:74-85`

```kotlin
private suspend fun loadWorkoutPlan(profile: UserProfile, cycle: Cycle?): WorkoutPlan {
    val basePlan = workoutRepository.getWorkoutPlanForUser(profile)
    val planCycle = workoutRepository.getCycleWorkoutPlan(basePlan, profile.frequency)
    val dates = workoutRepository.generateCycleDates(...)
    val finalPlan = workoutRepository.getWorkoutPlanWithDates(planCycle, dates)
    return applyWeightProgression(finalPlan, cycleNumber)  // План возвращается БЕЗ сохранения!
}
```

**Impact**: После создания план существует только в памяти и теряется при перезапуске приложения.

#### Проблема 2: План не загружается при повторном входе
**Location**: `CycleUseCase.kt:32-72`

```kotlin
suspend fun initializeCycleForUser(username: String, profile: UserProfile): CycleState {
    val cycle = when {
        completedDate != null -> { ... }  // Новый цикл
        currentCycle == null -> { ... }   // Новый цикл
        else -> currentCycle              // Существующий цикл
    }
    
    val plan = loadWorkoutPlan(profile, cycle)  // Всегда генерируется НОВЫЙ план!
}
```

**Impact**: Даже при существующем активном цикле план генерируется заново при каждом входе.

#### Проблема 3: Единственная точка сохранения
**Location**: `WeightProgressionUseCase.kt:76`

План сохраняется только после применения адаптивной прогрессии весов (после завершения 5 дней).

**Impact**: 
- План не сохраняется при первом создании
- План не сохраняется при обычном входе в приложение
- Теряются все данные между сессиями до завершения микроцикла

### Data Flow Diagram (Current - Broken)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CURRENT FLOW (BROKEN)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  User enters app                                                             │
│        │                                                                     │
│        ▼                                                                     │
│  WorkoutViewModel.initializeWorkout()                                        │
│        │                                                                     │
│        ▼                                                                     │
│  CycleUseCase.initializeCycleForUser(username, profile)                      │
│        │                                                                     │
│        ├──► Check cycle state (from DataStore)                               │
│        │                                                                    │
│        ▼                                                                     │
│  loadWorkoutPlan(profile, cycle)  ◄── ALWAYS GENERATES NEW PLAN              │
│        │                                                                     │
│        ├──► getWorkoutPlanForUser(profile)     ──► New base plan             │
│        ├──► getCycleWorkoutPlan(basePlan)      ──► New 10-day plan           │
│        ├──► generateCycleDates()               ──► New dates                 │
│        └──► applyWeightProgression()           ──► New weights               │
│        │                                                                     │
│        ▼                                                                     │
│  Return plan (NOT SAVED TO DATASTORE!)                                       │
│        │                                                                     │
│        ▼                                                                     │
│  User sees plan                                                              │
│        │                                                                     │
│        ▼                                                                     │
│  User exits app ──► PLAN LOST!                                               │
│                                                                              │
│  Next entry:                                                                 │
│        │                                                                     │
│        ▼                                                                     │
│  Same process ──► DIFFERENT PLAN GENERATED                                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Expected State

### Desired Data Flow (Fixed)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FIXED FLOW                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  User enters app                                                             │
│        │                                                                     │
│        ▼                                                                     │
│  WorkoutViewModel.initializeWorkout()                                        │
│        │                                                                     │
│        ▼                                                                     │
│  CycleUseCase.initializeCycleForUser(username, profile)                      │
│        │                                                                     │
│        ├──► Check cycle state                                                │
│        │                                                                    │
│        ▼                                                                     │
│  ┌─────────────────────────────────────────────┐                             │
│  │ DOES SAVED PLAN EXIST IN DATASTORE?         │                             │
│  └─────────────────────────────────────────────┘                             │
│        │                                                                     │
│        │ YES                                    │ NO                          │
│        ▼                                         ▼                            │
│  ┌──────────────────┐                   ┌──────────────────┐                  │
│  │ Load from        │                   │ Generate NEW     │                  │
│  │ DataStore        │                   │ plan             │                  │
│  └──────────────────┘                   └──────────────────┘                  │
│        │                                         │                            │
│        │                                         ▼                            │
│        │                                ┌──────────────────┐                   │
│        │                                │ SAVE to DataStore│                   │
│        │                                └──────────────────┘                   │
│        │                                         │                            │
│        └─────────────────────────────────────────┘                            │
│                           │                                                  │
│                           ▼                                                  │
│                    Return plan                                               │
│                           │                                                  │
│                           ▼                                                  │
│                    User sees plan                                            │
│                                                                              │
│  Next entry:                                                                 │
│        │                                                                     │
│        ▼                                                                     │
│  Load from DataStore ──► SAME PLAN!                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Acceptance Criteria

### AC1: Сохранение плана при создании
- **Given** Пользователь впервые входит в приложение (или начат новый цикл)
- **When** Система создаёт новый 10-дневный план тренировок
- **Then** План немедленно сохраняется в DataStore с ключом `{username}_workout_plan`
- **And** Сохранение происходит до возврата плана пользователю
- **And** При ошибке сохранения логируется ошибка

### AC2: Загрузка плана при повторном входе
- **Given** В DataStore существует сохранённый план для пользователя
- **And** Цикл не завершён (нет completedDate)
- **When** Пользователь входит в приложение
- **Then** План загружается из DataStore (а не генерируется заново)
- **And** Упражнения в каждом дне идентичны предыдущему входу

### AC3: Стабильность упражнений при повторном входе
- **Given** Пользователь вошёл в "День 1" и увидел упражнения [A, B, C, D]
- **When** Пользователь выходит и снова входит в "День 1"
- **Then** Отображаются те же упражнения [A, B, C, D] в том же порядке
- **And** Сохраняются все параметры: подходы, повторения, веса

### AC4: Создание нового плана при завершении цикла
- **Given** Пользователь завершил 10-дневный цикл (completedDate установлен)
- **When** Пользователь входит в приложение
- **Then** Старый план очищается
- **And** Создаётся новый 10-дневный план
- **And** Новый план сохраняется в DataStore

### AC5: Обработка ошибок DataStore
- **Given** При загрузке плана из DataStore произошла ошибка (коррупция, null)
- **When** Система пытается загрузить план
- **Then** Логируется ошибка
- **And** Создаётся новый план
- **And** Новый план сохраняется в DataStore
- **And** Пользователь не видит ошибку (graceful degradation)

---

## Technical Requirements

### Components Affected

| Component | Change Type | Description |
|-----------|-------------|-------------|
| `CycleUseCase.kt` | Modify | Добавить логику загрузки/сохранения плана |
| `WorkoutRepository.kt` | No change | Интерфейс уже содержит нужные методы |
| `WorkoutRepositoryImpl.kt` | No change | Реализация уже существует |
| `WorkoutViewModel.kt` | No change | Использует CycleUseCase |

### Architecture Impact

Изменения только в слое **domain/usecase** - CycleUseCase. Clean Architecture не нарушается.

### Required Changes in CycleUseCase.kt

**Файл**: `app/src/main/java/com/example/fitness_plan/domain/usecase/CycleUseCase.kt`

**Текущий код** (строки 74-85):
```kotlin
private suspend fun loadWorkoutPlan(profile: UserProfile, cycle: Cycle?): WorkoutPlan {
    val basePlan = workoutRepository.getWorkoutPlanForUser(profile)
    val planCycle = workoutRepository.getCycleWorkoutPlan(basePlan, profile.frequency)
    val dates = workoutRepository.generateCycleDates(
        cycle?.startDate ?: System.currentTimeMillis(),
        profile.frequency
    )
    val finalPlan = workoutRepository.getWorkoutPlanWithDates(planCycle, dates)
    
    val cycleNumber = cycle?.cycleNumber ?: 1
    return applyWeightProgression(finalPlan, cycleNumber)
}
```

**Изменённый код**:
```kotlin
private suspend fun loadWorkoutPlan(
    username: String,
    profile: UserProfile,
    cycle: Cycle?,
    shouldCreateNewPlan: Boolean
): WorkoutPlan {
    if (!shouldCreateNewPlan) {
        val savedPlan = workoutRepository.getWorkoutPlan(username).first()
        if (savedPlan != null && savedPlan.days.size == Cycle.DAYS_IN_CYCLE) {
            Log.d(TAG, "Loaded existing plan from DataStore for user $username")
            return savedPlan
        }
    }
    
    Log.d(TAG, "Creating new plan for user $username")
    val basePlan = workoutRepository.getWorkoutPlanForUser(profile)
    val planCycle = workoutRepository.getCycleWorkoutPlan(basePlan, profile.frequency)
    val dates = workoutRepository.generateCycleDates(
        cycle?.startDate ?: System.currentTimeMillis(),
        profile.frequency
    )
    val finalPlan = workoutRepository.getWorkoutPlanWithDates(planCycle, dates)
    
    val cycleNumber = cycle?.cycleNumber ?: 1
    val planWithProgression = applyWeightProgression(finalPlan, cycleNumber)
    
    workoutRepository.saveWorkoutPlan(username, planWithProgression)
    Log.d(TAG, "New plan saved to DataStore for user $username")
    
    return planWithProgression
}
```

**Изменения в initializeCycleForUser** (строки 32-72):
```kotlin
suspend fun initializeCycleForUser(username: String, profile: UserProfile): CycleState {
    Log.d(TAG, "initializeCycleForUser: username=$username")
    
    val completedDate = cycleRepository.getCompletedDate(username)
    val currentCycle = cycleRepository.getCurrentCycleSync(username)
    val now = System.currentTimeMillis()
    
    var shouldCreateNewPlan = false
    
    val cycle = when {
        completedDate != null -> {
            Log.d(TAG, "Completing old cycle and starting new one")
            cycleRepository.resetCycle(username)
            exerciseCompletionRepository.clearCompletion(username)
            shouldCreateNewPlan = true
            cycleRepository.startNewCycle(username, now)
        }
        currentCycle == null -> {
            Log.d(TAG, "Starting new cycle")
            shouldCreateNewPlan = true
            cycleRepository.startNewCycle(username, now)
        }
        currentCycle.totalDays != Cycle.DAYS_IN_CYCLE -> {
            Log.d(TAG, "Migrating old cycle to new format")
            cycleRepository.markCycleCompleted(username, now)
            cycleRepository.resetCycle(username)
            exerciseCompletionRepository.clearCompletion(username)
            shouldCreateNewPlan = true
            cycleRepository.startNewCycle(username, now)
        }
        else -> {
            Log.d(TAG, "Using existing cycle ${currentCycle.cycleNumber}")
            shouldCreateNewPlan = false
            currentCycle
        }
    }
    
    val plan = loadWorkoutPlan(username, profile, cycle, shouldCreateNewPlan)
    
    val history = cycleRepository.getCycleHistory(username).first()
    
    return CycleState(cycle, plan, history)
}
```

---

## Implementation Notes

### Порядок реализации

1. **Изменить сигнатуру loadWorkoutPlan()**
   - Добавить параметры: `username: String`, `shouldCreateNewPlan: Boolean`

2. **Добавить проверку сохранённого плана**
   - В начале loadWorkoutPlan проверить `shouldCreateNewPlan`
   - Если false - попытаться загрузить план из DataStore

3. **Добавить сохранение плана**
   - После генерации нового плана вызвать `saveWorkoutPlan()`

4. **Обновить initializeCycleForUser()**
   - Определить флаг `shouldCreateNewPlan` на основе состояния цикла
   - Передать username в loadWorkoutPlan

### Проверка валидности плана

При загрузке плана из DataStore проверять:
- `savedPlan != null`
- `savedPlan.days.size == Cycle.DAYS_IN_CYCLE` (10 дней)

### Логирование

Добавить логи для отладки:
- При загрузке плана из DataStore
- При создании нового плана
- При сохранении плана
- При ошибках

---

## Validation Plan

### Unit Tests

**File**: `app/src/test/java/com/example/fitness_plan/domain/usecase/CycleUseCaseTest.kt`

```kotlin
@Test
fun `initializeCycleForUser loads saved plan when cycle exists`() = runTest {
    // Arrange
    val username = "testuser"
    val savedPlan = createTestPlan()
    coEvery { mockWorkoutRepository.getWorkoutPlan(username) } returns flowOf(savedPlan)
    coEvery { mockCycleRepository.getCurrentCycleSync(username) } returns existingCycle
    
    // Act
    val result = cycleUseCase.initializeCycleForUser(username, profile)
    
    // Assert
    assertEquals(savedPlan, result.workoutPlan)
    coVerify(exactly = 0) { mockWorkoutRepository.getWorkoutPlanForUser(any()) }
}

@Test
fun `initializeCycleForUser creates and saves new plan when no cycle exists`() = runTest {
    // Arrange
    val username = "testuser"
    coEvery { mockCycleRepository.getCurrentCycleSync(username) } returns null
    coEvery { mockWorkoutRepository.getWorkoutPlan(username) } returns flowOf(null)
    
    // Act
    val result = cycleUseCase.initializeCycleForUser(username, profile)
    
    // Assert
    assertNotNull(result.workoutPlan)
    coVerify { mockWorkoutRepository.saveWorkoutPlan(username, result.workoutPlan!!) }
}

@Test
fun `initializeCycleForUser creates new plan after cycle completion`() = runTest {
    // Arrange
    val username = "testuser"
    coEvery { mockCycleRepository.getCompletedDate(username) } returns System.currentTimeMillis()
    val oldPlan = createTestPlan()
    coEvery { mockWorkoutRepository.getWorkoutPlan(username) } returns flowOf(oldPlan)
    
    // Act
    val result = cycleUseCase.initializeCycleForUser(username, profile)
    
    // Assert
    assertNotEquals(oldPlan, result.workoutPlan)
    coVerify { mockWorkoutRepository.saveWorkoutPlan(username, result.workoutPlan!!) }
}
```

### Manual Testing

1. **Тест 1: Первый вход**
   - Установить приложение
   - Создать профиль
   - Запомнить упражнения в "День 1"
   - Закрыть приложение
   - Открыть приложение
   - Проверить: упражнения те же

2. **Тест 2: Многократный вход**
   - Войти в приложение 5 раз подряд
   - Каждый раз проверять упражнения в "День 1"
   - Ожидаемый результат: упражнения идентичны

3. **Тест 3: После завершения цикла**
   - Завершить все 10 дней
   - Перезайти в приложение
   - Проверить: создан новый план (упражнения могут отличаться)

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| План не загружается из DataStore | Low | High | Проверка null, создание нового плана |
| Ошибка при сохранении плана | Low | High | Try-catch, логирование, повторная попытка |
| Race condition при сохранении | Low | Medium | Синхронизация на уровне DataStore |
| Миграция существующих пользователей | Medium | Low | При отсутствии плана создаётся новый |

---

## Dependencies

- **Depends on**: REQ-001 (логика уникальности уже реализована детерминированно)
- **Blocks**: Нет
- **Related**: 
  - `WorkoutRepositoryImpl.kt` - методы save/get уже существуют
  - `WeightProgressionUseCase.kt` - также использует saveWorkoutPlan

---

## Metrics

### Success Metrics

1. **Стабильность плана**: 100% идентичность упражнений при повторном входе
2. **Время загрузки**: < 500ms для загрузки плана из DataStore
3. **Покрытие тестами**: > 90% для изменённого кода

### How to Verify

```kotlin
// Проверка в логах:
// 1. При первом входе: "Creating new plan for user X" + "New plan saved to DataStore"
// 2. При повторном входе: "Loaded existing plan from DataStore for user X"
// 3. При завершении цикла: "Creating new plan for user X"
```

---

## Notes

### Почему это работает

Упражнения в WorkoutRepositoryImpl детерминированы:
- Используются фиксированные списки упражнений
- Выбор происходит по индексу: `exerciseList[index]`
- Нет использования `shuffled()` или `random()`

Проблема была только в том, что план не сохранялся и каждый раз генерировался заново.

### Важно НЕ менять

- Логику выбора упражнений в WorkoutRepositoryImpl - она уже детерминирована
- Сигнатуры WorkoutRepository - они уже содержат нужные методы
- Механизм завершения цикла - он работает корректно

---

## Definition of Done

- [ ] CycleUseCase.loadWorkoutPlan() загружает план из DataStore при наличии
- [ ] CycleUseCase.loadWorkoutPlan() сохраняет план при создании нового
- [ ] CycleUseCase.initializeCycleForUser() передаёт username и флаг создания
- [ ] Unit тесты написаны и проходят
- [ ] Manual тестирование выполнено
- [ ] Документация обновлена
- [ ] Code review пройден

---

**Version History:**
- v1.0 (2026-02-17): Initial requirements document
