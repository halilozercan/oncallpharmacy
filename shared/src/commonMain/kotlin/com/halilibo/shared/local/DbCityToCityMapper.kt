package com.halilibo.shared.local

import com.halilibo.eczane.db.DbCity
import com.halilibo.shared.model.City
import com.halilibo.shared.model.Coordinates
import com.halilibo.shared.model.LocationBounds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun DbCity.toCity(): City {
    return City(
        id = id.toInt(),
        name = name,
        coordinates = Coordinates(lat, lng),
        bounds = LocationBounds(
            southWest = Coordinates(southwest_lat, southwest_lng),
            northEast = Coordinates(northeast_lat, northeast_lng)
        )
    )
}

fun Flow<List<DbCity>>.mapToCity(): Flow<List<City>> {
    return map { list -> list.map(DbCity::toCity) }
}