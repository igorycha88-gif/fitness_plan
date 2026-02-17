# Developer Onboarding - Complete

Welcome to the Fitness Plan project! This document guides you through the complete onboarding process and sets expectations for all developers.

---

## 🎯 Phase 1: Complete Project Understanding (1-2 hours)

### Required Reading (DO NOT SKIP)

**Step 1: Quick Overview (10 minutes)**
- 📄 Read: **QUICK_REFERENCE.md**
- ✅ Goal: Understand the project at high level

**Step 2: Navigation Guide (10 minutes)**
- 📄 Read: **DOCUMENTATION_INDEX.md**
- ✅ Goal: Know how to find information

**Step 3: Comprehensive Reference (45 minutes)**
- 📄 Read: **SYSTEM_PROMPT.md** (all sections)
- ✅ Goal: Detailed understanding of architecture and components

**Step 4: Visual Architecture (30 minutes)**
- 📄 Read: **ARCHITECTURE.md** (all diagrams)
- ✅ Goal: Understand data flows and design patterns

**Step 5: Testing Guide (15 minutes)**
- 📄 Read: **TESTING.md**
- ✅ Goal: Understand testing framework and practices

### Verification

After reading, you should know:
- ✅ Project structure (data, domain, presentation, ui layers)
- ✅ Key components (9 models, 8 repositories, 6 use cases, 5 ViewModels, 9 screens)
- ✅ Architecture (Clean Architecture with Hilt DI)
- ✅ Data storage (DataStore with encryption)
- ✅ Testing framework (JUnit, Mockito, Coroutines Test)
- ✅ Navigation structure (bottom tab navigation)
- ✅ Key algorithms (weight progression, plan generation)
- ✅ Current issues (REQ-001: plan persistence)

---

## 🏗️ Phase 2: Development Environment Setup (30 minutes)

### Prerequisites
- ✅ Android Studio (latest)
- ✅ JDK 17
- ✅ Git
- ✅ Gradle (bundled)

### Setup Steps

```bash
# 1. Clone repository
git clone <repository-url>
cd fitness_plan

# 2. Build project
./gradlew clean build

# 3. Verify tests pass
./gradlew testDebugUnitTest

# 4. Build APK
./gradlew assembleDebug

# 5. (Optional) Run on device/emulator
./gradlew installDebug
```

### Verification Checklist
- [ ] Project builds without errors: `BUILD SUCCESSFUL`
- [ ] All tests pass: `All tests passed`
- [ ] No missing dependencies
- [ ] IDE recognizes all files (no red errors)
- [ ] Can run app on emulator

---

## 📋 Phase 3: Understanding Development Requirements

### Read These Mandatory Documents

**1. DEVELOPER_GUIDELINES.md** (30 minutes)
- Core requirements for all development
- Architecture rules (DO's and DON'Ts)
- Code quality standards
- Testing requirements
- Critical error prevention

**2. DEV_CHECKLIST.md** (5 minutes)
- Quick reference checklist
- Pre-commit verification
- Critical errors to avoid
- Quick commands

### Key Takeaways

**YOU MUST:**
1. ✅ Read documentation before coding
2. ✅ Create and get approval for todo list
3. ✅ Respect Clean Architecture layers
4. ✅ Write unit tests (85%+ coverage)
5. ✅ Run and verify all tests pass
6. ✅ Follow code quality standards
7. ✅ Get code review before merge
8. ✅ Update documentation

**YOU MUST NOT:**
1. ❌ Skip documentation
2. ❌ Code without approved todo list
3. ❌ Violate architecture layers
4. ❌ Commit without tests
5. ❌ Leave failing tests
6. ❌ Commit with broken functionality
7. ❌ Ignore code review feedback
8. ❌ Skip documentation updates

---

## 🚀 Phase 4: First Task Assignment

### Task Selection Criteria

Your first task should:
- ✅ Be relatively simple (4-8 hours total)
- ✅ Not involve complex algorithms
- ✅ Have clear requirements
- ✅ Have existing patterns to follow
- ✅ Not require architectural changes

**Example First Tasks:**
- Add a new UI screen
- Fix a small bug
- Add input validation
- Improve existing functionality
- Add tests to untested code

### Task Assignment Process

1. **Get assigned a task**
   - Igor assigns feature/bug to you
   - Clear requirements provided

2. **Create Todo List**
   - Follow template in DEVELOPER_GUIDELINES.md
   - Break down into specific tasks
   - Include research, implementation, testing, docs

3. **Get Approval**
   - Post todo list to Igor
   - Get feedback
   - Update as needed
   - Wait for approval before coding

4. **Begin Implementation**
   - Mark todo as `in_progress`
   - Follow all guidelines
   - Run tests frequently
   - Update todo as you complete tasks

5. **Submit for Review**
   - Mark todo as `completed`
   - Create PR with detailed description
   - Include checklist verification
   - Request review from Igor

---

## 📚 Common First Task: Fix a Small Bug

### Example Walkthrough

**Scenario**: Users report that exercise filter doesn't work properly

### Step 1: Research (1 hour)
- [ ] Read SYSTEM_PROMPT.md § ExerciseLibrary
- [ ] Read ARCHITECTURE.md § Data Flow
- [ ] Check GitHub issue #123
- [ ] Review related code:
  - `ExerciseLibraryRepositoryImpl.kt`
  - `ExerciseLibraryViewModel.kt`
  - `ExerciseLibraryScreen.kt`
- [ ] Understand the bug

### Step 2: Plan (30 min)
```markdown
## Bug Fix: Exercise Filter Not Working

### Root Cause (research found)
Filter logic in ExerciseLibraryRepositoryImpl.filterByType()
incorrectly combines conditions

### Tasks:
1. [ ] Research & understand issue
2. [ ] Write test case that reproduces bug
3. [ ] Fix filtering logic
4. [ ] Update tests
5. [ ] Manual testing
6. [ ] Update SYSTEM_PROMPT.md
7. [ ] Create PR & code review
```

### Step 3: Get Approval
- Post the plan to Igor
- Wait for approval
- Update todo as `in_progress`

### Step 4: Implementation (2-3 hours)

**Write Test First**
```kotlin
@Test
fun test_filter_by_type_strength() {
    val repository = ExerciseLibraryRepositoryImpl(context)
    val strengthExercises = repository.filterByType(ExerciseType.STRENGTH)

    // Should only contain strength exercises
    assertTrue(strengthExercises.all { it.type == ExerciseType.STRENGTH })
    assertTrue(strengthExercises.isNotEmpty())
}
```

**Run Test (should fail)**
```bash
./gradlew testDebugUnitTest --tests "ExerciseLibraryRepositoryTest"
```

**Fix Implementation**
```kotlin
// ExerciseLibraryRepositoryImpl.kt
fun filterByType(type: ExerciseType): List<ExerciseLibrary> {
    return exercises.filter { it.exerciseType == type }  // FIX: was using wrong field
}
```

**Run Test (should pass)**
```bash
./gradlew testDebugUnitTest --tests "ExerciseLibraryRepositoryTest"
```

**Manual Testing**
- [ ] Run app on emulator
- [ ] Go to Exercise Library
- [ ] Filter by Type
- [ ] Verify only selected type shows
- [ ] Test with different types
- [ ] Verify no crashes

### Step 5: Code Review (30 min)
```markdown
## PR: Fix exercise type filter

**Description**:
Fixed bug where exercise filter by type was not working correctly.
Root cause was filtering on wrong field in repository.

**Testing**:
- Added unit test: `test_filter_by_type_strength()`
- Manual testing on emulator: Verified filtering works
- All tests passing: `BUILD SUCCESSFUL`

**Changes**:
- Fixed: ExerciseLibraryRepositoryImpl.filterByType()
- Added: Unit test for filter logic
- Updated: SYSTEM_PROMPT.md with fix note
```

---

## 🎓 Development Workflow Summary

### Every Feature/Bug Fix Follows This Pattern:

```
1. PLAN
   ├─ Read documentation
   ├─ Create todo list
   └─ Get approval

2. IMPLEMENT
   ├─ Write tests first
   ├─ Implement code
   ├─ Run tests frequently
   └─ Update todo

3. TEST
   ├─ All unit tests pass
   ├─ Manual testing
   ├─ Edge cases verified
   └─ No errors

4. DOCUMENT
   ├─ Update code comments
   ├─ Update SYSTEM_PROMPT.md
   ├─ Update related docs
   └─ Clear commit message

5. REVIEW
   ├─ Self-review checklist
   ├─ Create PR
   ├─ Request review
   └─ Address feedback

6. MERGE
   ├─ Approval received
   ├─ Final tests pass
   ├─ Mark todo complete
   └─ Merge to develop/main
```

---

## 🧪 Testing Practices

### Rule: No Code Without Tests

**Test Coverage Requirements:**
- Domain Models: 95%+ coverage
- Use Cases: 85%+ coverage
- Repositories: 80%+ coverage
- ViewModels: 70%+ coverage
- Overall new code: 85%+ coverage

**Verify Before Committing:**
```bash
./gradlew testDebugUnitTest
# Expected: BUILD SUCCESSFUL
```

### Test Template

```kotlin
@Test
fun should_[expected_behavior]_when_[condition]() {
    // ARRANGE - Set up test data
    val input = "test"
    val expected = "TEST"

    // ACT - Execute code under test
    val result = function(input)

    // ASSERT - Verify result
    assertEquals(expected, result)
}
```

---

## 💡 Pro Tips for Success

### Tip 1: Start Small
- First task should be simple
- Gain confidence with the system
- Ask questions freely
- Learn patterns from existing code

### Tip 2: Read Code Before Writing
- Check similar implementations
- Copy successful patterns
- Understand design decisions
- Follow existing conventions

### Tip 3: Ask Early, Ask Often
- Architecture questions? Ask Igor
- Understanding? Ask Igor
- Stuck? Ask Igor immediately
- **Better to ask and be sure than to code wrong**

### Tip 4: Test Everything
- Write tests as you code
- Run tests frequently (not just at end)
- Fix failing tests immediately
- Never push failing tests

### Tip 5: Document as You Go
- Add comments while coding (logic fresh)
- Update docs after feature complete
- Clear commit messages help future you
- Good documentation saves time

---

## 🚨 Common Mistakes to Avoid

### Mistake 1: Skipping Documentation
❌ **Problem**: Don't read docs, start coding, violate architecture
✅ **Solution**: Read docs FIRST, understand architecture, THEN code

### Mistake 2: No Todo List
❌ **Problem**: Start coding without plan, get lost, waste time
✅ **Solution**: Create todo, get approval, follow plan

### Mistake 3: Testing Later
❌ **Problem**: Implement, then write tests, tests fail, debug
✅ **Solution**: Write tests first, code to pass tests, tests guide design

### Mistake 4: Violating Architecture
❌ **Problem**: UI accessing repository directly, tight coupling
✅ **Solution**: Follow layers strictly, use ViewModel, inject dependencies

### Mistake 5: Committing Failing Tests
❌ **Problem**: Commit with failing tests, breaks CI, needs revert
✅ **Solution**: Run tests before commit, fix all failures immediately

### Mistake 6: Silent Failures
❌ **Problem**: Catch exception, do nothing, user sees nothing
✅ **Solution**: Show error to user, log for debugging, handle gracefully

### Mistake 7: No Input Validation
❌ **Problem**: Accept any input, crash on edge cases
✅ **Solution**: Validate all input, throw meaningful errors

### Mistake 8: Forgetting Documentation
❌ **Problem**: Code works but undocumented, others confused
✅ **Solution**: Update docs alongside code

---

## 📞 Getting Help

### Resources in Order of Preference

1. **Documentation** (check first!)
   - QUICK_REFERENCE.md - Fast answers
   - SYSTEM_PROMPT.md - Detailed info
   - ARCHITECTURE.md - Design patterns
   - DEV_CHECKLIST.md - Verification

2. **Code Examples**
   - Similar implementations in codebase
   - Unit tests for patterns
   - Existing ViewModels/Repositories

3. **Igor**
   - Ask questions
   - Architectural decisions
   - Code review feedback
   - Debugging help

### How to Ask Effectively

**Good Question:**
```
"I'm implementing the weight progression feature.
Should I create a separate calculator class like WeightCalculator.kt
or put the logic in the use case?

I see WorkoutDateCalculator exists, so I'm thinking separate class
would be consistent. Should I follow that pattern?"
```

**Bad Question:**
```
"How do I do this?"
```

---

## ✅ Onboarding Completion Checklist

### Before Your First Task

- [ ] Read QUICK_REFERENCE.md
- [ ] Read DOCUMENTATION_INDEX.md
- [ ] Read SYSTEM_PROMPT.md (all sections)
- [ ] Read ARCHITECTURE.md (all sections)
- [ ] Read TESTING.md
- [ ] Read DEVELOPER_GUIDELINES.md
- [ ] Read DEV_CHECKLIST.md
- [ ] Project builds successfully
- [ ] All tests pass
- [ ] Can run app on emulator
- [ ] Understand requirements from this document
- [ ] Know where to find information
- [ ] Understand development workflow

### For Your First Task

- [ ] Task assigned and understood
- [ ] Created detailed todo list
- [ ] Got approval from Igor
- [ ] Implementation complete
- [ ] All tests written and passing
- [ ] Manual testing verified
- [ ] Documentation updated
- [ ] PR created with description
- [ ] Code review completed
- [ ] Feedback addressed
- [ ] PR approved and merged
- [ ] Mark todo as completed

---

## 🎉 You're Ready!

Once you've completed this onboarding:

✅ You understand the project architecture
✅ You know the development workflow
✅ You know where to find information
✅ You understand testing requirements
✅ You know code quality standards
✅ You can start developing!

---

## 📋 Quick Reference After Onboarding

**When you need to:**
- Quick answer → QUICK_REFERENCE.md
- Find something → DOCUMENTATION_INDEX.md
- Detailed info → SYSTEM_PROMPT.md
- Design patterns → ARCHITECTURE.md
- Testing help → TESTING.md
- Development rules → DEVELOPER_GUIDELINES.md
- Verification → DEV_CHECKLIST.md
- First task help → This document (Phase 4)

---

## 🚀 Ready to Start?

1. ✅ Complete all phases above
2. ✅ Verify all checklist items
3. ✅ Ask Igor to assign first task
4. ✅ Create todo list
5. ✅ Get approval
6. ✅ Begin implementation following guidelines

**Welcome to the team! 🎉**

---

**Last Updated**: 2026-02-16
**Version**: 1.0
**Status**: Complete & Ready for New Developers

---

## 📞 Questions About Onboarding?

Contact Igor with:
- What didn't make sense?
- Which part was unclear?
- Do you need clarification on anything?

**This onboarding document will be updated based on feedback.**
