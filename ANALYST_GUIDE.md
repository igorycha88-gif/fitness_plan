# Analyst Guide - Fitness Plan Project

## 🎯 Role Overview

As an analyst on the Fitness Plan project, your role is to:
- ✅ Understand project requirements and scope
- ✅ Analyze current state and identify gaps
- ✅ Document findings and recommendations
- ✅ Create requirements for new features/fixes
- ✅ Validate implementation against requirements
- ✅ Track metrics and progress
- ✅ Communication bridge between stakeholders and development team

---

## 📚 MANDATORY: Documentation Study

### Required Reading (BEFORE any analysis work)

**Phase 1: Project Understanding (1-2 hours)**

1. **QUICK_REFERENCE.md** (5 min)
   - Quick facts about the project
   - Build commands
   - Current issues at a glance

2. **SYSTEM_PROMPT.md** (60 min)
   - Complete architecture
   - All components & models
   - Business logic & algorithms
   - Data storage schema
   - Current features & status

3. **ARCHITECTURE.md** (30 min)
   - Visual architecture diagrams
   - Data flow for all major features
   - Design patterns used
   - State management approach

4. **DOCUMENTATION_INDEX.md** (10 min)
   - Navigation guide
   - How to find specific information
   - Component & feature links

**Phase 2: Specific Feature Understanding (varies)**

Before analyzing any feature:
- Read relevant section in SYSTEM_PROMPT.md
- Study related data flow in ARCHITECTURE.md
- Review related code patterns in DEVELOPER_GUIDELINES.md
- Check current status in GitHub Issues

### Knowledge Checklist

After reading, you should know:

✅ **Architecture**
- [ ] Clean Architecture (3 layers: data, domain, presentation, ui)
- [ ] Dependency Injection (Hilt)
- [ ] Data flow patterns
- [ ] Repository pattern

✅ **Core Components**
- [ ] 9 domain models (UserProfile, WorkoutPlan, Exercise, etc.)
- [ ] 8 repositories (Workout, ExerciseLibrary, Cycle, User, etc.)
- [ ] 6 use cases
- [ ] 5 main ViewModels
- [ ] 9 UI screens

✅ **Features**
- [ ] User registration & authentication
- [ ] Workout plan generation
- [ ] Exercise tracking & completion
- [ ] Adaptive weight progression (10-day microcycles)
- [ ] Statistics & charts
- [ ] Exercise library (100+ exercises)
- [ ] Admin panel
- [ ] Notifications

✅ **Technical**
- [ ] DataStore for persistence
- [ ] Encryption (Android Security Crypto)
- [ ] Testing framework (JUnit, Mockito)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Current issues (REQ-001: plan persistence)

---

## 🔍 Analysis Methodology

### Standard Analysis Process

#### Step 1: Define Scope & Requirements

**Document:**
```markdown
## Analysis: [Feature/Issue Name]

### Objective
[Clear statement of what you're analyzing]

### Scope
- Features included: [list]
- Features excluded: [list]
- Stakeholders affected: [list]

### Key Questions
- [ ] What is the current state?
- [ ] What is the desired state?
- [ ] What gaps exist?
- [ ] What are the impacts?
- [ ] What are the risks?

### Assumptions
- [List any assumptions made]
```

#### Step 2: Gather Current State Information

**Data Sources:**
- SYSTEM_PROMPT.md (architecture & current implementation)
- QUICK_REFERENCE.md (features status)
- GitHub Issues (reported problems)
- Code review (actual implementation)
- Test results (validation)

**Document:**
```markdown
### Current State
- Feature: [description]
- Components involved: [list]
- Related data models: [list]
- User flow: [steps]
- Data flow: [diagram or description]
- Known issues: [list]
- Test coverage: [percentage]
```

#### Step 3: Identify Gaps & Issues

**Analysis Types:**
- Functional gaps (feature doesn't work as intended)
- Non-functional gaps (performance, security, usability)
- Implementation gaps (code quality, testing, documentation)
- Design gaps (architecture violations, inefficiencies)

**Document:**
```markdown
### Identified Gaps

#### Gap 1: [Gap Name]
- **Type**: Functional / Non-functional / Implementation / Design
- **Current State**: [What is happening now]
- **Expected State**: [What should happen]
- **Impact**: [Effect on users/system]
- **Severity**: Critical / High / Medium / Low
- **Evidence**: [Screenshots, logs, code references]

#### Gap 2: [Gap Name]
- ...
```

#### Step 4: Create Requirements

**Requirement Template:**
```markdown
## Feature: [Feature Name]
**Status**: New / In Progress / In Review / Complete
**Priority**: Critical / High / Medium / Low
**Complexity**: Simple / Medium / Complex / Very Complex

### Description
[Clear, concise description of what needs to be done]

### User Story
As a [user type],
I want to [action],
So that [benefit/outcome]

### Acceptance Criteria
- [ ] Criterion 1: [specific, testable, measurable]
- [ ] Criterion 2: [specific, testable, measurable]
- [ ] Criterion 3: [specific, testable, measurable]

### Technical Requirements
- Architecture impact: [list changes needed]
- Components affected: [list]
- Data models affected: [list]
- New APIs needed: [list]
- Test coverage: [required percentage]

### Dependencies
- Depends on: [list other features/issues]
- Blocks: [list other features/issues]
- Related to: [list related features/issues]

### Implementation Notes
- Recommended approach: [description]
- Potential risks: [list]
- Performance considerations: [list]
- Security considerations: [list]

### Validation Plan
- Unit tests: [what to test]
- Manual testing: [steps to verify]
- Edge cases: [list edge cases]
- Error scenarios: [list error scenarios]

### Metrics
- Success metric 1: [measurable]
- Success metric 2: [measurable]

### Notes
- [Additional notes]
```

#### Step 5: Create Issue Documentation

**GitHub Issue Template:**
```markdown
## [Type: Feature / Bug / Enhancement / Documentation]

### Title
[Clear, concise title]

### Description
[Use requirement template above]

### Related Issues
- Relates to: #123
- Depends on: #456
- Blocks: #789

### Labels
- priority: high
- type: feature
- status: ready-for-dev
```

#### Step 6: Store Requirements Documentation

**📁 Requirements Repository Location:**
```
/docs/requirements/
```

**File Naming Convention:**
```
REQ-[NUMBER]-[Feature-Name].md
Example: REQ-001-FixExerciseRepetition.md
```

**Directory Structure:**
```
docs/
├── requirements/
│   ├── REQ-001-FixExerciseRepetition.md
│   ├── REQ-002-EnhanceStatistics.md
│   ├── REQ-003-ImproveNotifications.md
│   └── README.md (requirements index)
├── analysis/
│   ├── Feature-Analysis-[Name].md
│   └── Market-Analysis-[Name].md
└── reports/
    ├── Weekly-Progress-[Date].md
    └── Monthly-Metrics-[Date].md
```

**What to Store in `/docs/requirements/`:**

1. **Feature Requirements Documents** (PRE-DEVELOPMENT)
   - Full requirements (from template above)
   - Acceptance criteria
   - Technical specifications
   - Design diagrams if applicable
   - Example: `REQ-002-AddDarkMode.md`

2. **Bug/Issue Documentation** (PRE-FIX)
   - Bug analysis & reproduction steps
   - Root cause analysis
   - Fix approach & validation plan
   - Example: `REQ-001-FixExerciseRepetition.md`

3. **Enhancement Specifications**
   - Improvement details
   - User impact
   - Implementation approach
   - Example: `REQ-003-EnhanceFiltering.md`

4. **Analysis Reports** (OPTIONAL)
   - Feature analysis findings
   - Gap analysis
   - Impact assessment
   - Location: `/docs/analysis/`

**Example Requirement File Structure:**

File: `docs/requirements/REQ-002-AddDarkMode.md`
```markdown
# REQ-002: Add Dark Mode Support

**Status**: New / In Progress / In Review / Complete
**Priority**: High
**Complexity**: Medium
**Created**: 2026-02-16
**Updated**: 2026-02-20

## Description
Add dark mode theme support to the application...

## User Story
As a user,
I want to enable dark mode in app settings,
So that I can reduce eye strain in low-light conditions

## Acceptance Criteria
- [ ] Settings screen has dark mode toggle
- [ ] All screens render correctly in dark mode
- [ ] Theme preference persists across sessions
- [ ] Performance not impacted (< 100ms theme switch)

## Technical Requirements
- Use Material3 dynamic theming
- Store preference in DataStore
- No breaking changes to existing code
- Test coverage: > 85%

## Implementation Notes
- Location: `ui/theme/Theme.kt`
- Components affected: All Composable screens
- No new dependencies needed

## Validation Plan
- Manual testing on light & dark modes
- Test on different devices
- Verify persistence
- Performance testing

## Notes
- Reference: SYSTEM_PROMPT.md § UI/Theme
- Related: REQ-001 (after this)
- Blocks: None currently
```

**Workflow:**

1. **Analyst Creates Requirement**
   ```bash
   Create: docs/requirements/REQ-XXX-[Name].md
   ```

2. **Review & Approval**
   ```
   Stakeholder reviews → Analyst refines → Igor approves
   ```

3. **Share with Development**
   ```
   Link in GitHub Issue → Development team reviews
   ```

4. **During Development**
   ```
   Update status: In Progress
   Add implementation notes
   Track changes
   ```

5. **After Completion**
   ```
   Mark status: Complete
   Add validation results
   Archive for reference
   ```

**Accessing Requirements:**

For developers:
```bash
# Find relevant requirements
ls docs/requirements/

# Read specific requirement
cat docs/requirements/REQ-001-FixExerciseRepetition.md

# Link to GitHub issue
Reference: See docs/requirements/REQ-XXX-[Name].md
```

For tracking:
```bash
# All requirements
grep -r "^#" docs/requirements/

# By status
grep "Status.*In Progress" docs/requirements/*.md

# By priority
grep "Priority.*Critical" docs/requirements/*.md
```

**Important:**
- ✅ Always store in `/docs/requirements/` folder
- ✅ Use REQ-NUMBER naming convention
- ✅ Keep requirements clear & detailed
- ✅ Link GitHub issues to requirements files
- ✅ Update status during development
- ✅ Archive completed requirements for reference

---

## 📊 Current State Analysis

### Project Status Overview

**Version**: 2.2
**Status**: Active Development, Production Ready
**Code Size**: ~12,100 lines of Kotlin
**Test Coverage**: 80-95% (varies by layer)
**Documentation**: Comprehensive (SYSTEM_PROMPT.md, etc.)

### Feature Status Matrix

| Feature | Status | Notes |
|---------|--------|-------|
| User Authentication | ✅ Complete | Login, register, profiles |
| Workout Plan Generation | ✅ Complete | Based on user profile |
| Exercise Tracking | ✅ Complete | Mark complete/incomplete |
| Adaptive Weight Progression | ✅ Complete | 10-day microcycles |
| Statistics & Charts | ✅ Complete | Weight, volume, frequency |
| Exercise Library | ✅ Complete | 100+ exercises with filters |
| Admin Panel | ✅ Complete | Custom plan upload |
| Notifications | ✅ Complete | Reminders via WorkManager |
| **Plan Persistence** | 🔧 **Issue REQ-001** | **Not saving to DataStore** |

### Critical Issues

#### REQ-001: Exercise Plan Repetition on App Restart

**Status**: In Development
**Priority**: Critical
**Severity**: High (affects user experience)

**Problem:**
- User creates profile → Plan generated in memory
- User logout → Plan lost
- User login → New plan generated (different exercises)
- Expected: Plan should persist & reload with same exercises

**Root Cause:** Plan not saved to DataStore immediately after generation

**Solution Path:** Save to DataStore in WorkoutRepositoryImpl.generateNewPlan()

**Components Affected:**
- WorkoutRepositoryImpl.kt
- CycleUseCase.kt
- DataStore schema

**Documentation:**
- See: docs/requirements/REQ-001-FixExerciseRepetition.md
- See: QUICK_REFERENCE.md § Current Issue

---

## 🏗️ Analyzing New Features

### Feature Analysis Checklist

Before proposing a new feature, verify:

**1. Alignment**
- [ ] Aligns with project vision
- [ ] Fits within scope
- [ ] Addresses user need
- [ ] Doesn't duplicate existing feature

**2. Technical Feasibility**
- [ ] Can be implemented within architecture
- [ ] Doesn't require major refactoring
- [ ] Has clear implementation path
- [ ] Performance acceptable
- [ ] Security considered

**3. Impact Assessment**
- [ ] What components need changes?
- [ ] New models needed?
- [ ] New repositories needed?
- [ ] New use cases needed?
- [ ] UI changes required?
- [ ] Database/DataStore changes?
- [ ] Breaking changes to existing APIs?

**4. Effort Estimation**
- [ ] Complexity: Simple / Medium / Complex
- [ ] Estimated developer hours: X-Y
- [ ] Testing effort: X-Y
- [ ] Documentation effort: X hours

**5. Risk Assessment**
- [ ] What could go wrong?
- [ ] Performance impact?
- [ ] Security risks?
- [ ] User experience risks?
- [ ] Mitigation strategies?

### Feature Analysis Template

```markdown
## Feature Analysis: [Feature Name]

### Executive Summary
[One-paragraph overview of the feature, its benefits, and impact]

### User Story
As a [user type],
I want to [specific action],
So that [benefit]

### Current State
[What exists today]
- Current implementation: [brief description]
- Related features: [list]
- Known limitations: [list]

### Proposed Solution
[Detailed description of the new feature]

### Benefits
- Benefit 1: [description + metrics if possible]
- Benefit 2: [description + metrics if possible]
- Benefit 3: [description + metrics if possible]

### Technical Impact
**New Components:**
- Models: [list]
- Repositories: [list]
- Use Cases: [list]
- ViewModels: [list]
- Screens: [list]

**Modified Components:**
- [Component]: [changes]
- [Component]: [changes]

**Data Changes:**
- New DataStore keys: [list]
- Modified models: [list]
- Migration needed: [yes/no]

### Implementation Approach
1. Step 1: [description]
2. Step 2: [description]
3. Step 3: [description]

### Acceptance Criteria
- [ ] Criterion 1: [specific, testable]
- [ ] Criterion 2: [specific, testable]
- [ ] Criterion 3: [specific, testable]
- [ ] All tests pass
- [ ] Code coverage > 85%
- [ ] Documentation updated

### Effort Estimation
- Development: X-Y hours
- Testing: X-Y hours
- Documentation: X hours
- **Total: X-Y days**

### Risk Assessment
| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| [Risk] | High/Med/Low | High/Med/Low | [Strategy] |

### Dependencies
- Depends on: [list features/issues]
- Blocks: [list features/issues]
- Parallel work: [list features that can be done in parallel]

### Success Metrics
- Metric 1: [measurable definition of success]
- Metric 2: [measurable definition of success]

### Open Questions
- [ ] Question 1: [description + who should answer]
- [ ] Question 2: [description + who should answer]

### Next Steps
1. [ ] Get stakeholder approval
2. [ ] Create detailed requirements document
3. [ ] Create GitHub issues for development
4. [ ] Schedule kickoff with development team
5. [ ] Setup success metrics tracking
```

---

## 🐛 Analyzing Bugs & Issues

### Bug Analysis Process

**Step 1: Reproduce**
- [ ] Can you reproduce the issue?
- [ ] Consistent or intermittent?
- [ ] What steps trigger it?
- [ ] On which devices/versions?
- [ ] What's the expected vs actual behavior?

**Step 2: Investigate**
- [ ] Review code involved
- [ ] Check test coverage for code path
- [ ] Look at recent changes
- [ ] Check DataStore/data consistency
- [ ] Verify error handling

**Step 3: Document**
```markdown
## Bug Report: [Bug Name]

### Description
[Clear description of the problem]

### Reproduction Steps
1. [Step 1]
2. [Step 2]
3. [Step 3]

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happens]

### Environment
- App version: [version]
- Device: [device]
- Android version: [version]
- Test/Production: [environment]

### Impact
- Severity: Critical / High / Medium / Low
- User impact: [what users experience]
- Frequency: Always / Often / Sometimes / Rare

### Root Cause
[Analysis of what's causing the issue]

### Affected Code
- File: [path]
- Function: [name]
- Lines: [numbers]

### Related Issues
- Related to: #123
- Depends on: #456

### Fix Approach
[High-level description of the fix]

### Validation
- How to verify fix: [steps]
- Test case needed: [yes/no]
- Edge cases to check: [list]
```

---

## 📈 Metrics & Reporting

### Key Metrics to Track

**Development Metrics:**
```
Build Success Rate: [target: 100%]
Test Pass Rate: [target: 100%]
Code Coverage: [target: > 85%]
CI/CD Pipeline Duration: [target: < 10 min]
Lint Violations: [target: 0]
```

**Feature Metrics:**
```
Features Completed: [count]
Features In Progress: [count]
Features Blocked: [count]
Average Feature Completion Time: [X days]
Feature Complexity Distribution: [breakdown]
```

**Quality Metrics:**
```
Bug Reports: [count]
Critical Issues: [count]
High Priority Issues: [count]
Time to Fix (Critical): [target: < 24 hours]
Time to Fix (High): [target: < 1 week]
```

**Team Metrics:**
```
Code Review Turnaround: [X hours average]
PR Approval Rate: [target: > 95%]
Test Writing Rate: [target: >= code]
Documentation Coverage: [target: 100%]
```

### Reporting Template

```markdown
## Weekly Progress Report - Week of [Date]

### Summary
[Brief overview of the week's progress]

### Completed
- Feature/Fix 1: [description] (#issue)
- Feature/Fix 2: [description] (#issue)

### In Progress
- Feature/Fix 1: [progress %] (#issue)
- Feature/Fix 2: [progress %] (#issue)

### Blocked
- Issue 1: [blocker] (waiting for #xxx)
- Issue 2: [blocker] (waiting for #xxx)

### Metrics
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Build Success Rate | 100% | 100% | ✅ |
| Test Pass Rate | 100% | 100% | ✅ |
| Code Coverage | 82% | > 85% | ⚠️ |

### Risks & Issues
- Risk 1: [description] - Mitigation: [action]
- Issue 1: [description] - Resolution: [action]

### Next Week
- Priority 1: [task]
- Priority 2: [task]
- Priority 3: [task]

### Notes
[Any additional information]
```

---

## 🤝 Stakeholder Communication

### Requirement Communication Template

**To Development Team:**
```markdown
## Feature Requirements: [Feature Name]

### Overview
[One-paragraph summary]

### User Story
As a [user type], I want to [action], so that [benefit]

### Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

### Implementation Notes
[Technical guidance or constraints]

### Questions for Discussion
[Open questions]
```

**To Business Stakeholders:**
```markdown
## Feature Status: [Feature Name]

### Status
[✅ Complete / 🔄 In Progress / 🔧 Blocked / ⏳ Pending]

### Progress
[Current completion percentage + description]

### User Impact
[How this benefits users]

### Timeline
[Expected completion date]

### Risks/Blockers
[Any issues affecting delivery]

### Next Steps
[What happens next]
```

### Issue/Bug Communication

**To Users:**
```markdown
## Issue: [Issue Name]

### Description
[What users experience]

### Status
[Investigating / In Progress / Fixed / Workaround Available]

### Impact
[Who/what is affected]

### Workaround (if applicable)
[Temporary solution if available]

### ETA
[Expected fix date]

### Updates
[Regular status updates]
```

---

## 💡 Analysis Best Practices

### DO's

✅ **DO read the documentation first**
- Always consult SYSTEM_PROMPT.md
- Review ARCHITECTURE.md for technical context
- Check existing GitHub issues
- Look at current code before proposing changes

✅ **DO be specific**
- Use exact file paths and line numbers
- Reference specific models and functions
- Include code snippets when relevant
- Provide concrete examples

✅ **DO consider architecture**
- Ensure proposals fit Clean Architecture
- Respect layer boundaries
- Consider data flow implications
- Think about testing strategy

✅ **DO document thoroughly**
- Write clear requirement documents
- Create well-structured GitHub issues
- Include acceptance criteria
- Document design decisions

✅ **DO communicate with development**
- Get early feedback on feasibility
- Discuss technical approach
- Ask clarifying questions
- Be available for discussions

✅ **DO validate assumptions**
- Verify understanding with stakeholders
- Test assumptions with data/prototypes
- Get feedback on requirements
- Confirm acceptance criteria

### DON'Ts

❌ **DON'T skip documentation**
- Never propose features without reading SYSTEM_PROMPT.md
- Don't assume understanding without studying architecture
- Don't ignore current issues status
- Don't create requirements without understanding current state

❌ **DON'T be vague**
- Avoid "improve performance"
- Don't say "make it better"
- Don't suggest features without user stories
- Don't create requirements without acceptance criteria

❌ **DON'T ignore architecture**
- Don't propose cross-layer access
- Don't suggest adding logic to UI layer
- Don't propose breaking changes without impact analysis
- Don't ignore Clean Architecture principles

❌ **DON'T under-document**
- Don't create vague requirements
- Don't skip acceptance criteria
- Don't forget to document design decisions
- Don't overlook edge cases

❌ **DON'T work in isolation**
- Don't make decisions without developer input
- Don't skip stakeholder communication
- Don't create requirements without validation
- Don't block team without communication

❌ **DON'T ignore testing**
- Don't propose features without test strategy
- Don't skip validation planning
- Don't ignore coverage requirements
- Don't overlook edge cases and error scenarios

---

## 🔗 Working With Development Team

### Before Starting Development

**Analyst Responsibilities:**
1. ✅ Create detailed requirements document
2. ✅ Create GitHub issue with full context
3. ✅ Get stakeholder approval
4. ✅ Answer developer questions about requirements
5. ✅ Validate that requirements are clear and testable

**Questions to Answer:**
- What is the exact user need?
- How will success be measured?
- What are the constraints/limitations?
- What are edge cases?
- How urgent is this?

### During Development

**Analyst Responsibilities:**
1. ✅ Answer clarifying questions
2. ✅ Validate design decisions
3. ✅ Help with acceptance criteria interpretation
4. ✅ Review progress
5. ✅ Stay available for discussions

**Be Available For:**
- Design discussions
- Clarification of requirements
- Edge case discussions
- Testing strategy validation

### After Development

**Analyst Responsibilities:**
1. ✅ Validate implementation against requirements
2. ✅ Review acceptance criteria
3. ✅ Test edge cases
4. ✅ Sign off on completion
5. ✅ Document learnings

---

## 📋 Analysis Checklists

### Pre-Analysis Checklist

- [ ] Read QUICK_REFERENCE.md
- [ ] Read SYSTEM_PROMPT.md (relevant sections)
- [ ] Read ARCHITECTURE.md (relevant sections)
- [ ] Reviewed current GitHub issues
- [ ] Understand current state
- [ ] Identified key stakeholders
- [ ] Clear on objectives

### Feature Analysis Checklist

- [ ] User story created
- [ ] Acceptance criteria defined (specific & testable)
- [ ] Technical impact assessed
- [ ] Components identified
- [ ] New models/repositories identified
- [ ] Data flow documented
- [ ] Testing strategy defined
- [ ] Effort estimated
- [ ] Risks identified
- [ ] Success metrics defined
- [ ] Open questions documented
- [ ] Validated with stakeholders
- [ ] Ready for development handoff

### Bug Analysis Checklist

- [ ] Issue reproduced
- [ ] Root cause identified
- [ ] Affected code documented
- [ ] Impact assessed
- [ ] Fix approach proposed
- [ ] Test cases identified
- [ ] Severity assigned
- [ ] Dependencies noted
- [ ] Ready for development

### Requirement Document Checklist

- [ ] Clear title
- [ ] Complete description
- [ ] User story included
- [ ] Acceptance criteria (5-10 items)
- [ ] Technical requirements
- [ ] Dependencies listed
- [ ] Validation plan defined
- [ ] Success metrics specified
- [ ] Risks documented
- [ ] Implementation notes included
- [ ] Diagrams/examples provided
- [ ] Ready for development handoff

### GitHub Issue Checklist

- [ ] Title is clear and concise
- [ ] Description complete
- [ ] Acceptance criteria listed
- [ ] Linked to related issues
- [ ] Assigned correct labels
- [ ] Assigned to milestone
- [ ] Priority set
- [ ] Ready for development queue

---

## 📚 Reference Materials

### Key Documents to Consult

| Document | Purpose | When to Use |
|----------|---------|------------|
| SYSTEM_PROMPT.md | Comprehensive reference | Detailed technical questions |
| ARCHITECTURE.md | Design & patterns | Understanding data flow |
| QUICK_REFERENCE.md | Fast lookup | Quick questions |
| TESTING.md | Testing approach | Test strategy questions |
| DEVELOPER_GUIDELINES.md | Dev requirements | Understanding constraints |
| GitHub Issues | Current work | Tracking status |
| Code | Implementation details | Validation & clarification |
| docs/requirements/ | **Requirements repository** | **Store all requirement documents** |

### Requirements Documentation Repository

**Location**: `docs/requirements/` folder

**Purpose**: Central repository for all analytical requirements documents

**What to Store Here:**
- Feature requirements (pre-development)
- Bug/issue documentation (pre-fix)
- Enhancement specifications
- Analysis reports
- Impact assessments

**File Naming**: `REQ-[NUMBER]-[Feature-Name].md`

**Examples in Repository:**
- `docs/requirements/REQ-001-FixExerciseRepetition.md` (existing issue)
- `docs/requirements/REQ-002-AddDarkMode.md` (new feature)
- `docs/requirements/REQ-003-EnhanceFiltering.md` (enhancement)

**Workflow:**
1. Create requirement: `docs/requirements/REQ-XXX-[Name].md`
2. Review & approve
3. Link to GitHub issue
4. Update status during development
5. Mark complete after implementation
6. Archive for reference

**Access Requirements:**
```bash
# View all requirements
ls docs/requirements/

# View specific requirement
cat docs/requirements/REQ-001-FixExerciseRepetition.md

# In GitHub issue, reference:
See: docs/requirements/REQ-XXX-[Name].md
```

**💡 Important:**
- ✅ Always create detailed requirement documents
- ✅ Store in `/docs/requirements/` folder
- ✅ Use consistent naming convention (REQ-NUMBER)
- ✅ Keep updated throughout development
- ✅ Link from GitHub issues

### Key Contacts

**For Technical Questions:**
- Development Lead (Igor)
- Senior Developer
- Architecture Decisions

**For Product Questions:**
- Product Manager
- Business Stakeholder
- User Representatives

**For Process Questions:**
- Project Manager
- Team Lead
- Process Owner

---

## 🎯 Your First Analysis Task

### Sample Task: Analyze "Alternative Exercise Selection"

**Requirements:**
1. Read SYSTEM_PROMPT.md § ExerciseLibrary
2. Read ARCHITECTURE.md § Data Flow
3. Review code: WorkoutViewModel, ExerciseLibraryViewModel
4. Analyze current implementation
5. Document findings
6. Create analysis document

**Deliverable:**
```markdown
## Analysis: Alternative Exercise Selection

### Current State
[Current implementation description]
- Where: ExerciseLibraryScreen.kt, WorkoutViewModel.kt
- How it works: [description]
- Limitations: [list]

### User Impact
[How users interact with it]
- Positive aspects: [list]
- Pain points: [list]

### Technical Assessment
[Technical evaluation]
- Architecture compliance: [yes/no + why]
- Code quality: [assessment]
- Test coverage: [percentage]
- Performance: [assessment]

### Opportunities
[Improvement suggestions]
1. [Opportunity 1]
2. [Opportunity 2]

### Risks
[Identified risks]
1. [Risk 1]

### Recommendations
[What should be done]
1. [Recommendation 1]
```

---

## 📞 Getting Help

### When You're Stuck

1. **Check Documentation First**
   - SYSTEM_PROMPT.md
   - ARCHITECTURE.md
   - QUICK_REFERENCE.md

2. **Review Code**
   - Related implementations
   - Test cases
   - Comments and docs

3. **Ask Development Team**
   - Ask Igor for clarification
   - Schedule discussion
   - Get technical guidance

### Resources Available

- ✅ Complete codebase
- ✅ Comprehensive documentation
- ✅ Test suite & coverage reports
- ✅ GitHub issues & discussions
- ✅ Development team (for questions)
- ✅ Architecture diagrams
- ✅ Design patterns

---

## ✨ Quality Standards

### Analysis Quality Checklist

**Requirements Documents Must Have:**
- [ ] Clear, specific title
- [ ] Detailed description
- [ ] User story with "As a... I want... So that..."
- [ ] 5-10 specific, testable acceptance criteria
- [ ] Technical requirements & impacts
- [ ] Effort estimation
- [ ] Risk assessment
- [ ] Success metrics
- [ ] Testing strategy
- [ ] Validation plan

**GitHub Issues Must Have:**
- [ ] Clear, concise title
- [ ] Complete description
- [ ] Acceptance criteria
- [ ] Proper labels
- [ ] Linked related issues
- [ ] Priority assigned
- [ ] Ready for development

**Status Reports Must Include:**
- [ ] Summary of progress
- [ ] Completed items
- [ ] Current work
- [ ] Blockers
- [ ] Metrics
- [ ] Risks
- [ ] Next steps

---

## 🏆 Success Metrics for Analyst

**Quality Metrics:**
- ✅ Requirements clarity: 0 developer questions (goal)
- ✅ Acceptance criteria: 100% testable
- ✅ Documentation completeness: 100%
- ✅ Issue creation: All required fields
- ✅ Stakeholder satisfaction: High (target)

**Efficiency Metrics:**
- ✅ Analysis turnaround: 1-3 days
- ✅ Requirement-to-development time: < 1 week
- ✅ Issue clarity: 0 clarifications needed (goal)

**Communication Metrics:**
- ✅ Response time to questions: < 4 hours
- ✅ Status report timeliness: Weekly
- ✅ Stakeholder updates: Regular

---

## 📝 Analysis Templates Summary

### Quick Links to Templates

1. **Feature Analysis Template** → § Analyzing New Features
2. **Bug Analysis Template** → § Analyzing Bugs & Issues
3. **Requirement Document Template** → § Create Requirements
4. **GitHub Issue Template** → § Create Issue Documentation
5. **Report Template** → § Reporting Template
6. **Communication Templates** → § Stakeholder Communication

---

**Last Updated**: 2026-02-16
**Version**: 1.0
**Status**: Ready for Use

---

## 🎓 Next Steps

1. ✅ Read this guide completely
2. ✅ Study SYSTEM_PROMPT.md
3. ✅ Review ARCHITECTURE.md
4. ✅ Look at current GitHub issues
5. ✅ Ask Igor any clarifying questions
6. ✅ Start your first analysis task
7. ✅ Get feedback on your analysis
8. ✅ Refine your process based on feedback

**Welcome to the analysis team! 🎉**

For questions or clarifications, reach out to Igor.
