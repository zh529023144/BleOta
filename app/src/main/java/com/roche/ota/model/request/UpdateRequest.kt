package com.roche.ota.model.request

data class UpdateRequest(
    val blueToothId: String,
    val newVersion: String,
    val oldVersion: String,
    val status: Int,
    val msg:String
)