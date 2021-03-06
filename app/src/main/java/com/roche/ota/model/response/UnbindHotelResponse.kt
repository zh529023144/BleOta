package com.roche.ota.model.response

data class UnbindHotelResponse(
    val code: Int,
    val message: String,
    val result: UnbindHotelResult,
    val success: Boolean,
    val timestamp: Long
)

data class UnbindHotelResult(
    val blueToothId: String,
    val btVersion: String,
    val devId: String,
    val hotelManager: String,
    val hotelName: String,
    val initBtCode: String,
    val lastPartner: String,
    val notUpgradeDevCount: Int,
    val prohibitDevCount: Int,
    val roomCode: String,
    val status: Int,
    val sureUpgradeDevCount: Int,
    val totalDevCount: Int,
    val upgradeDevCount: Int
)