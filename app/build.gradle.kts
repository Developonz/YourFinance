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
    // Подключаем модули presentation и data
    // Domain подключится транзитивно через data и presentation
    implementation(project(":presentation"))
    implementation(project(":data")) // Нужно для Hilt, чтобы найти реализации из data

    // Hilt (для Application класса и DI графа)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // KSP для Hilt

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Основные AndroidX библиотеки (многие придут транзитивно, но можно указать явно)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Часто нужен для тем и базовых стилей

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    // Удалены зависимости, которые теперь находятся в presentation или data:
    // libs.room.*, libs.circleimageview, libs.recyclerview, libs.androidx.preference,
    // libs.androidx.lifecycle.*, libs.androidx.navigation.*, libs.androidx.fragment.ktx,
    // libs.androidx.constraintlayout
    // libs.androidx.legacy.support.v4 - удален как устаревший
}