# Параметры тела - Документация функционала

## 📋 Обзор

Функционал отслеживания параметров тела позволяет пользователям записывать свои физические измерения, просматривать историю изменений и отслеживать прогресс.

### Основные возможности:
- ✅ Запись измерений тела (вес, рост, окружности)
- ✅ Автоматический расчёт вычисляемых параметров (ИМТ, жир, мышцы)
- ✅ Хранение истории в DataStore
- ✅ Опциональные поля - можно заполнить минимум 1 параметр
- ✅ Валидация всех параметров по диапазонам

---

## 🏗️ Архитектура

### Структура пакетов

```
com.example.fitness_plan/
├── domain/
│   ├── model/
│   │   ├── BodyParameter.kt              # Модель параметра
│   │   ├── BodyParameterType.kt          # Типы параметров (enum)
│   │   ├── MeasurementInput.kt           # Входные данные
│   │   └── CalculationMethod.kt          # Способ расчёта (AUTO/MANUAL)
│   ├── repository/
│   │   └── BodyParametersRepository.kt   # Интерфейс репозитория
│   └── usecase/
│       ├── BodyParameterCalculator.kt      # Калькулятор формул
│       ├── MeasurementValidator.kt         # Валидатор
│       └── BodyParametersUseCase.kt       # Use Case (координация)
├── data/
│   └── BodyParametersRepository.kt        # Реализация (DataStore)
├── presentation/
│   └── viewmodel/
│       └── BodyParametersViewModel.kt      # ViewModel
└── ui/
    ├── BodyParametersSection.kt            # Секция в профиле
    ├── AddMeasurementDialog.kt            # Диалог добавления
    ├── CurrentParametersCard.kt           # Карточка параметров
    └── MeasurementInputField.kt          # Поле ввода
```

---

## 📊 Типы параметров

### Основные параметры (необязательные)

| Тип | Единица | Минимум | Максимум | Описание |
|-----|---------|----------|-----------|-----------|
| WEIGHT | кг | 20 | 300 | Вес |
| HEIGHT | см | 50 | 250 | Рост |
| CHEST | см | 10 | 200 | Обхват груди |
| WAIST | см | 10 | 200 | Обхват талии |
| HIPS | см | 10 | 200 | Обхват бёдер |
| BICEPS | см | 10 | 200 | Бицепс |
| THIGH | см | 10 | 200 | Бедро |
| CALF | см | 10 | 200 | Икра |
| NECK | см | 10 | 200 | Шея |
| SHOULDERS | см | 10 | 200 | Плечи |

### Вычисляемые параметры (AUTO)

| Тип | Единица | Минимум | Максимум | Формула |
|-----|---------|----------|-----------|---------|
| BODY_FAT | % | 1 | 60 | US Navy Formula |
| BODY_MASS_INDEX | - | 10 | 60 | BMI = вес / рост² |
| MUSCLE_MASS | кг | 10 | 150 | ~55% сухой массы |

---

## 🔬 Формулы расчёта

### ИМТ (BMI)

```kotlin
fun calculateBMI(weightKg: Double, heightCm: Double): Double {
    val heightM = heightCm / 100.0
    return weightKg / (heightM * heightM)
}
```

Интерпретация:
- `< 18.5` - Недостаточный вес
- `18.5 - 24.9` - Норма
- `25 - 29.9` - Избыточный вес
- `≥ 30` - Ожирение

### Жир в организме (US Navy Formula)

**Для мужчин:**
```kotlin
val diff = waistCm - neckCm
val value = 1.0324 - (0.19077 * log10(diff)) + (0.15456 * log10(heightCm))
val bodyFat = (495 / value) - 450
```

**Для женщин:**
```kotlin
val sum = waistCm + hipsCm - neckCm
val value = 1.29579 - (0.35004 * log10(sum)) + (0.22100 * log10(heightCm))
val bodyFat = (495 / value) - 450
```

Точность: ±3-5% при правильных измерениях

### Мышечная масса

```kotlin
fun calculateMuscleMass(weightKg: Double, bodyFatPercent: Double): Double {
    val leanBodyMass = weightKg * (1 - bodyFatPercent / 100)
    return leanBodyMass * 0.55  // ~55% сухой массы
}
```

---

## 📦 Модели данных

### BodyParameter

```kotlin
data class BodyParameter(
    val parameterType: BodyParameterType,
    val value: Double,
    val unit: String,
    val date: Long,
    val calculationMethod: CalculationMethod = CalculationMethod.MANUAL,
    val measurementId: String = UUID.randomUUID().toString()
)
```

### MeasurementInput

```kotlin
data class MeasurementInput(
    val date: Long = System.currentTimeMillis(),
    val parameters: Map<BodyParameterType, Double?> = emptyMap()
) {
    fun hasAnyData(): Boolean = parameters.values.any { it != null }
    fun getFilledParameters(): Map<BodyParameterType, Double>
    fun isParameterFilled(type: BodyParameterType): Boolean
}
```

---

## 💾 Хранение данных

### DataStore
- **Файл**: `body_parameters_{username}.preferences_pb`
- **Формат**: JSON (Gson)
- **Структура**: `List<BodyParameter>`

### DataStore Key
```kotlin
private fun getParametersKey(username: String): Preferences.Key<String> {
    return stringPreferencesKey("${username}_body_parameters")
}
```

---

## 🧪 Тестирование

### Unit тесты

**BodyParameterCalculatorTest** (19 тестов):
- ✅ Расчёт ИМТ (норма, недостаточный/избыточный вес, ожирение)
- ✅ Расчёт жира (мужчины, женщины)
- ✅ Расчёт мышечной массы
- ✅ Вычисление всех параметров с разными данными

**MeasurementValidatorTest** (17 тестов):
- ✅ Валидация корректных данных
- ✅ Проверка минимальных требований (минимум 1 параметр)
- ✅ Валидация диапазонов для всех типов
- ✅ Проверка граничных значений

### Запуск тестов

```bash
# Все unit тесты
./gradlew test

# Только калькулятор
./gradlew test --tests "*BodyParameterCalculatorTest"

# Только валидатор
./gradlew test --tests "*MeasurementValidatorTest"
```

---

## 🎯 Правила бизнес-логики

### П1: Все параметры опциональны
- Пользователь может заполнить любое подмножество полей
- Обязательное условие: минимум 1 параметр для сохранения

### П2: Минимум 1 параметр для сохранения
```kotlin
if (!input.hasAnyData()) {
    return Result.failure(Exception("Минимум 1 параметр должен быть заполнен"))
}
```

### П3: Статистика только по заполненным параметрам
- Графики и таблицы показывают только параметры с данными
- Пустые значения отображаются как "--"

### П4: Максимум 5 параметров на графике
- Реализовано в Этапе 2 (Статистика)

### П5: Авто-расчёт вычисляемых параметров
```kotlin
if (calculateAuto && gender != null) {
    val calculatedParams = calculator.calculateCalculatedParameters(
        input = validationResult.validatedData,
        gender = gender,
        date = input.date
    )
    parametersToSave.addAll(calculatedParams)
}
```

### П6: Валидация диапазонов
- Все параметры проверяются на соответствие диапазонам
- При ошибке возвращается конкретное сообщение

### П7: Пустые ячейки в таблице = "--"
- Реализовано в Этапе 2 (Статистика)

---

## 🚀 Использование

### Добавление измерения в UI

```kotlin
val input = MeasurementInput(
    date = System.currentTimeMillis(),
    parameters = mapOf(
        BodyParameterType.WEIGHT to 85.0,
        BodyParameterType.HEIGHT to 178.0,
        BodyParameterType.WAIST to 90.0
    )
)
viewModel.saveMeasurement(input, calculateAuto = true)
```

### Получение текущих параметров

```kotlin
val latestMeasurements by viewModel.latestMeasurements.collectAsState()
// latestMeasurements: Map<BodyParameterType, BodyParameter>
```

### Расчёт параметров программно

```kotlin
val bmi = calculator.calculateBMI(weightKg = 85.0, heightCm = 178.0)
val bodyFat = calculator.calculateBodyFatUSNavy(
    gender = "Мужской",
    heightCm = 178.0,
    neckCm = 38.0,
    waistCm = 90.0
)
```

### Валидация ввода

```kotlin
val result = validator.validate(parametersMap)
when (result) {
    is MeasurementValidator.ValidationResult.Success -> {
        // Сохранение данных
    }
    is MeasurementValidator.ValidationResult.Error -> {
        // Показать ошибку: result.errorMessage
    }
}
```

---

## 📝 Примечания к разработке

### Точность формул
- **ИМТ**: точный расчёт
- **Жир (US Navy)**: ±3-5% точности при правильных измерениях
- **Мышечная масса**: приблизительный расчёт (~55% сухой массы)

### Цветовая палитра
- Primary: #2DD4BF (Turquoise)
- Secondary: #E07A5F (Terracotta)
- Tertiary: #6B4C9A (Violet)

### Material Design 3
- OutlinedTextField с primary border
- Button с primary-to-tertiary gradient
- Card с RoundedCornerShape(16.dp)

---

## 🔮 Планируемые улучшения (Этап 2)

- [ ] Multi-linear chart для параметров
- [ ] Таблица истории измерений
- [ ] Фильтры по времени и типам
- [ ] Экспорт данных (CSV/JSON)
- [ ] Редактирование/удаление измерений
- [ ] Сравнение с предыдущими измерениями
