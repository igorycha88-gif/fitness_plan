// build.gradle.kts (корневой)

// Добавляем переменную для управления версией компилятора Compose
// Версия 1.5.4 совместима с Kotlin 1.9.20
extra["composeCompilerVersion"] = "1.5.4"

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
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
