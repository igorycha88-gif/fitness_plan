# REQ-012: Обновление изображения и масштаба окна дня тренировки

**Status**: New  
**Priority**: High  
**Complexity**: Simple  
**Created**: 2026-02-18  
**Updated**: 2026-02-18

---

## Description

Доработка интерфейса в двух направлениях:
1. При смене упражнения с рекомендуемого на альтернативное должно обновляться изображение упражнения
2. Масштаб окна дня тренировки в автоматическом плане должен быть унифицирован с масштабом окна дня тренировки в "Моем плане"

---

## User Story

### US-012.1: Обновление изображения при смене упражнения

Как пользователь приложения,  
Я хочу видеть актуальное изображение выбранного альтернативного упражнения,  
Чтобы правильно понимать технику выполнения упражнения.

### US-012.2: Унификация масштаба окна дня тренировки

Как пользователь приложения,  
Я хочу, чтобы окна дней тренировки имели одинаковый масштаб в обоих типах планов,  
Чтобы интерфейс был визуально единообразным и удобным.

---

## Current State Analysis

### REQ-012.1: Проблема с изображением альтернативного упражнения

**Расположение**: `ExerciseDetailScreen.kt:368-378`

**Текущий код**:
```kotlin
selectedExercise = exercise?.copy(
    name = alt.name,
    description = alt.description,
    muscleGroups = alt.muscleGroups,
    equipment = alt.equipment,
    exerciseType = alt.exerciseType,
    stepByStepInstructions = alt.stepByStepInstructions,
    animationUrl = alt.animationUrl
)
```

**Проблема**: Поля `imageUrl` и `imageRes` из альтернативного упражнения (`alt`) не копируются в `selectedExercise`. В результате при выборе альтернативного упражнения отображается изображение исходного упражнения.

**Модель данных** (`ExerciseLibrary.kt:13-14`):
```kotlin
val imageUrl: String? = null,
val imageRes: String? = null,
```

### REQ-012.2: Проблема с масштабом окна дня тренировки

**Расположение**: `HomeScreen.kt`

| Компонент | Файл | Строки | Описание |
|-----------|------|--------|----------|
| `WorkoutDayCard` | HomeScreen.kt | 638-799 | День тренировки в автоматическом плане |
| `UserWorkoutDayCard` | HomeScreen.kt | 1113+ | День тренировки в "Моем плане" |

**Анализ различий**:
- Оба компонента используют схожую структуру Card + Column
- Padding и стили могут отличаться
- Требуется визуальное сравнение и унификация

---

## Requirements

### REQ-012.1: Обновление изображения при смене упражнения

**Приоритет**: High  
**Сложность**: Simple

#### Описание

Добавить копирование полей `imageUrl` и `imageRes` при выборе альтернативного упражнения.

#### Затронутые экраны

1. **ExerciseDetailScreen** - экран детализации упражнения
2. Работает как для автоматического плана, так и для "Моего плана"

#### Текущее состояние (Код)

`ExerciseDetailScreen.kt:368-378`:
```kotlin
onClick = {
    selectedExercise = exercise?.copy(
        name = alt.name,
        description = alt.description,
        muscleGroups = alt.muscleGroups,
        equipment = alt.equipment,
        exerciseType = alt.exerciseType,
        stepByStepInstructions = alt.stepByStepInstructions,
        animationUrl = alt.animationUrl
    )
    expanded = false
}
```

#### Ожидаемое состояние (Код)

```kotlin
onClick = {
    selectedExercise = exercise?.copy(
        name = alt.name,
        description = alt.description,
        muscleGroups = alt.muscleGroups,
        equipment = alt.equipment,
        exerciseType = alt.exerciseType,
        stepByStepInstructions = alt.stepByStepInstructions,
        animationUrl = alt.animationUrl,
        imageUrl = alt.imageUrl,
        imageRes = alt.imageRes
    )
    expanded = false
}
```

#### Расположение изменений

| Файл | Строка | Изменение |
|------|--------|-----------|
| `ExerciseDetailScreen.kt` | 368-378 | Добавить `imageUrl` и `imageRes` в copy() |

---

### REQ-012.2: Унификация масштаба окна дня тренировки

**Приоритет**: Medium  
**Сложность**: Simple

#### Описание

Унифицировать визуальное отображение окна дня тренировки в автоматическом плане с окном дня тренировки в "Моем плане".

#### Затронутые компоненты

1. **AutoPlanSection** - автоматический план (`HomeScreen.kt:936-999`)
2. **WorkoutDaysList** - список дней в автоматическом плане (`HomeScreen.kt:607-634`)
3. **WorkoutDayCard** - карточка дня в автоматическом плане (`HomeScreen.kt:638-799`)
4. **UserWorkoutDayCard** - карточка дня в "Моем плане" (`HomeScreen.kt:1113+`)

#### Текущие параметры WorkoutDayCard (автоматический план)

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = borderColor
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Контент карточки
    }
}
```

#### Текущие параметры UserWorkoutDayCard (Мой план)

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Контент карточки
    }
}
```

#### Выявленные различия

| Параметр | WorkoutDayCard (Авто) | UserWorkoutDayCard (Мой план) |
|----------|----------------------|-------------------------------|
| `padding(horizontal)` | 16.dp | Нет (внешний padding) |
| Внешний padding | В Card | В родительском Column (8.dp между карточками) |

#### Решение

Унифицировать структуру, используя подход `UserWorkoutDayCard` как эталон (без дублирования padding в Card).

**Изменения в WorkoutDayCard** (`HomeScreen.kt:664-668`):

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = borderColor
)
```

**Примечание**: Padding `horizontal = 16.dp` должен применяться на уровне родительского компонента `AutoPlanSection` или `WorkoutDaysList`, а не в каждой карточке.

---

## Acceptance Criteria

### REQ-012.1: Обновление изображения

- [ ] **AC-012.1.1**: При выборе альтернативного упражнения в автоматическом плане изображение обновляется
- [ ] **AC-012.1.2**: При выборе альтернативного упражнения в "Моем плане" изображение обновляется
- [ ] **AC-012.1.3**: При возврате к исходному упражнению отображается его изображение
- [ ] **AC-012.1.4**: Если у альтернативного упражнения нет изображения, отображается placeholder
- [ ] **AC-012.1.5**: Работает корректно для упражнений с `imageUrl`
- [ ] **AC-012.1.6**: Работает корректно для упражнений с `imageRes`

### REQ-012.2: Унификация масштаба

- [ ] **AC-012.2.1**: Карточки дней в автоматическом плане имеют тот же размер что и в "Моем плане"
- [ ] **AC-012.2.2**: Визуальный отступ от краёв экрана одинаковый для обоих типов планов
- [ ] **AC-012.2.3**: Расстояние между карточками дней одинаковое
- [ ] **AC-012.2.4**: Padding внутри карточки одинаковый (16.dp)
- [ ] **AC-012.2.5**: Шрифты и размеры текста идентичны

---

## Technical Requirements

### Architecture Impact

| Аспект | Влияние |
|--------|---------|
| Архитектура | Минимальное - только UI слой |
| Слои | Presentation/UI |
| Навигация | Без изменений |
| Данные | Без изменений |

### Components Affected

| Компонент | Файл | Изменения |
|-----------|------|-----------|
| ExerciseDetailScreen | `ui/ExerciseDetailScreen.kt` | Добавить `imageUrl`, `imageRes` в copy() |
| WorkoutDayCard | `ui/HomeScreen.kt` | Убрать `padding(horizontal = 16.dp)` из Card |

### Data Models Affected

Нет изменений в моделях данных.

### API Changes

Нет изменений в API.

### Test Coverage

- Unit тесты: не требуются (UI изменения)
- UI тесты: обновить существующие тесты при необходимости

---

## Implementation Notes

### Порядок реализации

1. **REQ-012.1** (обновление изображения) - 5 минут
2. **REQ-012.2** (унификация масштаба) - 15 минут

### REQ-012.1: Изменения в коде

**Файл**: `app/src/main/java/com/example/fitness_plan/ui/ExerciseDetailScreen.kt`

**Строки**: 368-378

**Изменение**: Добавить две строки в блок `copy()`:
```kotlin
imageUrl = alt.imageUrl,
imageRes = alt.imageRes
```

### REQ-012.2: Изменения в коде

**Файл**: `app/src/main/java/com/example/fitness_plan/ui/HomeScreen.kt`

**Вариант 1**: Убрать `padding(horizontal = 16.dp)` из `WorkoutDayCard`

**Вариант 2**: Добавить padding в родительский контейнер `WorkoutDaysList`

Рекомендуется **Вариант 2** для консистентности с `UserPlanSection`.

### Potential Risks

| Риск | Вероятность | Влияние | Митигация |
|------|-------------|---------|-----------|
| Регрессия в отображении карточек | Низкая | Среднее | Визуальное тестирование |
| Placeholder не отображается для упражнения без картинки | Низкая | Низкое | Проверить ExerciseImageCard |

### Performance Considerations

Изменения не влияют на производительность (только UI).

### Security Considerations

Нет влияния на безопасность.

---

## Validation Plan

### Manual Testing

#### REQ-012.1: Обновление изображения

1. Открыть приложение
2. Перейти на вкладку "План" (автоматический)
3. Развернуть день тренировки
4. Нажать на упражнение с альтернативными вариантами
5. Запомнить изображение текущего упражнения
6. Выбрать альтернативное упражнение из выпадающего списка
7. **Проверить**: изображение изменилось на изображение альтернативного упражнения
8. Выбрать исходное упражнение
9. **Проверить**: изображение вернулось к исходному
10. Повторить для вкладки "Мой план"

#### REQ-012.2: Унификация масштаба

1. Открыть вкладку "Автоматический план"
2. Запомнить визуальный масштаб карточки дня тренировки
3. Переключиться на вкладку "Мой план"
4. **Проверить**: масштаб карточки дня такой же
5. Сравнить:
   - Отступ от краёв экрана
   - Расстояние между карточками
   - Padding внутри карточки
   - Размеры текста

### Edge Cases

- Альтернативное упражнение без изображения (должен отображаться placeholder)
- Упражнение только с `imageUrl` (без `imageRes`)
- Упражнение только с `imageRes` (без `imageUrl`)
- План без дней
- День без упражнений

### Error Scenarios

- Некорректный URL изображения (должен отображаться placeholder)
- Несуществующий ресурс `imageRes` (должен отображаться placeholder)

---

## Dependencies

- Зависит от: REQ-010 (исправление открытия экрана упражнения в "Моём плане")
- Блокирует: Нет
- Связано с: REQ-009 (редизайн "Мой план")

---

## Estimated Effort

| Этап | Время |
|------|-------|
| Анализ | 30 мин (выполнено) |
| Разработка REQ-012.1 | 10 мин |
| Разработка REQ-012.2 | 20 мин |
| Тестирование | 20 мин |
| **Итого** | **~1.5 часа** |

---

## Metrics

### Success Metrics

- **REQ-012.1**: 100% успешное обновление изображения при смене упражнения
- **REQ-012.2**: Визуальная идентичность карточек дня в обоих типах планов

### Quality Metrics

- Отсутствие регрессий в существующем функционале
- Все UI тесты проходят успешно

---

## Notes

- Изменение REQ-012.1 является критичным для пользовательского опыта - пользователи ожидают видеть изображение именно выбранного упражнения
- Изменение REQ-012.2 улучшает визуальную консистентность интерфейса
- Рекомендуется реализовать оба требования в рамках одной задачи

---

## References

- `ExerciseDetailScreen.kt:276-282` - использование `ExerciseImageCard`
- `ExerciseImageCard.kt:26-92` - компонент отображения изображения
- `ExerciseLibrary.kt:13-14` - модель с полями изображений
- `HomeScreen.kt:638-799` - `WorkoutDayCard`
- `HomeScreen.kt:1113+` - `UserWorkoutDayCard`
