# REQ-010: Исправление открытия экрана упражнения во вкладке "Мой план"

**Status**: New
**Priority**: High
**Complexity**: Simple
**Type**: Bug Fix
**Created**: 2026-02-17

---

## Description

При выборе упражнения в дне тренировки во вкладке "Мой план" не открывается экран упражнения, в то время как во вкладке "Автоматический план" всё работает корректно.

---

## User Story

Как пользователь приложения,
Я хочу, чтобы при нажатии на упражнение во вкладке "Мой план" открывался экран упражнения,
Чтобы я мог записать результаты тренировки и отметить выполнение упражнения.

---

## Current State (Фактическое поведение)

1. Пользователь переходит во вкладку "Мой план"
2. Раскрывает день тренировки
3. Нажимает на упражнение
4. Открывается экран `ExerciseDetailScreen`, но:
   - Упражнение не находится в плане
   - Экран зависает в состоянии загрузки (`CircularProgressIndicator`)

---

## Expected Behavior (Ожидаемое поведение)

1. Пользователь переходит во вкладку "Мой план"
2. Раскрывает день тренировки
3. Нажимает на упражнение
4. Открывается экран `ExerciseDetailScreen` с корректными данными упражнения
5. Пользователь может записать результаты и отметить выполнение

---

## Root Cause Analysis

### Проблема в файле: `ExerciseDetailScreen.kt:66`

```kotlin
LaunchedEffect(decodedName, currentWorkoutPlan, adminWorkoutPlan, isAdmin) {
    val workoutPlan = if (isAdmin) adminWorkoutPlan else currentWorkoutPlan
    // ...
}
```

**Суть проблемы:**
- `ExerciseDetailScreen` при поиске упражнения проверяет только `currentWorkoutPlan` (Автоматический план) или `adminWorkoutPlan`
- `userWorkoutPlan` (Мой план) **никогда не проверяется**
- Когда пользователь кликает на упражнение из своего кастомного плана, система пытается найти это упражнение в автоматическом плане
- Если упражнение не найдено, `selectedExercise` остаётся `null`
- UI показывает бесконечный индикатор загрузки

### Связанный код

`WorkoutViewModel.kt:78-79`:
```kotlin
private val _userWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
val userWorkoutPlan: StateFlow<WorkoutPlan?> = _userWorkoutPlan.asStateFlow()
```

`HomeScreen.kt:140`:
```kotlin
val userWorkoutPlan by viewModel.userWorkoutPlan.collectAsState()
```

---

## Affected Components

| Компонент | Файл | Влияние |
|-----------|------|---------|
| ExerciseDetailScreen | `ui/ExerciseDetailScreen.kt` | Основное изменение |
| WorkoutViewModel | `presentation/viewmodel/WorkoutViewModel.kt` | Добавить exposing selectedPlanType |
| HomeScreen | `ui/HomeScreen.kt` | Передать информацию о типе плана |

---

## Technical Requirements

### Решение 1: Передать тип выбранного плана в ExerciseDetailScreen (Рекомендуется)

**Изменения в `ExerciseDetailScreen.kt`:**

1. Добавить параметр `planType: SelectedPlanType`:
```kotlin
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    dayIndex: Int = -1,
    onBackClick: () -> Unit,
    workoutViewModel: WorkoutViewModel,
    isAdmin: Boolean = false,
    planType: SelectedPlanType = SelectedPlanType.AUTO  // Новый параметр
)
```

2. Изменить логику выбора плана:
```kotlin
val userWorkoutPlan by workoutViewModel.userWorkoutPlan.collectAsState()

LaunchedEffect(decodedName, currentWorkoutPlan, adminWorkoutPlan, userWorkoutPlan, isAdmin, planType) {
    val workoutPlan = when {
        isAdmin -> adminWorkoutPlan
        planType == SelectedPlanType.CUSTOM -> userWorkoutPlan
        else -> currentWorkoutPlan
    }
    // ...
}
```

**Изменения в `MainActivity.kt`:**

1. Добавить аргумент `planType` в маршрут навигации:
```kotlin
composable(
    route = "exercise_detail/{exerciseName}/{dayIndex}/{planType}",
    arguments = listOf(
        navArgument("exerciseName") { type = NavType.StringType },
        navArgument("dayIndex") { type = NavType.IntType; defaultValue = -1 },
        navArgument("planType") { type = NavType.StringType; defaultValue = "AUTO" }
    )
)
```

2. Передать `planType` при навигации из `HomeScreen`.

**Изменения в `HomeScreen.kt`:**

1. Добавить `selectedPlanType` в параметры `onExerciseClick`:
```kotlin
onExerciseClick: (Exercise, Int, SelectedPlanType) -> Unit
```

2. Передавать текущий `selectedPlanType` при клике на упражнение.

**Изменения в `MainScreen.kt` и `MainActivity.kt`:**

1. Обновить сигнатуры callback-функций.

---

### Решение 2: Получить план напрямую из ViewModel (Альтернатива)

Вместо передачи `planType` через навигацию, можно получить `selectedPlanType` напрямую из `WorkoutViewModel` в `ExerciseDetailScreen`.

**Изменения в `ExerciseDetailScreen.kt`:**
```kotlin
val selectedPlanType by workoutViewModel.selectedPlanType.collectAsState()
val userWorkoutPlan by workoutViewModel.userWorkoutPlan.collectAsState()

LaunchedEffect(...) {
    val workoutPlan = when {
        isAdmin -> adminWorkoutPlan
        selectedPlanType == SelectedPlanType.CUSTOM -> userWorkoutPlan
        else -> currentWorkoutPlan
    }
    // ...
}
```

**Преимущества Решения 2:**
- Меньше изменений в навигации
- Не нужно модифицировать маршрут

**Недостатки Решения 2:**
- Зависимость от состояния ViewModel, которое может не синхронизироваться при глубокой навигации

---

## Recommended Approach

**Решение 2** рекомендуется как более простое и с меньшим риском регрессии.

---

## Acceptance Criteria

- [ ] При клике на упражнение во вкладке "Мой план" открывается экран упражнения
- [ ] На экране упражнения отображаются корректные данные (название, подходы, повторения)
- [ ] Можно сохранить результаты тренировки для упражнения из "Мой план"
- [ ] Можно отметить упражнение как выполненное
- [ ] При клике на упражнение во вкладке "Автоматический план" поведение остаётся прежним (регрессия отсутствует)
- [ ] Админ-панель продолжает работать корректно

---

## Validation Plan

### Unit Tests
- Проверить что `ExerciseDetailScreen` корректно выбирает план на основе `selectedPlanType`

### Manual Testing
1. Создать план во вкладке "Мой план"
2. Добавить день и упражнения
3. Кликнуть на упражнение
4. Проверить что экран упражнения открылся с корректными данными
5. Записать результат подхода
6. Отметить упражнение как выполненное
7. Вернуться и проверить что статус сохранился

### Edge Cases
- [ ] Пользователь кликает на упражнение, которое существует в обоих планах
- [ ] Пользователь переключает тип плана и сразу кликает на упражнение
- [ ] Упражнение добавлено в "Мой план", но отсутствует в библиотеке

---

## Impact Assessment

| Критерий | Оценка |
|----------|--------|
| Критичность | High - Функциональность полностью неработоспособна |
| Влияние на пользователей | High - Пользователи не могут работать со своим планом |
| Риск регрессии | Low - Изменения локализованы |
| Сложность исправления | Simple - 1-2 файла, ~20 строк кода |

---

## Dependencies

- Зависит от: Нет
- Блокирует: Нет
- Связано с: REQ-009-MyPlanRedesign

---

## Estimated Effort

- Анализ: 30 мин (выполнено)
- Разработка: 1 час
- Тестирование: 30 мин
- **Итого: ~2 часа**

---

## Implementation Notes

1. Начать с Решения 2 - оно проще и не требует изменений в навигации
2. Добавить `userWorkoutPlan` и `selectedPlanType` в `ExerciseDetailScreen`
3. Обновить логику выбора плана в `LaunchedEffect`
4. Протестировать оба типа планов

---

## Metrics

**Success Metrics:**
- Успешное открытие экрана упражнения: 100% для "Мой план"
- Время загрузки экрана упражнения: < 1 сек

---

## Notes

- Баг был обнаружен в процессе тестирования функциональности "Мой план"
- Схожая логика выбора плана может понадобиться в других экранах в будущем
