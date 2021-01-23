import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.halilibo"
version = "1.0"

android {
    compileSdkVersion(AndroidSdk.compile)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }

    defaultConfig {
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    android()
    ios {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}") {
                    isForce = true
                }

                // Ktor
                implementation(Ktor.clientCore)
                implementation(Ktor.clientJson)
                implementation(Ktor.clientLogging)
                implementation(Ktor.clientSerialization)

                // Kotlinx Serialization
                implementation(Serialization.core)

                // SQL Delight
                implementation(SqlDelight.runtime)
                implementation(SqlDelight.coroutineExtensions)

                implementation("com.russhwolf:multiplatform-settings:0.7")
                implementation("com.russhwolf:multiplatform-settings-no-arg:0.7")
                implementation("com.russhwolf:multiplatform-settings-coroutines-native-mt:0.7")

                // koin
                api(Koin.core)

                // kermit
                api(Deps.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Ktor.clientAndroid)
                implementation(SqlDelight.androidDriver)
                implementation("com.google.android.material:material:1.2.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(Test.junit)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(Ktor.clientIos)
                implementation(SqlDelight.nativeDriver)
            }
        }
        val iosTest by getting
    }
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphoneos"
    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}

tasks.getByName("build").dependsOn(packForXcode)

sqldelight {
    database("OnCallPharmacyDatabase") {
        packageName = "com.halilibo.eczane.db"
        sourceFolders = listOf("sqldelight")
    }
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
