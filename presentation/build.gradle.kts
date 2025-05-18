plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.yourfinance.presentation"
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
        viewBinding = true
        // compose = true
    }

    // К каталогу генерируемых исходников
    val genDir = layout.buildDirectory.dir("generated/iconmap")
    sourceSets["main"].kotlin.srcDir(genDir)
}

/** Таск генерации IconMap.kt */
val generateIconMap by tasks.registering {
    val resDir = file("src/main/res/drawable")
    val outDir = layout.buildDirectory
        .dir("generated/iconmap/com/example/yourfinance/presentation")
        .get().asFile

    inputs.dir(resDir)
    outputs.dir(outDir)

    doLast {
        outDir.mkdirs()

        val pkg = "com.example.yourfinance.presentation"
        val className = "IconMap"
        val kt = File(outDir, "$className.kt")

        kt.writeText("""
            |package $pkg
            |
            |/**
            | * Сгенерировано автоматически.
            | * Мапа строковых имён drawable → R.drawable.id
            | */
            |object $className {
            |
            |    private val map: Map<String, Int> = mapOf(
        """.trimMargin())

        resDir.listFiles { f ->
            f.isFile && f.extension.lowercase() in setOf("xml","png","jpg","jpeg","webp")
        }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?.forEach { name ->
                kt.appendText("""        "$name" to R.drawable.$name,""" + "\n")
            }

        kt.appendText("""
            |    )
            |
            |    /** Получить ResId по имени, или дефолтный */
            |    fun idOf(name: String, default: Int = R.drawable.ic_checkmark): Int =
            |        map[name] ?: default
            |
            |    /** Получить имя по ResId, или null */
            |    fun nameOf(id: Int): String? =
            |        map.entries.firstOrNull { it.value == id }?.key
            |
            |    /** Оператор доступа: IconMap["foo"] */
            |    operator fun get(name: String): Int = idOf(name)
            |
            |    /** Все доступные строковые ключи */
            |    val names: List<String> get() = map.keys.toList()
            |}
            |""".trimMargin())
    }
}

// Генерим до компиляции
afterEvaluate {
    tasks.named("preBuild") {
        dependsOn(generateIconMap)
    }
}

dependencies {
    implementation(project(":domain"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // AndroidX UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Preferences
    implementation(libs.androidx.preference.ktx)

    // Прочие
    implementation(libs.circleimageview)

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
