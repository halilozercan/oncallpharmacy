package com.halilibo.eczane.ui.main.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import com.halilibo.eczane.ui.common.StatefulViewModel
import com.halilibo.shared.distance
import com.halilibo.shared.model.LocationBounds
import com.halilibo.shared.model.Pharmacy
import com.halilibo.shared.repository.PharmacyMapsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

class HomeViewModel(
    context: Context,
    private val locationFlow: Flow<Location>,
    private val pharmacyMapsRepository: PharmacyMapsRepository
) : StatefulViewModel<HomeState>(HomeState()) {

    private var locationJob: Job? = null

    init {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            updateLocationPermissionStatus(PermissionGrantStatus.DENIED)
        } else {
            updateLocationPermissionStatus(PermissionGrantStatus.GRANTED)
        }

        pharmacyMapsRepository.pharmaciesFlow
            .flowOn(Dispatchers.IO)
            .combine(stateFlow.map { it.currentLocation }) { pharmacyList, currentLocation ->
                pharmacyList to currentLocation
            }
            .map { (pharmacyList, currentLocation) ->
                if (currentLocation != null) {
                    pharmacyList.sortedBy { pharmacy ->
                        distance(
                            lat1 = currentLocation.latitude,
                            lat2 = pharmacy.latitude,
                            lon1 = currentLocation.longitude,
                            lon2 = pharmacy.longitude,
                            el1 = .0,
                            el2 = .0
                        )
                    }
                } else {
                    pharmacyList
                }
            }
            .onEach { pharmacyList ->
                setState {
                    it.copy(
                        selectedPharmacy = it.selectedPharmacy.takeIf(pharmacyList::contains),
                        pharmacyList = pharmacyList
                    )
                }
            }
            .launchIn(viewModelScope)

        // Send the visible window from map to pharmacyMapsRepository
        stateFlow.map { it.locationBounds }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach {
                pharmacyMapsRepository.updateMapBounds(it)
            }
            .launchIn(viewModelScope)
    }

    fun updateLocationPermissionStatus(newStatus: PermissionGrantStatus) {
        locationJob = if (newStatus == PermissionGrantStatus.GRANTED) {
            locationFlow
                .onEach { location ->
                    setState { it.copy(currentLocation = location) }
                }
                .launchIn(viewModelScope)
        } else {
            locationJob?.cancel()
            null
        }

        setState {
            it.copy(locationPermissionStatus = newStatus)
        }
    }

    fun setVisibleBounds(locationBounds: LocationBounds) = setState {
        it.copy(locationBounds = locationBounds)
    }

    fun getHumanReadableDistanceToCurrentLocation(pharmacy: Pharmacy): String {
        val currentLocation = stateFlow.value.currentLocation ?: return ""
        val distanceInMeters = distance(
            pharmacy.latitude, currentLocation.latitude,
            pharmacy.longitude, currentLocation.longitude,
            0.0, 0.0
        ).roundToInt()

        return if (distanceInMeters < 1000) {
            "~${(distanceInMeters / 100) * 100}m"
        } else {
            "~${distanceInMeters / 1000}.${distanceInMeters % 1000 / 100}km"
        }
    }

    fun setPharmacySelected(phone: String?) {
        setState {
            val selectedPharmacy = it.pharmacyList.firstOrNull { it.phone == phone }
            it.copy(
                selectedPharmacy = selectedPharmacy
            )
        }
    }
}

enum class PermissionGrantStatus {
    GRANTED,
    NEVER_ASK_AGAIN,
    LOADING,
    DENIED
}

data class HomeState(
    val pharmacyList: List<Pharmacy> = emptyList(),
    val locationPermissionStatus: PermissionGrantStatus = PermissionGrantStatus.LOADING,
    val currentLocation: Location? = null,
    val locationBounds: LocationBounds? = null,
    val selectedPharmacy: Pharmacy? = null
)

val HomeState.isPharmacySelected: Boolean
    get() = selectedPharmacy != null