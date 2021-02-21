import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

dependencies {
    implementation(AndroidX.lifecycleExtensions)
    implementation(AndroidX.coreKtx)

    implementation(Compose.ui)
    implementation(Compose.uiGraphics)
    implementation(Compose.uiTooling)
    implementation(Compose.foundationLayout)
    implementation(Compose.material)
    implementation(Compose.icons)
    implementation(Compose.iconsExtended)
    implementation(Compose.activity)
    implementation(Compose.viewModel)

    implementation(Accompanist.coil)
    implementation(Accompanist.insets)

    implementation(Koin.android)
    implementation(Koin.androidViewModel)

    implementation(PlayServices.location)
    implementation(PlayServices.mapsKtx)
    implementation(PlayServices.mapsUtilKtx)
    implementation(PlayServices.mapsPlayServices)
    implementation(PlayServices.mapsPlayServicesUtils)

    implementation(Deps.permissionDispatcher)

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test:runner:1.3.0")

    implementation(project(":shared"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf(
            "-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

android {
    compileSdkVersion(AndroidSdk.compile)
    defaultConfig {
        applicationId = "com.halilibo.eczane"
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)

        val properties = Properties()
        if (rootProject.file("local.properties").exists()) {
            properties.load(rootProject.file("local.properties").inputStream())
        }

        // Inject the Maps API key into the manifest
        setManifestPlaceholders(
            mutableMapOf(
                "mapsApiKey" to properties.getProperty("MAPS_API_KEY", "")
            )
        )

        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lintOptions {
        isAbortOnError = false
    }
}