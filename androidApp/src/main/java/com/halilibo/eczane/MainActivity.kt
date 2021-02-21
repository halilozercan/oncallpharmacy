package com.halilibo.eczane

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.halilibo.eczane.ui.common.OnCallPharmacyTheme
import com.halilibo.eczane.ui.main.home.HomePage
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets


@ExperimentalAnimationApi
@ExperimentalMaterialApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun App() {
    OnCallPharmacyTheme {
        ProvideWindowInsets {
            HomePage()
        }
    }
}