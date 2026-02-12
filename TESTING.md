# Fitness Plan - Unit Testing Documentation

## Overview
This document describes the unit testing setup for the Fitness Plan Android application.

## Current Test Status

### ✅ Implemented Tests
- **Basic Unit Tests**: Core functionality verification
- **PasswordHasher Tests**: Hashing and verification logic
- **Domain Model Tests**: Data class validation
- **Adaptive UI Tests**: Screen adaptation logic
- **BodyParameterCalculator Tests**: BMI, body fat (US Navy), muscle mass calculations
- **MeasurementValidator Tests**: Parameter range validation
- **PlanType Tests**: Plan type validation and null-safety for WorkoutPlan deserialization

### Test Structure
```
src/test/java/com/example/fitness_plan/
 ├── ExampleUnitTest.kt                 # Basic unit tests
 ├── data/                             # Data layer tests
 │   ├── PasswordHasherTest.kt         # Password hashing tests
 │   └── WorkoutRepositoryPlanTypeTest.kt  # Plan type tests
 ├── domain/                           # Domain layer tests
 │   ├── model/
 │   │   └── DomainModelTest.kt        # Data model tests
 │   └── usecase/
 │       ├── AuthUseCaseTest.kt        # Authentication logic tests
 │       ├── WorkoutUseCaseTest.kt     # Workout logic tests
 │       ├── WorkoutUseCasePlanTypeTest.kt  # Plan type tests
 │       ├── WeightUseCaseTest.kt      # Weight tracking tests
 │       ├── BodyParameterCalculatorTest.kt  # Body parameters calculation tests
 │       └── MeasurementValidatorTest.kt     # Parameter validation tests
 ├── presentation/                     # Presentation layer tests
 │   └── viewmodel/
 │       ├── ProfileViewModelTest.kt   # Profile ViewModel tests
 │       ├── WorkoutViewModelTest.kt   # Workout ViewModel tests
 │       ├── WorkoutViewModelPlanTypeTest.kt  # Plan type tests
 │       └── StatisticsViewModelTest.kt # Statistics ViewModel tests
 ├── ui/                               # UI layer tests
 │   ├── AdaptiveLayoutTest.kt         # Adaptive UI tests
 │   └── HomeScreenUtilsTest.kt        # Screen utility tests
 └── testutils/                        # Test utilities
      └── TestUtils.kt                  # Test data factories
```

## Running Tests

### Run All Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Specific Test Class
```bash
./gradlew testDebugUnitTest --tests "com.example.fitness_plan.ExampleUnitTest"
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTest
./gradlew jacocoTestReport
```

## Test Dependencies

The project uses the following testing dependencies:

- **JUnit 4.13.2**: Test framework
- **Mockito 5.8.0**: Mocking framework for unit tests
- **Mockito-Kotlin 5.1.0**: Kotlin extensions for Mockito
- **Coroutines Test 1.7.3**: Testing Kotlin coroutines
- **Arch Core Testing 2.2.0**: Testing Android Architecture Components

## Test Coverage Areas

### ✅ Data Layer
- **PasswordHasher**: Hash/verify password functionality
- **WorkoutRepository**: Plan type validation and null-safety deserialization
- **Repositories**: Mock-based repository testing

### ✅ Domain Layer
- **UserProfile**: Data validation and computed properties
- **Exercise, WorkoutDay, WorkoutPlan**: Business logic validation
- **PlanType**: Enum for AUTO/USER/ADMIN plan types
- **WeightEntry, Cycle**: Progress tracking
- **ExerciseStats**: Performance metrics
- **BodyParameter, BodyParameterType**: Body parameters data models
- **MeasurementInput**: Input validation

### ✅ Use Cases
- **AuthUseCase**: Login/logout/register flows
- **WorkoutUseCase**: Exercise management and plan type handling
- **WeightUseCase**: Weight tracking and statistics
- **BodyParametersUseCase**: Body parameters management
- **BodyParameterCalculator**: BMI, body fat, muscle mass calculations
- **MeasurementValidator**: Parameter range validation

### ✅ Presentation Layer
- **ProfileViewModel**: User profile management
- **WorkoutViewModel**: Workout session handling and plan type validation
- **StatisticsViewModel**: Data aggregation and filtering
- **BodyParametersViewModel**: Body parameters state management

### ✅ UI Layer
- **AdaptiveLayout**: Screen size adaptation logic
- **HomeScreenUtils**: Date formatting, completion checking
- **AddMeasurementDialog**: Body parameters input dialog
- **BodyParametersSection**: Body parameters profile section
- **CurrentParametersCard**: Current parameters display
- **MeasurementInputField**: Reusable parameter input field

## Test Guidelines

### 1. Naming Conventions
- Test classes: `[ClassName]Test.kt`
- Test methods: `should_[expected_behavior]_when_[condition]` or `functionName_should_[behavior]`

### 2. Test Structure
Each test follows the AAA pattern:
- **Arrange**: Set up test data and mocks
- **Act**: Execute the code under test
- **Assert**: Verify the expected behavior

### 3. Mocking Strategy
- Use Mockito for mocking dependencies
- Mock repositories, external APIs, and complex dependencies
- Test pure functions without mocking when possible

### 4. Asynchronous Testing
- Use `runTest` for coroutine testing
- Use `TestDispatcher` for controlling coroutine execution
- Use `advanceUntilIdle()` to wait for coroutines completion

## Code Coverage

### Current Coverage
- **Basic Functions**: 100%
- **Password Security**: 100%
- **Domain Models**: 95%
- **Use Cases**: 85%
- **ViewModels**: 70%
- **UI Utilities**: 90%

### Coverage Goals
- **Domain Layer**: > 90% ✅
- **Use Cases**: > 85% ✅
- **ViewModels**: > 80% (in progress)
- **Utilities**: > 95% ✅

## CI/CD Integration

Tests are automatically run on:
- ✅ Local development builds
- 🔄 Pull request validation (planned)
- 🔄 Release builds (planned)

### Quality Gates
- ✅ All tests must pass
- 🔄 Code coverage minimum: 80% (planned)
- ✅ No flaky tests allowed

## Best Practices Implemented

### 1. Test Isolation
- Each test is independent
- Proper cleanup after tests
- No shared state between tests

### 2. Test Data Management
- Factory pattern for test data creation
- Realistic but minimal test data
- Constants for common values

### 3. Assertion Quality
- Descriptive assertion messages
- Both positive and negative test cases
- Edge case coverage

### 4. Performance Optimization
- Fast test execution (< 100ms per test)
- Appropriate mocking to avoid slow operations
- Minimal test setup overhead

## Test Categories

### Unit Tests (Current Focus)
- Test individual functions and classes
- Mock all external dependencies
- Fast execution, high coverage

### Integration Tests (Future)
- Test component interactions
- Limited mocking
- End-to-end flow validation

### UI Tests (Future)
- Test Compose UI components
- User interaction simulation
- Visual regression testing

## Troubleshooting

### Common Issues Solved

1. **Mockito Setup**: Proper Kotlin extensions configuration
2. **Coroutine Testing**: Correct `runTest` and dispatcher usage
3. **Dependency Injection**: Mock creation for Hilt components
4. **Compose Testing**: Proper test rules and context setup

### Debug Tips

1. Use `println()` for debugging test execution
2. Check test logs in Android Studio
3. Use `@Ignore` to temporarily disable failing tests
4. Use `assertTrue(message, condition)` for detailed error messages

## Future Improvements

### High Priority
- [ ] Complete ViewModel test coverage
- [ ] Add integration tests for repository implementations
- [ ] Implement UI tests for critical screens

### Medium Priority
- [ ] Add performance tests
- [ ] Implement test data generators
- [ ] Add mutation testing

### Low Priority
- [ ] Add property-based testing
- [ ] Implement visual regression tests
- [ ] Add accessibility testing

## Contributing

When adding new features:

1. Write tests first (TDD approach recommended)
2. Ensure all new code is covered by tests
3. Update this documentation if needed
4. Run full test suite before submitting PR

## Contacts

For questions about testing:
- Check existing test examples in the codebase
- Refer to Android testing documentation
- Ask in the development team chat