#!/usr/bin/env bash
set -euo pipefail

# Path to the release APK produced by Gradle
APK_PATH="$(pwd)/app/build/outputs/apk/release/app-release-unsigned.apk"

if [ ! -f "$APK_PATH" ]; then
  echo "Release APK not found at $APK_PATH"
  exit 1
fi

echo "Installing release APK..."
adb install -r "$APK_PATH"

echo "Launching app..."
adb shell am start -n com.example.fitness_plan/.MainActivity

echo "SMOKE TEST: Admin login should be available. Use admin/admin123 to login."
echo "After login, verify admin main screen is visible and basic admin actions work."
echo "Logs: adb logcat | grep -i FitnessPlan or search for AdminUseCase calls"

echo "Smoke test script completed."
