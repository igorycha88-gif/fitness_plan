// build.gradle.kts (корневой)

// Добавляем переменную для управления версией компилятора Compose
// Версия 1.4.3 совместима с BOM 2023.08.00
extra["composeCompilerVersion"] = "1.4.3"

// Версии Hilt для тестов
extra["hiltVersion"] = "2.51"
extra["hiltTestingVersion"] = "1.2.0"

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

// Вручную добавляем Hilt plugin
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
