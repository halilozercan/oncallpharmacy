import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    failFast = true
    buildUponDefaultConfig = true

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        txt.enabled =
            true // similar to the console output, contains issue signature to manually edit baseline files
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = "1.8"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")

    implementation(AndroidX.core)

    implementation(Compose.ui)
    implementation(Compose.uiGraphics)
    implementation(Compose.uiTooling)
    implementation(Compose.foundationLayout)
    implementation(Compose.material)
    implementation(Compose.runtimeLiveData)
    implementation(Compose.navigation)
    implementation(Compose.icons)
    implementation(Compose.iconsExtended)

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

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test:runner:1.2.0")

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
        kotlinCompilerVersion = "1.4.21"
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
}