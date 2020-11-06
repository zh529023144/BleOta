package com.roche.ota.model.response

data class BleUpdateConfigResponse(
    val code: Int,
    val message: String,
    val result: BleUpdateConfigResult,
    val success: Boolean,
    val timestamp: Long
)

data class BleUpdateConfigResult(
    val createBy: String,
    val createTime: String,
    val delFlag: Int,
    val id: String,
    val installPackage: String,
    val model: String,
    val remark: String,
    val status: Int,
    val updateBy: String,
    val updateTime: String,
    val version: String,
    val versionRange: List<String>
)