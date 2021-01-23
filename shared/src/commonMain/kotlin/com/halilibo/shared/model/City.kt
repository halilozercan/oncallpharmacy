package com.halilibo.shared.model

data class City(
    val id: Int,
    val name: String,
    val coordinates: Coordinates,
    val bounds: LocationBounds
)