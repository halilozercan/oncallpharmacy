package com.halilibo.eczane.ui.common

import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.fragment.app.FragmentActivity

@Composable
fun RegisterBackPressHandler(
    isEnabled: Boolean,
    callback: () -> Unit
) {
    val activity = AmbientContext.current as FragmentActivity
    val lifecycleOwner = AmbientLifecycleOwner.current

    val onBackPressedCallback = remember {
        object: OnBackPressedCallback(isEnabled) {
            override fun handleOnBackPressed() {
                callback()
            }
        }
    }

    onActive {
        activity.onBackPressedDispatcher.addCallback(
            lifecycleOwner,
            onBackPressedCallback
        )
    }

    onCommit(isEnabled) {
        onBackPressedCallback.isEnabled = isEnabled
    }

    onDispose {
        onBackPressedCallback.remove()
    }
}