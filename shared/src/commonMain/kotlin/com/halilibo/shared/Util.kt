package com.halilibo.shared

import com.halilibo.shared.model.Coordinates
import kotlin.math.*

/**
 * Calculate distance between two points in latitude and longitude taking
 * into account height difference. If you are not interested in height
 * difference pass 0.0. Uses Haversine method as its base.
 *
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 * @returns Distance in Meters
 */
fun distance(
    lat1: Double, lat2: Double, lon1: Double,
    lon2: Double, el1: Double, el2: Double
): Double {
    val R = 6371 // Radius of the earth
    val latDistance = (lat2 - lat1).toRadians()
    val lonDistance = (lon2 - lon1).toRadians()
    val a = (sin(latDistance / 2) * sin(latDistance / 2)
            + (cos(lat1.toRadians()) * cos(lat2.toRadians())
            * sin(lonDistance / 2) * sin(lonDistance / 2)))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    var distance = R * c * 1000 // convert to meters
    val height = el1 - el2
    distance = distance.pow(2.0) + height.pow(2.0)
    return sqrt(distance)
}

fun Double.toRadians() = this / 180.0 * PI

fun getLocationBlock(lat: Double, lng: Double): Long {
    return if (lat > 0f && lng > 0f) {
        "${(lat * 2).roundToInt()}${(lng).roundToInt()}".toLong()
    }
    else {
        0L
    }
}

val Coordinates.locationBlock: Long
    get() = getLocationBlock(latitude, longitude)