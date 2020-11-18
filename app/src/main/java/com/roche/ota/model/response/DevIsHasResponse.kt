package com.roche.ota.model.response

data class DevIsHasResponse(
    val code: Int,
    val message: String,
    val result: DevIsHasResult,
    val success: Boolean,
    val timestamp: Long
)

data class DevIsHasResult(
    val model: String,
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
    val upgradeDevCount: Int,


    val hasPwd:Int,
    val weigh:Int,
    val electricityType:Int,
    val connectBluetooth:Int,
    val charge:Int,
    val connectNetwork:Int,
    val cellNum:Int


)