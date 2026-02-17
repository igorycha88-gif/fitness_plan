# SYSTEM_PROMPT.md - Fitness Plan Application

## Project Overview

**Fitness Plan** — мобильное приложение для Android (Jetpack Compose) для планирования и отслеживания тренировок с адаптивной прогрессией весов. Приложение использует Clean Architecture с Hilt Dependency Injection и offline-first подход к хранению данных через DataStore.

**Версия**: 2.2
**Application ID**: `com.example.fitness_plan`
**SDK**: minSdk 24, targetSdk 34, compileSdk 34
**Язык**: Kotlin 1.8.10
**UI Framework**: Jetpack Compose (Material3)
**Объем кода**: ~12,100 строк Kotlin кода
**Статус**: Активная разработка, Production Ready

---

## Tech Stack

### Core Framework
- **Kotlin**: 1.8.10
- **Jetpack Compose**: BOM 2023.08.00
- **Compose Compiler**: 1.4.3
- **AndroidX Core**: 1.12.0
- **Activity Compose**: 1.8.2
- **Navigation Compose**: 2.8.0
- **Material3**: 2023.08.00

### Dependency Injection
- **Hilt**: 2.51 (Android + Navigation + Work)
- **Hilt Navigation Compose**: 1.2.0
- **Hilt Work**: 1.2.0

### Data & Storage
- **DataStore Preferences**: 1.1.1 (хранение данных)
- **Gson**: 2.10.1 (сериализация)
- **Kotlinx Serialization**: 1.6.3 (JSON)

### Security
- **Android Security Crypto**: 1.1.0-alpha06 (шифрование)
- **BCrypt**: 0.4 (хеширование паролей)

### Testing
- **JUnit**: 4.13.2
- **Mockito**: 5.8.0
- **Mockito-Kotlin**: 5.1.0
- **Mockk**: 1.13.5
- **Coroutines Test**: 1.7.3
- **Truth**: 1.1.3
- **AndroidX Navigation Testing**: 2.8.0
- **Hilt Testing**: 2.51

### Background Work & Notifications
- **WorkManager**: 2.9.0

### Charts
- **Vico Compose M3**: 2.0.0-beta.2

### Other
- **MultiDex**: 2.0.1

---

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (UI, ViewModels, Navigation)      │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│        Domain Layer                │
│  (Use Cases, Models, Repositories) │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         Data Layer                 │
│  (Repository Implementations,       │
│   DataStore, Security)             │
└─────────────────────────────────────┘
```

### Package Structure

```
com.example.fitness_plan/
├── data/                          # Data Layer
│   ├── AppModule.kt               # Hilt DI модуль
│   ├── WorkoutRepositoryImpl.kt     # Реализация репозитория планов
│   ├── CycleRepository.kt          # Реализация репозитория циклов
│   ├── UserRepository.kt           # Реализация репозитория пользователей
│   ├── ExerciseLibraryRepositoryImpl.kt  # Библиотека упражнений
│   ├── ExerciseCompletionRepository.kt     # Выполнение упражнений
│   ├── ExerciseStatsRepository.kt  # Статистика упражнений
│   ├── WorkoutScheduleRepository.kt # Расписание тренировок
│   ├── NotificationRepository.kt    # Уведомления
│   ├── CredentialsRepository.kt    # Аутентификация
│   ├── admin/                     # Admin функционал
│   └── PasswordHasher.kt          # Хеширование паролей
│
├── domain/                        # Domain Layer
│   ├── model/                     # Domain Models
│   │   ├── WorkoutPlan.kt         # План тренировки
│   │   ├── WorkoutDay.kt          # День тренировки
│   │   ├── Exercise.kt            # Упражнение
│   │   ├── UserProfile.kt         # Профиль пользователя
│   │   ├── Cycle.kt               # Цикл тренировок
│   │   ├── ExerciseLibrary.kt      # Библиотека упражнений
│   │   ├── ExerciseStats.kt        # Статистика упражнений
│   │   ├── ExerciseTypes.kt        # Типы и перечисления
│   │   ├── WeightProgressionResult.kt  # Прогрессия весов
│   │   └── PlanHistory.kt         # История планов
│   ├── repository/                # Repository Interfaces
│   │   ├── WorkoutRepository.kt
│   │   ├── CycleRepository.kt
│   │   ├── UserRepository.kt
│   │   ├── ExerciseLibraryRepository.kt
│   │   ├── ExerciseCompletionRepository.kt
│   │   ├── ExerciseStatsRepository.kt
│   │   ├── WorkoutScheduleRepository.kt
│   │   └── NotificationRepository.kt
│   ├── usecase/                   # Use Cases
│   │   ├── WorkoutUseCase.kt
│   │   ├── CycleUseCase.kt        # Управление циклами
│   │   ├── AuthUseCase.kt         # Аутентификация
│   │   ├── ProfileViewModel.kt      # Управление профилем
│   │   ├── StatisticsViewModel.kt   # Статистика
│   │   ├── WeightProgressionUseCase.kt  # Адаптивная прогрессия весов
│   │   └── ExerciseLibraryUseCase.kt
│   └── calculator/                # Business Logic
│       ├── WeightCalculator.kt      # Расчет весов
│       └── WorkoutDateCalculator.kt # Расписание дат
│
├── presentation/                  # Presentation Layer
│   └── viewmodel/                 # ViewModels
│       ├── WorkoutViewModel.kt      # Тренировки
│       ├── ProfileViewModel.kt      # Профиль
│       ├── StatisticsViewModel.kt   # Статистика
│       ├── ExerciseLibraryViewModel.kt  # Библиотека
│       └── AdminLoginViewModel.kt   # Admin
│
├── ui/                           # UI Layer (Jetpack Compose)
│   ├── MainActivity.kt            # Главная активность
│   ├── MainScreen.kt              # Основной экран с TabBar
│   ├── HomeScreen.kt              # Главная вкладка
│   ├── ProfileScreen.kt           # Профиль
│   ├── LoginScreen.kt             # Логин
│   ├── RegisterScreen.kt           # Регистрация
│   ├── UserProfileForm.kt          # Форма профиля
│   ├── AdminMainScreen.kt          # Admin панель
│   ├── AdminLoginScreen.kt         # Admin логин
│   ├── ExerciseDetailScreen.kt     # Детали упражнения
│   ├── ExerciseGuideScreen.kt     # Гид по упражнениям
│   ├── ExerciseLibraryScreen.kt    # Библиотека упражнений
│   ├── CycleHistoryScreen.kt       # История циклов
│   ├── StatisticsScreen.kt         # Статистика
│   ├── AdaptiveLayout.kt           # Адаптивный layout
│   ├── components/                 # UI компоненты
│   │   └── OverallStatsCard.kt
│   ├── charts/                    # Графики
│   │   ├── WeightChart.kt
│   │   ├── VolumeChart.kt
│   │   └── FrequencyChart.kt
│   └── theme/                     # Тема приложения
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── notification/                  # Уведомления
│   ├── NotificationHelper.kt       # Помощник уведомлений
│   └── ScheduleCheckWorker.kt      # WorkManager worker
│
├── security/                      # Безопасность
│   └── SecurityModule.kt          # Шифрование SharedPreferences
│
└── FitnessPlanApplication.kt       # Application класс
```

---

## Key Domain Models

### UserProfile
```kotlin
data class UserProfile(
    val username: String,
    val goal: String,           // Похудение, Наращивание мышечной массы, Поддержание формы
    val level: String,          // Новичок, Любитель, Профессил
    val frequency: String,       // 1 раз в неделю, 3 раза в неделю, 5 раз в неделю
    val weight: Double,
    val height: Double,
    val gender: String,         // Мужской, Женский
    val favoriteExercises: Set<String>
)
```

### WorkoutPlan
```kotlin
data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val days: List<WorkoutDay>,
    val goal: String,
    val level: String
)
```

### WorkoutDay
```kotlin
data class WorkoutDay(
    val id: Int,
    val dayName: String,
    val exercises: List<Exercise>,
    val muscleGroups: List<String>,
    val scheduledDate: Long?
)
```

### Exercise
```kotlin
data class Exercise(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: String,
    val weight: Float?,
    val isCompleted: Boolean,
    val alternatives: List<Exercise>,
    val description: String?,
    val recommendedWeight: Float?,
    val recommendedRepsPerSet: String?,
    val equipment: List<EquipmentType>,
    val muscleGroups: List<MuscleGroup>,
    val exerciseType: ExerciseType,
    val stepByStepInstructions: String?,
    val animationUrl: String?,
    val isFavoriteSubstitution: Boolean
)
```

### Cycle
```kotlin
data class Cycle(
    val cycleNumber: Int,
    val startDate: Long,
    val completedDate: Long?,
    val daysCompleted: Int,
    val totalDays: Int = DAYS_IN_CYCLE,  // 30 дней
    val completedMicrocycles: Int         // 10-дневные микроциклы
)
```

### ExerciseLibrary
```kotlin
data class ExerciseLibrary(
    val id: String,
    val name: String,
    val description: String,
    val exerciseType: ExerciseType,     // STRENGTH, CARDIO, STRETCHING
    val equipment: List<EquipmentType>,
    val muscleGroups: List<MuscleGroup>,
    val difficulty: String,
    val stepByStepInstructions: String,
    val animationUrl: String?,
    val tipsAndAdvice: String?,
    val progressionAdvice: String?
)
```

### Enums

**ExerciseType**: `STRENGTH`, `CARDIO`, `STRETCHING`

**EquipmentType**: `BODYWEIGHT`, `DUMBBELLS`, `CABLE_MACHINE`, `BARBELL`, `LEVER_MACHINE`, `EXPANDER`, `KETTLEBELL`, и т.д. (20+ типов оборудования)

**MuscleGroup**: `CHEST`, `TRICEPS`, `LATS`, `BICEPS`, `SHOULDERS`, `ABS`, `FOREARMS`, `TRAPS`, `GLUTES`, `QUADS`, `HAMSTRINGS`, `CALVES`, `LOWER_BACK`, `BRACHIALIS`

---

## Key Use Cases

### CycleUseCase
Управляет жизненным циклом тренировочного плана:
- `initializeCycleForUser()`: Инициализация цикла, проверка необходимости нового плана
- `updateProgress()`: Обновление прогресса по дням
- `checkAndApplyMicrocycleProgression()`: Адаптивная прогрессия весов каждые 10 дней

### WorkoutUseCase
Управление тренировками и упражнениями:
- `toggleExerciseCompletion()`: Переключение выполнения упражнения
- `getWorkoutPlan()`: Получение плана из DataStore
- `saveAdminWorkoutPlan()`: Сохранение admin плана
- `updateWorkoutSchedule()`: Обновление расписания

### WeightProgressionUseCase
Адаптивная прогрессия весов на основе истории тренировок:
- `applyAdaptiveProgression()`: Автоматическая корректировка весов на основе выполнений

### ExerciseLibraryUseCase
Библиотека упражнений:
- `getAllExercises()`: Все упражнения
- `getAlternativeExercises()`: Поиск альтернатив по группам мышц

### AuthUseCase
Аутентификация и регистрация:
- `register()`: Регистрация нового пользователя
- `login()`: Вход в систему
- `logout()`: Выход

---

## Data Storage

### DataStore Keys
- `workout_plans`: Хранилище планов тренировки
- `cycles`: Хранилище циклов
- `exercises_completed`: Выполненные упражнения
- `exercise_stats`: Статистика упражнений
- `workout_schedules`: Расписания тренировок
- `user_profiles`: Профили пользователей
- `notifications`: Настройки уведомлений

### Storage Pattern
```kotlin
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "store_name")

// Save
context.dataStore.edit { preferences ->
    preferences[stringPreferencesKey("key")] = "value"
}

// Load (Flow)
context.dataStore.data.map { preferences ->
    preferences[stringPreferencesKey("key")]
}
```

---

## Navigation Structure

### Main Routes
- `login_screen` - Экран входа
- `register_screen` - Экран регистрации
- `profile_form/{username}` - Форма профиля
- `admin_login` - Вход админа
- `admin_main` - Главная панель админа
- `main_tabs` - Основной экран с TabBar

### Bottom Navigation Tabs
- `home` - Главная (HomeScreen)
- `profile` - Профиль (ProfileScreen)
- `statistics` - Статистика (StatisticsScreen)
- `exercise_library` - Библиотека упражнений (ExerciseLibraryScreen)
- `cycle_history` - История циклов (CycleHistoryScreen)

### Nested Routes
- `exercise_detail/{exerciseName}` - Детали упражнения
- `exercise_guide/{exerciseId}` - Гид по упражнению

---

## Business Logic

### Workout Plan Generation
Планы создаются на основе:
1. **Цель (Goal)**: Похудение, Наращивание мышечной массы, Поддержание формы
2. **Уровень (Level)**: Новичок, Любитель, Профессил
3. **Частота (Frequency)**: 1x, 3x, 5x в неделю
4. **Вес (Weight)**: Для расчета рекомендуемых весов
5. **Пол (Gender)**: Для корректировки расчетов

### Weight Progression
- **Базовый вес**: Рассчитывается на основе веса тела, уровня, цели, пола
- **Стандартные веса**: Гантели (1.25-60 кг), Штанги (2.5-100 кг)
- **Адаптивная прогрессия**: Автоматическое увеличение веса при выполнении плана
- **Микроциклы**: Прогрессия каждые 10 дней

### Cycle Management
- **Цикл**: 30 дней (3 микроцикла по 10 дней)
- **Микроцикл**: 10 дней с адаптивной прогрессией весов
- **История**: Сохраняется в DataStore с датами начала/завершения

---

## Key Features

### ✅ Implemented
1. **Пользовательская система**
   - Регистрация с профилем
   - Логин/логаут
   - Admin функционал

2. **Планирование тренировок**
   - Генерация планов на основе профиля
   - 10-дневные планы с уникальными упражнениями
   - Различные типы планов (Full Body, Split, 5x)
   - Интеграция любимых упражнений

3. **Отслеживание прогресса**
   - Выполнение упражнений
   - Статистика весов и объема
   - История тренировок
   - Графики прогресса

4. **Библиотека упражнений**
   - 100+ упражнений с описаниями
   - Фильтрация по типам, оборудованию, группам мышц
   - Поиск
   - Альтернативные упражнения

5. **Адаптивная прогрессия**
   - Автоматическая корректировка весов
   - Уведомления об изменениях

6. **Уведомления**
   - Напоминания о тренировках
   - Уведомления о прогрессии

### 🚧 In Progress
- Улучшение уникальности упражнений (REQ-001)
- Сохранение плана в DataStore

---

## Testing

### Test Structure
```
src/test/
├── data/                    # Data layer tests
├── domain/                  # Domain layer tests
│   ├── usecase/            # Use cases tests
│   └── calculator/        # Calculator tests
├── presentation/            # Presentation layer tests
│   └── viewmodel/         # ViewModel tests
└── ui/                     # UI tests

src/androidTest/
└── ui/                     # Instrumentation tests
```

### Test Framework
- **Unit Tests**: JUnit 4 + Mockito/Mockk
- **UI Tests**: Compose Testing
- **Async Tests**: Coroutines Test + TestDispatcher

### Running Tests
```bash
# All unit tests
./gradlew testDebugUnitTest

# Specific test class
./gradlew testDebugUnitTest --tests "com.example.fitness_plan.WorkoutViewModelTest"

# Instrumentation tests
./gradlew connectedAndroidTest
```

---

## Dependency Injection (Hilt)

### Key Modules
- **AppModule**: Основные зависимости (Context, Repositories, UseCases)
- **SecurityModule**: Шифрование SharedPreferences
- **Admin Module**: Admin репозитории

### Example Usage
```kotlin
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutRepository: WorkoutRepository,
    private val cycleUseCase: CycleUseCase
) : ViewModel()
```

---

## Security

### Encryption
- **SharedPreferences**: Зашифрованы через Android Security Crypto
- **Passwords**: BCrypt хеширование
- **Master Password**: Зашифрованное хранение master пароля

### Authentication
- **User Credentials**: Хранятся в зашифрованном SharedPreferences
- **Admin Credentials**: Отдельный репозиторий

---

## Important Notes

### Current Issues
1. **Exercise Repetition Issue** (REQ-001)
   - План пересоздается при каждом входе
   - Упражнения не сохраняются
   - Решение: Сохранение плана в DataStore

### Data Flow
1. Пользователь создает профиль → CycleUseCase создает план → План сохраняется в DataStore
2. Пользователь входит → План загружается из DataStore → Отображается в UI
3. Пользователь выполняет упражнения → ExerciseCompletionRepository сохраняет → Cycle обновляется
4. Завершение цикла → Создается новый план

### Key Files to Know
- **WorkoutRepositoryImpl.kt**: Генерация планов, выбор упражнений
- **CycleUseCase.kt**: Управление циклами, инициализация планов
- **WorkoutViewModel.kt**: UI логика для тренировок
- **ExerciseLibraryRepositoryImpl.kt**: Библиотека упражнений (100+ упражнений)
- **WeightCalculator.kt**: Расчет весов и прогрессии

---

## Development Guidelines

### Code Style
- Kotlin 1.8.10
- Jetpack Compose
- Material3 Design System
- Clean Architecture
- Coroutines for async operations
- DataStore for storage

### Git Workflow
- Main branch: `main`
- Feature branches: `feature/feature-name`
- Pull requests required for merging

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew testDebugUnitTest

# Clean build
./gradlew clean
```

---

## API References

### Internal APIs (DataStore)
Все данные хранятся локально в DataStore:
- Пользователи
- Планы тренировок
- Циклы
- Статистика
- Прогресс

### External APIs
Нет внешних API calls. Приложение полностью offline-first.

---

## Performance Considerations

### Optimizations
1. **Lazy Loading**: Flow для реактивных данных
2. **DataStore**: Асинхронное хранение данных
3. **Coroutines**: Non-blocking operations
4. **Compose State Management**: Efficient recomposition

### Memory Management
- **MultiDex**: Включено для поддержки большого количества классов
- **Image Loading**: Использовать Coil/Glide (TBD)
- **Lifecycle-aware Components**: Correct cleanup

---

## Deployment

### Signing
- **Keystore**: `fitness_plan.jks`
- **Key Alias**: В `keystore.properties`

### Build Variants
- **debug**: Debug build without minification
- **release**: Release build with ProGuard (disabled currently)

---

## Documentation

### Available Documentation
- `TESTING.md` - Testing documentation
- `docs/requirements/REQ-001-FixExerciseRepetition.md` - Exercise uniqueness requirements

### Code Documentation
- KDoc comments for public APIs
- Inline comments for complex logic
- README.md for each major component (planned)

---

## Contact & Support

### Project Status
- **Active Development**: ✅
- **Production Ready**: ✅
- **Current Version**: 2.2

### Issue Tracking
Использовать GitHub Issues для отслеживания багов и фич.

---

## Future Roadmap

### High Priority
- [ ] Улучшение уникальности упражнений (REQ-001)
- [ ] Интеграция с облачным хранилищем
- [ ] Улучшение UI/UX

### Medium Priority
- [ ] Экспорт/импорт данных
- [ ] Социальные функции
- [ ] Интеграция с wearables

### Low Priority
- [ ] Темная тема
- [ ] Персонализация
- [ ] Рекомендации на базе ML

---

---

## CI/CD Pipeline

### GitHub Actions Workflows

#### ci.yml - Main CI/CD Pipeline
Запускается при:
- Push на `main`, `develop`, `feature/**`, `fix/**` ветки
- Pull Request на `main` или `develop`

**Шаги**:
1. Checkout кода
2. Setup JDK 17
3. Cache Gradle packages для ускорения
4. Grant permissions gradlew
5. Run unit tests (`./gradlew test`)
6. Build debug APK (`./gradlew assembleDebug`)
7. Upload APK как artifact с именем `app-debug-{branch-name}`

**Требования**:
- ✅ Все тесты должны проходить
- ✅ Build должен успешно завершиться
- ✅ Artifacts автоматически загружаются в GitHub

#### auto-version.yml - Automatic Version Bump
Запускается при merged PR на `main` ветку

**Функция**: Автоматически инкрементирует версию
- `versionCode`: +1
- `versionName`: Минорная версия +1 (e.g., 2.1 → 2.2)

**Парсинг**: Из `app/build.gradle.kts`

---

## Development Workflow

### Git Strategy
- **Main branch**: `main` — production-ready code
- **Develop branch**: `develop` — integration branch
- **Feature branches**: `feature/feature-name` — new features
- **Fix branches**: `fix/bug-name` — bug fixes
- **Pull Requests**: Требуются для всех merges

### Branch Protection Rules
- CI/CD tests must pass перед merge
- PR review (planned)
- No force push to main/develop

### Commit Message Format
```
<type>: <subject>

<body>

<footer>
```

**Types**:
- `feat`: Новая фича
- `fix`: Bug fix
- `docs`: Документация
- `refactor`: Рефакторинг без изменения функционала
- `test`: Добавление/изменение тестов
- `chore`: Build, deps, config
- `ci`: CI/CD изменения

**Примеры**:
```
feat(Statistics): Implement MVP version - basic charts and overall stats
fix(Exercise): Fill exercise data from ExerciseLibrary for alternative exercises
ci: Add CI workflow for develop/main branches
```

---

## Project Structure Deep Dive

### Data Layer (`app/src/main/java/com/example/fitness_plan/data/`)

**Core Repositories**:

1. **WorkoutRepositoryImpl.kt**
   - Генерация workout plans на основе профиля пользователя
   - Выбор упражнений с учетом любимых
   - Адаптивное распределение упражнений
   - Persistence в DataStore

2. **CycleRepository.kt**
   - Управление 30-дневными циклами
   - Отслеживание микроциклов (10 дней)
   - История завершенных циклов
   - Сохранение прогресса

3. **ExerciseLibraryRepositoryImpl.kt**
   - 100+ упражнений с метаданными
   - Фильтрация по типам (STRENGTH, CARDIO, STRETCHING)
   - Поиск по оборудованию (20+ типов)
   - Группы мышц (14 категорий)
   - Альтернативные упражнения

4. **ExerciseCompletionRepository.kt**
   - Отслеживание выполнения упражнений
   - Mark as completed/uncompleted
   - Историчность

5. **ExerciseStatsRepository.kt**
   - Статистика по упражнениям
   - Weight progression tracking
   - Volume tracking (sets × reps × weight)
   - Frequency analysis

6. **WorkoutScheduleRepository.kt**
   - Расписание тренировок
   - Scheduled dates для каждого дня
   - Ежедневные напоминания

7. **UserRepository.kt**
   - Профили пользователей
   - Preferences
   - Favorite exercises

8. **CredentialsRepository.kt**
   - User authentication (BCrypt hashing)
   - Login/logout sessions
   - Encrypted storage

**Admin System** (`data/admin/`):
- `AdminCredentialsRepository.kt` — Admin login (master password)
- `AdminWorkoutPlanRepository.kt` — Upload custom plans
- Admin management UI

**Utilities**:
- `PasswordHasher.kt` — BCrypt password hashing/verification
- `AppModule.kt` — Hilt DI configuration

### Domain Layer (`app/src/main/java/com/example/fitness_plan/domain/`)

**Models** (`domain/model/`):
```
UserProfile          → Данные пользователя (goal, level, freq, antro)
WorkoutPlan         → План тренировки (30 дней)
WorkoutDay          → День тренировки (упражнения, дата)
Exercise            → Упражнение с метаданными
Cycle               → 30-дневный цикл с микроциклами
ExerciseLibrary     → Базовое упражнение из библиотеки
ExerciseStats       → Статистика упражнения (вес, объем, рец)
WeightProgressionResult → Результат адаптивной прогрессии
PlanHistory         → История планов пользователя
PlanCompletionStatus → Статус завершения плана
```

**Interfaces** (`domain/repository/`):
- Определяют контракты для всех repositories
- Implementation-agnostic

**Use Cases** (`domain/usecase/`):

1. **CycleUseCase**
   - `initializeCycleForUser()` — Create/load cycle
   - `updateProgress()` — Update daily progress
   - `checkAndApplyMicrocycleProgression()` — Weight adaptation every 10 days
   - `calculateCompletionPercentage()` — Cycle progress %

2. **WorkoutUseCase**
   - `toggleExerciseCompletion()` — Mark complete
   - `getWorkoutPlan()` — Load current plan
   - `saveAdminWorkoutPlan()` — Admin uploads plan
   - `updateWorkoutSchedule()` — Schedule updates

3. **WeightProgressionUseCase**
   - `applyAdaptiveProgression()` — Auto-adjust weights
   - `calculateNextCycleWeights()` — Predict next cycle
   - Based on completion history

4. **ExerciseLibraryUseCase**
   - `getAllExercises()` — Full library
   - `getAlternativeExercises(muscleGroups)` — Substitutions
   - `searchExercises()` — Full-text search
   - `filterByEquipment()`, `filterByType()` — Filtration

5. **AuthUseCase**
   - `register(username, password, profile)` — Registration
   - `login(username, password)` — Authentication
   - `logout()` — Session cleanup
   - `validateCredentials()` — Input validation

6. **AdminUseCase**
   - `loginAdmin(masterPassword)` — Admin access
   - `uploadWorkoutPlan()` — Custom plan upload
   - `manageUsers()` — User management

**Calculators** (`domain/calculator/`):

1. **WeightCalculator.kt**
   ```kotlin
   // Базовый расчет
   baseWeight = userWeight * 0.6-0.8 (зависит от пола, уровня, цели)

   // Стандартные веса
   // Гантели: 1.25, 2.5, 5, 7.5, 10, 15, 20, 25, 30, 40, 50, 60 кг
   // Штанги: 2.5, 5, 10, 15, 20, 25, 30, 40, 50, 60, 80, 100 кг

   // Адаптивная прогрессия
   if (completionRate > 90%) → +5%
   if (completionRate > 70%) → +2.5%
   ```

2. **WorkoutDateCalculator.kt**
   - Calculate scheduled dates for each day
   - Handle workout frequency (1x, 3x, 5x per week)
   - Microcycle alignment (10-day blocks)
   - Rest days calculation

### Presentation Layer (`app/src/main/java/com/example/fitness_plan/presentation/`)

**ViewModels** (`presentation/viewmodel/`):

1. **WorkoutViewModel**
   - Current workout display
   - Exercise completion toggling
   - Alternative exercise selection
   - Real-time weight calculations
   - Statistics updates

2. **ProfileViewModel**
   - User profile management
   - Goal/level/frequency changes
   - Weight tracking
   - Favorite exercises management

3. **StatisticsViewModel**
   - Data aggregation
   - Progress calculations
   - Chart data preparation
   - Filtering and date range selection

4. **ExerciseLibraryViewModel**
   - Exercise search/filter
   - Alternative suggestions
   - Exercise details
   - Favorite marking

5. **AdminLoginViewModel**
   - Master password validation
   - Admin session management

### UI Layer (`app/src/main/java/com/example/fitness_plan/ui/`)

**Screens**:

1. **Authentication**
   - `LoginScreen.kt` — User login
   - `RegisterScreen.kt` — Registration
   - `UserProfileForm.kt` — Initial profile setup

2. **Main Application** (after auth)
   - `MainScreen.kt` — TabBar navigation container
   - `HomeScreen.kt` — Current workout display
   - `ProfileScreen.kt` — User profile management
   - `StatisticsScreen.kt` — Charts & analytics
   - `ExerciseLibraryScreen.kt` — Exercise browser
   - `CycleHistoryScreen.kt` — Past cycles

3. **Details/Nested**
   - `ExerciseDetailScreen.kt` — Full exercise info
   - `ExerciseGuideScreen.kt` — Step-by-step instructions
   - `ExerciseLibraryDetailScreen.kt` — Library item details

4. **Admin**
   - `AdminLoginScreen.kt` — Master password entry
   - `AdminMainScreen.kt` — Admin dashboard

**Components** (`ui/components/`):
- `OverallStatsCard.kt` — Stats summary widget

**Charts** (`ui/charts/`):
- `WeightChart.kt` — Weight progression over time (Vico)
- `VolumeChart.kt` — Total volume (sets × reps × weight)
- `FrequencyChart.kt` — Exercise frequency heatmap

**Theme** (`ui/theme/`):
- `Color.kt` — Material3 color palette
- `Theme.kt` — App theming (Material3)
- `Type.kt` — Typography

### Notification System (`app/src/main/java/com/example/fitness_plan/notification/`)

1. **NotificationHelper.kt**
   - Channel setup (Material3 colors)
   - Build notifications
   - Reminders scheduling

2. **ScheduleCheckWorker.kt**
   - WorkManager integration
   - Periodic checks (daily)
   - Trigger notifications

### Security (`app/src/main/java/com/example/fitness_plan/security/`)

1. **SecurityModule.kt**
   - Encrypted SharedPreferences setup
   - EncryptedSharedPreferences wrapper
   - Master key generation

**Encryption Strategy**:
- Android Security Crypto library
- Master key stored in Android Keystore
- Transparent encryption/decryption

---

## DataStore Schema

**Location**: `Context.dataStore` with encrypted SharedPreferences backend

**Keys**:
```kotlin
// User profiles
stringPreferencesKey("user_profiles") → JSON list

// Workout management
stringPreferencesKey("workout_plans") → Current/historical plans
stringPreferencesKey("cycles") → 30-day cycles history
stringPreferencesKey("exercises_completed") → Completion tracking

// Statistics
stringPreferencesKey("exercise_stats") → Per-exercise stats
stringPreferencesKey("workout_schedules") → Date-based scheduling

// Configuration
stringPreferencesKey("user_preferences") → Settings
stringPreferencesKey("favorite_exercises") → Set of exercise IDs
stringPreferencesKey("notifications_enabled") → Boolean

// Authentication
stringPreferencesKey("current_user") → Logged-in user ID
stringPreferencesKey("user_credentials") → BCrypt hashed passwords
```

**Access Pattern**:
```kotlin
// Write
context.dataStore.edit { preferences ->
    preferences[stringPreferencesKey("key")] = value
}

// Read (Flow)
context.dataStore.data.map { preferences ->
    preferences[stringPreferencesKey("key")] ?: defaultValue
}
```

---

## Enums & Constants

### ExerciseType (3 типа)
```
STRENGTH   → Силовые упражнения
CARDIO     → Кардио
STRETCHING → Растяжка
```

### EquipmentType (20+ типов)
```
BODYWEIGHT, DUMBBELLS, BARBELL, KETTLEBELL,
CABLE_MACHINE, LEVER_MACHINE, EXPANDER,
SMITH_MACHINE, SQUAT_RACK, BENCH,
TRX, RESISTANCE_BAND, MEDICINE_BALL,
MEDICINE_BALL_TWISTING, FOAM_ROLLER, EZ_BAR,
TRAP_BAR, SAFETY_BAR, AXLE_BAR, OTHER
```

### MuscleGroup (14 групп)
```
CHEST, TRICEPS, LATS, BICEPS, SHOULDERS,
ABS, FOREARMS, TRAPS, GLUTES, QUADS,
HAMSTRINGS, CALVES, LOWER_BACK, BRACHIALIS
```

### Goal (3 цели)
```
WEIGHT_LOSS        → Похудение
MUSCLE_BUILDING    → Наращивание мышечной массы
MAINTENANCE        → Поддержание формы
```

### Level (3 уровня)
```
BEGINNER    → Новичок
AMATEUR     → Любитель
PROFESSIONAL → Профессионал
```

### Frequency (3 варианта)
```
ONE_TIME_PER_WEEK      → 1x в неделю
THREE_TIMES_PER_WEEK   → 3x в неделю
FIVE_TIMES_PER_WEEK    → 5x в неделю
```

### Gender (2 пола)
```
MALE    → Мужской
FEMALE  → Женской
```

---

## Key Algorithms

### 1. Adaptive Weight Progression

**Микроцикл**: 10 дней, затем пересчет весов

```
Algorithm:
1. Track completion rate for each exercise over 10 days
2. On day 10:
   - If completion rate >= 90% → Weight += 5%
   - If completion rate 70-89% → Weight += 2.5%
   - If completion rate < 70% → Weight stays same or -2.5%
3. Round to nearest standard weight plate
4. Update WorkoutPlan for next 10 days
5. Notify user of changes
```

**Standard Plates**:
- Dumbbells: 1.25, 2.5, 5, 7.5, 10, 15, 20, 25, 30, 40, 50, 60 кг
- Barbells: 2.5, 5, 10, 15, 20, 25, 30, 40, 50, 60, 80, 100 кг

### 2. Workout Plan Generation

```
Input: UserProfile (weight, goal, level, frequency, gender)

Algorithm:
1. Select 3 main muscle group combinations based on frequency
2. For each day:
   - Select 4-6 exercises
   - Mix of compound + isolation
   - Vary equipment based on availability
   - Prefer favorite exercises (if provided)
3. Calculate base weight:
   baseWeight = userWeight × 0.6-0.8 (adjusted by gender/level)
4. Distribute exercises uniquely across 10-day plan
5. Return WorkoutPlan with scheduled dates

Uniqueness: No exercise repeats in same 10-day block
```

### 3. Cycle Management

```
Cycle Duration: 30 days (3 × 10-day microcycles)

Timeline:
- Days 1-10: Microcycle 1 (weights: W)
- Days 11-20: Microcycle 2 (weights: W + progression)
- Days 21-30: Microcycle 3 (weights: W + 2×progression)

On completion:
- Archive cycle in history
- Apply final progression calculations
- Generate new cycle with updated weights
```

---

## Recent Changes & Issues

### ✅ Recently Completed
1. **Statistics MVP** (v2.2)
   - Basic charts (Weight, Volume, Frequency)
   - Overall stats card
   - Date filtering

2. **Exercise Library Filters**
   - Equipment filter
   - Muscle group filter
   - Type filter
   - Combination filters

3. **Alternative Exercise Selection**
   - Replace exercises mid-workout
   - Filter by muscle group
   - Show availability (equipment)

### 🔧 Current Issues (REQ-001)

**Issue**: Exercise repetition when reopening app
- Root cause: Plan regenerated on each login
- Impact: Users lose selected exercises
- Solution: Persist plan to DataStore immediately after generation
- Status: In development

**Behavior** (current):
1. User creates profile → Plan generated (in memory)
2. User logout → Plan lost
3. User login → New plan generated (same exercises)

**Expected Behavior**:
1. User creates profile → Plan generated → Saved to DataStore
2. User logout → Plan preserved
3. User login → Plan loaded from DataStore → Same exercises

### 📋 Pending Features

1. **Cloud Sync** (future)
   - Backup to cloud
   - Multi-device sync

2. **Social Features** (future)
   - Share workouts
   - Compare stats
   - Friend challenges

3. **Wearable Integration** (future)
   - Smartwatch notifications
   - Heart rate tracking

---

## Testing Infrastructure

### Test Pyramid

```
     UI Tests (Instrumentation)
        ↑
        │
   ViewModels Tests
        ↑
        │
   Use Case Tests
        ↑
        │
   Unit Tests (Calculators, Repositories)
```

### Test Coverage by Layer

| Layer | Coverage | Status |
|-------|----------|--------|
| Domain Models | 95% | ✅ |
| Use Cases | 85% | ✅ |
| Calculators | 100% | ✅ |
| ViewModels | 70% | 🔄 |
| Repositories | 80% | ✅ |
| UI Components | 40% | 🚧 |

### Key Test Files

```
src/test/java/com/example/fitness_plan/
├── data/
│   ├── WorkoutRepositoryFavoriteExercisesTest.kt
│   ├── WorkoutPlanExerciseDistributionTest.kt
│   └── CredentialsRepositoryTest.kt
├── domain/
│   ├── usecase/
│   │   ├── WorkoutUseCaseTest.kt
│   │   └── WeightProgressionUseCaseTest.kt
│   └── calculator/
│       ├── WeightCalculatorTest.kt
│       └── WorkoutDateCalculatorTest.kt
├── presentation/
│   ├── viewmodel/
│   │   ├── WorkoutViewModelTest.kt
│   │   ├── ProfileViewModelTest.kt
│   │   └── ExerciseLibraryViewModelTest.kt
│   └── usecase/
│       └── AdminUseCaseTest.kt
└── ui/
    └── ExerciseLibraryScreenTest.kt

src/androidTest/java/com/example/fitness_plan/
├── ui/
│   ├── ExerciseLibraryScreenTest.kt
│   └── AdminMainScreenTest.kt
└── ExampleInstrumentedTest.kt
```

### Running Tests

```bash
# All unit tests
./gradlew testDebugUnitTest

# Specific test
./gradlew testDebugUnitTest --tests "com.example.fitness_plan.WorkoutViewModelTest"

# With coverage
./gradlew testDebugUnitTest jacocoTestReport

# Instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest
```

---

## Build Configuration

### build.gradle.kts Key Settings

```kotlin
android {
    namespace = "com.example.fitness_plan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitness_plan"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "2.2"
        multiDexEnabled = true  // Large number of classes
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xskip-metadata-version-check"
    }
}
```

### Key Dependencies

```
// Core
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0

// Compose
androidx.compose:compose-bom:2023.08.00
androidx.compose.material3:material3
androidx.navigation:navigation-compose:2.8.0

// Dependency Injection
com.google.dagger:hilt-android:2.51

// Data
androidx.datastore:datastore-preferences:1.1.1
org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3

// Security
androidx.security:security-crypto:1.1.0-alpha06
org.mindrot:jbcrypt:0.4

// Charts
com.patrykandpatrick.vico:compose-m3:2.0.0-beta.2

// WorkManager
androidx.work:work-runtime-ktx:2.9.0
```

---

## Code Patterns & Conventions

### Repository Pattern

```kotlin
// Interface (domain/repository/)
interface WorkoutRepository {
    suspend fun getCurrentWorkoutPlan(): Flow<WorkoutPlan?>
    suspend fun saveWorkoutPlan(plan: WorkoutPlan)
}

// Implementation (data/)
class WorkoutRepositoryImpl(
    @ApplicationContext private val context: Context
) : WorkoutRepository {
    override suspend fun getCurrentWorkoutPlan(): Flow<WorkoutPlan?> =
        context.dataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey("workout_plans")]
            json?.let { parseJson(it) }
        }
}
```

### ViewModel Pattern

```kotlin
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutRepository: WorkoutRepository,
    private val cycleUseCase: CycleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val plan = workoutRepository.getCurrentWorkoutPlan().first()
                _uiState.value = UiState.Success(plan)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }
}
```

### Composable Pattern

```kotlin
@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onNavigateToExerciseDetail: (exerciseName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Success -> SuccessScreen(uiState.data)
        is UiState.Error -> ErrorScreen(uiState.message)
    }
}
```

---

## Performance Optimization

### Memory Management
- **MultiDex**: Enabled (large app with 300+ classes)
- **Lazy Loading**: Use `lazy { }` for expensive resources
- **State Optimization**: Remember composables only what's needed
- **Flow Operators**: Use `distinctUntilChanged()`, `debounce()` where applicable

### Data Access
- **Coroutines**: Always use for IO operations
- **Flow**: Prefer over LiveData
- **Caching**: DataStore automatically caches preferences
- **Pagination**: (Future) for large exercise lists

### UI Rendering
- **Compose Recomposition**: Minimize scope with local state
- **Remember**: Cache expensive computations
- **Derivation**: Use derived state (`derivedStateOf`) for derived data
- **Keys**: Use explicit keys in lists for better performance

---

## Debugging & Troubleshooting

### Common Issues

1. **DataStore Serialization Errors**
   ```kotlin
   // Problem: JSON parse fails
   // Solution: Validate JSON before parsing, use try-catch
   try {
       val plan = Gson().fromJson(jsonString, WorkoutPlan::class.java)
   } catch (e: JsonSyntaxException) {
       Log.e("DataStore", "Parse failed", e)
   }
   ```

2. **Weight Calculation Rounding**
   ```kotlin
   // Problem: Weight doesn't match plate
   // Solution: Use roundToNearestStandardPlate()
   fun roundToNearestStandardPlate(weight: Float): Float =
       standardPlates.minByOrNull { abs(it - weight) } ?: weight
   ```

3. **Microcycle Progression Not Triggering**
   ```kotlin
   // Problem: Days not counted correctly
   // Cause: Scheduled dates might be in future
   // Solution: Verify dates calculation in WorkoutDateCalculator
   ```

4. **Exercise Repetition Issue (REQ-001)**
   ```kotlin
   // Problem: Same exercises after logout/login
   // Root: Plan not persisted to DataStore
   // Solution: Save plan immediately after generation
   // File: WorkoutRepositoryImpl.generateNewPlan()
   ```

### Debug Tips

1. **DataStore Inspection**:
   ```bash
   adb shell run-as com.example.fitness_plan cat /data/data/com.example.fitness_plan/shared_prefs/store_name.xml
   ```

2. **Logcat Filtering**:
   ```bash
   adb logcat | grep "Fitness\|DEBUG\|ERROR"
   ```

3. **Database Dumps** (for future Room integration):
   ```bash
   adb shell run-as com.example.fitness_plan \
     cp /data/data/com.example.fitness_plan/databases/fitness_db.db /sdcard/
   ```

---

## File Navigation Guide

### Most Frequently Modified Files

1. **UI Screens**: `app/src/main/java/com/example/fitness_plan/ui/*.kt`
   - HomeScreen.kt
   - StatisticsScreen.kt
   - ExerciseLibraryScreen.kt

2. **ViewModels**: `app/src/main/java/com/example/fitness_plan/presentation/viewmodel/`
   - WorkoutViewModel.kt
   - ProfileViewModel.kt
   - StatisticsViewModel.kt

3. **Use Cases**: `app/src/main/java/com/example/fitness_plan/domain/usecase/`
   - CycleUseCase.kt
   - WorkoutUseCase.kt
   - WeightProgressionUseCase.kt

4. **Repositories**: `app/src/main/java/com/example/fitness_plan/data/`
   - WorkoutRepositoryImpl.kt
   - ExerciseLibraryRepositoryImpl.kt

5. **Calculators**: `app/src/main/java/com/example/fitness_plan/domain/calculator/`
   - WeightCalculator.kt
   - WorkoutDateCalculator.kt

### Important Configuration Files

- `app/build.gradle.kts` — Dependencies and build config
- `build.gradle.kts` (root) — Project-level config
- `gradle.properties` — Build flags and versioning
- `.github/workflows/ci.yml` — CI/CD pipeline
- `.github/workflows/auto-version.yml` — Auto versioning

---

## Security Considerations

### Implemented Security
✅ BCrypt password hashing (not stored in plain text)
✅ Encrypted SharedPreferences (via Android Security Crypto)
✅ Master key in Android Keystore
✅ No sensitive data in DataStore keys (encryption at storage layer)
✅ Admin master password protected

### Future Security Improvements
🔒 OAuth2 authentication (instead of local credentials)
🔒 JWT tokens for future API integration
🔒 Biometric authentication
🔒 SSL/TLS for cloud sync (future)
🔒 Data encryption at rest (future)

---

## Deployment & Distribution

### Signing Configuration
- **Keystore File**: `fitness_plan.jks`
- **Configuration**: `keystore.properties`
- **Example**: `keystore.properties.example`

### Build Variants
- **Debug**: Full logging, no obfuscation
- **Release**: ProGuard (disabled currently)

### APK Output
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

### Version Management
Auto-updated via GitHub Actions:
- Merged PR on main → Version bump
- versionCode: +1
- versionName: Minor +1

---

## Documentation Files

- **SYSTEM_PROMPT.md** (this file) — Comprehensive project guide
- **TESTING.md** — Testing documentation
- **docs/requirements/REQ-001-FixExerciseRepetition.md** — Exercise persistence requirement
- **Code Comments** — KDoc for public APIs, inline for complex logic

---

## Contact & Support

**Project Maintainer**: Igor (@username)
**Repository**: GitHub (fitness-plan)
**Issue Tracker**: GitHub Issues
**Wiki**: GitHub Wiki (planned)

### Useful Commands

```bash
# Development
./gradlew clean build
./gradlew installDebug
./gradlew runAdb

# Testing
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest

# Analysis
./gradlew lint
./gradlew jacocoTestReport

# Build
./gradlew assembleDebug
./gradlew assembleRelease
```

---

**Last Updated**: 2026-02-16
**Document Version**: 2.0
**Status**: Complete & Current
