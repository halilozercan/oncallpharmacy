package com.halilibo.eczane.ui.main.home

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.halilibo.eczane.R

@Composable
fun HomePageBottomBar(
    onCitiesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
        modifier = modifier
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
            selected = false,
            onClick = onCitiesClick,
            label = { Text(stringResource(R.string.cities)) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick = onSettingsClick,
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}