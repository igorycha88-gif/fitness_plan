#!/bin/bash

# Скрипт для увеличения версии приложения
# Использование: 
#   ./bump_version.sh patch  - увеличить MINOR версию (2.3 -> 2.4)
#   ./bump_version.sh major  - увеличить MAJOR версию (2.3 -> 3.0)

PROJECT_DIR="/Users/igor/AndroidStudioProjects/fitness_plan"
BUILD_GRADLE="$PROJECT_DIR/app/build.gradle.kts"

if [ -z "$1" ]; then
    echo "Использование: ./bump_version.sh [patch|major]"
    echo "  patch - увеличить MINOR версию (2.3 -> 2.4)"
    echo "  major - увеличить MAJOR версию (2.3 -> 3.0)"
    exit 1
fi

cd "$PROJECT_DIR" || exit 1

# Получить текущую версию
CURRENT_VERSION=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\([^"]*\)".*/\1/')
CURRENT_CODE=$(grep "versionCode" app/build.gradle.kts | head -1 | sed 's/.*= *\([0-9]*\).*/\1/')

# Разбить версию на MAJOR и MINOR
MAJOR=$(echo "$CURRENT_VERSION" | cut -d. -f1)
MINOR=$(echo "$CURRENT_VERSION" | cut -d. -f2)

if [ "$1" = "major" ]; then
    NEW_MAJOR=$((MAJOR + 1))
    NEW_MINOR=0
elif [ "$1" = "patch" ]; then
    NEW_MAJOR=$MAJOR
    NEW_MINOR=$((MINOR + 1))
else
    echo "Неизвестный тип: $1. Используйте patch или major"
    exit 1
fi

NEW_VERSION="$NEW_MAJOR.$NEW_MINOR"
NEW_CODE=$((CURRENT_CODE + 1))

echo "Текущая версия: $CURRENT_VERSION (code: $CURRENT_CODE)"
echo "Новая версия: $NEW_VERSION (code: $NEW_CODE)"

# Обновить versionCode
sed -i '' "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"

# Обновить versionName
sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" "$BUILD_GRADLE"

echo "✅ Версия обновлена в $BUILD_GRADLE"

# Обновить историю версий в DEVELOPMENT_RULES.md
RULES_FILE="$PROJECT_DIR/DEVELOPMENT_RULES.md"
TODAY=$(date +%Y-%m-%d)

# Проверим, существует ли файл
if [ -f "$RULES_FILE" ]; then
    # Обновить текущую версию в файле правил
    sed -i '' "s/- \*\*versionCode\*\*: $CURRENT_CODE/- **versionCode**: $NEW_CODE/" "$RULES_FILE"
    sed -i '' "s/- \*\*versionName\*\*: $CURRENT_VERSION/- **versionName**: $NEW_VERSION/" "$RULES_FILE"
    sed -i '' "s/- \*\*Дата сборки\*\*: .*/- **Дата сборки**: $TODAY/" "$RULES_FILE"
    echo "📝 Обновлён файл DEVELOPMENT_RULES.md"
fi

echo ""
echo "Готово! Теперь можно собрать APK:"
echo "  ./build_apk.sh"
