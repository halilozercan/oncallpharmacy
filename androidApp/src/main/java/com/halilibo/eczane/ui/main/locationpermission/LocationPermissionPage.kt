package com.halilibo.eczane.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.platform.AmbientView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.halilibo.eczane.R
import com.halilibo.eczane.ui.main.home.PermissionGrantStatus
import com.halilibo.shared.repository.getLogger
import dev.chrisbanes.accompanist.insets.AmbientWindowInsets
import permissions.dispatcher.ktx.LocationPermission
import permissions.dispatcher.ktx.constructLocationPermissionRequest

@ExperimentalMaterialApi
@Composable
fun LocationPermissionPage(
    onPermissionGrantStatusChange: (PermissionGrantStatus) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activity = AmbientContext.current as FragmentActivity

    val permissionsRequester = remember(activity, onPermissionGrantStatusChange) {
        activity.constructLocationPermissionRequest(
            LocationPermission.FINE, LocationPermission.COARSE,
            onShowRationale = {
                getLogger().d("LocationPermission", "onShowRationale")
                it.proceed()
            },
            onPermissionDenied = {
                getLogger().d("LocationPermission", "permission denied")
                onPermissionGrantStatusChange(PermissionGrantStatus.DENIED)
            },
            onNeverAskAgain = {
                getLogger().d("LocationPermission", "never ask again")
                onPermissionGrantStatusChange(PermissionGrantStatus.NEVER_ASK_AGAIN)
            }
        ) {
            getLogger().d("LocationPermission", "granted")
            onPermissionGrantStatusChange(PermissionGrantStatus.GRANTED)
            onNavigateBack()
        }
    }

    Scaffold {
        val totalHeight = with(AmbientDensity.current) {
            (AmbientView.current.height - AmbientWindowInsets.current.systemBars.top - AmbientWindowInsets.current.systemBars.bottom).toDp()
        }

        ScrollableColumn(
            modifier = Modifier.fillMaxWidth()
                .height(totalHeight)
                .padding(16.dp)
        ) {

            Row {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft.copy(
                            defaultWidth = 48.dp,
                            defaultHeight = 48.dp
                        ),
                    )
                }
            }

            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.location),
                    style = MaterialTheme.typography.h3
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Image(
                    imageVector = vectorResource(id = R.drawable.my_location_illustration)
                )
            }

            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.location_permission_request),
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        permissionsRequester.launch()
                    },
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(stringResource(R.string.enable_uppercase))
                }
            }
        }
    }
}
