// data/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.yourfinance.data" // Уникальный namespace для модуля
    compileSdk = 34 // Используем из toml неявно или указываем явно

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro") // Правила Proguard для потребителей этого модуля
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Или true, если нужна минификация для этого модуля
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Настройки для debug сборки, если нужны
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Зависимость от domain модуля (для интерфейсов репозиториев и моделей)
    implementation(project(":domain"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // Используем KSP для Room

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Используем KSP для Hilt

    // Coroutines
    implementation(libs.kotlinx.coroutines.core) // Уже может быть транзитивно из domain, но лучше указать явно
    implementation(libs.kotlinx.coroutines.android) // Если нужны Android-специфичные корутины (напр. Dispatchers.Main)

    // AndroidX Core (может понадобиться для Context или других утилит)
    implementation(libs.androidx.core.ktx)

    // Preferences DataStore или SharedPreferences (если работа с настройками идет здесь)
    implementation(libs.androidx.preference.ktx) // Если используется SharedPreferences через AndroidX Preference


    implementation(libs.gson)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}