# Quick Reference Guide - Fitness Plan

## 🚀 Quick Start

### Build & Run
```bash
./gradlew clean build          # Clean build
./gradlew installDebug          # Install on device
./gradlew testDebugUnitTest     # Run tests
```

### Project Structure at a Glance
```
com.example.fitness_plan/
├── data/          → Repositories, DataStore access
├── domain/        → Use cases, business logic, models
├── presentation/  → ViewModels (UI logic)
└── ui/            → Jetpack Compose screens
```

---

## 📱 Main Features

| Feature | Status | Key File |
|---------|--------|----------|
| User Auth | ✅ | AuthUseCase.kt, LoginScreen.kt |
| Workout Plans | ✅ | WorkoutRepositoryImpl.kt |
| Exercise Library | ✅ | ExerciseLibraryRepositoryImpl.kt |
| Weight Progression | ✅ | WeightCalculator.kt |
| Statistics | ✅ | StatisticsScreen.kt |
| Admin Panel | ✅ | AdminMainScreen.kt |
| Notifications | ✅ | NotificationHelper.kt |
| **Plan Persistence** | 🔧 | REQ-001 (DataStore issue) |

---

## 🔑 Key Classes

### ViewModels
- `WorkoutViewModel` → Current workout display
- `ProfileViewModel` → User profile management
- `StatisticsViewModel` → Charts & analytics
- `ExerciseLibraryViewModel` → Exercise browser

### Repositories
- `WorkoutRepository` → Workout plans
- `ExerciseLibraryRepository` → 100+ exercises
- `CycleRepository` → 30-day cycles
- `UserRepository` → User profiles

### Use Cases
- `WorkoutUseCase` → Exercise toggling, plan loading
- `CycleUseCase` → Cycle management, progression
- `WeightProgressionUseCase` → Adaptive weight adjustment
- `AuthUseCase` → Login/register

### Screens
- `HomeScreen` → Current workout
- `StatisticsScreen` → Charts
- `ExerciseLibraryScreen` → Exercise browser
- `ProfileScreen` → User settings

---

## 📊 Data Flow

```
User Creates Profile
    ↓
CycleUseCase.initializeCycleForUser()
    ↓
WorkoutRepositoryImpl.generateNewPlan()
    ↓
Plan saved to DataStore ⚠️ (ISSUE: not persisted)
    ↓
User logs in
    ↓
Plan loaded from DataStore ⚠️ (loads empty/new)
    ↓
HomeScreen displays workout
```

---

## 🔍 Important Files to Know

### If you're working on...

**Weight Progression**
→ `WeightCalculator.kt` + `WeightProgressionUseCase.kt`

**Plan Generation**
→ `WorkoutRepositoryImpl.kt` (generateNewPlan method)

**Exercise Alternatives**
→ `ExerciseLibraryRepositoryImpl.kt` + `WorkoutViewModel.kt`

**Statistics/Charts**
→ `StatisticsScreen.kt` + `*Chart.kt` files

**User Profiles**
→ `ProfileViewModel.kt` + `UserRepository.kt`

**Authentication**
→ `AuthUseCase.kt` + `CredentialsRepository.kt`

**Admin Features**
→ `AdminMainScreen.kt` + `AdminCredentialsRepository.kt`

---

## 🐛 Current Issue: REQ-001

**Problem**: Exercise plan regenerates on every login

**Root Cause**:
```kotlin
// WorkoutRepositoryImpl.kt - plan is generated but not saved to DataStore
fun generateNewPlan(): WorkoutPlan {
    // ✅ Plan generated in memory
    // ❌ Not saved to DataStore
    return plan
}
```

**Solution**: Add DataStore persistence
```kotlin
// AFTER generating plan:
context.dataStore.edit { preferences ->
    preferences[stringPreferencesKey("current_workout_plan")] = Gson().toJson(plan)
}
```

**File**: `docs/requirements/REQ-001-FixExerciseRepetition.md`

---

## 🏗️ Architecture Rules

✅ **DO**:
- Use Clean Architecture (data → domain → presentation)
- Inject dependencies via Hilt
- Use Flow for data streams
- Launch coroutines in viewModelScope
- Save data to DataStore
- Test business logic

❌ **DON'T**:
- Store UI state in repository
- Call repository directly from Composable
- Use SharedPreferences directly (use DataStore)
- Block UI thread
- Mix business logic with UI logic

---

## 🧪 Testing

### Run Tests
```bash
# All tests
./gradlew testDebugUnitTest

# Specific test
./gradlew testDebugUnitTest --tests "WorkoutViewModelTest"

# With coverage
./gradlew jacocoTestReport
```

### Test Location: `src/test/java/com/example/fitness_plan/`
```
domain/calculator/      → Unit math tests ✅ (100% coverage)
domain/usecase/         → Business logic tests ✅ (85%)
presentation/viewmodel/ → ViewModel tests 🔄 (70%)
data/                   → Repository mocks ✅
ui/                     → Utility tests ✅
```

---

## 📦 Dependencies

**Core**: Kotlin, Jetpack Compose, Material3
**DI**: Hilt 2.51
**Storage**: DataStore Preferences
**Security**: Android Security Crypto, BCrypt
**Charts**: Vico Compose M3
**Testing**: JUnit, Mockito, Mockk, Coroutines Test

---

## 🚀 Build Variants

- **debug** → Full logging, no obfuscation
- **release** → Optimized, ProGuard (disabled currently)

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

---

## 🔐 Security

✅ Encrypted SharedPreferences (Android Security Crypto)
✅ BCrypt password hashing
✅ Master key in Android Keystore
🔒 OAuth (future)
🔒 JWT tokens (future)

---

## 📱 UI Navigation

```
Login/Register
    ↓
User Profile Form
    ↓
Main Tabs:
├── 🏠 Home (Current Workout)
├── 👤 Profile (User Settings)
├── 📊 Statistics (Charts)
├── 📚 Exercise Library
└── 🔄 Cycle History

+ Admin Panel (separate login)
```

---

## 🔄 Git Workflow

```bash
# Feature branch
git checkout -b feature/feature-name
git push -u origin feature/feature-name

# Create PR → CI/CD runs tests
# After approval → Merge to main/develop

# Auto-versioning on main merge
# versionCode +1, versionName (minor +1)
```

---

## 💡 Pro Tips

1. **Always save to DataStore after changes**
   ```kotlin
   context.dataStore.edit { /* your changes */ }
   ```

2. **Use Flow for reactive data**
   ```kotlin
   val data: Flow<MyData> = dataStore.data.map { /* parse */ }
   ```

3. **Test with coroutines**
   ```kotlin
   @Test
   fun test() = runTest {
       // your async code
   }
   ```

4. **Round weights to standard plates**
   ```kotlin
   val rounded = WeightCalculator.roundToNearestPlate(weight)
   ```

5. **Log DataStore issues**
   ```bash
   adb logcat | grep "DataStore\|ERROR"
   ```

---

## 📚 Related Documentation

- **SYSTEM_PROMPT.md** — Full project documentation
- **TESTING.md** — Testing guide
- **REQ-001** — Exercise persistence requirement
- **GitHub Issues** — Bug tracking

---

**Last Updated**: 2026-02-16 | **Version**: 2.2
