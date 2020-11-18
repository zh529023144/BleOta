package com.roche.ota.model.response

data class HotelModelResponse(
    val code: Int,
    val message: String,
    val result: List<HotelModelResult>,
    val success: Boolean,
    val timestamp: Long
)

data class HotelModelResult(
    val blueToothId: Any,
    val btVersion: Any,
    val devId: Any,
    val hotelId: String,
    val hotelManager: Any,
    val hotelName: String,
    val initBtCode: Any,
    val lastPartner: Any,
    val model: String,
    val notUpgradeDevCount: Int,
    val prohibitDevCount: Int,
    val roomCode: Any,
    val status: Any,
    val sureUpgradeDevCount: Any,
    val totalDevCount: Int,
    val upgradeDevCount: Int
)