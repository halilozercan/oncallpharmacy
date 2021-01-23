package com.halilibo.shared.repository

import co.touchlab.kermit.Kermit
import com.halilibo.eczane.db.OnCallPharmacyDatabase
import com.halilibo.shared.CommonFlow
import com.halilibo.shared.asCommonFlow
import com.halilibo.shared.getLocationBlock
import com.halilibo.shared.local.mapToPharmacy
import com.halilibo.shared.locationBlock
import com.halilibo.shared.model.LocationBounds
import com.halilibo.shared.model.Pharmacy
import com.halilibo.shared.remote.PharmacyApi
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject


class PharmacyMapsRepository() : KoinComponent {
    private val pharmacyApi: PharmacyApi by inject()
    private val logger: Kermit by inject()

    private val coroutineScope: CoroutineScope = MainScope()
    private val onCallPharmacyDatabase: OnCallPharmacyDatabase by inject()
    private val onCallPharmacyQueries = onCallPharmacyDatabase.onCallPharmacyQueries

    private val mapProjectionSource = MutableSharedFlow<LocationBounds>(
        replay = 0,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    fun updateMapBounds(bounds: LocationBounds) {
        mapProjectionSource.tryEmit(bounds)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmaciesFlow: CommonFlow<List<Pharmacy>> = mapProjectionSource
        .map { bounds ->
            setOf(
                bounds.northEast,
                bounds.southWest,
                bounds.northWest,
                bounds.southEast
            )
                .distinctBy { it.locationBlock }
        }
        .distinctUntilChanged { oldCoordinates, newCoordinates ->
            val oldBlocks = oldCoordinates.map { it.locationBlock }
            val newBlocks = newCoordinates.map { it.locationBlock }

            if (newBlocks.isEmpty()) {
                false
            }
            else {
                newBlocks.asSequence()
                    .map { oldBlocks.contains(it) }
                    .reduce { acc, b -> acc && b }
            }
        }
        .onEach { allCoordinates ->
            allCoordinates.forEach { location ->
                val isBlockFetched = onCallPharmacyQueries
                    .selectBlockCache(location.locationBlock)
                    .executeAsOneOrNull() != null

                if (!isBlockFetched) {

                    logger.d { "Fetching from API for block ${location.locationBlock}" }

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
                } else {
                    logger.d { "Serving from DB for block ${location.locationBlock}" }
                }
            }
        }
        .flatMapLatest { allCoordinates ->
            val allBlocks = allCoordinates.map { it.locationBlock }
            onCallPharmacyQueries.selectPharmacyByBlock(allBlocks)
                .asFlow()
                .mapToList()
                .mapToPharmacy()

        }
        .asCommonFlow()
}