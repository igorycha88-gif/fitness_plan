# Testing Prompt for Developers - Fitness Plan Project

This document serves as a prompt/guide for developers actively writing and maintaining tests in the Fitness Plan project. Use this as your reference during the testing process.

---

## 📌 Your Testing Responsibilities

As a developer on Fitness Plan, you have the following testing responsibilities:

### ✅ Before Implementation
- [ ] Read the feature/bug requirements carefully
- [ ] Understand what needs to be tested
- [ ] Identify test cases (happy path, error, edge cases)
- [ ] Check existing tests for patterns to follow
- [ ] Create a todo list for tests needed

### ✅ During Implementation
- [ ] Write tests FIRST (Test-Driven Development)
- [ ] Implement code to make tests pass
- [ ] Run tests frequently
- [ ] Keep tests focused and atomic
- [ ] Follow the AAA pattern rigorously

### ✅ After Implementation
- [ ] Verify all tests pass
- [ ] Verify coverage > 85%
- [ ] Refactor tests for clarity
- [ ] Update test documentation
- [ ] Prepare tests for code review

### ✅ Code Review
- [ ] Review test names (are they descriptive?)
- [ ] Review test structure (do they follow AAA?)
- [ ] Review test coverage (is it comprehensive?)
- [ ] Review test data (are test factories used?)
- [ ] Review mocking (is it appropriate?)

---

## 🎯 Understanding Test Requirements

### What Is Tested?

**Domain Layer** (HIGHEST PRIORITY)
- ✅ Business logic (calculations, algorithms)
- ✅ Data models (validation, properties)
- ✅ Use cases (workflows, business rules)
- ✅ Algorithms (weight progression, plan generation)

**Data Layer**
- ✅ Repository implementations
- ✅ Data persistence
- ✅ Data transformations
- ✅ Error handling

**Presentation Layer (ViewModels)**
- ✅ State management
- ✅ Event handling
- ✅ Data transformation for UI
- ✅ Error state display

**UI Layer** (LOWER PRIORITY)
- ✅ Screen layout logic
- ✅ Navigation
- ✅ User input handling
- ⚠️ Visual rendering (lower priority)

### What Is NOT Tested?

- ❌ Third-party library behavior (assume it works)
- ❌ Android framework internals (assume it works)
- ❌ Configuration file format (assume it's correct)
- ❌ Trivial getters/setters (unless they have logic)

### Coverage Requirements

| Layer | Minimum | Target | Reason |
|-------|---------|--------|--------|
| Domain Models | 90% | 95% | Most critical for correctness |
| Use Cases | 85% | 90% | Business logic must be verified |
| Repositories | 80% | 85% | Data layer important for consistency |
| ViewModels | 70% | 80% | State management important for UX |
| Screens | 60% | 70% | UI less critical than logic |

**⚠️ IMPORTANT**: If your new code's coverage falls below the minimum, your PR will be rejected.

---

## 🏗️ Writing Tests Step-by-Step

### Step 1: Understand What You're Testing

```kotlin
// Example: You're implementing weight progression calculator

fun calculateWeightProgression(
    currentWeight: Double,
    targetReps: Int,
    repsAchieved: Int,
    progressionRate: Double = 2.5
): Double {
    return if (repsAchieved >= targetReps) {
        currentWeight + progressionRate
    } else {
        currentWeight
    }
}

// Questions to answer:
// 1. What is the happy path? (normal usage)
//    → When repsAchieved >= targetReps, add progressionRate
// 2. What are error cases? (things that shouldn't happen)
//    → Negative weight, negative reps
// 3. What are edge cases? (boundary conditions)
//    → Exactly equal reps, zero weight, zero progression
```

### Step 2: Create Test Class

```kotlin
// File: app/src/test/java/com/example/fitness_plan/domain/usecase/WeightProgressionUseCaseTest.kt

import org.junit.Test
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightProgressionUseCaseTest {
    // Declare the system under test
    private lateinit var useCase: WeightProgressionUseCase

    @Before
    fun setup() {
        // Initialize test fixtures
        useCase = WeightProgressionUseCase()
    }

    // Tests will go here
}
```

### Step 3: Write First Test (Happy Path)

```kotlin
@Test
fun should_increase_weight_when_target_reps_achieved() {
    // ARRANGE - Set up test data
    val currentWeight = 50.0
    val targetReps = 10
    val repsAchieved = 10
    val expectedNewWeight = 52.5 // 50.0 + 2.5

    // ACT - Execute the function
    val result = useCase.calculateWeightProgression(
        currentWeight = currentWeight,
        targetReps = targetReps,
        repsAchieved = repsAchieved
    )

    // ASSERT - Verify the result
    assertEquals(expectedNewWeight, result)
}
```

### Step 4: Write Edge Case Tests

```kotlin
@Test
fun should_increase_weight_when_exceeded_target_reps() {
    // Exceeding target by 1
    val result = useCase.calculateWeightProgression(
        currentWeight = 50.0,
        targetReps = 10,
        repsAchieved = 11
    )
    assertEquals(52.5, result)
}

@Test
fun should_not_increase_weight_when_target_reps_not_met() {
    val result = useCase.calculateWeightProgression(
        currentWeight = 50.0,
        targetReps = 10,
        repsAchieved = 9
    )
    assertEquals(50.0, result)
}

@Test
fun should_not_increase_weight_when_one_rep_short() {
    val result = useCase.calculateWeightProgression(
        currentWeight = 50.0,
        targetReps = 10,
        repsAchieved = 9
    )
    assertEquals(50.0, result)
}
```

### Step 5: Write Error Case Tests

```kotlin
@Test
fun should_throw_exception_when_negative_weight() {
    val exception = assertThrows(IllegalArgumentException::class.java) {
        useCase.calculateWeightProgression(
            currentWeight = -50.0,
            targetReps = 10,
            repsAchieved = 10
        )
    }
    assertEquals("Weight must be positive", exception.message)
}

@Test
fun should_throw_exception_when_negative_reps() {
    val exception = assertThrows(IllegalArgumentException::class.java) {
        useCase.calculateWeightProgression(
            currentWeight = 50.0,
            targetReps = -10,
            repsAchieved = 10
        )
    }
    assertEquals("Target reps must be positive", exception.message)
}
```

### Step 6: Write Tests for Special Cases

```kotlin
@Test
fun should_use_custom_progression_rate() {
    val result = useCase.calculateWeightProgression(
        currentWeight = 50.0,
        targetReps = 10,
        repsAchieved = 10,
        progressionRate = 5.0 // Custom rate
    )
    assertEquals(55.0, result) // 50.0 + 5.0
}

@Test
fun should_handle_fractional_weights() {
    val result = useCase.calculateWeightProgression(
        currentWeight = 50.5,
        targetReps = 10,
        repsAchieved = 10
    )
    assertEquals(53.0, result) // 50.5 + 2.5
}
```

### Step 7: Verify Coverage

```bash
# Generate coverage report
./gradlew jacocoTestReport

# Check report at:
# app/build/reports/jacoco/jacocoTestReport/html/index.html

# Look for:
# - Green (covered)
# - Red (uncovered)
# - Make sure critical paths are green
```

---

## 🧪 Testing Different Layers

### Testing Domain Models

```kotlin
// ✅ Test: User profile validation
@Test
fun should_create_user_with_valid_data() {
    val user = UserProfile(
        id = UUID.randomUUID(),
        name = "John",
        age = 25,
        weight = 75.0
    )

    assertEquals("John", user.name)
    assertEquals(25, user.age)
    assertEquals(75.0, user.weight)
}

// ✅ Test: Validation logic
@Test
fun should_throw_when_age_invalid() {
    val exception = assertThrows(IllegalArgumentException::class.java) {
        UserProfile(
            id = UUID.randomUUID(),
            name = "John",
            age = -5,
            weight = 75.0
        )
    }
    assertTrue(exception.message!!.contains("age"))
}
```

### Testing Use Cases

```kotlin
@Test
fun should_calculate_weight_progression_correctly() = runTest {
    // ARRANGE
    val useCase = WeightProgressionUseCase()
    val currentWeight = 50.0
    val newWeight = 52.5

    // ACT
    val result = useCase.calculateProgression(currentWeight)

    // ASSERT
    assertEquals(newWeight, result)
}
```

### Testing Repositories

```kotlin
@Test
fun should_save_and_retrieve_user() = runTest {
    // ARRANGE
    val context = mock(Context::class.java)
    val repository = UserRepositoryImpl(context)
    val user = TestDataFactory.createUserProfile(name = "Test User")

    // ACT
    repository.saveUser(user)
    val retrieved = repository.getUser().first()

    // ASSERT
    assertEquals(user, retrieved)
}
```

### Testing ViewModels

```kotlin
@Test
fun should_load_workout_data() = runTest {
    // ARRANGE
    val mockUseCase = mock(WorkoutUseCase::class.java)
    val testWorkout = TestDataFactory.createWorkout()
    coEvery { mockUseCase.getWorkout() } returns testWorkout

    val viewModel = WorkoutViewModel(mockUseCase)

    // ACT
    viewModel.loadWorkout()
    advanceUntilIdle()

    // ASSERT
    assertEquals(WorkoutState.Loaded(testWorkout), viewModel.state.value)
}
```

---

## 🔧 Mocking Patterns

### Basic Mocking

```kotlin
// Mock a dependency
val mockRepository = mock(UserRepository::class.java)

// Set up mock behavior
`when`(mockRepository.getUser()).thenReturn(testUser)

// Verify mock was called
verify(mockRepository).getUser()

// Verify mock was called with specific arguments
verify(mockRepository).saveUser(testUser)
```

### Mocking with Coroutines (coEvery)

```kotlin
// Use coEvery for suspend functions
val mockUseCase = mockk<WeightProgressionUseCase>()

coEvery { mockUseCase.calculateProgression(50.0) } returns 52.5

// Use runTest for coroutine testing
@Test
fun test_with_coroutines() = runTest {
    val result = mockUseCase.calculateProgression(50.0)
    assertEquals(52.5, result)
}
```

### Mocking Lists and Collections

```kotlin
val mockExercises = listOf(
    Exercise(id = 1, name = "Bench Press"),
    Exercise(id = 2, name = "Squat"),
    Exercise(id = 3, name = "Deadlift")
)

`when`(mockRepository.getExercises()).thenReturn(mockExercises)

// In test
val exercises = mockRepository.getExercises()
assertEquals(3, exercises.size)
assertTrue(exercises.any { it.name == "Bench Press" })
```

---

## ✍️ Test Naming Convention

**MANDATORY FORMAT**: `should_[expected_behavior]_when_[condition]`

### Good Test Names

```kotlin
// ✅ Clear and descriptive
should_increase_weight_when_target_reps_achieved
should_return_exercises_when_muscle_group_matches
should_throw_exception_when_input_invalid
should_update_profile_when_valid_data_provided
should_calculate_volume_correctly_when_reps_and_weight_given
should_filter_exercises_by_type_when_filter_applied
```

### Bad Test Names

```kotlin
// ❌ Not descriptive enough
test_weight()
testReps()
test1()
checkFunction()
function_test()

// ❌ Describes implementation, not behavior
test_iteration_logic()
test_loop_count()
test_private_method_initialization()

// ❌ Ambiguous
test_calculation()
test_data()
test_process()
```

### Renaming Bad Tests

```kotlin
// ❌ BAD
@Test
fun test_weight_calculation() {
    val result = calculateWeight(50.0, 2.5)
    assertEquals(52.5, result)
}

// ✅ GOOD - Now clear what behavior is being tested
@Test
fun should_add_progression_to_current_weight_when_calculating_new_weight() {
    val result = calculateWeight(50.0, 2.5)
    assertEquals(52.5, result)
}
```

---

## 🐛 Debugging Failing Tests

### Step 1: Read the Error Message

```
FAILED: WeightProgressionUseCaseTest::should_increase_weight_when_target_reps_achieved
Expected: 52.5
Actual: 50.0
```

This tells you:
- Test name: `should_increase_weight_when_target_reps_achieved`
- Expected value: 52.5
- Actual value: 50.0
- Something's wrong with the logic

### Step 2: Run Test in Isolation

```bash
# Run only the failing test
./gradlew testDebugUnitTest --tests "WeightProgressionUseCaseTest.should_increase_weight_when_target_reps_achieved"
```

### Step 3: Add Debug Output

```kotlin
@Test
fun should_increase_weight_when_target_reps_achieved() {
    val currentWeight = 50.0
    val targetReps = 10
    val repsAchieved = 10

    println("Debug: currentWeight = $currentWeight")
    println("Debug: targetReps = $targetReps")
    println("Debug: repsAchieved = $repsAchieved")

    val result = useCase.calculateWeightProgression(
        currentWeight = currentWeight,
        targetReps = targetReps,
        repsAchieved = repsAchieved
    )

    println("Debug: result = $result")

    assertEquals(52.5, result)
}
```

### Step 4: Check Implementation

If test says `actual = 50.0` but expected `52.5`, the implementation probably:
- Isn't adding the progression
- Has a bug in the condition
- Isn't being called correctly

Fix the implementation, not the test (unless the test expectation was wrong).

### Step 5: Verify Again

```bash
./gradlew testDebugUnitTest
# Should see: BUILD SUCCESSFUL
```

---

## 📊 Generating Coverage Report

### Step 1: Run Tests with Coverage

```bash
./gradlew testDebugUnitTest
```

### Step 2: Generate Report

```bash
./gradlew jacocoTestReport
```

### Step 3: Open Report

```bash
# Mac
open app/build/reports/jacoco/jacocoTestReport/html/index.html

# Linux
xdg-open app/build/reports/jacoco/jacocoTestReport/html/index.html

# Windows
start app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Step 4: Review Coverage

In the HTML report, look for:
- **Green lines**: Code that's tested ✅
- **Red lines**: Code that's NOT tested ❌
- **Yellow lines**: Partial coverage
- **Coverage percentage**: Should be > 85%

### Step 5: Improve Coverage

If coverage is low, add tests for:
- Red lines (untested code)
- Error cases
- Edge cases
- Different branches

---

## 📋 Complete Testing Workflow

### Phase 1: Planning (5 minutes)
```
[ ] What feature am I testing?
[ ] What are the happy paths?
[ ] What are the error cases?
[ ] What are the edge cases?
[ ] What coverage do I need?
```

### Phase 2: Setup (5 minutes)
```
[ ] Created test file
[ ] Set up test class
[ ] Created test fixtures/mocks
[ ] Set up test data factory
```

### Phase 3: Happy Path Tests (15 minutes)
```
[ ] Test normal/expected behavior
[ ] Test with different inputs
[ ] Test with boundary values
[ ] Verify all green
```

### Phase 4: Error Cases (10 minutes)
```
[ ] Test invalid inputs
[ ] Test exception throwing
[ ] Test error messages
[ ] Verify error handling
```

### Phase 5: Edge Cases (10 minutes)
```
[ ] Test boundary conditions
[ ] Test special cases
[ ] Test unusual but valid inputs
[ ] Verify all covered
```

### Phase 6: Coverage Verification (10 minutes)
```
[ ] Run coverage report
[ ] Verify > 85%
[ ] Identify uncovered code
[ ] Add tests for uncovered code
```

### Phase 7: Refactoring (5 minutes)
```
[ ] Improve test names
[ ] Extract common setup
[ ] Remove duplication
[ ] Add documentation
```

### Phase 8: Final Check (5 minutes)
```
[ ] All tests pass
[ ] No debug code
[ ] No @Ignore
[ ] Coverage good
[ ] Ready for PR
```

---

## 🚀 Test-Driven Development (TDD)

The recommended approach for Fitness Plan:

### Step 1: Write Test First

```kotlin
@Test
fun should_increase_weight_when_reps_achieved() {
    val result = useCase.calculateProgression(50.0, 10, 10)
    assertEquals(52.5, result)
}
```

### Step 2: Run Test (Should FAIL)

```bash
./gradlew testDebugUnitTest
# Test fails because function doesn't exist yet
```

### Step 3: Implement Minimum Code

```kotlin
fun calculateProgression(current: Double, target: Int, achieved: Int): Double {
    return if (achieved >= target) current + 2.5 else current
}
```

### Step 4: Run Test (Should PASS)

```bash
./gradlew testDebugUnitTest
# Test passes!
```

### Step 5: Add More Tests

```kotlin
@Test
fun should_not_increase_when_reps_not_achieved() {
    val result = useCase.calculateProgression(50.0, 10, 9)
    assertEquals(50.0, result)
}
```

### Step 6: Repeat Until Complete

Keep adding tests → implementing code → refactoring

**Benefits of TDD**:
- ✅ Forces you to think about edge cases
- ✅ Tests are written as you code
- ✅ Coverage is naturally high
- ✅ Code is testable by design
- ✅ Fewer bugs in final product

---

## 📚 Quick Reference

### Essential Commands

```bash
# Run all tests
./gradlew testDebugUnitTest

# Run specific test
./gradlew testDebugUnitTest --tests "TestClassName"

# Generate coverage
./gradlew jacocoTestReport

# View coverage
open app/build/reports/jacoco/jacocoTestReport/html/index.html

# Clean and rebuild
./gradlew clean testDebugUnitTest
```

### Test Templates

**Simple Unit Test**:
```kotlin
@Test
fun should_[behavior]_when_[condition]() {
    // ARRANGE
    val input = createTestData()

    // ACT
    val result = functionUnderTest(input)

    // ASSERT
    assertEquals(expected, result)
}
```

**Coroutine Test**:
```kotlin
@Test
fun should_[behavior]_when_[condition]() = runTest {
    // ARRANGE
    val mock = mockk<Dependency>()
    coEvery { mock.suspend() } returns testData

    // ACT
    val result = systemUnderTest.action()
    advanceUntilIdle()

    // ASSERT
    assertEquals(expected, result)
}
```

**Exception Test**:
```kotlin
@Test
fun should_throw_when_[condition]() {
    val exception = assertThrows(CustomException::class.java) {
        functionUnderTest(badInput)
    }
    assertEquals("Expected message", exception.message)
}
```

---

## ✅ Pre-Submission Checklist

Before creating a PR, verify ALL of these:

- [ ] **All tests pass**: `./gradlew testDebugUnitTest` → `BUILD SUCCESSFUL`
- [ ] **Coverage > 85%**: `./gradlew jacocoTestReport`
- [ ] **No skipped tests**: No `@Ignore`, all tests run
- [ ] **Proper naming**: All tests follow `should_*_when_*` format
- [ ] **AAA pattern**: Every test has Arrange, Act, Assert
- [ ] **Meaningful assertions**: Every test actually verifies something
- [ ] **No debug code**: No `println()`, no commented code
- [ ] **Test isolation**: Tests can run in any order
- [ ] **Factory usage**: Using test data factories
- [ ] **Error cases**: Testing both success and failure
- [ ] **Edge cases**: Testing boundary conditions
- [ ] **Documentation**: Test purposes are clear
- [ ] **No flaky tests**: Tests pass every time
- [ ] **Ready for review**: All items above completed

---

## 📞 Getting Help

### When Tests Fail

1. **Read the error message carefully**
   - What test failed?
   - What was expected?
   - What was actual?

2. **Run test in isolation**
   - `./gradlew testDebugUnitTest --tests "TestName"`

3. **Add debug output**
   - `println()` statements to trace execution

4. **Check the implementation**
   - Is the code correct?
   - Is the test correct?
   - Are they mismatched?

5. **Fix one thing at a time**
   - Fix test or code, not both
   - Run tests to verify

6. **Ask Igor if stuck**
   - Describe what you're testing
   - Show the error message
   - Show your test code
   - Describe what you've tried

### Common Questions

**Q: How much coverage do I need?**
A: 85%+ for new code. More is better, but diminishing returns after 90%.

**Q: Do I have to test everything?**
A: Test all business logic. Getters/setters without logic can be skipped.

**Q: How many test cases per function?**
A: Minimum 3: happy path, error case, edge case. More complex logic needs more tests.

**Q: Can I use @Ignore?**
A: No. Fix or remove the test. Never commit with @Ignore.

**Q: What if tests are slow?**
A: Mock slow dependencies. Use test doubles instead of real implementations.

**Q: How do I test async code?**
A: Use `runTest` and `advanceUntilIdle()` for coroutines.

---

## 🎓 Learning Path

### Day 1: Basics
- [ ] Read TESTING_RULES.md
- [ ] Run `./gradlew testDebugUnitTest`
- [ ] Open and review existing tests
- [ ] Understand AAA pattern

### Day 2: Writing Tests
- [ ] Write a simple unit test
- [ ] Run tests and verify coverage
- [ ] Add tests for edge cases
- [ ] Practice test naming

### Day 3: Advanced
- [ ] Mock dependencies
- [ ] Test with coroutines
- [ ] Test error cases
- [ ] Achieve 85%+ coverage

### Day 4+: Mastery
- [ ] Write tests first (TDD)
- [ ] Review others' tests
- [ ] Improve coverage in existing code
- [ ] Mentor other developers

---

**Last Updated**: 2026-02-16
**Version**: 1.0
**Status**: Active Testing Guide for All Developers
