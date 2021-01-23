package com.halilibo.shared.model

data class Pharmacy(
    val address: String,
    val name: String,
    val notes: String,
    val longitude: Double,
    val latitude: Double,
    val phone: String,
    val city: City? = null
)