package com.halilibo.eczane.ui.common

import android.os.Parcelable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.*
import com.halilibo.shared.model.Coordinates
import com.halilibo.shared.model.LocationBounds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Composable
fun ComposeGoogleMap(
    composeGoogleMapState: ComposeGoogleMapState,
    modifier: Modifier = Modifier,
    googleMapOptionsKtx: GoogleMapOptionsKtx = GoogleMapOptionsKtx()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val googleMapOptions = remember(googleMapOptionsKtx) { googleMapOptionsKtx.build() }

    AndroidView(
        viewBlock = {
            MapView(it, googleMapOptions).also { mapView ->
                coroutineScope.launch {
                    val googleMap = mapView.awaitMap()
                    composeGoogleMapState.onMapViewCreated(googleMap)
                }

                object : DefaultLifecycleObserver {
                    override fun onCreate(owner: LifecycleOwner) = mapView.onCreate(bundleOf())

                    override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()

                    override fun onPause(owner: LifecycleOwner) = mapView.onPause()

                    override fun onStart(owner: LifecycleOwner) = mapView.onStart()

                    override fun onResume(owner: LifecycleOwner) = mapView.onResume()

                    override fun onStop(owner: LifecycleOwner) = mapView.onStop()
                }.also { lifecycleObserver ->
                    composeGoogleMapState.lifecycleObserver = lifecycleObserver
                    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                }
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            composeGoogleMapState.lifecycleObserver?.let { lifecycleObserver ->
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        }
    }
}

@Stable
class ComposeGoogleMapState(
    private val onMapReady: (GoogleMap) -> Unit,
    private val cameraIdleListener: (GoogleMap) -> Unit,
    private val cameraMoveListener: (GoogleMap) -> Unit,
    private val cameraMoveCanceledListener: (GoogleMap) -> Unit,
    private val cameraMoveStartedListener: (GoogleMap, Int) -> Unit
): State<GoogleMap?> {
    private val googleMapState: MutableState<GoogleMap?> = mutableStateOf(null)

    internal var lifecycleObserver: LifecycleObserver? = null

    internal suspend fun onMapViewCreated(googleMap: GoogleMap) {
        googleMapState.value = googleMap
        googleMap.setOnMapLoadedCallback {
            onMapReady(googleMap)
        }
        googleMap.cameraEvents()
            .collect { event ->
                when (event) {
                    CameraIdleEvent -> cameraIdleListener(googleMap)
                    CameraMoveCanceledEvent -> cameraMoveCanceledListener(googleMap)
                    CameraMoveEvent -> cameraMoveListener(googleMap)
                    is CameraMoveStartedEvent -> cameraMoveStartedListener(googleMap, event.reason)
                }
            }
    }

    override val value: GoogleMap?
        get() = googleMapState.value
}

@Composable
fun rememberComposeGoogleMapState(
    onMapReady: (GoogleMap) -> Unit = {},
    cameraIdleListener: (GoogleMap) -> Unit = {},
    cameraMoveListener: (GoogleMap) -> Unit = {},
    cameraMoveCanceledListener: (GoogleMap) -> Unit = {},
    cameraMoveStartedListener: (GoogleMap, Int) -> Unit = { _, _ -> },
): ComposeGoogleMapState {
    return remember {
        ComposeGoogleMapState(
            onMapReady = onMapReady,
            cameraIdleListener = cameraIdleListener,
            cameraMoveListener = cameraMoveListener,
            cameraMoveCanceledListener = cameraMoveCanceledListener,
            cameraMoveStartedListener = cameraMoveStartedListener
        )
    }
}

data class GoogleMapOptionsKtx(
    // TODO: https://issuetracker.google.com/issues/180712131
    // val latLngBoundsForCameraTarget: LatLngBounds? = null,
    val liteMode: Boolean? = null,
    val mapToolbarEnabled: Boolean? = null,
    val mapType: Int? = null,
    val maxZoomPreference: Float? = null,
    val minZoomPreference: Float? = null,
    val rotateGesturesEnabled: Boolean? = null,
    val scrollGesturesEnabled: Boolean? = null,
    val scrollGesturesEnabledDuringRotateOrZoom: Boolean? = null,
    val tiltGesturesEnabled: Boolean? = null,
    val useViewLifecycleInFragment: Boolean? = null,
    val zOrderOnTop: Boolean? = null,
    val zoomControlsEnabled: Boolean? = null,
    val zoomGesturesEnabled: Boolean? = null,
) {

    fun build(): GoogleMapOptions {
        val source = this

        return buildGoogleMapOptions {
            // source.latLngBoundsForCameraTarget?.let { latLngBoundsForCameraTarget(it) }
            source.liteMode?.let { liteMode(it) }
            source.mapToolbarEnabled?.let { mapToolbarEnabled(it) }
            source.mapType?.let { mapType(it) }
            source.maxZoomPreference?.let { maxZoomPreference(it) }
            source.minZoomPreference?.let { minZoomPreference(it) }
            source.rotateGesturesEnabled?.let { rotateGesturesEnabled(it) }
            source.scrollGesturesEnabled?.let { scrollGesturesEnabled(it) }
            source.scrollGesturesEnabledDuringRotateOrZoom?.let { scrollGesturesEnabledDuringRotateOrZoom(it) }
            source.tiltGesturesEnabled?.let { tiltGesturesEnabled(it) }
            source.useViewLifecycleInFragment?.let { useViewLifecycleInFragment(it) }
            source.zOrderOnTop?.let { zOrderOnTop(it) }
            source.zoomControlsEnabled?.let { zoomControlsEnabled(it) }
            source.zoomGesturesEnabled?.let { zoomGesturesEnabled(it) }
        }
    }
}