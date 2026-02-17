# Architecture Overview - Fitness Plan

## Clean Architecture Implementation

```
┌────────────────────────────────────────────────────────────────┐
│                     UI LAYER (Presentation)                    │
│                    Jetpack Compose Screens                     │
├────────────────────────────────────────────────────────────────┤
│  LoginScreen  │ HomeScreen  │ ProfileScreen  │ StatisticsScreen │
│ ExerciseLib   │ CycleHistory│  AdminScreen   │   DetailScreens   │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ↓
┌────────────────────────────────────────────────────────────────┐
│              PRESENTATION LAYER (ViewModels)                   │
│             Hilt-Injected @HiltViewModel                       │
├────────────────────────────────────────────────────────────────┤
│ WorkoutViewModel        │ ProfileViewModel                      │
│ StatisticsViewModel     │ ExerciseLibraryViewModel              │
│ AdminLoginViewModel     │ (UI State + Event Handling)           │
└──────────────────────────┬───────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
┌──────────────────┐ ┌───────────────┐ ┌──────────────┐
│  Domain Layer    │ │ Calculators   │ │ Use Cases    │
│  (Interfaces)    │ │ (Business     │ │ (Business    │
│                  │ │  Logic)       │ │  Logic)      │
└────────┬─────────┘ └───────┬───────┘ └──────┬───────┘
         │                   │                 │
         └───────────────────┼─────────────────┘
                             │
                             ↓
        ┌────────────────────────────────────────────┐
        │       DOMAIN LAYER                         │
        │  Repository Interfaces + Models + UseCases │
        ├────────────────────────────────────────────┤
        │                                            │
        │  Interfaces:                               │
        │  • WorkoutRepository                       │
        │  • ExerciseLibraryRepository              │
        │  • CycleRepository                        │
        │  • UserRepository                         │
        │  • ExerciseCompletionRepository           │
        │  • WorkoutScheduleRepository              │
        │  • NotificationRepository                 │
        │  • CredentialsRepository                  │
        │                                            │
        │  Models (domain/model/):                  │
        │  • UserProfile, WorkoutPlan              │
        │  • Exercise, Cycle, ExerciseLibrary      │
        │  • ExerciseStats, PlanHistory            │
        │                                            │
        │  Calculators (domain/calculator/):        │
        │  • WeightCalculator                      │
        │  • WorkoutDateCalculator                 │
        │                                            │
        │  Use Cases (domain/usecase/):             │
        │  • WorkoutUseCase                        │
        │  • CycleUseCase                          │
        │  • AuthUseCase                           │
        │  • WeightProgressionUseCase              │
        │  • ExerciseLibraryUseCase                │
        │  • AdminUseCase                          │
        │                                            │
        └────────────────┬───────────────────────────┘
                         │
                         ↓
        ┌────────────────────────────────────────────┐
        │      DATA LAYER (Implementation)           │
        │     Repository Implementations             │
        ├────────────────────────────────────────────┤
        │                                            │
        │  WorkoutRepositoryImpl                      │
        │  ├─→ Plan generation                      │
        │  ├─→ Exercise selection                   │
        │  └─→ DataStore persistence                │
        │                                            │
        │  ExerciseLibraryRepositoryImpl              │
        │  ├─→ 100+ exercises database              │
        │  ├─→ Filtering & search                   │
        │  └─→ Alternative suggestions              │
        │                                            │
        │  CycleRepository                           │
        │  ├─→ Cycle tracking (30 days)             │
        │  ├─→ Microcycle management (10 days)      │
        │  └─→ History persistence                  │
        │                                            │
        │  CredentialsRepository                     │
        │  ├─→ BCrypt password hashing              │
        │  ├─→ User authentication                  │
        │  └─→ Session management                   │
        │                                            │
        │  Other Repositories:                       │
        │  • ExerciseCompletionRepository           │
        │  • ExerciseStatsRepository                │
        │  • WorkoutScheduleRepository              │
        │  • UserRepository                         │
        │  • NotificationRepository                 │
        │  • Admin repositories                     │
        │                                            │
        └────────────────┬───────────────────────────┘
                         │
        ┌────────────────┴────────────────────┐
        │                                     │
        ↓                                     ↓
┌──────────────────┐              ┌────────────────────┐
│  DataStore       │              │  Android Crypto    │
│  Preferences     │              │  SharedPreferences │
│                  │              │  (Encrypted)       │
│  Storage:        │              │                    │
│  • Profiles      │              │  Encryption:       │
│  • Plans         │              │  • Master key      │
│  • Cycles        │              │  • Keystore        │
│  • Stats         │              │                    │
│  • Schedules     │              │ Password Hashing:  │
│  • Credentials   │              │  • BCrypt 0.4      │
│  • Preferences   │              │                    │
└──────────────────┘              └────────────────────┘
```

---

## Dependency Injection with Hilt

### DI Graph Structure

```
┌─────────────────────────────────────────┐
│         Hilt Application Class           │
│     (FitnessPlanApplication.kt)          │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│        AppModule (Hilt Modules)         │
├─────────────────────────────────────────┤
│                                         │
│  @Module                                │
│  @InstallIn(SingletonComponent::class)  │
│  object AppModule {                     │
│                                         │
│    @Singleton                           │
│    @Provides                            │
│    fun provideContext(app: Application) │
│      = app                              │
│                                         │
│    @Singleton                           │
│    @Provides                            │
│    fun provideWorkoutRepository(        │
│      @ApplicationContext ctx: Context   │
│    ): WorkoutRepository =               │
│      WorkoutRepositoryImpl(ctx)          │
│                                         │
│    @Provides                            │
│    fun provideWorkoutUseCase(           │
│      repo: WorkoutRepository,           │
│      cycleUseCase: CycleUseCase         │
│    ): WorkoutUseCase =                  │
│      WorkoutUseCase(repo, cycleUseCase) │
│                                         │
│    // ... other providers               │
│                                         │
│  }                                      │
│                                         │
└─────────────────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│      Component Injection Points         │
├─────────────────────────────────────────┤
│                                         │
│  @HiltViewModel                         │
│  class WorkoutViewModel @Inject         │
│    constructor(                         │
│      private val workoutUseCase: ...    │
│      private val cycleUseCase: ...      │
│    ) : ViewModel()                      │
│                                         │
│  @Composable                            │
│  fun HomeScreen(                        │
│    viewModel: WorkoutViewModel =        │
│      hiltViewModel()                    │
│  ) { ... }                              │
│                                         │
└─────────────────────────────────────────┘
```

---

## Data Flow Diagram

### 1. User Registration & Profile Setup

```
┌─────────────────┐
│  RegisterScreen │
└────────┬────────┘
         │ User enters credentials
         ↓
┌──────────────────────────┐
│   AuthUseCase.register() │
└────────┬─────────────────┘
         │ Hash password with BCrypt
         ↓
┌─────────────────────────────────────────┐
│  CredentialsRepository.saveCredentials()│
│  └─→ Encrypt & save to SharedPreferences │
└────────┬────────────────────────────────┘
         │
         ↓
┌────────────────────────┐
│  UserProfileForm       │
│  (Goal, Level, Freq)   │
└────────┬───────────────┘
         │ Save profile
         ↓
┌──────────────────────────────────────┐
│  UserRepository.saveProfile()        │
│  └─→ Save to DataStore              │
└────────┬─────────────────────────────┘
         │
         ↓
┌──────────────────────────────────────┐
│  CycleUseCase.initializeCycleForUser()│
└────────┬─────────────────────────────┘
         │ Check if new cycle needed
         ↓
```

### 2. Workout Plan Generation

```
┌──────────────────────────────────────┐
│  CycleUseCase.initializeCycleForUser()│
└────────┬─────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────────┐
│  WorkoutRepositoryImpl.generateNewPlan()    │
├────────────────────────────────────────────┤
│                                            │
│  1. Load UserProfile from DataStore        │
│  2. Select muscle group split based on     │
│     frequency (1x/3x/5x)                   │
│  3. For each day (10 days):                │
│     a) Select 4-6 exercises               │
│     b) Mix compound + isolation            │
│     c) Check favorites                     │
│     d) Ensure uniqueness in 10-day plan   │
│  4. Calculate base weight:                 │
│     W = userWeight × 0.6-0.8               │
│  5. Assign weight to each exercise         │
│  6. Round to standard plates               │
│  7. Assign scheduled dates (WorkoutDate    │
│     Calculator)                            │
│  8. Return WorkoutPlan object              │
│                                            │
└────────┬─────────────────────────────────┘
         │ ⚠️ ISSUE: Not persisted
         ↓
┌────────────────────────────────────────────┐
│  ❌ WorkoutRepositoryImpl should:           │
│     context.dataStore.edit {               │
│       preferences[key] = Gson().toJson(    │
│         plan                               │
│       )                                    │
│     }                                      │
└────────┬─────────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────────┐
│  CycleRepository.saveCycle()               │
│  └─→ Save Cycle object to DataStore       │
└────────┬─────────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────────┐
│  HomeScreen displays WorkoutPlan           │
└────────────────────────────────────────────┘
```

### 3. Exercise Completion & Progress

```
┌────────────────────────────┐
│  User toggles exercise     │
│  in HomeScreen             │
└────────┬───────────────────┘
         │
         ↓
┌────────────────────────────────────┐
│  WorkoutViewModel.toggleExercise() │
└────────┬─────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────┐
│  WorkoutUseCase.toggleExerciseCompletion(│
├────────────────────────────────────────┤
│  1. Mark exercise as completed         │
│  2. Update WorkoutPlan in memory       │
│  3. Update Exercise.isCompleted = true │
│  4. Save updated plan to DataStore     │
│                                        │
└────────┬─────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────┐
│  ExerciseCompletionRepository.save()   │
│  └─→ Persist completion to DataStore   │
└────────┬─────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────┐
│  ExerciseStatsRepository.recordStat()  │
│  └─→ Save weight/reps/volume           │
└────────┬─────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────┐
│  CycleUseCase.updateProgress()         │
│  1. Increment daysCompleted            │
│  2. Check if microcycle complete (10d) │
│  3. If yes → checkAndApplyMicrocycle   │
│     Progression()                      │
│                                        │
└────────┬─────────────────────────────┘
         │
         ↓
```

### 4. Adaptive Weight Progression (Every 10 Days)

```
┌──────────────────────────────────────────┐
│  Day 10 of Microcycle Reached            │
│  CycleUseCase.checkAndApply              │
│  MicrocycleProgression()                 │
└────────┬─────────────────────────────────┘
         │
         ↓
┌──────────────────────────────────────────┐
│  WeightProgressionUseCase                │
│  .applyAdaptiveProgression()             │
├──────────────────────────────────────────┤
│                                          │
│  For each exercise:                      │
│  1. Load ExerciseStats from DataStore    │
│  2. Calculate completion rate (10d)      │
│  3. If rate ≥ 90%  → +5% weight         │
│  4. If rate 70-89% → +2.5% weight       │
│  5. If rate < 70%  → -2.5% weight       │
│  6. Round to nearest standard plate      │
│  7. Update exercise.weight              │
│                                          │
└────────┬─────────────────────────────────┘
         │
         ↓
┌──────────────────────────────────────────┐
│  WorkoutRepository.updateExerciseWeights │
│  └─→ Save updated WorkoutPlan           │
└────────┬─────────────────────────────────┘
         │
         ↓
┌──────────────────────────────────────────┐
│  NotificationRepository.notify()         │
│  └─→ Show weight change notification     │
└──────────────────────────────────────────┘
```

### 5. Statistics Display

```
┌──────────────────────┐
│  StatisticsScreen    │
└────────┬─────────────┘
         │ Load data
         ↓
┌──────────────────────────────────┐
│  StatisticsViewModel             │
│  .loadStatistics()               │
└────────┬─────────────────────────┘
         │
         ├─ Get ExerciseStats (all)
         │   └─→ ExerciseStatsRepository
         │       .getAllStats()
         │
         ├─ Get Cycle history
         │   └─→ CycleRepository
         │       .getCycleHistory()
         │
         └─ Calculate aggregates:
            • Overall weight progression
            • Total volume per exercise
            • Frequency heatmap
            • Completion rate
            └─→ Charts display data
```

---

## State Management Flow

### ViewModel State Pattern

```
┌──────────────────────────────────┐
│  Composable Function             │
│  (e.g., HomeScreen)              │
└────────┬─────────────────────────┘
         │ collectAsState()
         ↓
┌──────────────────────────────────┐
│  val uiState by              │
│  viewModel.uiState               │
│  .collectAsState()               │
└────────┬─────────────────────────┘
         │
         ↓
┌──────────────────────────────────────────┐
│  ViewModel                               │
│                                          │
│  private val _uiState =                  │
│    MutableStateFlow<UiState>(            │
│      UiState.Loading                     │
│    )                                     │
│  val uiState = _uiState.asStateFlow()    │
│                                          │
│  init {                                  │
│    viewModelScope.launch {               │
│      try {                               │
│        val data = repository.get...()    │
│        _uiState.value =                  │
│          UiState.Success(data)           │
│      } catch (e: Exception) {            │
│        _uiState.value =                  │
│          UiState.Error(e.message)        │
│      }                                   │
│    }                                     │
│  }                                       │
│                                          │
│  fun onAction(action: UiAction) {        │
│    viewModelScope.launch {               │
│      val result = useCase.execute(...)   │
│      _uiState.value = ...                │
│    }                                     │
│  }                                       │
│                                          │
└──────────────────────────────────────────┘
```

---

## Navigation Structure

```
┌─────────────────────────────────────────────────┐
│  NavHost                                        │
│  (Hilt Navigation Compose)                      │
├─────────────────────────────────────────────────┤
│                                                 │
│  Authentication Routes:                         │
│  ├─ login_screen                               │
│  │  └─ → register_screen                       │
│  │     └─ → profile_form/{username}            │
│  │        └─ → main_tabs                       │
│  └─ admin_login                                │
│     └─ → admin_main                            │
│                                                 │
│  Main App Routes (after login):                │
│  ├─ main_tabs (BottomNavigation)                │
│  │  ├─ home                                    │
│  │  │  ├─ → exercise_detail/{exerciseName}    │
│  │  │  └─ → exercise_guide/{exerciseId}       │
│  │  ├─ profile                                 │
│  │  ├─ statistics                              │
│  │  ├─ exercise_library                        │
│  │  └─ cycle_history                           │
│  │                                             │
│  └─ Nested Destinations:                       │
│     ├─ exercise_library_detail/{id}            │
│     ├─ admin_main (nested)                     │
│     └─ etc.                                    │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## Threading & Coroutines

```
┌────────────────────────────────────────────────┐
│  UI Thread (Main)                              │
│  ├─ Composable rendering                       │
│  └─ State updates trigger recomposition        │
└──────────────────┬─────────────────────────────┘
                   │
                   ↓
┌────────────────────────────────────────────────┐
│  viewModelScope.launch (Dispatchers.Main)      │
│  (launched on Main, can be suspended)          │
├────────────────────────────────────────────────┤
│                                                │
│  viewModelScope.launch {                       │
│    val data = withContext(Dispatchers.IO) {    │
│      // IO operations:                         │
│      // • DataStore reads/writes               │
│      // • Encryption/decryption                │
│      // • JSON serialization                   │
│    }                                           │
│    _uiState.value = UiState.Success(data)      │
│  }                                             │
│                                                │
└────────────────────────────────────────────────┘
```

---

## Layering Rules

### ✅ Allowed Dependencies

```
UI (Compose)
  ↓ uses
Presentation (ViewModel)
  ↓ uses
Domain (UseCase/Model)
  ↓ uses
Data (Repository)
  ↓ accesses
Storage (DataStore)
```

### ❌ Not Allowed

```
❌ UI → Repository (skip ViewModel/UseCase)
❌ ViewModel → UI components
❌ Repository → UseCase (wrong direction)
❌ Domain → Data (Interfaces only)
❌ Circular dependencies
```

---

## Security Layers

```
┌────────────────────────────────────────┐
│  User Input                            │
│  (LoginScreen, RegisterScreen)         │
└────────┬───────────────────────────────┘
         │
         ↓ Validate input
┌────────────────────────────────────────┐
│  AuthUseCase                           │
│  .validateInput()                      │
└────────┬───────────────────────────────┘
         │
         ↓ Hash password
┌────────────────────────────────────────┐
│  PasswordHasher (BCrypt)               │
│  .hash(password) → bcryptHash          │
└────────┬───────────────────────────────┘
         │
         ↓ Encrypt & store
┌────────────────────────────────────────┐
│  CredentialsRepository                 │
│  .saveCredentials()                    │
└────────┬───────────────────────────────┘
         │
         ↓
┌────────────────────────────────────────┐
│  EncryptedSharedPreferences            │
│  (Android Security Crypto)             │
│  └─ Master key in Keystore             │
│  └─ Automatic encryption/decryption    │
└────────────────────────────────────────┘
```

---

## Testing Architecture

```
┌─────────────────────────────────────┐
│  Test Structure                     │
├─────────────────────────────────────┤
│                                     │
│  Domain Layer Tests (Unit)          │
│  ├─ Model validation ✅             │
│  ├─ Calculator logic ✅             │
│  └─ UseCase flows ✅                │
│                                     │
│  Data Layer Tests (Unit)            │
│  ├─ Repository logic (mocked)       │
│  ├─ Serialization                   │
│  └─ Persistence mocks               │
│                                     │
│  Presentation Tests (Unit)          │
│  ├─ ViewModel state                 │
│  ├─ Event handling                  │
│  └─ Coroutine flows                 │
│                                     │
│  UI Tests (Integration)             │
│  ├─ Screen rendering                │
│  ├─ User interactions               │
│  └─ Navigation (Instrumentation)    │
│                                     │
└─────────────────────────────────────┘
```

---

**Last Updated**: 2026-02-16
**Version**: 2.2
