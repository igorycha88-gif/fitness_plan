# REQ-007: Упрощение фильтра времени на вкладке "Параметры тела"

**Status**: New
**Priority**: Medium
**Complexity**: Simple
**Created**: 2026-02-17
**Updated**: 2026-02-17

## Description
Убрать фильтр "Всё время" из списка доступных фильтров на вкладке "Параметры тела" и сделать отображение всех данных поведением по умолчанию. Пользователь не должен выбирать "Всё время" - график всегда должен показывать все доступные данные.

## User Story
Как пользователь,
Я хочу сразу видеть все мои замеры на графике без необходимости выбирать фильтр,
Чтобы быстрее анализировать свой прогресс за всё время.

## Current State Analysis

### Текущие фильтры времени:
| Значение | Label | Days | Описание |
|----------|-------|------|----------|
| WEEK | "Неделя" | 7 | Последние 7 дней |
| MONTH | "Месяц" | 30 | Последние 30 дней |
| YEAR | "Год" | 365 | Последний год |
| ALL | "Всё время" | 0 | Все данные |

### Расположение в коде:
**1. StatisticsViewModel.kt:30-35:**
```kotlin
enum class TimeFilter(val days: Int, val label: String) {
    WEEK(7, "Неделя"),
    MONTH(30, "Месяц"),
    YEAR(365, "Год"),
    ALL(0, "Всё время")
}
```

**2. BodyParametersStatsViewModel.kt:42:**
```kotlin
private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTH)
```

**3. BodyParametersStatsViewModel.kt:117-122:**
```kotlin
val timeFiltered = if (timeFilter.days == 0) {
    typesFiltered  // ALL - показываем все данные
} else {
    val cutoffTime = System.currentTimeMillis() - (timeFilter.days * 24 * 60 * 60 * 1000L)
    typesFiltered.filter { it.date >= cutoffTime }
}
```

**4. BodyParametersScreen.kt:57-60:**
```kotlin
TimeFilterRow(
    selectedFilter = selectedTimeFilter,
    onFilterSelected = { viewModel.setTimeFilter(it) }
)
```

### Проблемы текущего решения:
1. Лишний выбор для пользователя ("Всё время" - не фильтр, а отсутствие фильтра)
2. По умолчанию показывается только месяц, что может скрывать важный долгосрочный прогресс
3. Лишний UI элемент занимает место

## Proposed Solution

### Вариант A: Убрать ALL из UI, сделать его дефолтом (РЕКОМЕНДУЕТСЯ)

**Изменения:**
1. Убрать `TimeFilter.ALL` из отображения в TimeFilterRow
2. Изменить дефолтное значение на ALL (показывать все данные сразу)
3. Оставить WEEK, MONTH, YEAR как опции для фильтрации

**Логика:**
- При входе на страницу показываются ВСЕ данные
- Пользователь может сузить период, выбрав Неделю/Месяц/Год
- Опция "Сбросить" или "Все данные" отсутствует - просто выбрать другой период

**UI:**
```
[Неделя] [Месяц] [Год]  <- только 3 кнопки, нет "Всё время"
```

### Вариант B: Убрать ALL полностью из enum

**Изменения:**
1. Удалить `TimeFilter.ALL` из enum
2. Использовать nullable TimeFilter для обозначения "без фильтра"
3. По умолчанию selectedTimeFilter = null

**Преимущества Варианта B:**
- Чище код (ALL не является "фильтром")
- null явно обозначает "нет фильтра"

**Недостатки Варианта B:**
- Требует изменения логики во многих местах
- Больше кода для null handling

### Рекомендация: Вариант A
- Минимальные изменения
- Enum остаётся без изменений (ALL используется в других местах - WeightScreen, ProgressScreen)
- UI изменения изолированы

## Acceptance Criteria
- [ ] Фильтр "Всё время" не отображается в списке фильтров
- [ ] По умолчанию показываются все данные (как ALL)
- [ ] Фильтры "Неделя", "Месяц", "Год" работают корректно
- [ ] При выборе фильтра данные корректно фильтруются
- [ ] Все unit тесты проходят
- [ ] Ручное тестирование подтверждает корректность

## Technical Requirements

### Components Affected:

**1. BodyParametersStatsViewModel.kt:**
```kotlin
// Было:
private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTH)

// Станет:
private val _selectedTimeFilter = MutableStateFlow(TimeFilter.ALL)
```

**2. BodyParametersScreen.kt - TimeFilterRow:**
```kotlin
// Было:
@Composable
fun TimeFilterRow(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            FilterChip(...)
        }
    }
}

// Станет:
@Composable
fun TimeFilterRow(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Показываем только WEEK, MONTH, YEAR (не ALL)
        listOf(TimeFilter.WEEK, TimeFilter.MONTH, TimeFilter.YEAR).forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
```

**3. Добавить опцию "Все данные" (опционально):**
Если пользователь хочет вернуть просмотр всех данных после выбора фильтра, можно добавить:
```kotlin
// Вариант 1: Добавить Chip "Все"
listOf(TimeFilter.WEEK, TimeFilter.MONTH, TimeFilter.YEAR, TimeFilter.ALL).forEach { ... }

// Вариант 2: Кнопка "Сбросить" рядом с фильтрами
// (не рекомендуется - усложняет UI)
```

**Рекомендация:** Не добавлять "Все данные" как отдельную опцию. Вместо этого:
- При выборе фильтра отображается выбранный период
- Если пользователь хочет видеть все данные - он может обновить страницу или перезапустить приложение

### Files to Modify:
- `presentation/viewmodel/BodyParametersStatsViewModel.kt` - изменить дефолт
- `ui/BodyParametersScreen.kt` - изменить TimeFilterRow

### Files NOT to Modify (ALL используется):
- `StatisticsViewModel.kt` - enum TimeFilter (не менять)
- `ui/WeightChart.kt` - использует ALL
- `ui/ProgressScreen.kt` - использует ProgressTimeFilter.ALL

## Dependencies
- Не зависит от других задач
- Может выполняться параллельно с REQ-005 и REQ-006

## Implementation Notes

### Рекомендуемый порядок:
1. Изменить дефолт в BodyParametersStatsViewModel.kt
2. Модифицировать TimeFilterRow для исключения ALL из отображения
3. Протестировать работу фильтров

### Потенциальные риски:
- **Низкий**: Пользователи привыкшие к "Всё время" могут не найти опцию
- **Митигация**: Документировать в changelog, что по умолчанию показываются все данные

### Performance Considerations:
- Нет влияния - фильтрация происходит так же

## Validation Plan

### Unit Tests:
- Обновить тесты в `BodyParametersStatsViewModelTest.kt`:
  - Изменить тесты с дефолтным MONTH на ALL
  - Убедиться что ALL корректно обрабатывается

### Manual Testing:
1. Открыть вкладку "Параметры тела"
2. Проверить что показываются все данные
3. Выбрать "Неделя" - проверить фильтрацию
4. Выбрать "Месяц" - проверить фильтрацию
5. Выбрать "Год" - проверить фильтрацию
6. Перезапустить приложение - проверить что снова все данные

### Edge Cases:
- Нет данных - показать пустой график
- Одно измерение - корректное отображение
- Большой объём данных (>100 измерений) - проверить performance

## Metrics
- Количество кликов для просмотра всех данных: 0 (было 1)
- Время до отображения данных: без изменений

## Alternative Considered
**Альтернатива:** Полностью убрать ALL из enum
- Отклонено: ALL используется в других экранах (WeightScreen, ProgressScreen)
- Требует больше изменений и тестирования

## Notes
- Изменение касается только вкладки "Параметры тела"
- Другие вкладки (Вес, Прогресс) сохраняют текущее поведение

## Related Documents
- `SYSTEM_PROMPT.md` - Section: BodyParameters/Statistics
- `FEATURE_BODY_PARAMETERS.md` - документация фичи
- `ui/BodyParametersScreen.kt` - текущая реализация
- `presentation/viewmodel/BodyParametersStatsViewModel.kt` - ViewModel
