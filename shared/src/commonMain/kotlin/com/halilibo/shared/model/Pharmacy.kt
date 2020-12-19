package com.halilibo.shared.model

import com.halilibo.shared.remote.City
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pharmacy(
    val address: String,
    val name: String,
    val notes: String,
    val longitude: String,
    val latitude: String,
    val phone: String,
    val city: City
)