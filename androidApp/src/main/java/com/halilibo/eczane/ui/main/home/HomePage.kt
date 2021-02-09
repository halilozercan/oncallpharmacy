package com.halilibo.eczane.ui.main.home

import android.location.Location
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animate
import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.buildGoogleMapOptions
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
    val (homeViewModel, homeState) = statefulViewModel<HomeViewModel, HomeState>()
    var bottomSheetNavigationState by remember {
        mutableStateOf<BottomSheetDestination>(BottomSheetDestination.CitiesList)
    }

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        confirmStateChange = { it != HalfExpanded }
    )
//    val bottomSheetNavController = rememberNavController().apply { enableOnBackPressed(false) }

    var isMyLocationVisibleOnMap by remember { mutableStateOf(false) }
    var markers by remember { mutableStateOf(emptyList<Marker>()) }

    val isBottomBarVisible = !homeState.isPharmacySelected &&
            modalBottomSheetState.targetValue == Hidden

    val bottomBarContentOffset by animateDpAsState(
        with(modalBottomSheetState.progress) {
            if (from == Hidden && to == HalfExpanded) {
                100.dp * (fraction)
            } else if (from == HalfExpanded && to == Hidden) {
                100.dp * (1 - fraction)
            } else if (from == Hidden && to == Hidden && !homeState.isPharmacySelected) {
                0.dp
            } else {
                100.dp
            }
        }
    )

    val (googleMap, onMapViewCreated) = rememberGoogleMapState(
        onMapReady = { googleMap ->
            googleMap.setOnMarkerClickListener { marker ->
                homeViewModel.setPharmacySelected(marker.snippet)
                false
            }
            googleMap.setOnMapClickListener {
                homeViewModel.setPharmacySelected(null)
            }
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(LatLng(36.0, 26.0), LatLng(42.0, 45.0)),
                    0
                )
            )
        },
        cameraIdleListener = { googleMap ->
            if (googleMap.cameraPosition.zoom >= 11f) {
                homeViewModel.setVisibleBounds(
                    googleMap.projection.visibleRegion.latLngBounds.locationBounds
                )
            } else {
                homeViewModel.clearPharmacyList()
            }
        },
        cameraMoveListener = { googleMap ->
            if (googleMap.cameraPosition.zoom < 11f) {
                isMyLocationVisibleOnMap = false
                homeViewModel.clearPharmacyList()
            } else {
                isMyLocationVisibleOnMap =
                    homeState.currentLocation in googleMap.projection.visibleRegion.latLngBounds.locationBounds
            }
        }
    )

    RegisterBackPressHandler(
        isEnabled = modalBottomSheetState.value != Hidden,
        callback = {
            if (modalBottomSheetState.isVisible) {
                modalBottomSheetState.hide()
            }
        }
    )

    RegisterBackPressHandler(
        isEnabled = homeState.isPharmacySelected,
        callback = {
            homeViewModel.setPharmacySelected(null)
        }
    )

    googleMap?.run {
        DecideMapsTheme()
        SetLocationSourceWhenAvailable(homeState.currentLocation)
        ZoomToCurrentLocationWhenAvailable(homeState.currentLocation)

        ZoomToSelectedPharmacyWhenAvailable(homeState.selectedPharmacy, markers)
        RenderPharmaciesWhenAvailable(homeState.pharmacyList) { markers = it }
    }

    val bottomBarAlpha = animatedFloat(initVal = 1f)
    LaunchedEffect(isBottomBarVisible) {
        bottomBarAlpha.animateTo(if (isBottomBarVisible) 1f else 0f)
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(
                    modifier = Modifier
                        .statusBarsHeight()
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primarySurface.copy(alpha = 0.7f))
                )
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            val nearestPharmacy = homeState.pharmacyList.firstOrNull()
            if (!isMyLocationVisibleOnMap || nearestPharmacy == null) {
                FloatingActionButton(
                    onClick = {
                        if (homeState.locationPermissionStatus == PermissionGrantStatus.GRANTED) {
                            zoomToLocation(homeState.currentLocation, googleMap)
                        } else {
                            bottomSheetNavigationState = BottomSheetDestination.LocationPermission
                            modalBottomSheetState.animateTo(targetValue = Expanded)
                        }
                    },
                    modifier = Modifier.offset(y = bottomBarContentOffset)
                ) {
                    Icon(Icons.Default.MyLocation, tint = Color.White, contentDescription = null)
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        homeViewModel.setPharmacySelected(nearestPharmacy.phone)
                    },
                    modifier = Modifier.offset(y = bottomBarContentOffset)
                ) {
                    Icon(Icons.Default.NearMe, tint = Color.White, contentDescription = null)
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                modifier = Modifier.navigationBarsPadding().offset(y = bottomBarContentOffset)
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
                    selected = false,
                    onClick = {
                        bottomSheetNavigationState= BottomSheetDestination.CitiesList
                        modalBottomSheetState.animateTo(targetValue = Expanded)
                    },
                    label = { Text(stringResource(R.string.cities)) }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {
                        bottomSheetNavigationState= BottomSheetDestination.Settings
                        modalBottomSheetState.animateTo(targetValue = Expanded)
                    },
                    label = { Text(stringResource(R.string.settings)) }
                )
            }
        }
    ) {
        ModalBottomSheetLayout(
            sheetState = modalBottomSheetState,
            sheetElevation = 4.dp,
            sheetContent = {
                Box(modifier = Modifier.heightIn(min = 200.dp)) {
                    when(bottomSheetNavigationState) {
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
                ComposeMapView(
                    onMapViewCreated = onMapViewCreated,
                    modifier = Modifier.fillMaxSize(),
                    googleMapOptions = buildGoogleMapOptions {
                        mapToolbarEnabled(false)
                        tiltGesturesEnabled(false)
                        rotateGesturesEnabled(false)
                        minZoomPreference(6f)
                    }
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
fun GoogleMap.ZoomToCurrentLocationWhenAvailable(
    location: Location?,
) {
    val googleMap = this
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
fun GoogleMap.ZoomToSelectedPharmacyWhenAvailable(
    selectedPharmacy: Pharmacy?,
    markers: List<Marker>
) {
    val googleMap = this

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
fun GoogleMap.DecideMapsTheme() {
    val isDarkTheme = AmbientDarkTheme.current
    val context = AmbientContext.current

    val googleMap = this

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
fun GoogleMap.SetLocationSourceWhenAvailable(location: Location?) {
    val googleMap = this

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
fun GoogleMap.RenderPharmaciesWhenAvailable(
    pharmacyList: List<Pharmacy>,
    onMarkersCreated: (List<Marker>) -> Unit
) {
    val googleMap = this
    LaunchedEffect(pharmacyList, googleMap) {
        googleMap.clear()
        val markers = pharmacyList.map {
            googleMap.addMarker {
                position(LatLng(it.latitude, it.longitude))
                title(it.name)
                snippet(it.phone)
            }
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