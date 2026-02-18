#!/bin/bash

# Скрипт сборки APK с автоматическим именованием по версии
# Использование: ./build_apk.sh

PROJECT_DIR="/Users/igor/AndroidStudioProjects/fitness_plan"
DESKTOP_DIR="$HOME/Desktop"

cd "$PROJECT_DIR" || exit 1

echo "🔨 Сборка release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    # Получить версию из build.gradle.kts
    VERSION=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\([^"]*\)".*/\1/')
    VERSION_CODE=$(grep "versionCode" app/build.gradle.kts | head -1 | sed 's/.*= *\([0-9]*\).*/\1/')
    
    APK_NAME="fitness_plan_$VERSION.apk"
    
    cp app/build/outputs/apk/release/app-release-unsigned.apk "$DESKTOP_DIR/$APK_NAME"
    
    echo ""
    echo "✅ APK успешно создан!"
    echo "📁 Файл: $DESKTOP_DIR/$APK_NAME"
    echo "🔢 Версия: $VERSION (code: $VERSION_CODE)"
    echo "📦 Размер: $(du -h "$DESKTOP_DIR/$APK_NAME" | cut -f1)"
else
    echo "❌ Ошибка сборки APK"
    exit 1
fi
