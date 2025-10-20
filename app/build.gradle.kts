// app/build.gradle.kts
plugins {
    // Основной плагин приложения
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // KSP нужен для Hilt в app модуле
    alias(libs.plugins.hilt) // Hilt должен быть и здесь
    // SafeArgs не нужен здесь, если он в presentation
    // alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.yourfinance" // Namespace основного приложения
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.yourfinance"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Можно добавить векторную графику, если используется
        // vectorDrawables {
        //     useSupportLibrary = true
        // }
        ndk {
            // Исправлено: Так как прямой метод set() не разрешен,
            // мы используем стандартный подход для MutableSet:
            // очищаем существующие фильтры и добавляем новый список.
            abiFilters.clear()
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Включайте true для релизных сборок!
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // applicationIdSuffix = ".debug" // Полезно для установки debug/release одновременно
            // isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // ViewBinding может быть не нужен здесь, если вся UI в presentation
        // viewBinding = true
        // compose = true // Если используете Compose в app модуле
    }
    // packagingOptions { // Пример для Compose, если используется
    //     resources {
    //         excludes += "/META-INF/{AL2.0,LGPL2.1}"
    //     }
    // }
}

dependencies {
    // Подключаем модули
    implementation(project(":presentation"))
    implementation(project(":data")) // Нужно для Hilt, чтобы найти реализации из data
    implementation(project(":domain"))

    // Hilt (для Application класса и DI графа)
    implementation(libs.hilt.android)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    ksp(libs.hilt.compiler) // KSP для Hilt

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Основные AndroidX библиотеки (многие придут транзитивно, но можно указать явно)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Часто нужен для тем и базовых стилей



    // Unit тесты
    testImplementation(libs.junit)

    // Интеграционные тесты (используем версии из каталога)
    androidTestImplementation(libs.androidx.test.junit) // 1.2.1 из каталога
    androidTestImplementation(libs.androidx.test.espresso.core) // 3.6.1 из каталога

    // Hilt для тестов
    androidTestImplementation(libs.hilt.android.testing)

    // Room тестирование - нужно добавить в каталог версий
    androidTestImplementation(libs.room.testing)

    // Architecture Components тестирование
    androidTestImplementation(libs.androidx.arch.core.testing)

    // Coroutines тестирование - используем версию из каталога
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Test Runner и Rules
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)

    // Дополнительные полезные тестовые зависимости
    androidTestImplementation(libs.androidx.test.truth)  // Для более читаемых assertions
    androidTestImplementation(libs.mockito.android) // Mockito для Android тестов




    implementation(libs.gson)
}