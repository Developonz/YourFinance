//// domain/build.gradle.kts
//plugins {
//    // Используем плагин Kotlin JVM, т.к. здесь нет Android зависимостей
//    alias(libs.plugins.kotlin.jvm)
//}
//
//java { // Настройки Java, если нужны специфичные вещи, обычно для Kotlin JVM не требуется
//    sourceCompatibility = JavaVersion.VERSION_11
//    targetCompatibility = JavaVersion.VERSION_11
//}
//
//dependencies {
//    // Kotlin Standard Library (обычно добавляется автоматически плагином)
//    // implementation(kotlin("stdlib")) // Раскомментируйте, если нужно явно
//
//    // Coroutines Core (без Android специфики)
//    implementation(libs.kotlinx.coroutines.core)
//
//    // Зависимость для аннотации @Inject (Hilt ее понимает)
//    implementation(libs.javax.inject)
//
//        implementation(libs.androidx.lifecycle.livedata.ktx)
//
//    // Здесь могут быть другие чистые Kotlin/Java библиотеки, если нужны
//    // Пример: kotlinx.serialization, Arrow KT, etc.
//}

// domain/build.gradle.kts

plugins {
    // 1. Применяем плагин Android Library вместо kotlin.jvm
    alias(libs.plugins.android.library)
    // 2. Применяем плагин Kotlin Android вместо kotlin.jvm
    alias(libs.plugins.kotlin.android)
    // НЕ применяйте здесь hilt или ksp, если они не нужны для самого domain слоя
}

// 3. Теперь блок android { ... } будет распознан Gradle
android {
    // Namespace ОБЯЗАТЕЛЕН для Android библиотек
    namespace = "com.example.yourfinance.domain" // Придумайте уникальный namespace

    // Укажите compileSdk (должен совпадать с другими модулями)
    compileSdk = 34 // Используйте версию из вашего libs.versions.toml

    defaultConfig {
        // Укажите minSdk (обычно совпадает с другими модулями)
        minSdk = 26
        // testInstrumentationRunner здесь обычно не нужен
        // consumerProguardFiles("consumer-rules.pro") // Если нужно для потребителей модуля
    }

    // compileOptions и kotlinOptions нужны для совместимости версий JVM
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // buildTypes { ... } // Обычно не нужны специфичные для domain
    // buildFeatures { ... } // Обычно не нужны специфичные для domain (никаких viewBinding и т.д.)
}

dependencies {
    // Теперь сюда можно добавлять Android-зависимости
    implementation(libs.androidx.lifecycle.livedata.ktx) // Ваша LiveData

    // Остальные зависимости domain слоя
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject) // Для @Inject

    // НЕ добавляйте сюда UI-библиотеки, Room runtime и т.д.
}