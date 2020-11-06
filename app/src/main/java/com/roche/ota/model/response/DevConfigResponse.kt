package com.roche.ota.model.response

data class DevConfigResponse(
    val code: Int,
    val message: String,
    val result: List<DevConfigResult>,
    val success: Boolean,
    val timestamp: Long
)

data class DevConfigResult(
    val cellNum: Int,
    val cellSchemeId: String,
    val charge: Int,
    val connectBluetooth: Int,
    val connectNetwork: Int,
    val createBy: String,
    val createTime: String,
    val delFlag: Int,
    val description: String,
    val hasPwd: Int,
    val id: String,
    val model: String,
    val name: String,
    val replenishType: String,
    val updateBy: String,
    val updateTime: String
)