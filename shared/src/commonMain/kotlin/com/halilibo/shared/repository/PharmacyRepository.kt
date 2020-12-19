package com.halilibo.shared.repository

import co.touchlab.kermit.Kermit
import com.halilibo.shared.model.Pharmacy
import com.halilibo.shared.remote.City
import com.halilibo.shared.remote.PharmacyApi
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class PharmacyRepository() : KoinComponent {
    private val pharmacyApi: PharmacyApi by inject()
    private val logger: Kermit by inject()

    private val coroutineScope: CoroutineScope = MainScope()
    private val onCallPharmacyDatabase = createDb()
    private val onCallPharmacyQueries = onCallPharmacyDatabase.onCallPharmacyQueries

    var peopleJob: Job? = null
    var issPositionJob: Job? = null

    init {
        coroutineScope.launch {
            fetchAndStorePharmaciesDefaultCities()
        }
    }

    // TODO: Order pharmacies by distance

    fun getPharmaciesByCityAsFlow(cityId: Int): Flow<List<Pharmacy>> {
        coroutineScope.launch {
            fetchAndStorePharmacies(cityId)
        }

        return onCallPharmacyQueries.selectPharmacyByCity(
            city_id = cityId.toLong(),
            mapper = { id: Long,
                       city_id: Long,
                       city_name: String,
                       name: String,
                       address: String,
                       longitude: String?,
                       latitude: String?,
                       notes: String?,
                       phone: String? ->
                Pharmacy(
                    name = name,
                    address = address,
                    longitude = longitude ?: "",
                    latitude = latitude ?: "",
                    notes = notes ?: "",
                    phone = phone ?: "",
                    city = City(id = city_id.toInt(), name = city_name)
                )
            })
            .asFlow()
            .mapToList()
    }

    private suspend fun fetchAndStorePharmaciesDefaultCities() {
        fetchAndStorePharmacies(6) // Ankara
        fetchAndStorePharmacies(34) // Istanbul
        fetchAndStorePharmacies(35) // Izmir
    }

    private suspend fun fetchAndStorePharmacies(cityId: Int)  {
        logger.d { "fetchAndStorePeople" }
        val result = pharmacyApi.fetchPharmaciesByCity(cityId)

        // this is very basic implementation for now that removes all existing rows
        // in db and then inserts results from api request
        onCallPharmacyQueries.deletePharmacyByCity(cityId.toLong())
        result.list.forEach {
            onCallPharmacyQueries.insertPharmacy(
                name = it.name,
                city_id = cityId.toLong(),
                city_name = it.cityName,
                longitude = it.longitude,
                latitude = it.latitude,
                address = it.address,
                notes = it.notes,
                phone = it.phone
            )
        }
    }

    fun getCitiesAsFlow(): Flow<List<City>> {
        return flow {
            val cities = getCities()
            emit(cities)
        }
    }

    private suspend fun getCities() = onCallPharmacyQueries.selectAllCity(mapper = { id, name ->
        City(id.toInt(), name)
    }).executeAsList()
//
//    // Used by web client atm
    private suspend fun fetchPharmaciesByCity(cityId: Int) = pharmacyApi.fetchPharmaciesByCity(cityId)
//
//    fun getPersonBio(personName: String): String {
//        return personBios[personName] ?: ""
//    }
//
//    fun getPersonImage(personName: String): String {
//        return personImages[personName] ?: ""
//    }
//
//    // called from Kotlin/Native clients
//    fun startObservingPeopleUpdates(success: (List<Assignment>) -> Unit) {
//        logger.d { "startObservingPeopleUpdates" }
//        peopleJob = coroutineScope.launch {
//            fetchPeopleAsFlow().collect {
//                success(it)
//            }
//        }
//    }
//
//    fun stopObservingPeopleUpdates() {
//        logger.d { "stopObservingPeopleUpdates, peopleJob = $peopleJob" }
//        peopleJob?.cancel()
//    }
//
//
//    fun startObservingISSPosition(success: (IssPosition) -> Unit) {
//        logger.d { "startObservingISSPosition" }
//        issPositionJob = coroutineScope.launch {
//            pollISSPosition().collect {
//                success(it)
//            }
//        }
//    }
//
//    fun stopObservingISSPosition() {
//        logger.d { "stopObservingISSPosition, peopleJob = $issPositionJob" }
//        issPositionJob?.cancel()
//    }
//
//
//    fun pollISSPosition(): Flow<IssPosition> = flow {
//        while (true) {
//            val position = peopleInSpaceApi.fetchISSPosition().iss_position
//            emit(position)
//            logger.d("PeopleInSpaceRepository") { position.toString() }
//            delay(POLL_INTERVAL)
//        }
//    }

    companion object {
        private const val POLL_INTERVAL = 10000L
    }
}

