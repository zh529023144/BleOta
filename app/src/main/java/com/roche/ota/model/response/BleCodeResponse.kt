package com.roche.ota.model.response

data class BleCodeResponse(
    val code: Int,
    val message: String,
    val result: BleCodeResult,
    val success: Boolean,
    val timestamp: Long
)

data class BleCodeResult(
    val origin: String,
    val action: Int,
    val awakenVoltage: Int,
    val error: Int,
    val func: String,
    val instruction: String,
    val keyStatus: String,
    val patch: String,
    val rollCode: Int,
    val subVersion: String,
    val version: String,
    val voltage: Int
)