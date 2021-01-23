package com.halilibo.eczane.ui.common

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.maps.android.ktx.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun ComposeMapView(
    onMapViewCreated: (MapView) -> Unit,
    modifier: Modifier = Modifier,
    googleMapOptions: GoogleMapOptions = buildGoogleMapOptions {  }
) {
    AndroidView(
        viewBlock = {
            MapView(it, googleMapOptions).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val mapView = this

                (it as ComponentActivity).lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onCreate(owner: LifecycleOwner) {
                        super.onCreate(owner)
                        mapView.onCreate(bundleOf())
                    }

                    override fun onDestroy(owner: LifecycleOwner) {
                        super.onDestroy(owner)
                        mapView.onDestroy()
                    }

                    override fun onPause(owner: LifecycleOwner) {
                        super.onPause(owner)
                        mapView.onPause()
                    }

                    override fun onStart(owner: LifecycleOwner) {
                        super.onStart(owner)
                        mapView.onStart()
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        super.onResume(owner)
                        mapView.onResume()
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        super.onStop(owner)
                        mapView.onStop()
                    }
                })

                onMapViewCreated(this)
            }
        },
        modifier = modifier
    )
}

class GoogleMapState(
    private val onMapReady: (GoogleMap) -> Unit,
    private val cameraIdleListener: (GoogleMap) -> Unit,
    private val cameraMoveListener: (GoogleMap) -> Unit,
    private val cameraMoveCanceledListener: (GoogleMap) -> Unit,
    private val cameraMoveStartedListener: (GoogleMap, Int) -> Unit,
    private val coroutineScope: CoroutineScope
): State<GoogleMap?> {

    private val googleMapState: MutableState<GoogleMap?> = mutableStateOf(null)

    fun onMapViewCreated(mapView: MapView) {
        coroutineScope.launch {
            with(mapView.awaitMap()) {
                googleMapState.value = this
                onMapReady(this)
                setListeners(this)
            }
        }
    }

    private fun setListeners(googleMap: GoogleMap) {
        googleMap.cameraEvents()
            .onEach { event ->
                when (event) {
                    CameraIdleEvent -> cameraIdleListener(googleMap)
                    CameraMoveCanceledEvent -> cameraMoveCanceledListener(googleMap)
                    CameraMoveEvent -> cameraMoveListener(googleMap)
                    is CameraMoveStartedEvent -> cameraMoveStartedListener(googleMap, event.reason)
                }
            }
            .launchIn(coroutineScope)

    }

    override val value: GoogleMap?
        get() = googleMapState.value

    operator fun component1(): GoogleMap? = googleMapState.value

    operator fun component2(): (MapView) -> Unit = this::onMapViewCreated
}

@Composable
fun rememberGoogleMapState(
    onMapReady: (GoogleMap) -> Unit = {},
    cameraIdleListener: (GoogleMap) -> Unit = {},
    cameraMoveListener: (GoogleMap) -> Unit = {},
    cameraMoveCanceledListener: (GoogleMap) -> Unit = {},
    cameraMoveStartedListener: (GoogleMap, Int) -> Unit = { _, _ -> },
): GoogleMapState {
    val coroutineScope = rememberCoroutineScope()
    return remember {
        GoogleMapState(
            onMapReady = onMapReady,
            cameraIdleListener = cameraIdleListener,
            cameraMoveListener = cameraMoveListener,
            cameraMoveCanceledListener = cameraMoveCanceledListener,
            cameraMoveStartedListener = cameraMoveStartedListener,
            coroutineScope = coroutineScope
        )
    }
}