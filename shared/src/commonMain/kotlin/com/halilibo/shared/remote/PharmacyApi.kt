package com.halilibo.shared.remote

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.KoinComponent

@Serializable
internal data class CityPharmacyListResponse(val list: List<PharmacyDto>)

@Serializable
internal data class LocationPharmacyListResponse(val list: List<PharmacyLocationDto>)

@Serializable
internal data class PharmacyDto(
    val address: String,
    val name: String,
    val notes: String,
    val longitude: Double,
    val latitude: Double,
    val phone: String,
    @SerialName("city_name") val cityName: String
)

@Serializable
internal data class PharmacyLocationDto(
    val address: String,
    val name: String,
    val notes: String,
    val longitude: Double,
    val latitude: Double,
    val phone: String,
    @SerialName("city_id") val cityId: Int,
    @SerialName("city_name") val cityName: String
)

internal class PharmacyApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://eczane.turqu.net",
) : KoinComponent {
    suspend fun fetchPharmaciesByCity(cityId: Int) = client.get<CityPharmacyListResponse>("$baseUrl/$cityId")

    suspend fun fetchPharmaciesByLocation(lat: Double, lng: Double) =
        client.get<LocationPharmacyListResponse>("$baseUrl/location?lat=$lat&lng=$lng")
}
