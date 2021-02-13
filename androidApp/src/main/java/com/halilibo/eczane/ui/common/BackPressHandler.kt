package com.halilibo.eczane.ui.common

import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity

@Composable
fun RegisterBackPressHandler(
    isEnabled: Boolean,
    callback: () -> Unit
) {
    val activity = LocalContext.current as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    val onBackPressedCallback = remember {
        object: OnBackPressedCallback(isEnabled) {
            override fun handleOnBackPressed() {
                callback()
            }
        }
    }

    DisposableEffect(Unit) {
        activity.onBackPressedDispatcher.addCallback(
            lifecycleOwner,
            onBackPressedCallback
        )

        onDispose {
            onBackPressedCallback.remove()
        }
    }

    LaunchedEffect(isEnabled) {
        onBackPressedCallback.isEnabled = isEnabled
    }
}