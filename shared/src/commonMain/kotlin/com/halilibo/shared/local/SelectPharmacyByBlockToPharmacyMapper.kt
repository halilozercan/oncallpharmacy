package com.halilibo.shared.local

import com.halilibo.eczane.db.SelectPharmacyByBlock
import com.halilibo.shared.model.City
import com.halilibo.shared.model.Coordinates
import com.halilibo.shared.model.LocationBounds
import com.halilibo.shared.model.Pharmacy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun SelectPharmacyByBlock.toPharmacy(): Pharmacy {
    return Pharmacy(
        city = City(
            id = city_id.toInt(),
            name = name_,
            coordinates = Coordinates(lat, lng),
            bounds = LocationBounds(
                southWest = Coordinates(southwest_lat, southwest_lng),
                northEast = Coordinates(northeast_lat, northeast_lng)
            )
        ),
        name = name,
        address = address,
        longitude = longitude ?: 0.0,
        latitude = latitude ?: 0.0,
        notes = notes ?: "",
        phone = phone ?: ""
    )
}

fun Flow<List<SelectPharmacyByBlock>>.mapToPharmacy(): Flow<List<Pharmacy>> {
    return map { list -> list.map(SelectPharmacyByBlock::toPharmacy) }
}