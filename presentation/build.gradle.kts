// presentation/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs) // Плагин Safe Args здесь
}

android {
    namespace = "com.example.yourfinance.presentation" // Уникальный namespace
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        viewBinding = true // Включаем ViewBinding для UI
        // compose = true // Раскомментируйте, если используете Jetpack Compose
    }
}

dependencies {
    // Зависимость от domain модуля (для UseCases и моделей)
    implementation(project(":domain"))

    // Hilt (для ViewModel Injection и др.)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // AndroidX UI & Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx) // Для работы с фрагментами

    // Lifecycle (ViewModel, LiveData, LifecycleScope)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Для lifecycleScope


    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines (для UI-потока и ViewModelScope)
    implementation(libs.kotlinx.coroutines.android)

    // Preferences (если UI работает с настройками напрямую, хотя лучше через ViewModel)
    implementation(libs.androidx.preference.ktx)

    // Другие UI библиотеки
    implementation(libs.circleimageview)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}