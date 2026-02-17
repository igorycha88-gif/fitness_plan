# Developer Guidelines - Fitness Plan

## 🚀 Welcome, Developer!

This document outlines **mandatory requirements, best practices, and quality criteria** for developing features in the Fitness Plan application. Following these guidelines ensures code quality, consistency, and project maintainability.

---

## 📚 MANDATORY: Project Documentation Study

### Before Starting Any Work

**You MUST read and understand the following documentation:**

1. **QUICK_REFERENCE.md** (5 min) ⚡
   - Quick overview of the project
   - Build commands
   - Current issues

2. **DOCUMENTATION_INDEX.md** (10 min) 🗺️
   - Navigation guide
   - "I want to..." section
   - Learning paths

3. **SYSTEM_PROMPT.md** (30-60 min) ⭐
   - Complete architecture
   - All components
   - Algorithms & business logic
   - Related to your feature

4. **ARCHITECTURE.md** (20-30 min) 🏗️
   - Data flow diagrams
   - State management
   - Security layers
   - Related to your feature

5. **Feature-Specific Documentation**
   - If working on weight progression → Read "Key Algorithms" section
   - If working on plan generation → Read "Workout Plan Generation"
   - If working on UI → Read "Navigation Structure"
   - If working on testing → Read "TESTING.md"

### Verification Checklist

Before proceeding with implementation, confirm:
- ✅ I've read all relevant documentation sections
- ✅ I understand the current architecture
- ✅ I understand the data flow for my feature
- ✅ I understand related components and dependencies
- ✅ I've identified potential issues or conflicts

**If you skip this step, your PR will be rejected.**

---

## 📋 MANDATORY: Todo Planning Before Implementation

### Step 1: Create Todo List

Before writing any code, you MUST create a detailed breakdown of tasks:

```markdown
## Feature: [Feature Name]

### Tasks:
1. [ ] Research & Planning
   - Read documentation
   - Understand architecture
   - Identify dependencies

2. [ ] Model Layer
   - Create/update domain models
   - Add repository interfaces
   - Write unit tests for models

3. [ ] Data Layer
   - Implement repository methods
   - Add DataStore persistence
   - Mock for testing

4. [ ] Domain/Business Logic
   - Implement use cases
   - Write business logic
   - Unit tests (85%+ coverage)

5. [ ] Presentation Layer
   - Create ViewModel
   - Implement UI state management
   - Unit tests for ViewModel

6. [ ] UI Layer
   - Create Composable screens
   - Connect to ViewModel
   - Add navigation routes

7. [ ] Integration Testing
   - Test data flow end-to-end
   - Test user interactions
   - Verify navigation

8. [ ] Documentation
   - Update SYSTEM_PROMPT.md
   - Add code comments
   - Update related docs

9. [ ] Code Review Preparation
   - Run all tests
   - Check code style
   - Self-review changes
```

### Step 2: Get Approval from Igor

**Create a GitHub Issue or message with:**

```markdown
### Feature: [Feature Name]

**Description**:
[Brief description of the feature]

**Architecture Impact**:
- [ ] New models
- [ ] New repositories
- [ ] New use cases
- [ ] New screens
- [ ] Data flow changes

**Tasks**:
[Paste the todo list from Step 1]

**Questions/Concerns**:
[Any architectural questions or concerns]

**Estimated Timeline**:
- Research: X hours
- Implementation: X hours
- Testing: X hours
- Review: X hours
```

**Wait for approval before proceeding with implementation.**

### Step 3: Update Todo List During Development

Mark tasks as `in_progress` and `completed` as you work:

```markdown
- [x] Research & Planning (COMPLETED)
- [x] Model Layer (COMPLETED)
- [ ] Data Layer (IN PROGRESS)
- [ ] Domain/Business Logic (PENDING)
```

---

## 🏗️ MANDATORY: Architecture Respect

### Layering Rules (NEVER BREAK THESE)

#### ✅ ALLOWED Dependencies

```
UI (Composable)
  ↓ uses
Presentation (ViewModel + @HiltViewModel)
  ↓ uses
Domain (UseCase + Interface)
  ↓ uses
Data (Implementation)
  ↓ accesses
Storage (DataStore, Encryption)
```

#### ❌ NOT ALLOWED (Will cause PR rejection)

- UI directly accessing Repository
- ViewModel using another ViewModel
- Repository calling UseCase
- Domain layer depending on Data layer (use interfaces only)
- Business logic in UI layer
- UI state in Repository
- Circular dependencies

### Implementation Template

#### 1. Domain Layer First

```kotlin
// domain/model/MyModel.kt
data class MyModel(
    val id: String,
    val name: String,
    val value: Double
)

// domain/repository/MyRepository.kt
interface MyRepository {
    suspend fun getData(): Flow<MyModel?>
    suspend fun saveData(model: MyModel)
}

// domain/usecase/MyUseCase.kt
class MyUseCase(
    private val repository: MyRepository
) {
    suspend fun execute(): MyModel? =
        repository.getData().first()
}
```

#### 2. Data Layer Second

```kotlin
// data/MyRepositoryImpl.kt
class MyRepositoryImpl(
    @ApplicationContext private val context: Context
) : MyRepository {
    override suspend fun getData(): Flow<MyModel?> =
        context.dataStore.data.map { prefs ->
            val json = prefs[stringPreferencesKey("my_data")]
            json?.let { Gson().fromJson(it, MyModel::class.java) }
        }

    override suspend fun saveData(model: MyModel) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("my_data")] =
                Gson().toJson(model)
        }
    }
}
```

#### 3. Presentation Layer Third

```kotlin
// presentation/viewmodel/MyViewModel.kt
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val data = useCase.execute()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }
}
```

#### 4. UI Layer Last

```kotlin
// ui/MyScreen.kt
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Success -> SuccessScreen((uiState as UiState.Success).data)
        is UiState.Error -> ErrorScreen((uiState as UiState.Error).message)
    }
}
```

---

## 🧪 MANDATORY: Unit Testing

### Testing Requirements

**BEFORE you finish:**
- ✅ 100% of business logic must have unit tests
- ✅ 85%+ ViewModel coverage
- ✅ 100% of domain models tested
- ✅ All edge cases covered
- ✅ All error scenarios tested
- ✅ **ALL TESTS MUST PASS**

### Test Structure (AAA Pattern)

```kotlin
@Test
fun should_return_success_when_data_loaded() {
    // ARRANGE
    val mockRepository = mock<MyRepository>()
    val useCase = MyUseCase(mockRepository)

    // ACT
    val result = runBlocking { useCase.execute() }

    // ASSERT
    assertNotNull(result)
    assertEquals("expected", result.value)
}
```

### Required Tests

#### 1. Domain Models

```kotlin
@Test
fun test_model_creation() {
    val model = MyModel("id", "name", 1.0)
    assertEquals("id", model.id)
    assertEquals("name", model.name)
    assertEquals(1.0, model.value)
}
```

#### 2. Use Cases

```kotlin
@Test
fun test_use_case_success() = runTest {
    val mockRepo = mock<MyRepository>()
    val expectedModel = MyModel("1", "test", 1.0)

    whenever(mockRepo.getData()).thenReturn(
        flowOf(expectedModel)
    )

    val useCase = MyUseCase(mockRepo)
    val result = useCase.execute()

    assertEquals(expectedModel, result)
    verify(mockRepo).getData()
}

@Test
fun test_use_case_error_handling() = runTest {
    val mockRepo = mock<MyRepository>()
    whenever(mockRepo.getData()).thenThrow(Exception("Test error"))

    val useCase = MyUseCase(mockRepo)
    assertThrows<Exception> {
        useCase.execute()
    }
}
```

#### 3. ViewModels

```kotlin
@Test
fun test_viewmodel_loads_data() = runTest {
    val mockUseCase = mock<MyUseCase>()
    val expectedData = MyModel("1", "test", 1.0)

    whenever(mockUseCase.execute()).thenReturn(expectedData)

    val viewModel = MyViewModel(mockUseCase)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is UiState.Success)
    assertEquals(expectedData, (state as UiState.Success).data)
}
```

#### 4. Error Scenarios

```kotlin
@Test
fun test_invalid_input_handled() {
    assertThrows<IllegalArgumentException> {
        MyModel("", "", -1.0)  // Invalid: empty string, negative value
    }
}

@Test
fun test_null_safety() {
    val model = MyModel("id", "name", 0.0)
    assertNotNull(model)
    assertNotNull(model.id)
}
```

### Running Tests

```bash
# All tests
./gradlew testDebugUnitTest

# Specific test class
./gradlew testDebugUnitTest --tests "com.example.fitness_plan.MyTest"

# With coverage
./gradlew testDebugUnitTest jacocoTestReport

# Check results
open build/reports/jacoco/testDebugUnitTest/html/index.html
```

### Test File Location

Place tests in: `app/src/test/java/com/example/fitness_plan/`

Structure mirrors main code:
```
src/test/java/com/example/fitness_plan/
├── domain/
│   ├── model/MyModelTest.kt
│   ├── usecase/MyUseCaseTest.kt
│   └── calculator/MyCalculatorTest.kt
├── data/
│   └── MyRepositoryTest.kt
└── presentation/
    └── viewmodel/MyViewModelTest.kt
```

---

## 💻 MANDATORY: Code Quality Standards

### Code Style

✅ **DO**:
- Use meaningful variable names
- Follow Kotlin naming conventions (camelCase for variables, PascalCase for classes)
- Keep functions small (< 30 lines)
- Write self-documenting code
- Add KDoc for public APIs
- Use appropriate access modifiers (private by default)
- Use immutable data structures (val, data class)

❌ **DON'T**:
- Use single-letter variable names (except i, j, k in loops)
- Create god classes (>500 lines)
- Nest more than 3 levels deep
- Use magic numbers (create constants instead)
- Mix concerns in single function
- Suppress warnings without reason
- Use platform types (always specify nullable)

### Example: Good Code

```kotlin
// ✅ GOOD
data class UserProfile(
    val username: String,
    val goal: Goal,
    val level: Level,
    val frequency: Frequency
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
    }
}

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(
        ProfileUiState.Loading
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                userRepository.save(profile)
                _uiState.value = ProfileUiState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message)
            }
        }
    }
}
```

### Example: Bad Code

```kotlin
// ❌ BAD
class Profile {
    var u: String? = null  // unclear name, nullable
    var g: String? = null
    var l: String? = null

    fun update() {
        // mixed concerns: validation + API + UI state
        if (u == null || u?.isEmpty() == true) {
            println("Error")
            return
        }
        val x = "http://api.com/update?u=" + u + "&g=" + g
        val response = /* blocking call */
        // UI state not managed
    }
}
```

### Naming Conventions

```kotlin
// Classes
class UserProfile { }
data class WorkoutPlan { }
interface WorkoutRepository { }

// Functions
fun getUserProfile(): UserProfile { }
fun calculateWeight(): Float { }
suspend fun saveProfile(): Unit { }

// Variables
val currentUser: User? = null
var isLoading = false
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

// Constants
const val MIN_USERNAME_LENGTH = 3
val DEFAULT_WEIGHT = 70.0f
enum class Goal { WEIGHT_LOSS, MUSCLE_BUILDING, MAINTENANCE }
```

---

## 🔍 MANDATORY: Code Review Checklist

Before submitting PR, verify ALL items:

### Architecture
- [ ] Code follows Clean Architecture layers
- [ ] No cross-layer dependencies
- [ ] Interfaces in domain, implementations in data
- [ ] No circular dependencies
- [ ] Business logic in domain/use cases, not UI

### Code Quality
- [ ] Code is readable and self-documenting
- [ ] Functions are small and focused
- [ ] No code duplication
- [ ] Proper error handling
- [ ] No magic numbers
- [ ] Meaningful variable names
- [ ] Follows project conventions

### Testing
- [ ] All tests pass: `./gradlew testDebugUnitTest`
- [ ] New code has unit tests
- [ ] Edge cases tested
- [ ] Error scenarios tested
- [ ] Test coverage > 85% for new code
- [ ] No `@Ignore` or skipped tests
- [ ] Tests are deterministic (not flaky)

### Documentation
- [ ] Code has appropriate comments
- [ ] Public APIs have KDoc
- [ ] Complex logic explained
- [ ] SYSTEM_PROMPT.md updated if needed
- [ ] README updated if needed

### Security
- [ ] No hardcoded secrets or credentials
- [ ] Proper input validation
- [ ] Secure data handling (encryption)
- [ ] No SQL injection vulnerabilities
- [ ] Proper authentication checks

### Performance
- [ ] No blocking operations on main thread
- [ ] Coroutines used for async operations
- [ ] No memory leaks
- [ ] Efficient database queries
- [ ] Images/resources properly sized

### Git/Commits
- [ ] Meaningful commit messages
- [ ] Feature branch from develop or main
- [ ] No merge commits (rebase if needed)
- [ ] Commits are atomic and logical
- [ ] No merge conflicts

---

## 🐛 Critical Error Prevention

### Errors That Will Cause PR Rejection

#### 1. Architectural Violations
```kotlin
// ❌ WRONG: UI accessing repository directly
@Composable
fun HomeScreen() {
    val repository = WorkoutRepositoryImpl(context)  // ❌ WRONG!
    val plan = repository.getPlan()  // ❌ WRONG!
}

// ✅ CORRECT: Use ViewModel
@Composable
fun HomeScreen(viewModel: WorkoutViewModel = hiltViewModel()) {
    val plan by viewModel.plan.collectAsState()
}
```

#### 2. Missing Tests
```kotlin
// ❌ WRONG: Function with no tests
fun calculateWeight(userWeight: Float, level: String): Float {
    return userWeight * 0.75f
}

// ✅ CORRECT: Function with comprehensive tests
fun calculateWeight(userWeight: Float, level: String): Float {
    return when(level) {
        "BEGINNER" -> userWeight * 0.6f
        "AMATEUR" -> userWeight * 0.75f
        "PROFESSIONAL" -> userWeight * 0.9f
        else -> throw IllegalArgumentException("Unknown level: $level")
    }
}

@Test
fun should_calculate_weight_for_beginner() {
    assertEquals(60f, calculateWeight(100f, "BEGINNER"))
}
```

#### 3. Test Failures
```bash
# ❌ WRONG: Committing with failing tests
./gradlew testDebugUnitTest
# FAILED: 3 tests failed

# ✅ CORRECT: All tests pass
./gradlew testDebugUnitTest
# BUILD SUCCESSFUL: All tests passed
```

#### 4. Data Layer Issues
```kotlin
// ❌ WRONG: Not saving to DataStore
fun generatePlan(): WorkoutPlan {
    val plan = createPlanInMemory()
    return plan  // ❌ Plan is lost on next app launch!
}

// ✅ CORRECT: Save to DataStore
fun generatePlan(): WorkoutPlan {
    val plan = createPlanInMemory()
    viewModelScope.launch {
        workoutRepository.savePlan(plan)  // ✅ Persisted
    }
    return plan
}
```

#### 5. State Management Issues
```kotlin
// ❌ WRONG: Mutable state exposed
class WorkoutViewModel {
    var workoutPlan = mutableStateOf<WorkoutPlan?>(null)  // ❌ Exposed!
}

// ✅ CORRECT: State properly managed
class WorkoutViewModel {
    private val _workoutPlan = mutableStateOf<WorkoutPlan?>(null)
    val workoutPlan: StateFlow<WorkoutPlan?> = _workoutPlan.asStateFlow()
}
```

#### 6. Resource Leaks
```kotlin
// ❌ WRONG: No cleanup
viewModelScope.launch {
    while(true) {
        repository.getData()  // ❌ Infinite loop, never completes
    }
}

// ✅ CORRECT: Proper async handling
viewModelScope.launch {
    repository.getDataFlow()
        .collect { data ->
            _uiState.value = UiState.Success(data)
        }
}
```

#### 7. Null Safety Issues
```kotlin
// ❌ WRONG: Unsafe null handling
val name = getUserName()
val length = name.length  // ❌ NPE if null!

// ✅ CORRECT: Proper null safety
val name = getUserName()
val length = name?.length ?: 0  // ✅ Safe

// ✅ ALSO CORRECT: Non-nullable by design
fun getRequiredName(): String {  // Never null
    return repository.fetchName() ?: "Unknown"
}
```

---

## 📝 Git & Commit Requirements

### Branch Naming

```
feature/add-weight-progression      ✅ Feature
feature/improve-exercise-filtering  ✅ Feature
fix/exercise-repetition-issue       ✅ Bug fix
fix/req-001-datastore-persistence   ✅ Specific issue

develop                             ✅ Integration
main                                ✅ Production
```

### Commit Messages

```
# ✅ GOOD
feat(Statistics): Add weight chart visualization with date range filtering
fix(Exercise): Resolve plan not persisting to DataStore on creation
test(WorkoutViewModel): Add comprehensive test coverage for state management
docs: Update SYSTEM_PROMPT with weight progression algorithm

# ❌ BAD
fix bug                             # Too vague
wip                                 # Work in progress
misc changes                        # Unclear
update                              # What updated?
```

### PR Description Template

```markdown
## 📝 Description
Brief description of the feature/fix

## 🎯 Type
- [ ] Feature
- [ ] Bug Fix
- [ ] Documentation
- [ ] Refactoring

## 🏗️ Architecture Impact
- Model changes: (list or "none")
- Repository changes: (list or "none")
- ViewModel changes: (list or "none")
- UI changes: (list or "none")

## ✅ Checklist
- [ ] Documentation read and understood
- [ ] Todo list created and approved
- [ ] Architecture respected
- [ ] All tests written and passing
- [ ] Code review checklist completed
- [ ] No breaking changes
- [ ] Related issues referenced

## 🔗 Related Issues
Closes #123

## 📸 Screenshots (if UI change)
[Add screenshots if applicable]
```

---

## 🎓 Best Practices

### 1. Always Start with Tests

```
BAD FLOW:  Write Code → Write Tests → Debug
GOOD FLOW: Write Tests → Write Code → Verify
```

**TDD Benefits**:
- Tests document expected behavior
- Cleaner code structure
- Fewer bugs
- Easier refactoring

### 2. Use Dependency Injection (Hilt)

```kotlin
// ✅ CORRECT: Constructor injection
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository,
    private val useCase: MyUseCase
) : ViewModel()

// ❌ WRONG: Manual instantiation
class MyViewModel {
    private val repository = MyRepositoryImpl()  // ❌ Tight coupling!
}
```

### 3. Use Coroutines Properly

```kotlin
// ✅ CORRECT: viewModelScope for lifecycle safety
viewModelScope.launch {
    val data = repository.fetchData()  // Cancelled on ViewModel destroyed
    _uiState.value = UiState.Success(data)
}

// ❌ WRONG: GlobalScope causes memory leaks
GlobalScope.launch {
    val data = repository.fetchData()  // Never cancelled!
    _uiState.value = UiState.Success(data)
}
```

### 4. Handle Errors Gracefully

```kotlin
// ✅ CORRECT: Comprehensive error handling
viewModelScope.launch {
    try {
        val data = repository.fetchData()
        _uiState.value = UiState.Success(data)
    } catch (e: NetworkException) {
        _uiState.value = UiState.Error("Network error. Please try again.")
    } catch (e: DataException) {
        _uiState.value = UiState.Error("Invalid data format.")
    } catch (e: Exception) {
        _uiState.value = UiState.Error("Unknown error occurred.")
        Log.e("ViewModel", "Unexpected error", e)
    }
}

// ❌ WRONG: Silent failures
viewModelScope.launch {
    try {
        val data = repository.fetchData()
        _uiState.value = UiState.Success(data)
    } catch (e: Exception) {
        // Silent failure - user sees nothing!
    }
}
```

### 5. Validate Input

```kotlin
// ✅ CORRECT: Input validation
fun createUserProfile(username: String, goal: Goal): UserProfile {
    require(username.isNotBlank()) { "Username cannot be blank" }
    require(username.length >= 3) { "Username must be at least 3 characters" }
    require(goal in Goal.values()) { "Invalid goal" }

    return UserProfile(username = username, goal = goal)
}

// ❌ WRONG: No validation
fun createUserProfile(username: String, goal: Goal): UserProfile {
    return UserProfile(username = username, goal = goal)  // Any input accepted!
}
```

### 6. Immutable Data Structures

```kotlin
// ✅ CORRECT: Immutable
data class WorkoutPlan(
    val id: String,
    val exercises: List<Exercise>,
    val createdAt: Long
)

// ❌ WRONG: Mutable
class WorkoutPlan {
    var id: String? = null
    var exercises: MutableList<Exercise> = mutableListOf()
    var createdAt: Long = 0L
}
```

---

## 🔄 Development Workflow

### Step-by-Step Process

#### Phase 1: Planning (30 min - 1 hour)
1. Read all relevant documentation
2. Create detailed todo list
3. Get approval from Igor
4. Update todo as `in_progress`

#### Phase 2: Implementation (2-4 hours)
1. Create domain models
2. Create repository interfaces
3. Implement use cases
4. Update ViewModels
5. Create UI components
6. Add navigation

**Write tests as you go** for each layer.

#### Phase 3: Testing (1-2 hours)
1. Run all tests: `./gradlew testDebugUnitTest`
2. Verify coverage: `./gradlew jacocoTestReport`
3. Fix failing tests immediately
4. Add missing tests
5. Test manual scenarios

#### Phase 4: Documentation (30 min)
1. Update SYSTEM_PROMPT.md
2. Add code comments
3. Update README if needed
4. Update todo list (mark as completed)

#### Phase 5: Code Review (30 min - 1 hour)
1. Self-review using checklist
2. Run lint: `./gradlew lint`
3. Check code style
4. Verify no TODOs or hacks
5. Create PR with detailed description
6. Request review from Igor

#### Phase 6: Address Feedback
1. Review comments
2. Make requested changes
3. Run tests again
4. Update PR
5. Request re-review

---

## 📊 Definition of Done (DoD)

### Feature is considered DONE when:

✅ **Functionality**
- [ ] Feature works as specified
- [ ] No known bugs
- [ ] Edge cases handled
- [ ] Tested on device/emulator

✅ **Code Quality**
- [ ] Follows architecture
- [ ] Follows code style
- [ ] No duplication
- [ ] Efficient implementation

✅ **Testing**
- [ ] Unit tests written (85%+ coverage)
- [ ] All tests passing
- [ ] Manual testing done
- [ ] No flaky tests

✅ **Documentation**
- [ ] Code is documented
- [ ] SYSTEM_PROMPT.md updated
- [ ] Related docs updated
- [ ] Commit messages clear

✅ **Security**
- [ ] No secrets exposed
- [ ] Input validated
- [ ] Data secured
- [ ] No vulnerabilities

✅ **Performance**
- [ ] No memory leaks
- [ ] No UI blocks
- [ ] Efficient queries
- [ ] Acceptable load time

✅ **Git/PR**
- [ ] PR created and described
- [ ] Code review requested
- [ ] All feedback addressed
- [ ] Approved and ready to merge

---

## 🚨 Emergency Procedures

### If Tests Fail

```bash
# 1. Run tests with verbose output
./gradlew testDebugUnitTest --info

# 2. Check specific test
./gradlew testDebugUnitTest --tests "TestClassName"

# 3. Run with stacktrace
./gradlew testDebugUnitTest --stacktrace

# NEVER commit with failing tests!
```

### If Architecture Issue Found

1. STOP - don't continue
2. Review ARCHITECTURE.md again
3. Check similar implementations
4. Refactor to follow architecture
5. Run tests
6. Request review

### If DataStore Issue

```bash
# Inspect DataStore data
adb shell run-as com.example.fitness_plan \
  cat /data/data/com.example.fitness_plan/shared_prefs/store_name.xml
```

### If UI Issue

```bash
# Check Compose errors
adb logcat | grep "Compose\|CompositionException"
```

---

## ✉️ Questions & Support

### When to Ask Questions

- ❓ Unclear requirements
- ❓ Architectural decisions
- ❓ Complex algorithms
- ❓ Design patterns
- ❓ Performance concerns

### How to Ask

1. Read relevant documentation first
2. Check similar implementations
3. Create issue with:
   - Clear problem statement
   - What you tried
   - What you expect
   - Code snippet or screenshot

### Resources

- SYSTEM_PROMPT.md - Comprehensive reference
- ARCHITECTURE.md - Design patterns
- QUICK_REFERENCE.md - Fast answers
- TESTING.md - Testing guide
- GitHub Issues - Existing questions

---

## 📋 Quick Reference Checklist

**Before you start:**
- [ ] Read QUICK_REFERENCE.md
- [ ] Read SYSTEM_PROMPT.md (relevant sections)
- [ ] Read ARCHITECTURE.md (relevant sections)

**Before you write code:**
- [ ] Create and get approval for todo list
- [ ] Understand data flow
- [ ] Identify dependencies
- [ ] Plan testing strategy

**While you code:**
- [ ] Follow architecture layers
- [ ] Write tests alongside code
- [ ] Keep functions small
- [ ] Add meaningful comments
- [ ] Use proper naming

**Before you commit:**
- [ ] All tests passing
- [ ] Code coverage > 85%
- [ ] No TODOs or hacks
- [ ] Meaningful commit message
- [ ] Documentation updated

**Before you submit PR:**
- [ ] Self-review checklist completed
- [ ] No linting errors
- [ ] PR description detailed
- [ ] Related issues referenced

---

## 🎉 Success Example

### Complete Feature Implementation

```markdown
## Feature: Add Exercise Favoriting

### 1. Planning ✅
- Read: SYSTEM_PROMPT.md § ExerciseLibrary
- Read: ARCHITECTURE.md § Data Flow
- Created todo list
- Got approval from Igor

### 2. Models & Repositories ✅
- Created: ExerciseFavorite.kt (domain model)
- Updated: ExerciseRepository interface
- Tests: 100% coverage
- Tests: All passing ✅

### 3. Use Cases ✅
- Created: FavoriteExerciseUseCase.kt
- Methods: toggleFavorite(), getFavorites()
- Tests: 90% coverage
- Tests: All passing ✅

### 4. ViewModels ✅
- Updated: ExerciseLibraryViewModel.kt
- Added: favoriteExercises state
- Added: toggleFavorite() method
- Tests: 85% coverage
- Tests: All passing ✅

### 5. UI ✅
- Updated: ExerciseLibraryScreen.kt
- Added: favorite button with heart icon
- Navigation: Works correctly
- Manual testing: Verified on device ✅

### 6. Testing ✅
./gradlew testDebugUnitTest
BUILD SUCCESSFUL - All tests passed ✅

### 7. Documentation ✅
- Updated: SYSTEM_PROMPT.md
- Added: KDoc comments
- Updated: Related sections

### 8. Code Review ✅
- PR created with detailed description
- Checklist completed
- Code style verified
- Ready for merge ✅
```

---

## 📞 Final Notes

**Remember:**
- Documentation comes first, code comes second
- Architecture is sacred - never violate layers
- Tests prove correctness
- Code review ensures quality
- Communication prevents rework

**Questions?** Ask Igor or review the documentation.

**Ready to code?** Follow this guide step-by-step.

---

**Last Updated**: 2026-02-16
**Version**: 1.0
**Status**: Mandatory for all developers

---

## 🎯 Signature Confirmation

By starting development on this project, you confirm:

✅ I have read and understood these guidelines
✅ I will follow the mandatory requirements
✅ I will respect the architecture
✅ I will write tests for all code
✅ I will follow the code review process
✅ I will communicate with Igor before major decisions

**Development can only begin after confirming all items above.**
