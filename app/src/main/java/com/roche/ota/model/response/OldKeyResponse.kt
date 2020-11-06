package com.roche.ota.model.response

data class OldKeyResponse(
    val code: Int,
    val message: String,
    val result: KeyResult,
    val success: Boolean,
    val timestamp: Long
)

data class KeyResult(
    val blueToothId: Any,
    val createBy: Any,
    val createTime: Any,
    val delFlag: Int,
    val devId: String,
    val hisKey: Any,
    val id: String,
    val model: Any,
    val newKey: Any,
    val oldKey: String,
    val rawKey: Any,
    val softVersion: Any,
    val tempOld: Any,
    val tempRaw: Any,
    val updateBy: Any,
    val updateTime: Any
)