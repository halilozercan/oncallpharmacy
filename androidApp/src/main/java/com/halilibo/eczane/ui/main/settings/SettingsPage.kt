package com.halilibo.eczane.ui.main.settings

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.eczane.R
import com.halilibo.eczane.ui.common.statefulViewModel
import com.halilibo.shared.model.ThemePreference
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

@ExperimentalMaterialApi
@Composable
fun SettingsPage(
    onNavigateBack: () -> Unit
) {
    val (settingsViewModel, settingsState) = statefulViewModel<SettingsViewModel, SettingsState>()

    Scaffold {
        val modalBottomSheetState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        ModalBottomSheetLayout(
            sheetState = modalBottomSheetState,
            sheetContent = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    ThemePreference.values().forEach {
                        Row(modifier = Modifier
                            .clickable(enabled = it != settingsState.themePreference) {
                                settingsViewModel.updateThemePreference(it)
                                modalBottomSheetState.hide()
                            }
                            .padding(16.dp)
                        ) {
                            Text(
                                text = it.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (it == settingsState.themePreference) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                }
            }
        ) {
            ScrollableColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {

                Row {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            modifier = Modifier.size(48.dp),
                            contentDescription = null
                        )
                    }
                }

                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.h3
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            modalBottomSheetState.show()
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_theme_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.settings_theme_description),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
