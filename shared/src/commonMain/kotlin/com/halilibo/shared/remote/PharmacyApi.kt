package com.halilibo.shared.remote

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.KoinComponent

@Serializable
data class CityPharmacyListResponse(val list: List<PharmacyDto>)

@Serializable
data class City(
    val id: Int,
    val name: String
)

@Serializable
data class County(
    val id: Int,
    val name: String,
    val cityId: Int
)

@Serializable
data class PharmacyDto(
    val address: String,
    val name: String,
    val notes: String,
    val longitude: String,
    val latitude: String,
    val phone: String,
    @SerialName("city_name") val cityName: String
)

class PharmacyApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://eczane.turqu.net",
) : KoinComponent {
    suspend fun fetchPharmaciesByCity(cityId: Int) = client.get<CityPharmacyListResponse>("$baseUrl/$cityId")
}
