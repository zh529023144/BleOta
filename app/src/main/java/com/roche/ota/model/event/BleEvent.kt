package com.roche.ota.model.event

data class BleEvent(
    val code: Int,
    val message: String,
    val result: String,
    val type:String,
    val hex: String=""
)