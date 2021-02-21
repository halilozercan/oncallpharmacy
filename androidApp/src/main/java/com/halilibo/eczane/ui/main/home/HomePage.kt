package com.halilibo.eczane.ui.main.home

import android.location.Location
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addMarker
import com.halilibo.eczane.R
import com.halilibo.eczane.ui.common.*
import com.halilibo.eczane.ui.main.LocationPermissionPage
import com.halilibo.eczane.ui.main.locationlist.LocationListPage
import com.halilibo.eczane.ui.main.settings.SettingsPage
import com.halilibo.eczane.util.asLatLngBounds
import com.halilibo.eczane.util.contains
import com.halilibo.eczane.util.locationBounds
import com.halilibo.shared.model.Pharmacy
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsHeight

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun HomePage() {
    val homeViewModel by koinViewModel<HomeViewModel>()
    val homeStateHolder = homeViewModel.stateFlow.collectAsState()
    val homeState = homeStateHolder.value

    var bottomSheetNavigationState by remember {
        mutableStateOf<BottomSheetDestination>(BottomSheetDestination.CitiesList)
    }

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        confirmStateChange = { it != HalfExpanded }
    )

    var isMyLocationVisibleOnMap by remember { mutableStateOf(false) }
    var markers by remember { mutableStateOf(emptyList<Marker>()) }

    val bottomBarContentOffset = with(modalBottomSheetState.progress) {
        when {
            from == Hidden && to == HalfExpanded -> {
                100.dp * (fraction)
            }
            from == HalfExpanded && to == Hidden -> {
                100.dp * (1 - fraction)
            }
            from == Hidden && to == Hidden && !homeState.isPharmacySelected -> {
                0.dp
            }
            else -> {
                100.dp
            }
        }
    }

    val composeGoogleMapState = rememberComposeGoogleMapState(
        onMapReady = { googleMap ->
            if (homeStateHolder.value.currentLocation == null) {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(LatLng(36.0, 26.0), LatLng(42.0, 45.0)),
                        0
                    )
                )
            }

            googleMap.setOnMarkerClickListener { marker ->
                homeViewModel.setPharmacySelected(marker.snippet)
                false
            }
            googleMap.setOnMapClickListener {
                homeViewModel.setPharmacySelected(null)
            }
            googleMap.uiSettings.isMyLocationButtonEnabled = false
        },
        cameraIdleListener = { googleMap ->
            homeViewModel.setVisibleBounds(googleMap.projection.visibleRegion.latLngBounds.locationBounds)
        },
        cameraMoveListener = { googleMap ->
            val visibleRegionBounds = googleMap.projection.visibleRegion.latLngBounds.locationBounds

            isMyLocationVisibleOnMap = googleMap.cameraPosition.zoom >= ZOOM_RENDER_THRESHOLD &&
                    homeStateHolder.value.currentLocation in visibleRegionBounds
        }
    )
    val googleMap = composeGoogleMapState.value

    BackHandler(
        enabled = modalBottomSheetState.value != Hidden,
        onBack = {
            if (modalBottomSheetState.isVisible) {
                modalBottomSheetState.hide()
            }
        }
    )

    BackHandler(
        enabled = homeState.isPharmacySelected,
        onBack = {
            homeViewModel.setPharmacySelected(null)
        }
    )

    if (googleMap != null) {
        DecideMapsTheme(googleMap)
        SetLocationSourceWhenAvailable(googleMap, homeState.currentLocation)
        ZoomToCurrentLocationWhenAvailable(googleMap, homeState.currentLocation)

        ZoomToSelectedPharmacyWhenAvailable(googleMap, homeState.selectedPharmacy, markers)
        RenderPharmaciesWhenAvailable(googleMap, homeState.pharmacyList) { markers = it }
    }

    Scaffold(
        topBar = { StatusBarSpaceHolder() },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            val nearestPharmacy = homeState.pharmacyList.firstOrNull()
            val centerButtonState =
                if (!isMyLocationVisibleOnMap || nearestPharmacy == null) {
                    CenterButtonState.MY_LOCATION
                } else {
                    CenterButtonState.NEAREST_PHARMACY
                }

            HomePageCenterButton(
                centerButtonState = centerButtonState,
                onClick = {
                    when (centerButtonState) {
                        CenterButtonState.MY_LOCATION -> {
                            if (homeState.locationPermissionStatus == PermissionGrantStatus.GRANTED) {
                                zoomToLocation(homeState.currentLocation, googleMap)
                            } else {
                                bottomSheetNavigationState =
                                    BottomSheetDestination.LocationPermission
                                modalBottomSheetState.animateTo(targetValue = Expanded)
                            }
                        }
                        CenterButtonState.NEAREST_PHARMACY -> {
                            homeViewModel.setPharmacySelected(nearestPharmacy?.phone)
                        }
                    }
                },
                modifier = Modifier.offset(y = bottomBarContentOffset)
            )
        },
        bottomBar = {
            HomePageBottomBar(
                onCitiesClick = {
                    bottomSheetNavigationState = BottomSheetDestination.CitiesList
                    modalBottomSheetState.animateTo(targetValue = Expanded)
                },
                onSettingsClick = {
                    bottomSheetNavigationState = BottomSheetDestination.Settings
                    modalBottomSheetState.animateTo(targetValue = Expanded)
                },
                modifier = Modifier.navigationBarsPadding().offset(y = bottomBarContentOffset)
            )
        }
    ) {
        ModalBottomSheetLayout(
            sheetState = modalBottomSheetState,
            sheetElevation = 4.dp,
            sheetContent = {
                Box(modifier = Modifier.heightIn(min = 200.dp)) {
                    when (bottomSheetNavigationState) {
                        is BottomSheetDestination.CitiesList -> {
                            LocationListPage(
                                onCityClick = { city ->
                                    googleMap?.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(
                                            city.bounds.asLatLngBounds,
                                            32
                                        )
                                    )
                                    modalBottomSheetState.hide()
                                },
                                onNavigateBack = {
                                    modalBottomSheetState.hide()
                                },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is BottomSheetDestination.Settings -> {
                            SettingsPage(
                                onNavigateBack = {
                                    modalBottomSheetState.hide()
                                }
                            )
                        }
                        is BottomSheetDestination.LocationPermission -> {
                            LocationPermissionPage(
                                onPermissionGrantStatusChange = {
                                    homeViewModel.updateLocationPermissionStatus(it)
                                },
                                onNavigateBack = {
                                    modalBottomSheetState.hide()
                                }
                            )
                        }
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxSize()
            ) {
                ComposeGoogleMap(
                    composeGoogleMapState = composeGoogleMapState,
                    modifier = Modifier.fillMaxSize(),
                    googleMapOptionsKtx = GoogleMapOptionsKtx(
                        mapToolbarEnabled = false,
                        tiltGesturesEnabled = false,
                        rotateGesturesEnabled = false,
                        minZoomPreference = 6f
                    )
                )

                if (homeState.isPharmacySelected) {
                    PharmacyPager(
                        pharmacyList = homeState.pharmacyList,
                        selectedPharmacy = homeState.selectedPharmacy,
                        onPharmacySelect = {
                            homeViewModel.setPharmacySelected(it.phone)
                        },
                        distance = {
                            homeViewModel.getHumanReadableDistanceToCurrentLocation(it)
                        },
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomToCurrentLocationWhenAvailable(
    googleMap: GoogleMap,
    location: Location?,
) {
    LaunchedEffect(location, googleMap) {
        zoomToLocation(location, googleMap)
    }
}

fun zoomToLocation(
    location: Location?,
    googleMap: GoogleMap?
) {
    if (location != null && googleMap != null) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                12f
            )
        )
    }
}

@Composable
fun ZoomToSelectedPharmacyWhenAvailable(
    googleMap: GoogleMap,
    selectedPharmacy: Pharmacy?,
    markers: List<Marker>
) {
    selectedPharmacy ?: return

    LaunchedEffect(selectedPharmacy, googleMap) {
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    LatLng(
                        selectedPharmacy.latitude,
                        selectedPharmacy.longitude
                    ),
                    // Zoom in or out only if necessary.
                    googleMap.cameraPosition.zoom.coerceIn(12f, 15f)
                )
            )
        )

        markers.firstOrNull {
            it.snippet == selectedPharmacy.phone
        }?.showInfoWindow()
    }
}

@Composable
fun DecideMapsTheme(googleMap: GoogleMap) {
    val isDarkTheme = LocalDarkTheme.current
    val context = LocalContext.current

    LaunchedEffect(googleMap, isDarkTheme) {
        if (isDarkTheme) {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.dark_maps
                )
            )
        } else {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.light_maps
                )
            )
        }
    }
}

@Composable
fun SetLocationSourceWhenAvailable(googleMap: GoogleMap, location: Location?) {
    val locationSource = rememberLocationSource(location)
    LaunchedEffect(googleMap, locationSource) {
        if (locationSource != null) {
            googleMap.setLocationSource(locationSource)
            googleMap.isMyLocationEnabled = true
        } else {
            googleMap.isMyLocationEnabled = false
        }
    }
}

@Composable
fun RenderPharmaciesWhenAvailable(
    googleMap: GoogleMap,
    pharmacyList: List<Pharmacy>,
    onMarkersCreated: (List<Marker>) -> Unit
) {
    val shouldDraw = googleMap.cameraPosition.zoom >= ZOOM_RENDER_THRESHOLD

    LaunchedEffect(pharmacyList, shouldDraw) {
        googleMap.clear()
        val markers = if (shouldDraw) {
            pharmacyList.map {
                googleMap.addMarker {
                    position(LatLng(it.latitude, it.longitude))
                    title(it.name)
                    snippet(it.phone)
                }
            }
        } else {
            emptyList()
        }
        onMarkersCreated(markers)
    }
}

@Composable
fun rememberLocationSource(location: Location? = null): LocationSource? {
    return remember(location) {
        location?.let {
            object : LocationSource {
                override fun activate(listener: LocationSource.OnLocationChangedListener?) {
                    listener?.onLocationChanged(location)
                }

                override fun deactivate() {}
            }
        }
    }
}

sealed class BottomSheetDestination(val route: String) {
    object CitiesList : BottomSheetDestination("cities")
    object Settings : BottomSheetDestination("settings")
    object LocationPermission : BottomSheetDestination("locationPermission")
}

const val ZOOM_RENDER_THRESHOLD = 10.5f
