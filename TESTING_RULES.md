# Fitness Plan - Testing Rules & Standards

This document defines the mandatory testing rules and standards for the Fitness Plan project. All developers MUST follow these rules without exception.

---

## 🎯 Core Testing Principles

### Principle 1: No Code Without Tests
**❌ RULE**: You cannot commit code without corresponding unit tests.
**✅ REQUIREMENT**: Every new function, class, and feature must have unit tests.
**📊 METRIC**: 85%+ code coverage is **NON-NEGOTIABLE**.

### Principle 2: All Tests Must Pass
**❌ RULE**: Never commit or submit a PR with failing tests.
**✅ REQUIREMENT**: `./gradlew testDebugUnitTest` must always return `BUILD SUCCESSFUL` with 0 failures.
**🔴 CONSEQUENCE**: PR will be **automatically rejected** if any tests fail.

### Principle 3: Test Isolation
**❌ RULE**: Tests cannot depend on each other or run in specific order.
**✅ REQUIREMENT**: Each test must be independent and runnable standalone.
**💡 PRINCIPLE**: Test A's pass/fail cannot affect Test B's pass/fail.

### Principle 4: Test Coverage by Layer
**📊 MANDATORY COVERAGE REQUIREMENTS**:
- Domain Models: **95%+** coverage
- Domain Use Cases: **85%+** coverage
- Data Layer Repositories: **80%+** coverage
- Presentation Layer ViewModels: **70%+** coverage
- UI Layer (Composables): **60%+** coverage (lower priority)
- **Overall new code: 85%+ coverage MINIMUM**

### Principle 5: Deterministic Tests
**❌ RULE**: Tests must be deterministic (same result every run).
**✅ REQUIREMENT**: No random data, timing-dependent logic, or external dependencies.
**🎲 AVOID**: Using `System.currentTimeMillis()`, `Random()`, external network calls.

---

## 📋 Testing Checklist (MANDATORY)

### Before Writing Code
- [ ] Read existing tests to understand patterns
- [ ] Identify what needs to be tested
- [ ] Plan test cases (happy path, error cases, edge cases)
- [ ] Have a clear definition of what "passing" means

### During Implementation
- [ ] Write tests FIRST (Test-Driven Development)
- [ ] Run tests FREQUENTLY (after every small change)
- [ ] Keep tests fast (< 100ms each)
- [ ] Use meaningful test names
- [ ] Follow AAA pattern (Arrange, Act, Assert)

### Before Commit
- [ ] All tests pass: `./gradlew testDebugUnitTest`
- [ ] Coverage verified: `./gradlew jacocoTestReport`
- [ ] No test skipped with `@Ignore` or `@Disabled`
- [ ] No `Thread.sleep()` or timing tricks
- [ ] No hardcoded test data in shared locations

### Before PR Submission
- [ ] Coverage > 85% for new code
- [ ] All existing tests still pass
- [ ] No flaky tests
- [ ] PR includes test updates
- [ ] Test descriptions in commit message

---

## 🏗️ Testing Architecture by Layer

### Domain Layer Tests (HIGHEST PRIORITY)

**Coverage Required**: 95%+

**What to Test**:
- ✅ All domain models (data validation, computed properties)
- ✅ All use cases (business logic, edge cases)
- ✅ All algorithms (weight progression, plan generation, etc.)
- ✅ Error cases and exceptions
- ✅ Boundary conditions

**Example**:
```kotlin
// ✅ GOOD: Tests business logic
@Test
fun should_calculate_weight_correctly_when_progressive_increase_applied() {
    // ARRANGE
    val currentWeight = 50.0
    val progression = 2.5

    // ACT
    val newWeight = calculateNewWeight(currentWeight, progression)

    // ASSERT
    assertEquals(52.5, newWeight)
}

// ❌ BAD: Tests nothing, just runs code
@Test
fun test_calculate_weight() {
    val result = calculateNewWeight(50.0, 2.5)
    println(result)
}
```

### Data Layer Tests

**Coverage Required**: 80%+

**What to Test**:
- ✅ Repository implementations (mock, save, retrieve data)
- ✅ Data transformations (entity → model conversions)
- ✅ Error handling (DataStore errors, invalid data)
- ✅ Persistence (data saved and loaded correctly)

**Example**:
```kotlin
@Test
fun should_save_and_retrieve_user_profile() = runTest {
    // ARRANGE
    val repository = UserRepositoryImpl(context)
    val profile = UserProfile(name = "Test User", age = 25)

    // ACT
    repository.saveProfile(profile)
    val retrieved = repository.getProfile().first()

    // ASSERT
    assertEquals(profile, retrieved)
}
```

### Presentation Layer Tests (ViewModels)

**Coverage Required**: 70%+

**What to Test**:
- ✅ State updates (when action triggers state change)
- ✅ Event handling (when user action happens)
- ✅ ViewModel lifecycle (initialization, cleanup)
- ✅ Error handling and error state display

**Example**:
```kotlin
@Test
fun should_update_loading_state_when_loading_data() = runTest {
    // ARRANGE
    val viewModel = WorkoutViewModel(workoutUseCase)

    // ACT
    viewModel.loadWorkout()
    advanceUntilIdle()

    // ASSERT
    assertTrue(viewModel.state.value is WorkoutState.Loaded)
}
```

### UI Layer Tests (Lower Priority)

**Coverage Required**: 60%+

**What to Test**:
- ✅ Adaptive layout logic (screen size adaptation)
- ✅ Navigation between screens
- ✅ User input handling
- ✅ Screen rendering (basic, no pixel-perfect testing)

---

## 🧪 Test Structure & Patterns

### AAA Pattern (MANDATORY)

Every test MUST follow Arrange-Act-Assert:

```kotlin
@Test
fun should_[expected_behavior]_when_[condition]() {
    // 1️⃣ ARRANGE - Set up test data, mocks, dependencies
    val input = "test_input"
    val expectedOutput = "TEST_OUTPUT"
    val mockRepository = mock(MyRepository::class.java)
    `when`(mockRepository.fetch()).thenReturn(expectedOutput)

    // 2️⃣ ACT - Execute the code under test
    val sut = MyClass(mockRepository) // sut = system under test
    val result = sut.process(input)

    // 3️⃣ ASSERT - Verify expected behavior
    assertEquals(expectedOutput, result)
    verify(mockRepository).fetch()
}
```

### Test Naming Convention (MANDATORY)

**Format**: `should_[expected_behavior]_when_[condition]`

**Examples**:
- ✅ `should_return_user_when_id_exists`
- ✅ `should_throw_exception_when_input_invalid`
- ✅ `should_update_weight_when_progression_applied`
- ❌ `test1` (not descriptive)
- ❌ `calculateWeight` (sounds like a function name)

### Test Data Factories

**MANDATORY**: Create test data using factories, not magic values:

```kotlin
// ✅ GOOD: Factory pattern
object TestDataFactory {
    fun createUserProfile(
        name: String = "Test User",
        age: Int = 25,
        weight: Double = 75.0
    ): UserProfile = UserProfile(
        id = UUID.randomUUID(),
        name = name,
        age = age,
        weight = weight
    )
}

// Usage in tests
@Test
fun test_profile() {
    val profile = TestDataFactory.createUserProfile(name = "Custom")
    // ...
}

// ❌ BAD: Magic values scattered everywhere
@Test
fun test_profile() {
    val profile = UserProfile(id = UUID.randomUUID(), name = "Test", age = 25)
    // Repeated in 50 other tests
}
```

---

## 🔧 Mocking Strategy

### When to Mock (MANDATORY)

**MOCK these**:
- ✅ External dependencies (databases, APIs, file systems)
- ✅ Platform services (sensors, location, calendar)
- ✅ Complex third-party libraries
- ✅ Repositories and data sources

**DON'T mock these**:
- ❌ Pure functions (they don't need mocking)
- ❌ Data classes (use real instances)
- ❌ Simple logic
- ❌ Local calculations

### Mocking with Mockito

```kotlin
// ✅ GOOD: Mock external dependencies
@Test
fun should_load_exercises_when_repository_returns_data() = runTest {
    // ARRANGE
    val mockRepository = mock(ExerciseRepository::class.java)
    val exercises = listOf(
        Exercise(id = 1, name = "Bench Press"),
        Exercise(id = 2, name = "Squat")
    )
    `when`(mockRepository.getExercises()).thenReturn(exercises)

    val viewModel = ExerciseViewModel(mockRepository)

    // ACT
    viewModel.loadExercises()
    advanceUntilIdle()

    // ASSERT
    verify(mockRepository).getExercises()
    assertTrue(viewModel.exercises.value is List)
}

// ❌ BAD: Over-mocking, creates complex test
@Test
fun test_exercise() {
    val mockViewModel = mock(ExerciseViewModel::class.java)
    val mockState = mock(StateFlow::class.java)
    // ... mock chain continues for 20 lines
    // Test doesn't actually test anything
}
```

### Mocking Coroutines (MANDATORY)

**Use `runTest` for all coroutine tests**:

```kotlin
@Test
fun should_update_state_when_data_loaded() = runTest {
    // ARRANGE
    val mockRepository = mock(DataRepository::class.java)
    val testData = "test"
    coEvery { mockRepository.fetch() } returns testData

    val viewModel = MyViewModel(mockRepository)

    // ACT
    viewModel.load()
    advanceUntilIdle() // Wait for all coroutines to complete

    // ASSERT
    assertEquals(testData, viewModel.data.value)
}
```

---

## 📊 Coverage Verification

### Generating Coverage Report

```bash
# Generate coverage report
./gradlew jacocoTestReport

# Report location
# Mac/Linux: app/build/reports/jacoco/jacocoTestReport/html/index.html
# Open in browser to view detailed coverage
```

### Coverage Requirements by Layer

| Layer | Minimum | Target | Priority |
|-------|---------|--------|----------|
| Domain Models | 90% | 95%+ | 🔴 CRITICAL |
| Use Cases | 85% | 90%+ | 🔴 CRITICAL |
| Repositories | 80% | 85%+ | 🟠 HIGH |
| ViewModels | 70% | 80%+ | 🟠 HIGH |
| Screens | 60% | 70% | 🟡 MEDIUM |
| Utilities | 85% | 95%+ | 🟠 HIGH |

### What Counts as Coverage

**✅ Counted**:
- Lines executed
- Branches taken
- Exception handling

**❌ NOT Counted**:
- Test code itself
- Coverage annotations
- Excluded code

---

## 🚫 Critical Testing Errors (PR REJECTION)

### Error 1: Failing Tests

**❌ RULE**: Cannot submit PR with failing tests.

```bash
# This is NOT acceptable:
./gradlew testDebugUnitTest
# BUILD FAILED
# com.example.test.MyTest::test_something FAILED
```

**✅ SOLUTION**:
1. Read the error message carefully
2. Debug locally: `./gradlew testDebugUnitTest --tests "TestName"`
3. Fix the issue
4. Run tests again until all pass
5. Only THEN submit PR

### Error 2: No Tests for New Code

**❌ RULE**: Every new function/class must have tests.

```kotlin
// ❌ REJECTED: New function, no tests
fun calculateNewWeight(current: Double, add: Double): Double {
    return current + add
}

// ✅ APPROVED: New function + tests
fun calculateNewWeight(current: Double, add: Double): Double {
    return current + add
}

@Test
fun should_add_weight_correctly_when_progression_applied() {
    val result = calculateNewWeight(50.0, 2.5)
    assertEquals(52.5, result)
}
```

### Error 3: Coverage Below 85%

**❌ RULE**: New code must have 85%+ coverage.

```
Coverage Report:
- New code coverage: 72%
❌ REJECTED: Below 85% minimum
```

**✅ SOLUTION**: Write more tests to cover:
- Happy path
- Error cases
- Edge cases
- Boundary conditions

### Error 4: Flaky Tests

**❌ RULE**: Tests that sometimes pass, sometimes fail are not acceptable.

**Causes of flakiness**:
- ❌ Timing/threading issues (`Thread.sleep()`)
- ❌ Random data generation
- ❌ System clock dependencies
- ❌ File I/O timing
- ❌ External API calls

**✅ SOLUTION**:
- Use deterministic data
- Use test dispatchers for coroutines
- Mock all external dependencies
- Avoid time-dependent code

### Error 5: Skipped Tests (@Ignore)

**❌ RULE**: Cannot submit PR with `@Ignore` on tests.

```kotlin
// ❌ NOT ALLOWED in PR
@Ignore
@Test
fun should_calculate_weight() {
    // ...
}

// ✅ ALLOWED: Fix the test or remove it
@Test
fun should_calculate_weight() {
    // Fixed implementation
}
```

### Error 6: Commented-Out Tests

**❌ RULE**: Cannot have commented-out test code in PR.

```kotlin
// ❌ NOT ALLOWED
// @Test
// fun test_old_behavior() {
//     // ...
// }

// ✅ SOLUTION: Delete the test or fix it
@Test
fun should_implement_new_behavior() {
    // Fixed implementation
}
```

### Error 7: Test Doesn't Actually Test

**❌ RULE**: Test code must have meaningful assertions.

```kotlin
// ❌ REJECTED: No assertions, doesn't test anything
@Test
fun test_user_creation() {
    val user = UserProfile(name = "Test")
    println(user)
}

// ✅ APPROVED: Has assertions that verify behavior
@Test
fun should_create_user_with_correct_name() {
    val user = UserProfile(name = "Test")
    assertEquals("Test", user.name)
}
```

---

## 🧪 Test Execution Workflow

### Daily Development

```bash
# 1. After implementing a feature/fix
./gradlew testDebugUnitTest

# Expected output:
# BUILD SUCCESSFUL
# 42 tests passed

# 2. If tests fail, investigate immediately
./gradlew testDebugUnitTest --tests "SpecificTestName"

# 3. Fix the issue and re-run
./gradlew testDebugUnitTest

# 4. Repeat until all pass
```

### Before Creating PR

```bash
# 1. Run complete test suite
./gradlew testDebugUnitTest

# Expected: BUILD SUCCESSFUL with 0 failures

# 2. Generate coverage report
./gradlew jacocoTestReport

# 3. Verify coverage > 85% for new code
# Check: app/build/reports/jacoco/jacocoTestReport/html/index.html

# 4. If coverage insufficient:
# - Add more tests
# - Re-run report until coverage OK

# 5. Commit and create PR
git add .
git commit -m "feat: Add new feature with tests"
```

### CI/CD Pipeline

```yaml
GitHub Actions automatically:
1. Runs all tests: ./gradlew testDebugUnitTest
2. Generates coverage report
3. Fails PR if tests fail
4. Fails PR if coverage < 85%
5. Only allows merge if all checks pass
```

---

## 🎓 Common Testing Mistakes

### Mistake 1: Testing Implementation Details

```kotlin
// ❌ BAD: Testing internal implementation
@Test
fun test_initialization_order() {
    val viewModel = MyViewModel()
    // Checking internal list initialization
    assertTrue(viewModel._privateList.isNotEmpty())
}

// ✅ GOOD: Testing behavior
@Test
fun should_return_exercises_when_loaded() {
    val viewModel = MyViewModel()
    viewModel.load()
    assertTrue(viewModel.exercises.value.isNotEmpty())
}
```

### Mistake 2: Over-Mocking

```kotlin
// ❌ BAD: Mocking too much
@Test
fun test_logic() {
    val mock1 = mock(Class1::class.java)
    val mock2 = mock(Class2::class.java)
    val mock3 = mock(Class3::class.java)
    val mock4 = mock(Class4::class.java)
    // ... 30 more lines of setup
}

// ✅ GOOD: Only mock what's necessary
@Test
fun test_logic() {
    val mockRepository = mock(Repository::class.java)
    `when`(mockRepository.getData()).thenReturn(testData)
    // Test actual logic with mocked dependency
}
```

### Mistake 3: Ignoring Edge Cases

```kotlin
// ❌ BAD: Only tests happy path
@Test
fun test_add_weight() {
    val result = addWeight(5.0, 3.0)
    assertEquals(8.0, result)
}

// ✅ GOOD: Tests multiple cases
@Test
fun should_add_positive_weights() {
    assertEquals(8.0, addWeight(5.0, 3.0))
}

@Test
fun should_handle_negative_weights() {
    assertEquals(2.0, addWeight(5.0, -3.0))
}

@Test
fun should_handle_zero() {
    assertEquals(5.0, addWeight(5.0, 0.0))
}
```

### Mistake 4: Timing-Dependent Tests

```kotlin
// ❌ BAD: Relies on timing
@Test
fun test_async_load() {
    viewModel.load()
    Thread.sleep(1000) // Fragile!
    assertTrue(viewModel.loaded.value)
}

// ✅ GOOD: Uses test runner
@Test
fun test_async_load() = runTest {
    viewModel.load()
    advanceUntilIdle() // Proper coroutine testing
    assertTrue(viewModel.loaded.value)
}
```

### Mistake 5: Not Using Test Factories

```kotlin
// ❌ BAD: Magic values everywhere
@Test
fun test_profile1() {
    val p = UserProfile(id = "1", name = "Test", age = 25)
}

@Test
fun test_profile2() {
    val p = UserProfile(id = "2", name = "Test", age = 25)
}

@Test
fun test_profile3() {
    val p = UserProfile(id = "3", name = "Test", age = 25)
}

// ✅ GOOD: Use factory
@Test
fun test_profile1() {
    val p = TestDataFactory.createUserProfile(id = "1")
}

@Test
fun test_profile2() {
    val p = TestDataFactory.createUserProfile(id = "2")
}
```

---

## 🔍 Test Debugging

### Running Specific Test

```bash
# Run single test class
./gradlew testDebugUnitTest --tests "com.example.MyTest"

# Run single test method
./gradlew testDebugUnitTest --tests "com.example.MyTest.test_something"

# Run tests matching pattern
./gradlew testDebugUnitTest --tests "*Weight*"
```

### Viewing Test Logs

```bash
# In Android Studio, check:
# 1. Test output panel
# 2. Logcat filtered by "test:" keyword
# 3. Test report HTML
```

### Common Test Failures

| Error | Cause | Solution |
|-------|-------|----------|
| `AssertionError` | Assertion failed | Check expected vs actual value |
| `NullPointerException` | Unmocked dependency | Mock the dependency |
| `TimeoutException` | Coroutine not completing | Use `advanceUntilIdle()` |
| `NoMappingFoundException` | Mock not configured | Add `when()` stub |

---

## 📚 Testing Resources

### Project Testing Files
- `app/src/test/java/` - Unit tests
- `app/src/androidTest/java/` - Integration/UI tests
- `TESTING.md` - General testing documentation
- `TESTING_PROMPT.md` - Testing workflow guide

### Test Dependencies
- **JUnit 4.13.2** - Test framework
- **Mockito 5.8.0** - Mocking
- **Mockito-Kotlin 5.1.0** - Kotlin extensions
- **Mockk 1.13.5** - Alternative mocking
- **Coroutines Test 1.7.3** - Coroutine testing

### External Resources
- [Android Testing Docs](https://developer.android.com/training/testing)
- [JUnit Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

---

## ✅ Testing Checklist for PR Submission

Before creating a PR, verify ALL items:

- [ ] **All tests pass**: `./gradlew testDebugUnitTest` → `BUILD SUCCESSFUL`
- [ ] **No skipped tests**: No `@Ignore` or `@Disabled`
- [ ] **Coverage verified**: `./gradlew jacocoTestReport` → > 85%
- [ ] **New code has tests**: Every function/class has corresponding tests
- [ ] **Tests are meaningful**: Each test has real assertions
- [ ] **No flaky tests**: Tests pass consistently
- [ ] **Proper naming**: Test methods follow `should_*_when_*` pattern
- [ ] **AAA pattern**: All tests have Arrange, Act, Assert
- [ ] **Mocking appropriate**: Only external deps mocked
- [ ] **No debugging code**: No `println()`, `@Ignore`, commented tests
- [ ] **Test data factories**: Using consistent test data
- [ ] **Edge cases covered**: Not just happy path
- [ ] **Error cases tested**: Exception handling verified
- [ ] **Documentation updated**: Test purpose is clear
- [ ] **Commit message includes tests**: "Add feature with tests"

---

## 🚀 Continuous Improvement

### Monthly Review
- Review test failure trends
- Identify commonly tested code
- Identify under-tested code
- Update this document if needed

### Coverage Goals
- **Current**: 75% overall
- **Target**: 85%+ for critical layers
- **Aspirational**: 95%+ for domain layer

### Future Improvements
- [ ] Integration tests
- [ ] UI tests
- [ ] Performance tests
- [ ] Property-based testing
- [ ] Mutation testing

---

**Last Updated**: 2026-02-16
**Version**: 1.0
**Status**: Active - All Developers Must Comply
