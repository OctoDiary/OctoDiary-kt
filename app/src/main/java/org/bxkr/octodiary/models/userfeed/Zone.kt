package org.bxkr.octodiary.models.userfeed

data class Zone(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val radius: Int,
    val zoneType: String
)