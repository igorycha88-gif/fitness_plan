# REQ-005: Унификация цветовой схемы AppBar

**Status**: New
**Priority**: Medium
**Complexity**: Simple
**Created**: 2026-02-17
**Updated**: 2026-02-17

## Description
Привести AppBar (TopAppBar) к единой цветовой схеме на всех страницах приложения. Эталонной цветовой схемой является страница "План" (HomeScreen), где AppBar использует стандартные цвета Material3 без явного указания containerColor.

## User Story
Как пользователь,
Я хочу видеть единообразный дизайн AppBar на всех страницах,
Чтобы приложение выглядело целостно и профессионально.

## Current State Analysis

### Страницы с нецелевой цветовой схемой (primary color):
| Файл | Страница | Текущий цвет AppBar |
|------|----------|---------------------|
| `StatisticsScreen.kt:20-25` | Статистика | `MaterialTheme.colorScheme.primary` |
| `BodyParametersScreen.kt:38-44` | Параметры тела | `MaterialTheme.colorScheme.primary` |
| `ProfileComponents.kt:115-120` | Профиль | `MaterialTheme.colorScheme.primary` |
| `MuscleGroupStatsScreen.kt:32-36` | Группы мышц | `MaterialTheme.colorScheme.primary` |
| `MuscleGroupDetailScreen.kt:28-40` | Детали группы | `MaterialTheme.colorScheme.primary` |
| `ExerciseDetailScreen.kt:148-154` | Детали упражнения | `MaterialTheme.colorScheme.primary` |
| `ExerciseGuideScreen.kt:47-56` | Гид по упражнениям | `MaterialTheme.colorScheme.primary` |
| `ExerciseLibraryDetailScreen.kt:46-55` | Детали библиотеки | `MaterialTheme.colorScheme.primary` |
| `UserProfileForm.kt:98-110` | Форма профиля | `MaterialTheme.colorScheme.primary` |
| `RegisterScreen.kt:34` | Регистрация | Стандартный (без colors) |

### Эталонная страница (без явного colors):
| Файл | Страница | Цвет AppBar |
|------|----------|-------------|
| `HomeScreen.kt:168-178` | План | Стандартный Material3 |

## Target State
Все страницы должны использовать TopAppBar без явного указания colors, что применит стандартную цветовую схему Material3 (surface container).

### Изменения в коде:
**Было:**
```kotlin
TopAppBar(
    title = { Text("Статистика") },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    )
)
```

**Станет:**
```kotlin
TopAppBar(
    title = { Text("Статистика") }
)
```

## Acceptance Criteria
- [ ] TopAppBar на StatisticsScreen использует стандартные цвета Material3
- [ ] TopAppBar на BodyParametersScreen использует стандартные цвета Material3
- [ ] TopAppBar на ProfileScreen (через ProfileTopBar) использует стандартные цвета Material3
- [ ] TopAppBar на MuscleGroupStatsScreen использует стандартные цвета Material3
- [ ] TopAppBar на MuscleGroupDetailScreen использует стандартные цвета Material3
- [ ] TopAppBar на ExerciseDetailScreen использует стандартные цвета Material3
- [ ] TopAppBar на ExerciseGuideScreen использует стандартные цвета Material3
- [ ] TopAppBar на ExerciseLibraryDetailScreen использует стандартные цвета Material3
- [ ] TopAppBar на UserProfileForm использует стандартные цвета Material3
- [ ] Все тесты проходят успешно
- [ ] Визуальная проверка на устройстве подтверждает единообразие

## Technical Requirements

### Components Affected:
- `ui/StatisticsScreen.kt` - убрать параметр colors из TopAppBar
- `ui/BodyParametersScreen.kt` - убрать параметр colors из TopAppBar
- `ui/ProfileComponents.kt` - убрать параметр colors из ProfileTopBar
- `ui/MuscleGroupStatsScreen.kt` - убрать параметр colors из TopAppBar
- `ui/MuscleGroupDetailScreen.kt` - убрать параметр colors из TopAppBar
- `ui/ExerciseDetailScreen.kt` - убрать параметр colors из TopAppBar
- `ui/ExerciseGuideScreen.kt` - убрать параметр colors из TopAppBar
- `ui/ExerciseLibraryDetailScreen.kt` - убрать параметр colors из TopAppBar
- `ui/UserProfileForm.kt` - убрать параметр colors из TopAppBar

### Architecture Impact:
- Минимальное - только UI слой
- Не требует изменений в ViewModel или data слое
- Не требует миграции данных

## Dependencies
- Не зависит от других задач
- Может выполняться параллельно с REQ-006 и REQ-007

## Implementation Notes

### Рекомендуемый подход:
1. Удалить параметр `colors` из всех TopAppBar компонентов
2. Убедиться, что навигационные иконки корректно отображаются (цвет onSurface)

### Потенциальные риски:
- **Низкий**: Иконки действий могут быть менее заметны на surface фоне - проверить visibility

### Performance Considerations:
- Нет влияния на производительность

## Validation Plan

### Unit Tests:
- Существующие тесты UI должны продолжать работать

### Manual Testing:
1. Открыть каждую страницу с измененным AppBar
2. Проверить визуальное отображение
3. Сравнить с эталонной страницей "План"
4. Проверить читаемость текста и видимость иконок

### Edge Cases:
- Тёмная тема (dark mode)
- Различные размеры экранов

## Metrics
- Консистентность дизайна: 100% страниц используют единую цветовую схему

## Notes
- Это косметическое изменение для улучшения UX
- Не влияет на функциональность
- Желательно выполнить перед релизом

## Related Documents
- `SYSTEM_PROMPT.md` - Section: UI/Theme
- `ARCHITECTURE.md` - Section: UI Components
