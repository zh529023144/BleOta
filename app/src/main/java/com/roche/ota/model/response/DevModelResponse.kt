package com.roche.ota.model.response

data class DevModelResponse(
    val code: Int,
    val message: String,
    val result: ModelResult,
    val success: Boolean,
    val timestamp: Long
)

data class ModelResult(
    val current: Int,
    val pages: Int,
    val records: List<ModelRecord>,
    val size: Int,
    val total: Int
)

data class ModelRecord(
    val cellNum: Int,
    val cellSchemeId: String,
    val charge: Int,
    val hasPwd:Int,
    val connectBluetooth: Int,
    val connectNetwork: Int,
    val createBy: String,
    val createTime: String,
    val delFlag: Int,
    val description: String,
    val id: String,
    val model: String,
    val name: String,
    val updateBy: String,
    val updateTime: String
) {
    override fun toString(): String {
        return model
    }
}