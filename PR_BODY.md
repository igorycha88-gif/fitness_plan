## Описание изменений

Исправлен критический баг `NullPointerException` при загрузке `WorkoutPlan` после авторизации пользователя.

### Проблема
Приложение падало после авторизации пользователя с ошибкой:
```
java.lang.NullPointerException: Parameter specified as non-null is null: method com.example.fitness_plan.domain.model.WorkoutPlan.copy, parameter planType
```

Причина: поле `planType` не указывалось явно при создании объектов `WorkoutPlan` в различных частях приложения, что приводило к `null` значению при десериализации из DataStore через Gson.

### Решение

1. **Добавлено явное указание `planType` во всех местах создания планов:**

   **WorkoutRepositoryImpl.kt:**
   - `createWeightLossPlanBySplit()` - `planType = PlanType.AUTO`
   - `createWeightLossBeginnerPlan()` - `planType = PlanType.AUTO`
   - `createWeightLossIntermediatePlan()` - `planType = PlanType.AUTO`
   - `createMuscleGainBeginnerPlan()` - `planType = PlanType.AUTO`
   - `createMuscleGainIntermediatePlan()` - `planType = PlanType.AUTO`
   - `createMuscleGainAdvancedPlan()` - `planType = PlanType.AUTO`
   - `createMaintenancePlan()` - `planType = PlanType.AUTO`

   **WorkoutViewModel.kt:**
   - `createAdminPlan()` - `planType = PlanType.ADMIN`

   **WorkoutUseCase.kt:**
   - `createUserPlan()` - `planType = PlanType.USER`

2. **Добавлена безопасная десериализация для обработки старых данных:**

   **WorkoutRepositoryImpl.kt:**
   - `getWorkoutPlan()` - возвращает `PlanType.AUTO` если `planType == null`
   - `getAdminWorkoutPlan()` - возвращает `PlanType.ADMIN` если `planType == null`
   - `getUserWorkoutPlan()` - возвращает `PlanType.USER` если `planType == null`

3. **Добавлены юнит тесты для валидации исправлений:**
   - `WorkoutRepositoryPlanTypeTest.kt` - тесты для репозитория
   - `WorkoutUseCasePlanTypeTest.kt` - тесты для use case
   - `WorkoutViewModelPlanTypeTest.kt` - тесты для view model

### Файлы изменены

**Изменено:**
- `app/src/main/java/com/example/fitness_plan/data/WorkoutRepositoryImpl.kt`
- `app/src/main/java/com/example/fitness_plan/presentation/viewmodel/WorkoutViewModel.kt`
- `app/src/main/java/com/example/fitness_plan/domain/usecase/WorkoutUseCase.kt`

**Добавлено (тесты):**
- `app/src/test/java/com/example/fitness_plan/data/WorkoutRepositoryPlanTypeTest.kt`
- `app/src/test/java/com/example/fitness_plan/domain/usecase/WorkoutUseCasePlanTypeTest.kt`
- `app/src/test/java/com/example/fitness_plan/presentation/viewmodel/WorkoutViewModelPlanTypeTest.kt`

**Обновлено (документация):**
- `TESTING.md` - добавлена информация о новых тестах planType

### Тестирование

✅ Все unit тесты успешно пройдены (8 тестов для planType)
✅ Проект успешно собирается (`BUILD SUCCESSFUL`)
✅ Все существующие тесты продолжают работать

### Обратная совместимость

- ✅ Существующие пользователи с сохраненными планами без `planType` могут продолжать пользоваться приложением
- ✅ Старые планы автоматически обрабатываются и заменяются на дефолтные значения
- ✅ Безопасная десериализация предотвращает краши для существующих пользователей

### Проверка

Для проверки исправления:
1. Создайте нового пользователя и авторизуйтесь
2. Убедитесь, что план загружается без краша
3. Для существующего пользователя со старыми данными - убедитесь, что план загружается корректно

### Связанные требования

Закрывает требования из `требования/bug_fix_nullPointerException_workoutplan_planType.md`
