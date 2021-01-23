package com.halilibo.shared.repository

import co.touchlab.kermit.Kermit
import com.halilibo.eczane.db.OnCallPharmacyDatabase
import com.halilibo.shared.*
import com.halilibo.shared.local.mapToCity
import com.halilibo.shared.local.mapToPharmacy
import com.halilibo.shared.model.City
import com.halilibo.shared.model.Coordinates
import com.halilibo.shared.model.LocationBounds
import com.halilibo.shared.model.Pharmacy
import com.halilibo.shared.remote.PharmacyApi
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject


class PharmacyRepository() : KoinComponent {
    private val pharmacyApi: PharmacyApi by inject()
    private val logger: Kermit by inject()

    private val coroutineScope: CoroutineScope = MainScope()
    private val onCallPharmacyDatabase: OnCallPharmacyDatabase by inject()
    private val onCallPharmacyQueries = onCallPharmacyDatabase.onCallPharmacyQueries

    init {
        coroutineScope.launch {
            fetchAndStorePharmaciesDefaultCities()
        }
    }

    fun getPharmaciesByCity(
        cityId: Int,
        center: Coordinates?
    ): CommonFlow<List<Pharmacy>> {
        coroutineScope.launch {
            fetchAndStorePharmaciesByCity(cityId)
        }

        return onCallPharmacyQueries.selectPharmacyByCity(
            city_id = cityId.toLong()
        )
            .asFlow()
            .mapToList()
            .mapToPharmacy()
            .apply { if(center != null) sortByDistance(center) }
            .asCommonFlow()
    }

    fun getPharmaciesByLocation(
        center: Coordinates,
        bounds: LocationBounds?
    ): CommonFlow<List<Pharmacy>> {
        // Check whether database has information about this block.
        // If there is no information or outdated information, go ahead and fetch it from API
        // If there is information in database pass it first.

        val allCoordinates = mutableSetOf(
            center,
            bounds?.northEast,
            bounds?.southWest,
            bounds?.northWest,
            bounds?.southEast
        )
            .filterNotNull()
            .distinctBy { it.locationBlock }

        allCoordinates.forEach { location ->
            val isBlockFetched = onCallPharmacyQueries
                .selectBlockCache(location.locationBlock)
                .executeAsOneOrNull() != null

            if (!isBlockFetched) {
                coroutineScope.launch {
                    val locationPharmacyListResponse = pharmacyApi
                        .fetchPharmaciesByLocation(
                            lat = location.latitude,
                            lng = location.longitude
                        )

                    onCallPharmacyQueries.insertBlockCache(location.locationBlock)

                    locationPharmacyListResponse.list.forEach {
                        onCallPharmacyQueries.insertPharmacy(
                            name = it.name,
                            city_id = it.cityId.toLong(),
                            longitude = it.longitude,
                            latitude = it.latitude,
                            address = it.address,
                            notes = it.notes,
                            phone = it.phone,
                            block = getLocationBlock(it.latitude, it.longitude)
                        )
                    }
                }
            }
        }

        return onCallPharmacyQueries.selectPharmacyByBlock(
            allCoordinates.map { it.locationBlock }
        )
            .asFlow()
            .mapToList()
            .mapToPharmacy()
            .sortByDistance(center)
            .asCommonFlow()
    }

    private suspend fun fetchAndStorePharmaciesDefaultCities() {
        fetchAndStorePharmaciesByCity(6) // Ankara
        fetchAndStorePharmaciesByCity(34) // Istanbul
        fetchAndStorePharmaciesByCity(35) // Izmir
    }

    private suspend fun fetchAndStorePharmaciesByCity(cityId: Int) {
        logger.d { "fetchAndStorePharmaciesByCity $cityId" }
        val result = pharmacyApi.fetchPharmaciesByCity(cityId)

        // this is very basic implementation for now that removes all existing rows
        // in db and then inserts results from api request
        onCallPharmacyQueries.deletePharmacyByCity(cityId.toLong())
        result.list.forEach {
            onCallPharmacyQueries.insertPharmacy(
                name = it.name,
                city_id = cityId.toLong(),
                longitude = it.longitude,
                latitude = it.latitude,
                address = it.address,
                notes = it.notes,
                phone = it.phone,
                block = getLocationBlock(it.latitude, it.longitude)
            )
        }
    }

    fun getCities(): CommonFlow<List<City>> {
        return onCallPharmacyQueries.selectAllCity()
            .asFlow()
            .mapToList()
            .mapToCity()
            .asCommonFlow()
    }
}

fun Flow<List<Pharmacy>>.sortByDistance(center: Coordinates): Flow<List<Pharmacy>> = map {
    it.sortedBy { pharmacy ->
        distance(
            lat1 = center.latitude,
            lat2 = pharmacy.latitude,
            lon1 = center.longitude,
            lon2 = pharmacy.longitude,
            el1 = .0,
            el2 = .0
        )
    }
}

