# Developer Checklist - Quick Access

## 🚀 Before You Start

### Documentation (MANDATORY)
- [ ] Read QUICK_REFERENCE.md (5 min)
- [ ] Read DOCUMENTATION_INDEX.md (10 min)
- [ ] Read SYSTEM_PROMPT.md § relevant sections (30 min)
- [ ] Read ARCHITECTURE.md § relevant sections (20 min)
- [ ] Understand current architecture
- [ ] Identified dependencies & related components

### Planning (MANDATORY)
- [ ] Created detailed todo list
- [ ] Got approval from Igor
- [ ] Marked todo as `in_progress`
- [ ] Clear on requirements
- [ ] Architecture plan documented

---

## 💻 During Development

### Code Structure
- [ ] Following Clean Architecture (data → domain → presentation → ui)
- [ ] Domain layer first (models, interfaces)
- [ ] Data layer second (implementations)
- [ ] Presentation layer third (ViewModels)
- [ ] UI layer last (Composables)
- [ ] No cross-layer violations
- [ ] Using dependency injection (Hilt)

### Code Quality
- [ ] Meaningful variable names
- [ ] Functions < 30 lines
- [ ] No code duplication
- [ ] Self-documenting code
- [ ] No magic numbers
- [ ] Proper error handling
- [ ] Immutable data structures
- [ ] Input validation

### Testing (MANDATORY)
- [ ] Unit tests written for domain layer
- [ ] Unit tests written for data layer
- [ ] Unit tests written for ViewModels
- [ ] Edge cases covered
- [ ] Error scenarios covered
- [ ] Test coverage > 85%
- [ ] Using AAA pattern (Arrange, Act, Assert)
- [ ] Tests are deterministic (not flaky)

---

## 🧪 Before Commit

### Test Verification (MANDATORY)
```bash
./gradlew testDebugUnitTest
```
- [ ] **ALL TESTS PASS** ✅
- [ ] No failing tests
- [ ] No skipped tests
- [ ] No @Ignore annotations
- [ ] Coverage > 85%

### Code Review (MANDATORY)
- [ ] Code is readable
- [ ] No TODOs or hacks
- [ ] No console logging
- [ ] No commented-out code
- [ ] No debug code
- [ ] Lint errors checked: `./gradlew lint`
- [ ] No warnings suppressed without reason

### Documentation
- [ ] Code has KDoc for public APIs
- [ ] Complex logic explained
- [ ] SYSTEM_PROMPT.md updated
- [ ] README updated (if needed)

### Git & Commits
- [ ] Meaningful commit message
- [ ] Atomic commits (one feature per commit)
- [ ] No merge commits
- [ ] Feature branch from correct base
- [ ] No conflicts

---

## 📝 Before PR

### Architecture Verification
- [ ] ✅ Respect Clean Architecture layers
- [ ] ✅ Domain interfaces only (no implementations)
- [ ] ✅ Data layer has implementations
- [ ] ✅ Presentation layer has ViewModels
- [ ] ✅ UI layer has Composables
- [ ] ✅ No circular dependencies
- [ ] ✅ Business logic NOT in UI

### Testing Verification
- [ ] ✅ `./gradlew testDebugUnitTest` passes
- [ ] ✅ New code has unit tests
- [ ] ✅ All edge cases covered
- [ ] ✅ Error handling tested
- [ ] ✅ Coverage > 85%
- [ ] ✅ No flaky tests

### Security Check
- [ ] ✅ No hardcoded secrets
- [ ] ✅ No API keys exposed
- [ ] ✅ Input validated
- [ ] ✅ Proper data encryption
- [ ] ✅ No SQL injection risks

### Performance Check
- [ ] ✅ No blocking main thread
- [ ] ✅ Coroutines used for async
- [ ] ✅ No memory leaks
- [ ] ✅ Efficient database queries
- [ ] ✅ Images properly sized

### PR Description
- [ ] Title clear and concise
- [ ] Description explains what/why
- [ ] Architecture impact listed
- [ ] Related issues referenced
- [ ] Screenshots if UI change
- [ ] Checklist items completed

---

## ❌ CRITICAL ERRORS (Will Cause Rejection)

### Architecture Violations
- ❌ UI accessing repository directly
- ❌ ViewModel using another ViewModel
- ❌ Repository calling UseCase
- ❌ Business logic in UI layer
- ❌ Cross-layer dependencies

### Testing Failures
- ❌ Failing tests in PR
- ❌ No tests for new code
- ❌ Skipped tests (@Ignore)
- ❌ Flaky/unreliable tests
- ❌ Coverage < 85%

### Data Issues
- ❌ Plan not saved to DataStore
- ❌ Data lost on app restart
- ❌ No persistence for important data
- ❌ Silent failures in data operations

### Code Quality
- ❌ Commented-out code
- ❌ Debug logging left in
- ❌ Magic numbers without explanation
- ❌ No error handling
- ❌ Resource leaks

### Git Issues
- ❌ Vague commit messages
- ❌ Merge conflicts
- ❌ Commits not atomic
- ❌ Large single commit

---

## 🔄 During Code Review

### If Changes Requested
- [ ] Review feedback carefully
- [ ] Make changes
- [ ] Run tests: `./gradlew testDebugUnitTest`
- [ ] Verify all tests pass
- [ ] Push changes
- [ ] Request re-review

### If Tests Fail
- [ ] Run specific test: `./gradlew testDebugUnitTest --tests "TestName"`
- [ ] Check error message
- [ ] Debug and fix
- [ ] Re-run tests
- [ ] Never ignore failing tests

### If Architecture Issue Found
- [ ] Stop work
- [ ] Review ARCHITECTURE.md
- [ ] Check similar implementations
- [ ] Refactor to follow architecture
- [ ] Re-test
- [ ] Explain changes

---

## 📊 Quick Command Reference

```bash
# Build and Test
./gradlew clean build                          # Full build
./gradlew testDebugUnitTest                    # Run all tests
./gradlew testDebugUnitTest --tests "MyTest"   # Run specific test
./gradlew jacocoTestReport                     # Generate coverage report
./gradlew lint                                 # Check code style

# App Building
./gradlew assembleDebug                        # Build debug APK
./gradlew installDebug                         # Install on device
./gradlew connectedAndroidTest                 # Run instrumentation tests

# Debugging
adb logcat | grep "Fitness"                    # View app logs
adb shell run-as com.example.fitness_plan ...  # Access app data
```

---

## 📚 Documentation Quick Links

| Need | Document | Section |
|------|----------|---------|
| Quick answers | QUICK_REFERENCE.md | Any section |
| Architecture | ARCHITECTURE.md | Diagrams & flows |
| Complete info | SYSTEM_PROMPT.md | Relevant section |
| File locations | QUICK_REFERENCE.md | Important Files |
| How to find stuff | DOCUMENTATION_INDEX.md | "I want to..." |
| Testing guide | TESTING.md | All sections |
| This checklist | DEV_CHECKLIST.md | (this file) |
| Development rules | DEVELOPER_GUIDELINES.md | (companion file) |

---

## ✅ Success Criteria

### Feature is DONE when:

**Functionality** ✅
- [ ] Works as specified
- [ ] No known bugs
- [ ] Edge cases handled
- [ ] Tested on device

**Code Quality** ✅
- [ ] Follows architecture
- [ ] Follows code style
- [ ] No duplication
- [ ] Efficient

**Testing** ✅
- [ ] Unit tests written
- [ ] All tests passing
- [ ] Coverage > 85%
- [ ] Manual testing done

**Documentation** ✅
- [ ] Code documented
- [ ] SYSTEM_PROMPT updated
- [ ] Related docs updated
- [ ] Commits descriptive

**Security** ✅
- [ ] No secrets exposed
- [ ] Input validated
- [ ] Data secured

**Git** ✅
- [ ] PR created
- [ ] Detailed description
- [ ] Code review done
- [ ] Approved

---

## 🎓 Common Patterns

### Save to DataStore
```kotlin
context.dataStore.edit { prefs ->
    prefs[stringPreferencesKey("key")] = value
}
```

### Load from DataStore
```kotlin
context.dataStore.data.map { prefs ->
    prefs[stringPreferencesKey("key")] ?: defaultValue
}
```

### ViewModel State
```kotlin
private val _state = MutableStateFlow<State>(State.Loading)
val state: StateFlow<State> = _state.asStateFlow()
```

### Test with Coroutines
```kotlin
@Test
fun test() = runTest {
    // test code
    advanceUntilIdle()
}
```

### Repository Pattern
```kotlin
class MyRepository(private val context: Context) : IMyRepository {
    override fun getData(): Flow<Data> = /* implementation */
}
```

---

## 🚨 Emergency Contacts

**Need help?** Check in this order:
1. QUICK_REFERENCE.md
2. SYSTEM_PROMPT.md
3. ARCHITECTURE.md
4. Ask Igor

**Something broken?**
1. Read error message carefully
2. Check test output
3. Review recent changes
4. Check documentation
5. Ask Igor

---

## 📋 Pre-Submission Workflow

```
┌─ Read Documentation
│  └─ Create Todo List
│     └─ Get Approval
│        └─ CODE
│           └─ Write Tests
│              └─ Run Tests ✅
│                 └─ Update Documentation
│                    └─ Code Review Self-Check
│                       └─ Commit Changes
│                          └─ Create PR
│                             └─ Review Comments
│                                └─ Merge ✅
```

---

**Print this page and keep it handy! 📋**

**Last Updated**: 2026-02-16
**Version**: 1.0
