package com.halilibo.eczane.ui.main.home

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun HomePageCenterButton(
    centerButtonState: CenterButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (centerButtonState) {
        CenterButtonState.MY_LOCATION -> {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier
            ) {
                Icon(Icons.Default.MyLocation, tint = Color.White, contentDescription = null)
            }
        }
        CenterButtonState.NEAREST_PHARMACY -> {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier
            ) {
                Icon(Icons.Default.NearMe, tint = Color.White, contentDescription = null)
            }
        }
    }
}

enum class CenterButtonState {
    MY_LOCATION,
    NEAREST_PHARMACY
}