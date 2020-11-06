package com.roche.ota.model.response

data class HotelDetailResponse(
    val code: Int,
    val message: String,
    val result: HotelDetailResult,
    val success: Boolean,
    val timestamp: Long
)

data class HotelDetailResult(
    val current: Int,
    val hasNext: Boolean,
    val pages: Int,
    val records: List<HotelDetailResultRecord>,
    val size: Int,
    val total: Int
)

data class HotelDetailResultRecord(
    val blueToothId: String,
    val btVersion: String,
    val devId: String,
    val hotelId: String,
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