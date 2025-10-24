import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

val gitLatestCommit: String = ByteArrayOutputStream().use { outputStream ->
    project.exec {
        executable("git")
        args("log", "--oneline", "-1", "--format=format:%h", ".")
        standardOutput = outputStream
    }
    outputStream.toString()
}

android {
    namespace = "org.bxkr.octodiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.bxkr.octodiary"
        minSdk = 26
        targetSdk = 35
        versionCode = 32
        versionName = "2.1.6"
        archivesName = gitLatestCommit

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Конфигурация KSP для Room (не работает)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

// Явно устанавливаем JVM target для всех Kotlin задач
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.browser)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.glide)
    implementation(libs.converter.scalars)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.compose)
    implementation(libs.zoomable)
    implementation(libs.dotsindicator)

    // Room Database (без compiler - не работает с текущей конфигурацией)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // ONNX Runtime (для локальных AI моделей)
    implementation(libs.onnxruntime)
    
    // WorkManager (фоновые задачи)
    implementation(libs.work.runtime.ktx)
    
    // Gson (для JSON)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Markdown рендеринг для AI ответов (исключаем конфликтующие зависимости)
    implementation("io.noties.markwon:core:4.6.2") {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    implementation("io.noties.markwon:syntax-highlight:4.6.2") {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    
    // OkHttp (для AI API)
    implementation(libs.okhttp)
    
    // ML Kit for text recognition (OCR)
    // TODO: Add llama.cpp for local GGUF model inference
    // implementation("com.github.kherud:java-llama.cpp:3.0.0") // или другая версия
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Android-compatible PDF library
    implementation("com.itextpdf:itext7-core:8.0.2")
    
    // ZXing (для QR кодов)
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}