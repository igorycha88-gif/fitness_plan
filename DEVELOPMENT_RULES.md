# Правила разработки Fitness Plan

## 1. Версионирование приложения

### 1.1. Формат версии
- **versionName**: `MAJOR.MINOR` (например: `2.3`)
- **versionCode**: Целое число, увеличивается на 1 при каждом релизе

### 1.2. Когда увеличивать версию
Версия **ОБЯЗАТЕЛЬНО** увеличивается при:
- Любом изменении функционала
- Исправлении багов
- Добавлении новых экранов или компонентов
- Изменении API или структуры данных
- Обновлении зависимостей

### 1.3. Как увеличить версию
1. Открыть файл `app/build.gradle.kts`
2. Найти блок `defaultConfig`:
```kotlin
versionCode = 5      // Увеличить на 1
versionName = "2.3"  // Увеличить MINOR или MAJOR
```

### 1.4. Правила именования APK файлов
Формат: `fitness_plan_VERSION.apk`

Примеры:
- `fitness_plan_2.3.apk`
- `fitness_plan_2.4.apk`
- `fitness_plan_3.0.apk`

---

## 2. Сборка APK

### 2.1. Сборка release APK
```bash
cd /Users/igor/AndroidStudioProjects/fitness_plan
./gradlew assembleRelease
```

### 2.2. Копирование на рабочий стол с версией
```bash
# Получить версию из build.gradle.kts
VERSION=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\([^"]*\)".*/\1/')

# Скопировать APK
cp app/build/outputs/apk/release/app-release-unsigned.apk ~/Desktop/fitness_plan_$VERSION.apk
```

### 2.3. Быстрый скрипт сборки
```bash
# Полный цикл: обновить версию, собрать, скопировать
cd /Users/igor/AndroidStudioProjects/fitness_plan
./gradlew assembleRelease && \
VERSION=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\([^"]*\)".*/\1/') && \
cp app/build/outputs/apk/release/app-release-unsigned.apk ~/Desktop/fitness_plan_$VERSION.apk && \
echo "APK создан: ~/Desktop/fitness_plan_$VERSION.apk"
```

---

## 3. Текущая версия

- **versionCode**: 5
- **versionName**: 2.3
- **Дата сборки**: 2026-02-17

---

## 4. История версий

| Версия | Code | Дата | Описание |
|--------|------|------|----------|
| 2.3 | 5 | 2026-02-17 | Исправление подключения Health Connect на Samsung устройствах |
| 2.2 | 4 | 2026-02-17 | Добавлены инструкции для Health Connect |
| 2.1 | 3 | 2026-02-16 | Интеграция Health Connect для смарт-часов |
