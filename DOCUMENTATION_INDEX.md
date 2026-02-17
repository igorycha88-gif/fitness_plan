# Documentation Index - Fitness Plan Project

Welcome! This guide helps you navigate the comprehensive documentation for the Fitness Plan fitness tracking application.

---

## 📚 Documentation Files

### 1. **SYSTEM_PROMPT.md** (1,568 lines) 🌟 START HERE
**Comprehensive project documentation covering everything about Fitness Plan.**

**Sections**:
- ✅ Project Overview & Tech Stack
- ✅ Architecture & Package Structure
- ✅ All Domain Models (UserProfile, WorkoutPlan, Exercise, Cycle, etc.)
- ✅ Key Use Cases & Repositories
- ✅ DataStore Schema & Data Storage
- ✅ Navigation Structure
- ✅ Business Logic & Algorithms
- ✅ Key Features (implemented & in progress)
- ✅ Testing Infrastructure
- ✅ CI/CD Pipeline (GitHub Actions)
- ✅ Development Workflow & Git Strategy
- ✅ Deployment & Security
- ✅ Debugging & Troubleshooting
- ✅ Build Configuration
- ✅ Code Patterns & Conventions
- ✅ Performance Optimization

**Use when**: You need detailed information about any aspect of the project.

---

### 2. **QUICK_REFERENCE.md** (292 lines) ⚡ QUICK LOOKUP
**Fast reference guide for common tasks and important information.**

**Sections**:
- 🚀 Quick Start (build commands)
- 📱 Main Features overview
- 🔑 Key Classes (ViewModels, Repositories, Use Cases, Screens)
- 📊 Data Flow diagram
- 🔍 Important Files guide (what to edit for what feature)
- 🐛 Current Issue REQ-001 explanation
- 🏗️ Architecture Rules (DO's & DON'Ts)
- 🧪 Testing quick start
- 📦 Dependencies summary
- 🔐 Security overview
- 📱 UI Navigation map
- 💡 Pro Tips

**Use when**: You need quick answers or fast command reference.

**Best for**:
- Running tests or building APK
- Finding which file to edit
- Understanding the current issue (REQ-001)
- Quick architecture rules refresh

---

### 3. **ARCHITECTURE.md** (619 lines) 🏗️ DEEP DIVE
**Visual architecture diagrams and detailed data flow explanations.**

**Sections**:
- 🎯 Clean Architecture diagram (layered)
- 💉 Dependency Injection with Hilt (DI Graph)
- 📈 Data Flow Diagrams:
  - User Registration & Profile Setup
  - Workout Plan Generation
  - Exercise Completion & Progress
  - Adaptive Weight Progression (every 10 days)
  - Statistics Display
- 🔄 State Management Flow (ViewModel pattern)
- 🗺️ Navigation Structure
- 🧵 Threading & Coroutines
- ✅ Layering Rules (allowed/not allowed dependencies)
- 🔐 Security Layers
- 🧪 Testing Architecture

**Use when**: You need to understand architectural decisions, data flow, or design patterns.

**Best for**:
- Understanding how features connect
- Debugging data flow issues
- Learning the architecture
- Understanding ViewModel state management
- Implementing new features correctly

---

### 4. **TESTING.md** (228 lines) 🧪 EXISTING FRAMEWORK
**Original comprehensive testing documentation.**

**Sections**:
- Test Status & Coverage
- Test Structure & Location
- Running Tests (commands)
- Test Dependencies
- Test Coverage by Layer
- Test Guidelines & Best Practices
- CI/CD Integration
- Troubleshooting

**Use when**: Need reference on test structure and running tests.

---

### 5. **TESTING_RULES.md** (778 lines) 📋 MANDATORY RULES
**Definitive testing rules and standards for all developers.**

**Sections**:
- ✅ Core Testing Principles (5 mandatory rules)
- ✅ Testing Checklist (before coding, during, before commit, before PR)
- ✅ Testing Architecture by Layer (Domain, Data, Presentation, UI)
- ✅ Test Structure & Patterns (AAA pattern, naming convention, test factories)
- ✅ Mocking Strategy (when to mock, Mockito, coroutines)
- ✅ Coverage Verification (generating reports, requirements by layer)
- ✅ Critical Testing Errors (7 types of PR rejections)
- ✅ Test Execution Workflow (daily, before PR, CI/CD)
- ✅ Common Testing Mistakes (7 examples with fixes)
- ✅ Test Debugging (running specific tests, viewing logs, failures)
- ✅ Testing Resources & Links

**Key Requirements**:
- **85%+ code coverage MANDATORY** (non-negotiable)
- **ALL tests MUST pass** before commit
- **No code without tests** allowed
- **Failing tests = automatic PR rejection**
- **No @Ignore or skipped tests**

**Use when**: Need to understand WHAT testing rules to follow and WHY.

---

### 6. **TESTING_PROMPT.md** (887 lines) 🎯 DEVELOPER WORKFLOW
**Active testing prompt for developers writing tests during implementation.**

**Sections**:
- ✅ Testing Responsibilities (before, during, after, code review)
- ✅ Understanding Test Requirements (what/what-not to test, coverage)
- ✅ Writing Tests Step-by-Step (7-step walkthrough)
- ✅ Testing Different Layers (Domain, Data, Presentation, UI)
- ✅ Mocking Patterns (basic, coroutines, collections)
- ✅ Test Naming Convention (format, good/bad examples)
- ✅ Debugging Failing Tests (5-step process)
- ✅ Generating Coverage Reports (step-by-step)
- ✅ Complete Testing Workflow (8 phases)
- ✅ Test-Driven Development (TDD approach)
- ✅ Quick Reference (commands, templates)
- ✅ Pre-Submission Checklist (14 items)
- ✅ Common Questions & Answers
- ✅ Learning Path (4 days)

**Use when**: Actively writing tests, need immediate guidance, or learning TDD.

---

### 7. **docs/requirements/REQ-001-FixExerciseRepetition.md**
**Specific requirement document for the exercise persistence issue.**

**Content**:
- Problem description
- Root cause analysis
- Proposed solution
- Implementation plan
- Testing strategy

**Use when**: Working on fixing the plan persistence issue.

---

## 🗺️ Navigation Guide

### "I want to..."

#### 🚀 **Get Started / Set Up Development**
1. Read: **QUICK_REFERENCE.md** → Quick Start section
2. Read: **SYSTEM_PROMPT.md** → Tech Stack section
3. Commands: `./gradlew clean build`

#### 📚 **Understand the Project**
1. Read: **SYSTEM_PROMPT.md** → Project Overview & Architecture
2. Read: **ARCHITECTURE.md** → Clean Architecture diagram
3. Reference: **QUICK_REFERENCE.md** → Key Classes section

#### 🏗️ **Understand Data Flow**
1. Read: **ARCHITECTURE.md** → Data Flow Diagrams
2. Reference: **QUICK_REFERENCE.md** → Data Flow section
3. Dive: **SYSTEM_PROMPT.md** → Key Use Cases section

#### ✏️ **Add a New Feature**
1. Review: **ARCHITECTURE.md** → Layering Rules
2. Read: **SYSTEM_PROMPT.md** → Code Patterns & Conventions
3. Check: **QUICK_REFERENCE.md** → Architecture Rules

#### 🐛 **Fix a Bug**
1. Reference: **QUICK_REFERENCE.md** → Important Files guide
2. Review: **SYSTEM_PROMPT.md** → Debugging & Troubleshooting
3. Check: **ARCHITECTURE.md** → Data Flow (relevant area)

#### 🧪 **Write Tests**
1. Read: **TESTING.md** → Test Guidelines & Best Practices
2. Review: **QUICK_REFERENCE.md** → Testing section
3. Run: `./gradlew testDebugUnitTest`

#### 📊 **Work on Statistics**
1. File: `app/src/main/java/com/example/fitness_plan/ui/StatisticsScreen.kt`
2. ViewModel: `StatisticsViewModel.kt`
3. Charts: `app/src/main/java/com/example/fitness_plan/ui/charts/`
4. Reference: **ARCHITECTURE.md** → Statistics Display

#### ⚖️ **Work on Weight Progression**
1. Main files: **QUICK_REFERENCE.md** → "Work on Weight Progression"
2. Algorithm: **SYSTEM_PROMPT.md** → Key Algorithms section
3. Code: `WeightCalculator.kt`, `WeightProgressionUseCase.kt`

#### 🏋️ **Work on Workout Plans**
1. Main file: `WorkoutRepositoryImpl.kt`
2. Algorithm: **SYSTEM_PROMPT.md** → Workout Plan Generation
3. Issue: **QUICK_REFERENCE.md** → REQ-001 section

#### 👤 **Work on User Profiles**
1. Model: `UserProfile.kt`
2. ViewModel: `ProfileViewModel.kt`
3. Repository: `UserRepository.kt`
4. Screen: `ProfileScreen.kt`

#### 🏃 **Fix the REQ-001 Issue**
1. Read: **QUICK_REFERENCE.md** → REQ-001 explanation
2. Details: `docs/requirements/REQ-001-FixExerciseRepetition.md`
3. Code location: `WorkoutRepositoryImpl.kt`
4. Solution: **QUICK_REFERENCE.md** → Code example in REQ-001 section

#### 📱 **Work on UI/Screens**
1. Main location: `app/src/main/java/com/example/fitness_plan/ui/`
2. Navigation: **ARCHITECTURE.md** → Navigation Structure
3. Screens: **QUICK_REFERENCE.md** → Screens table
4. Theme: `app/src/main/java/com/example/fitness_plan/ui/theme/`

#### 🔐 **Work on Security**
1. Overview: **SYSTEM_PROMPT.md** → Security section
2. Implementation: `SecurityModule.kt`, `PasswordHasher.kt`
3. Architecture: **ARCHITECTURE.md** → Security Layers

#### 🔄 **Understand CI/CD**
1. Overview: **SYSTEM_PROMPT.md** → CI/CD Pipeline
2. Workflows: `.github/workflows/ci.yml`, `auto-version.yml`
3. Process: **QUICK_REFERENCE.md** → Git Workflow

#### 📚 **Write Documentation**
1. Location: `docs/` directory
2. Format: Markdown (GitHub-flavored)
3. Examples: Existing `.md` files

---

## 🎯 Key Concepts Quick Links

### By Component

**Repositories**:
- WorkoutRepository → SYSTEM_PROMPT.md (Data Layer)
- ExerciseLibraryRepository → SYSTEM_PROMPT.md (Data Layer)
- CycleRepository → SYSTEM_PROMPT.md (Data Layer)
- UserRepository → SYSTEM_PROMPT.md (Data Layer)
- CredentialsRepository → SYSTEM_PROMPT.md (Security section)

**ViewModels**:
- WorkoutViewModel → QUICK_REFERENCE.md (Key Classes)
- ProfileViewModel → QUICK_REFERENCE.md (Key Classes)
- StatisticsViewModel → QUICK_REFERENCE.md (Key Classes)
- ExerciseLibraryViewModel → QUICK_REFERENCE.md (Key Classes)

**Use Cases**:
- WorkoutUseCase → SYSTEM_PROMPT.md (Key Use Cases)
- CycleUseCase → SYSTEM_PROMPT.md (Key Use Cases)
- WeightProgressionUseCase → SYSTEM_PROMPT.md (Key Use Cases)
- AuthUseCase → SYSTEM_PROMPT.md (Key Use Cases)

**Models**:
- UserProfile → SYSTEM_PROMPT.md (Key Domain Models)
- WorkoutPlan → SYSTEM_PROMPT.md (Key Domain Models)
- Exercise → SYSTEM_PROMPT.md (Key Domain Models)
- Cycle → SYSTEM_PROMPT.md (Key Domain Models)
- ExerciseLibrary → SYSTEM_PROMPT.md (Key Domain Models)

### By Feature

**Weight Progression**:
- Algorithm: SYSTEM_PROMPT.md → Adaptive Weight Progression
- Implementation: WeightCalculator.kt, WeightProgressionUseCase.kt
- Architecture: ARCHITECTURE.md → Adaptive Weight Progression flow

**Plan Generation**:
- Algorithm: SYSTEM_PROMPT.md → Workout Plan Generation
- Implementation: WorkoutRepositoryImpl.kt
- Issue: QUICK_REFERENCE.md → REQ-001
- Architecture: ARCHITECTURE.md → Workout Plan Generation flow

**Exercise Library**:
- Data: 100+ exercises in ExerciseLibraryRepositoryImpl.kt
- Filtering: Equipment, muscle groups, type
- Alternatives: Search by muscle group
- UI: ExerciseLibraryScreen.kt

**Statistics**:
- ViewModel: StatisticsViewModel.kt
- Charts: WeightChart.kt, VolumeChart.kt, FrequencyChart.kt
- Data source: ExerciseStatsRepository.kt
- Screen: StatisticsScreen.kt

---

## 📋 File Organization

```
📁 fitness_plan/
├── 📄 SYSTEM_PROMPT.md ⭐ (comprehensive guide)
├── 📄 QUICK_REFERENCE.md ⚡ (quick lookup)
├── 📄 ARCHITECTURE.md 🏗️ (diagrams & flows)
├── 📄 TESTING.md (testing guide)
├── 📄 DOCUMENTATION_INDEX.md (this file)
│
├── 📁 app/
│   └── 📁 src/
│       ├── 📁 main/java/com/example/fitness_plan/
│       │   ├── 📁 data/ (repositories)
│       │   ├── 📁 domain/ (use cases, models)
│       │   ├── 📁 presentation/ (viewmodels)
│       │   ├── 📁 ui/ (compose screens)
│       │   ├── 📁 notification/ (notifications)
│       │   └── 📁 security/ (encryption)
│       │
│       ├── 📁 test/java/ (unit tests)
│       └── 📁 androidTest/java/ (instrumentation tests)
│
├── 📁 docs/
│   └── 📁 requirements/
│       └── 📄 REQ-001-FixExerciseRepetition.md
│
└── 📁 .github/
    └── 📁 workflows/
        ├── 📄 ci.yml (CI/CD pipeline)
        └── 📄 auto-version.yml (auto versioning)
```

---

## 🎓 Learning Path

### For New Team Members

1. **Day 1: Overview** (1-2 hours)
   - Read: QUICK_REFERENCE.md (entire)
   - Watch architecture: ARCHITECTURE.md → Clean Architecture diagram

2. **Day 2: Deep Dive** (2-3 hours)
   - Read: SYSTEM_PROMPT.md → Project Overview, Tech Stack, Architecture
   - Read: ARCHITECTURE.md → Data Flow Diagrams

3. **Day 3: Setup & Testing** (1-2 hours)
   - Setup: QUICK_REFERENCE.md → Quick Start
   - Tests: TESTING.md → Running Tests section
   - Run: `./gradlew testDebugUnitTest`

4. **Day 4: Key Concepts** (2-3 hours)
   - Models: SYSTEM_PROMPT.md → Key Domain Models
   - Use Cases: SYSTEM_PROMPT.md → Key Use Cases
   - Repositories: SYSTEM_PROMPT.md → Data Layer

5. **Day 5: Feature Deep Dive** (2-3 hours)
   - Choose a feature: Weight Progression, Plan Generation, Stats
   - Read: SYSTEM_PROMPT.md → relevant algorithm
   - Read: ARCHITECTURE.md → relevant data flow
   - Explore code: files listed in QUICK_REFERENCE.md

6. **First Task: Simple Bug Fix** (2-4 hours)
   - Reference: QUICK_REFERENCE.md → Important Files guide
   - Debugging: SYSTEM_PROMPT.md → Debugging & Troubleshooting
   - Testing: TESTING.md

### For Maintenance

- **Weekly**: QUICK_REFERENCE.md (stay fresh)
- **As Needed**: SYSTEM_PROMPT.md (detailed reference)
- **When Stuck**: ARCHITECTURE.md → relevant section

---

## 🔍 Using This Documentation

### Tips for Effective Navigation

1. **Use Ctrl+F / Cmd+F** to search for keywords
2. **Follow links** between documents for deeper context
3. **Reference diagrams** in ARCHITECTURE.md for visual understanding
4. **Check QUICK_REFERENCE.md first** for fast answers
5. **Go to SYSTEM_PROMPT.md** for comprehensive details

### Common Search Terms

- `DataStore` → SYSTEM_PROMPT.md § Data Storage
- `Weight Progression` → SYSTEM_PROMPT.md § Key Algorithms + ARCHITECTURE.md § Adaptive Weight Progression
- `REQ-001` → QUICK_REFERENCE.md § Current Issue
- `ViewModel` → ARCHITECTURE.md § State Management Flow
- `Repository` → SYSTEM_PROMPT.md § Data Layer
- `UseCase` → SYSTEM_PROMPT.md § Key Use Cases
- `Navigation` → ARCHITECTURE.md § Navigation Structure
- `CI/CD` → SYSTEM_PROMPT.md § CI/CD Pipeline
- `Encryption` → SYSTEM_PROMPT.md § Security + ARCHITECTURE.md § Security Layers
- `Exercise` → SYSTEM_PROMPT.md § ExerciseLibrary model + Key Domain Models

---

## 📞 Getting Help

### When You Get Stuck

1. **Check QUICK_REFERENCE.md**
   - Search for keywords
   - Look at Important Files section

2. **Search SYSTEM_PROMPT.md**
   - Use Ctrl+F for detailed searches
   - Check specific sections

3. **Review ARCHITECTURE.md**
   - Understand data flow
   - Review layering rules

4. **Check Code Examples**
   - SYSTEM_PROMPT.md has code patterns
   - Review existing implementations

### Reporting Issues

- Use GitHub Issues
- Reference: docs/requirements/REQ-001-*.md (as template)
- Include: Error message, steps to reproduce, expected behavior

---

## 📝 Maintaining Documentation

When you make changes:

1. **Update relevant doc** if documentation is affected
2. **Run tests** to ensure quality
3. **Update version date** in last section
4. **Add to QUICK_REFERENCE.md** if it's important
5. **Update SYSTEM_PROMPT.md** if it's architectural

---

## 📊 Documentation Statistics

| Document | Lines | Size | Purpose |
|----------|-------|------|---------|
| SYSTEM_PROMPT.md | 1,568 | 48KB | Comprehensive reference |
| ARCHITECTURE.md | 619 | 33KB | Diagrams & data flows |
| TESTING_RULES.md | 778 | 19KB | **NEW** Mandatory testing rules |
| TESTING_PROMPT.md | 887 | 20KB | **NEW** Active testing workflow |
| QUICK_REFERENCE.md | 292 | 6.3KB | Quick lookup |
| TESTING.md | 228 | 6.6KB | Testing framework reference |
| DOCUMENTATION_INDEX.md | ~450 | 11KB | Navigation (this file) |
| **TOTAL** | **~4,800+** | **~144KB+** | **Complete documentation** |

**Code Volume**: ~12,100 lines of Kotlin
**Documentation Ratio**: ~1 line of docs per 2.5 lines of code (significantly improved)
**Testing Documentation**: 2,463 lines across 4 dedicated documents

---

## 🎯 Quick Jump Links

- 🌟 **Start here**: [SYSTEM_PROMPT.md](SYSTEM_PROMPT.md)
- ⚡ **Quick answers**: [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- 🏗️ **Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md)
- 📋 **Testing Rules** (MANDATORY): [TESTING_RULES.md](TESTING_RULES.md)
- 🎯 **Testing Workflow**: [TESTING_PROMPT.md](TESTING_PROMPT.md)
- 🧪 **Testing Reference**: [TESTING.md](TESTING.md)
- 🐛 **Current issue**: [REQ-001](docs/requirements/REQ-001-FixExerciseRepetition.md)

---

**Last Updated**: 2026-02-16
**Documentation Version**: 3.0 (Added comprehensive testing rules & workflow)
**Project Version**: 2.2

**Recent Additions** (Today):
- TESTING_RULES.md: 778 lines of mandatory testing rules and standards
- TESTING_PROMPT.md: 887 lines of active testing workflow for developers

*This documentation provides a comprehensive guide to the Fitness Plan Android application. For the most up-to-date information, refer to the source code in the repository.*
