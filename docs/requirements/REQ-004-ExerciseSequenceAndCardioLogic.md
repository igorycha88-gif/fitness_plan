# REQ-004: Корректировка порядка групп мышц и логики кардио

**Status**: New
**Priority**: High
**Complexity**: Medium
**Created**: 2026-02-17
**Updated**: 2026-02-17
**Related**: REQ-003-RefactorPlanGenerationLogic.md
**Replaces**: Частично уточняет REQ-003

---

## Description

Уточнение и корректировка требований REQ-003 на основе конкретных требований заказчика:
1. Изменение порядка групп мышц: **Руки → Плечи → Грудь → Спина → Ноги** (сверху вниз)
2. Структура цикла: **4 силовых занятия + 1 день кардио**
3. Гарантия отсутствия повторений упражнений в течение 10-дневного цикла
4. Ротация упражнений между циклами

---

## User Story

**As a** Пользователь приложения,
**I want** План тренировок формировался от верхних мышц к нижним (руки → плечи → грудь → спина → ноги), с днём кардио после полного цикла силовых, и без повторения упражнений,
**So that** Я мог логично и эффективно тренировать всё тело, следуя анатомическому порядку с правильным восстановлением.

---

## Current State vs Desired State

### Current State (по результатам анализа кода)

**File**: `MuscleGroupSequence.kt`

```kotlin
enum class MuscleGroupSequence(...) {
    SHOULDERS_ARMS("Плечи и Руки", listOf(SHOULDERS, BICEPS, TRICEPS), 4),
    CHEST_LATS("Грудь и Широчайшие", listOf(CHEST, LATS), 4),
    BACK_CORE("Спина и Пресс", listOf(TRAPS, LOWER_BACK, ABS), 4),
    LEGS("Ноги", listOf(QUADS, HAMSTRINGS, GLUTES, CALVES), 4),
    CARDIO("Кардио", emptyList(), 2);

    companion object {
        val FULL_SEQUENCE = listOf(SHOULDERS_ARMS, CHEST_LATS, BACK_CORE, LEGS, CARDIO)
    }
}
```

**Проблемы:**
1. Порядок: Плечи+Руки объединены, нет отдельного дня для Рук
2. Порядок не соответствует требованию "Руки → Плечи → Грудь → Спина → Ноги"
3. Кардио каждый 5-й день, а не после полного цикла 4 силовых

### Desired State

**Новый порядок групп мышц (сверху вниз):**

| День | Группа мышц | Название дня | Кол-во упражнений |
|------|-------------|--------------|-------------------|
| 1 | Руки (Бицепс + Трицепс) | Руки | 4 |
| 2 | Плечи | Плечи | 4 |
| 3 | Грудь | Грудь | 4 |
| 4 | Спина + Пресс | Спина и Пресс | 4 |
| 5 | Кардио | Кардио | 2-3 |
| 6 | Руки (другие упражнения) | Руки | 4 |
| 7 | Плечи (другие упражнения) | Плечи | 4 |
| 8 | Грудь (другие упражнения) | Грудь | 4 |
| 9 | Спина + Пресс (другие упражнения) | Спина и Пресс | 4 |
| 10 | Кардио | Кардио | 2-3 |

---

## Gap Analysis

### Gap 1: Неверный порядок групп мышц

**Type**: Functional
**Current State**: SHOULDERS_ARMS объединяет плечи и руки в один день
**Expected State**: Отдельные дни для Рук и Плечей
**Impact**: Пользователь не может тренировать руки и плечи в разные дни
**Severity**: High

**Evidence**: `MuscleGroupSequence.kt:8`

### Gap 2: Порядок не "сверху вниз"

**Type**: Functional
**Current State**: Порядок Плечи+Руки → Грудь+Широчайшие → Спина+Пресс → Ноги
**Expected State**: Руки → Плечи → Грудь → Спина → Ноги
**Impact**: Нарушена анатомическая логика "от верха к низу"
**Severity**: Medium

### Gap 3: Нет гарантии уникальности в 10-дневном цикле

**Type**: Functional
**Current State**: `getWorkoutPlanWithSequence` использует `usedExercisesInCycle`, но может повторять упражнения если пул исчерпан
**Expected State**: 100% уникальные упражнения в цикле (или ближайшие альтернативы)
**Impact**: Пользователь видит одинаковые упражнения
**Severity**: High

---

## Technical Requirements

### 1. Изменение MuscleGroupSequence

**File**: `domain/model/MuscleGroupSequence.kt`

**Current:**
```kotlin
enum class MuscleGroupSequence(...) {
    SHOULDERS_ARMS("Плечи и Руки", listOf(SHOULDERS, BICEPS, TRICEPS), 4),
    CHEST_LATS("Грудь и Широчайшие", listOf(CHEST, LATS), 4),
    BACK_CORE("Спина и Пресс", listOf(TRAPS, LOWER_BACK, ABS), 4),
    LEGS("Ноги", listOf(QUADS, HAMSTRINGS, GLUTES, CALVES), 4),
    CARDIO("Кардио", emptyList(), 2);
}
```

**Proposed:**
```kotlin
enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    ARMS("Руки", listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS), 4),
    SHOULDERS("Плечи", listOf(MuscleGroup.SHOULDERS), 4),
    CHEST("Грудь", listOf(MuscleGroup.CHEST), 4),
    BACK_CORE("Спина и Пресс", listOf(MuscleGroup.LATS, MuscleGroup.TRAPS, MuscleGroup.LOWER_BACK, MuscleGroup.ABS), 4),
    LEGS("Ноги", listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES), 4),
    CARDIO("Кардио", emptyList(), 2);

    companion object {
        val STRENGTH_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST, BACK_CORE, LEGS)
        val FULL_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST, BACK_CORE, LEGS, CARDIO)
    }
}
```

**Note**: Если оставить 5 силовых групп + кардио = 6 дней на раунд, то в 10-дневном цикле не помещается 2 полных раунда.

**Alternative** (4 силовых + 1 кардио):
```kotlin
enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    ARMS("Руки", listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS), 4),
    SHOULDERS_CHEST("Плечи и Грудь", listOf(MuscleGroup.SHOULDERS, MuscleGroup.CHEST), 4),
    BACK_CORE("Спина и Пресс", listOf(MuscleGroup.LATS, MuscleGroup.TRAPS, MuscleGroup.LOWER_BACK, MuscleGroup.ABS), 4),
    LEGS("Ноги", listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES), 4),
    CARDIO("Кардио", emptyList(), 2);

    companion object {
        val STRENGTH_SEQUENCE = listOf(ARMS, SHOULDERS_CHEST, BACK_CORE, LEGS)
        val FULL_SEQUENCE = listOf(ARMS, SHOULDERS_CHEST, BACK_CORE, LEGS, CARDIO)
    }
}
```

**Final Recommendation** (по требованию пользователя - 4 силовых + 1 кардио):

```kotlin
enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    ARMS("Руки", listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS), 4),
    SHOULDERS("Плечи", listOf(MuscleGroup.SHOULDERS), 4),
    CHEST_BACK("Грудь и Спина", listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.TRAPS), 4),
    LEGS_CORE("Ноги и Пресс", listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES, MuscleGroup.ABS), 4),
    CARDIO("Кардио", emptyList(), 2);

    companion object {
        val STRENGTH_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST_BACK, LEGS_CORE)
        val FULL_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST_BACK, LEGS_CORE, CARDIO)
    }
}
```

### 2. Обновление WorkoutRepositoryImpl

**File**: `data/WorkoutRepositoryImpl.kt`

Метод `getWorkoutPlanWithSequence()` (строки 1264-1328):

**Changes Required:**
1. Использовать новую последовательность `MuscleGroupSequence.FULL_SEQUENCE`
2. Генерировать 10 дней: 2 раунда по (4 силовых + 1 кардио)
3. Гарантировать уникальность упражнений в рамках цикла через `usedExercisesInCycle`

### 3. Обновление ExercisePoolManager

**File**: `domain/usecase/ExercisePoolManager.kt`

Метод `getExercisesForSequence()`:

**Changes Required:**
1. Учитывать новые группы мышц из `MuscleGroupSequence`
2. При нехватке упражнений - возвращать ближайшие альтернативы

---

## Acceptance Criteria

### AC1: Порядок групп мышц "сверху вниз"
- **Given** Пользователь создаёт новый план тренировок
- **When** Система формирует 10-дневный план
- **Then** Дни следуют в порядке: Руки → Плечи → Грудь/Спина → Ноги → Кардио → Руки → Плечи → Грудь/Спина → Ноги → Кардио
- **And** Названия дней отражают группы мышц

### AC2: Уникальность упражнений в цикле
- **Given** Создаётся новый 10-дневный план
- **When** Система выбирает упражнения для каждого дня
- **Then** Каждое силовое упражнение встречается **максимум 1 раз** в цикле
- **And** Кардио упражнения могут повторяться (отдельный пул)

### AC3: День кардио после 4 силовых
- **Given** Формируется 10-дневный план
- **When** Пройдены все 4 силовых дня (дни 1-4)
- **Then** День 5 является днём кардио
- **And** День 10 также является днём кардио
- **And** Кардио день содержит 2-3 упражнения

### AC4: Ротация упражнений между циклами
- **Given** Пользователь завершил цикл N и начинает цикл N+1
- **When** Система формирует новый план
- **Then** Упражнения в новом цикле отличаются от предыдущего минимум на 60%
- **And** Система отслеживает историю использованных упражнений через `CycleExerciseHistory`

### AC5: Обратная совместимость
- **Given** Существующий пользователь с активным планом
- **When** Приложение обновляется
- **Then** Текущий план сохраняется до завершения цикла
- **And** Новый план создаётся по новой логике

---

## Implementation Notes

### Файлы для изменения

| File | Change Type | Description |
|------|-------------|-------------|
| `MuscleGroupSequence.kt` | Modify | Изменить enum на новую последовательность |
| `WorkoutRepositoryImpl.kt` | Modify | Обновить `getWorkoutPlanWithSequence()` |
| `ExercisePoolManager.kt` | Minor | Учесть новые группы мышц |
| `CycleUseCase.kt` | No change | Логика уже поддерживает исключение упражнений |

### Последовательность реализации

1. **Phase 1**: Обновить `MuscleGroupSequence.kt` с новой последовательностью
2. **Phase 2**: Обновить тесты `MuscleGroupSequenceTest.kt`
3. **Phase 3**: Проверить работу `ExercisePoolManager` с новыми группами
4. **Phase 4**: Провести регрессионное тестирование
5. **Phase 5**: Миграция для существующих пользователей

---

## Validation Plan

### Unit Tests

**File**: `MuscleGroupSequenceTest.kt`

```kotlin
@Test
fun `STRENGTH_SEQUENCE has correct order - Arms to Legs`() {
    val expected = listOf(ARMS, SHOULDERS, CHEST_BACK, LEGS_CORE)
    assertEquals(expected, MuscleGroupSequence.STRENGTH_SEQUENCE)
}

@Test
fun `FULL_SEQUENCE includes CARDIO at end`() {
    assertEquals(5, MuscleGroupSequence.FULL_SEQUENCE.size)
    assertEquals(CARDIO, MuscleGroupSequence.FULL_SEQUENCE.last())
}
```

**File**: `WorkoutRepositoryImplSequenceTest.kt`

```kotlin
@Test
fun `getWorkoutPlanWithSequence creates 10 days with cardio on 5 and 10`() = runTest {
    val plan = repository.getWorkoutPlanWithSequence(profile, emptyMap())
    
    assertEquals(10, plan.days.size)
    assertTrue(plan.days[4].dayName.contains("Кардио"))
    assertTrue(plan.days[9].dayName.contains("Кардио"))
}

@Test
fun `no duplicate strength exercises in cycle`() = runTest {
    val plan = repository.getWorkoutPlanWithSequence(profile, emptyMap())
    
    val strengthExercises = plan.days.flatMap { day ->
        day.exercises.filter { it.exerciseType == ExerciseType.STRENGTH }.map { it.name }
    }
    
    assertEquals(strengthExercises.size, strengthExercises.toSet().size)
}
```

### Manual Testing

1. Создать новый профиль пользователя
2. Проверить структуру плана:
   - День 1: Руки
   - День 2: Плечи
   - День 3: Грудь и Спина
   - День 4: Ноги и Пресс
   - День 5: Кардио
   - День 6-10: Повтор с другими упражнениями
3. Проверить отсутствие дублей упражнений

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Недостаточно упражнений для некоторых групп | Medium | Medium | Добавить упражнения в ExerciseLibrary |
| Нарушение существующих планов пользователей | Low | High | Миграция только после завершения цикла |
| Непонимание пользователями новой структуры | Low | Low | UI подсказки |

---

## Dependencies

- **Depends on**: REQ-003 (базовая структура уже реализована)
- **Blocks**: Нет
- **Related to**: ExerciseLibrary, CycleRepository

---

## Metrics

| Metric | Target | How to Verify |
|--------|--------|---------------|
| Уникальность упражнений в цикле | 100% | Unit test |
| Порядок групп мышц | Руки→Плечи→Грудь/Спина→Ноги→Кардио | Unit test |
| Дни кардио | Дни 5 и 10 | Manual test |
| Ротация между циклами | 60%+ разных | Integration test |

---

## Definition of Done

- [ ] `MuscleGroupSequence.kt` обновлён с новой последовательностью
- [ ] Unit тесты обновлены и проходят
- [ ] Integration тесты проходят
- [ ] Manual тестирование выполнено
- [ ] Документация обновлена
- [ ] Code review пройден

---

**Version History:**
- v1.0 (2026-02-17): Initial requirements document
