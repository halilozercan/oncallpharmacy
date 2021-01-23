package com.halilibo.eczane.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.halilibo.shared.model.Coordinates
import com.halilibo.shared.model.LocationBounds

val LocationBounds.asLatLngBounds: LatLngBounds
    get() = LatLngBounds(
        LatLng(south, west),
        LatLng(north, east),
    )

val LatLng.coordinates: Coordinates
    get() = Coordinates(latitude, longitude)

val LatLngBounds.locationBounds: LocationBounds
    get() = LocationBounds(southwest.coordinates, northeast.coordinates)

operator fun LocationBounds?.contains(location: Location?): Boolean {
    this ?: return false
    location ?: return false

    return location.latitude < north && location.latitude > south &&
            location.longitude < east && location.longitude > west
}
