# REQ-009: Редизайн вкладки "Мой план"

**Status**: New
**Priority**: Medium
**Complexity**: Medium
**Created**: 2026-02-17
**Updated**: 2026-02-17

---

## Description

Унификация визуального оформления вкладки "Мой план" с вкладкой "Автоматический план", а также добавление недостающего функционала для улучшения UX и визуальной консистентности приложения.

---

## User Story

Как пользователь приложения,
Я хочу, чтобы вкладка "Мой план" имела такой же дизайн и функциональность как "Автоматический план",
Чтобы интерфейс был единообразным и удобным в использовании.

---

## Current State Analysis

### Существующая реализация

| Аспект | Автоматический план | Мой план |
|--------|---------------------|----------|
| Цвет подложки Card | `MaterialTheme.colorScheme.surface` (белый) | `FitnessSecondaryLight.copy(alpha = 0.1f)` (цветной) |
| Контур Card | `BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)` (серый) | `BorderStroke(1.dp, FitnessSecondaryLight)` (цветной) |
| Кнопка удаления | N/A | Полноценная кнопка внизу секции |
| Выбор даты в дне | ✅ Есть (DatePicker) | ❌ Нет |
| Свернутый вид дня | Название, дата, группы мышц, кол-во упражнений | Название, кол-во упражнений |

### Затронутые компоненты

- `HomeScreen.kt`:
  - `UserPlanSection` (строки 1012-1123)
  - `UserWorkoutDayCard` (строки 1125-1221)

---

## Requirements

### REQ-009.1: Унификация цвета подложки плана

**Приоритет**: Medium

**Описание**:
Изменить цвет фона Card в компоненте `UserPlanSection` на белый, аналогично `AutoPlanSection`.

**Текущее состояние**:
```kotlin
colors = CardDefaults.cardColors(
    containerColor = FitnessSecondaryLight.copy(alpha = 0.1f)
)
```

**Ожидаемое состояние**:
```kotlin
colors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface
)
```

**Расположение**: `HomeScreen.kt:1032-1035`

---

### REQ-009.2: Унификация контура плана

**Приоритет**: Medium

**Описание**:
Изменить цвет границы Card в компоненте `UserPlanSection` на серый, аналогично `AutoPlanSection`.

**Текущее состояние**:
```kotlin
border = BorderStroke(1.dp, FitnessSecondaryLight)
```

**Ожидаемое состояние**:
```kotlin
border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
```

**Расположение**: `HomeScreen.kt:1036`

---

### REQ-009.3: Изменение кнопки удаления плана на иконку

**Приоритет**: Medium

**Описание**:
Заменить кнопку "Удалить план" на иконку корзины и разместить её рядом с иконкой редактирования (карандаша) в заголовке секции.

**Текущее состояние**:
- Кнопка располагается под кнопкой "Добавить день"
- Реализована как полноценная кнопка с текстом

**Ожидаемое состояние**:
- Иконка корзины размещается в Row заголовка рядом с +/- 
- При нажатии открывается диалог подтверждения удаления

**Текущий код** (строки 1083-1093):
```kotlin
Button(
    onClick = onDeletePlan,
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    )
) {
    Icon(Icons.Default.Delete, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("Удалить план")
}
```

**Предлагаемый код** (в Row заголовка):
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onDeletePlan) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Удалить план",
            tint = MaterialTheme.colorScheme.error
        )
    }
    Text(
        text = if (isExpanded) "-" else "+",
        style = MaterialTheme.typography.titleMedium
    )
}
```

**Расположение**: 
- Удалить: `HomeScreen.kt:1083-1093`
- Добавить: `HomeScreen.kt:1061-1066`

---

### REQ-009.4: Добавление выбора даты в день тренировки

**Приоритет**: Medium

**Описание**:
Добавить возможность выбора даты в компонент `UserWorkoutDayCard`, аналогично `WorkoutDayCard`.

**Текущее состояние**:
- Компонент `UserWorkoutDayCard` не имеет параметра `onDateChange`
- DatePicker не реализован

**Ожидаемое состояние**:
- Добавить параметр `onDateChange: ((Long?) -> Unit)? = null` в `UserWorkoutDayCard`
- Отображать выбранную дату в свернутом виде
- Добавить иконку календаря для выбора даты
- Реализовать DatePicker dialog

**Реализация**:

1. Добавить параметр в функцию:
```kotlin
@Composable
fun UserWorkoutDayCard(
    ...
    onDateChange: ((Long?) -> Unit)? = null
)
```

2. Добавить отображение даты и кнопку выбора (аналогично `WorkoutDayCard:680-702`):
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    day.scheduledDate?.let { date ->
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    if (onDateChange != null) {
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "Изменить дату",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

3. Добавить DatePicker dialog (аналогично `WorkoutDayCard:762-795`)

**Расположение**: `HomeScreen.kt:1125-1221`

---

### REQ-009.5: Расширение информации в свернутом виде дня тренировки

**Приоритет**: Medium

**Описание**:
Добавить отображение даты и групп мышц в свернутом виде компонента `UserWorkoutDayCard`, аналогично `WorkoutDayCard`.

**Текущее состояние**:
```kotlin
Column(modifier = Modifier.weight(1f)) {
    Text(
        text = day.dayName,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "${day.exercises.size} упражнений",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    // Статус выполнения...
}
```

**Ожидаемое состояние** (аналогично `WorkoutDayCard:674-726`):
```kotlin
Column(modifier = Modifier.weight(1f)) {
    Text(
        text = day.dayName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        day.scheduledDate?.let { date ->
            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // DatePicker button (REQ-009.4)
    }
    Text(
        text = "Группы мышц: ${day.muscleGroups.joinToString(", ")}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    // Статус выполнения...
}
```

**Расположение**: `HomeScreen.kt:1158-1184`

---

## Acceptance Criteria

- [ ] **AC-009.1**: Фон Card в секции "Мой план" белый (`MaterialTheme.colorScheme.surface`)
- [ ] **AC-009.2**: Контур Card в секции "Мой план" серый (`MaterialTheme.colorScheme.outlineVariant`)
- [ ] **AC-009.3**: Кнопка удаления плана отображается как иконка корзины
- [ ] **AC-009.4**: Иконка удаления расположена рядом с иконкой развертывания (+/-)
- [ ] **AC-009.5**: При нажатии на иконку удаления появляется диалог подтверждения
- [ ] **AC-009.6**: В свернутом виде дня тренировки отображается дата (если задана)
- [ ] **AC-009.7**: В свернутом виде дня тренировки отображаются группы мышц
- [ ] **AC-009.8**: Есть возможность выбрать дату тренировки через DatePicker
- [ ] **AC-009.9**: DatePicker позволяет очистить выбранную дату
- [ ] **AC-009.10**: Визуальный дизайн дня тренировки идентичен автоматическому плану

---

## Technical Requirements

### Components Affected

| Компонент | Файл | Изменения |
|-----------|------|-----------|
| `UserPlanSection` | `HomeScreen.kt:1012-1123` | Цвет фона, граница, кнопка удаления |
| `UserWorkoutDayCard` | `HomeScreen.kt:1125-1221` | DatePicker, отображение даты и групп мышц |

### Data Models Affected

Нет изменений в моделях данных. `WorkoutDay` уже содержит поле `scheduledDate`.

### API Changes

Нет изменений в API. Потребуется передача callback `onDateChange` через параметры компонентов.

### Test Coverage

- Unit тесты: не требуются (UI изменения)
- UI тесты: проверить отображение элементов в разных состояниях

---

## Implementation Notes

### Порядок реализации

1. **REQ-009.1 и REQ-009.2** (простые изменения стилей)
2. **REQ-009.5** (расширение отображения информации)
3. **REQ-009.4** (добавление DatePicker)
4. **REQ-009.3** (перенос кнопки удаления)

### Риски

| Риск | Вероятность | Влияние | Митигация |
|------|-------------|---------|-----------|
| Несовместимость стилей при смене темы | Низкая | Среднее | Тестирование в светлой/темной теме |
| Увеличение высоты свернутой карточки дня | Средняя | Низкое | Проверка на разных размерах экрана |

### Performance Considerations

Изменения не влияют на производительность (только UI).

### Security Considerations

Нет влияния на безопасность.

---

## Validation Plan

### Manual Testing

1. Открыть вкладку "Мой план"
2. Проверить белый фон и серый контур Card
3. Проверить наличие иконки удаления в заголовке
4. Нажать иконку удаления - должен появиться диалог
5. Развернуть план, проверить свернутый вид дня тренировки:
   - Отображается название дня
   - Отображается дата (если задана)
   - Отображаются группы мышц
   - Отображается количество упражнений
6. Нажать иконку календаря - должен появиться DatePicker
7. Выбрать дату - должна отобразиться в карточке
8. Очистить дату - должна исчезнуть из карточки

### Edge Cases

- День без заданной даты (не должна отображаться)
- День с заданной датой (должна отображаться)
- День без групп мышц (пустая строка или "не указаны")
- План без дней

### Visual Comparison

После реализации визуально сравнить:
- `AutoPlanSection` vs `UserPlanSection` (должны быть идентичны по стилю)
- `WorkoutDayCard` vs `UserWorkoutDayCard` (должны быть идентичны по структуре информации)

---

## Dependencies

- Depends on: None
- Blocks: None
- Related to: None

---

## Metrics

- Время реализации: ~2-3 часа
- Количество затронутых файлов: 1
- Количество измененных строк: ~50

---

## Notes

- Использовать существующий код `WorkoutDayCard` как reference для `UserWorkoutDayCard`
- Убедиться, что изменения не ломают существующий функционал редактирования плана
- Цвет текста заголовка "Мой план" можно оставить без изменений или унифицировать с "Автоматический план"

---

## References

- `HomeScreen.kt` - основной файл с компонентами
- `WorkoutDayCard` (строки 633-796) - reference implementation
- `AutoPlanSection` (строки 946-1010) - reference implementation
- ANALYST_GUIDE.md - формат требований
