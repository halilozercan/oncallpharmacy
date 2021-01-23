package com.halilibo.shared.model

data class LocationBounds(
    val southWest: Coordinates,
    val northEast: Coordinates
) {
    val south = southWest.latitude
    val north = northEast.latitude
    val east = northEast.longitude
    val west = southWest.longitude

    val southEast = Coordinates(south, east)
    val northWest = Coordinates(north, west)

    val center = Coordinates((south+north)/2, (east+west)/2)
}