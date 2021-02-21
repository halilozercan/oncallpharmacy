object Versions {
    const val kotlin = "1.4.30"
    const val kotlinCoroutines = "1.4.2-native-mt"
    const val ktor = "1.5.0"
    const val kotlinxSerialization = "1.0.1"
    const val koin = "3.0.0-alpha-4"
    const val sqlDelight = "1.4.2"
    const val kermit = "0.1.8"
    const val detekt = "1.15.0"

    const val sqliteJdbcDriver = "3.30.1"
    const val slf4j = "1.7.30"
    const val compose = "1.0.0-alpha12"
    const val nav_compose = "1.0.0-alpha07"
    const val accompanist = "0.5.1"

    const val junit = "4.13"
    const val testRunner = "1.3.0"
}


object AndroidSdk {
    const val min = 21
    const val compile = 29
    const val target = compile
}

object Deps {
    const val kermit = "co.touchlab:kermit:${Versions.kermit}"
    const val permissionDispatcher = "org.permissionsdispatcher:permissionsdispatcher-ktx:1.0.1"
}

object Test {
    const val junit = "junit:junit:${Versions.junit}"
}

object Compose {
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val icons = "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val iconsExtended = "androidx.compose.material:material-icons-extended:${Versions.compose}"
    const val runtimeLiveData = "androidx.compose.runtime:runtime-livedata:${Versions.compose}"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.nav_compose}"
    const val activity = "androidx.activity:activity-compose:1.3.0-alpha02"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha01"
}

object Accompanist {
    const val coil = "dev.chrisbanes.accompanist:accompanist-coil:${Versions.accompanist}"
    const val insets = "dev.chrisbanes.accompanist:accompanist-insets:${Versions.accompanist}"
}

object Koin {
    val core = "org.koin:koin-core:${Versions.koin}"
    val android = "org.koin:koin-android:${Versions.koin}"
    val androidViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
}

object Ktor {
    val clientCore = "io.ktor:ktor-client-core:${Versions.ktor}"
    val clientJson = "io.ktor:ktor-client-json:${Versions.ktor}"
    val clientLogging = "io.ktor:ktor-client-logging:${Versions.ktor}"
    val clientSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"

    val clientAndroid = "io.ktor:ktor-client-android:${Versions.ktor}"
    val clientApache = "io.ktor:ktor-client-apache:${Versions.ktor}"
    val slf4j = "org.slf4j:slf4j-simple:${Versions.slf4j}"
    val clientIos = "io.ktor:ktor-client-ios:${Versions.ktor}"
    val clientCio = "io.ktor:ktor-client-cio:${Versions.ktor}"
    val clientJs = "io.ktor:ktor-client-js:${Versions.ktor}"
}

object Serialization {
    val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}"
}

object SqlDelight {
    val runtime = "com.squareup.sqldelight:runtime:${Versions.sqlDelight}"
    val coroutineExtensions = "com.squareup.sqldelight:coroutines-extensions:${Versions.sqlDelight}"
    val androidDriver = "com.squareup.sqldelight:android-driver:${Versions.sqlDelight}"

    val nativeDriver = "com.squareup.sqldelight:native-driver:${Versions.sqlDelight}"
    val nativeDriverMacos = "com.squareup.sqldelight:native-driver-macosx64:${Versions.sqlDelight}"
    val jdbcDriver = "org.xerial:sqlite-jdbc:${Versions.sqliteJdbcDriver}"
    val sqlliteDriver = "com.squareup.sqldelight:sqlite-driver:${Versions.sqlDelight}"
}

object AndroidX {
    val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0"
    val coreKtx = "androidx.core:core-ktx:1.5.0-alpha05"
    val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
}

object PlayServices {
    val location = "com.google.android.gms:play-services-location:17.1.0"
    val mapsKtx = "com.google.maps.android:maps-ktx:2.2.0"
    val mapsUtilKtx = "com.google.maps.android:maps-utils-ktx:2.2.0"
    val mapsPlayServices = "com.google.android.gms:play-services-maps:17.0.0"
    val mapsPlayServicesUtils = "com.google.maps.android:android-maps-utils:2.0.3"
}
