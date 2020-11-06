package com.roche.ota.model.response

data class FactoryUserResponse(
    val code: Int,
    val message: String,
    val result: Results,
    val success: Boolean,
    val timestamp: Long
)

data class Results(
    val current: Int,
    val pages: Int,
    val records: List<Record>,
    val size: Int,
    val total: Int
)

data class Record(
    val city: Any,
    val contacts: String,
    val createBy: String,
    val createDevCount: Int,
    val createTime: String,
    val delFlag: Int,
    val factoryLine: String,
    val finish: Int,
    val id: String,
    val intoDevCount: Int,
    val model: String,
    val partnerAccount: String,
    val phone: String,
    val status: Int,
    val updateBy: String,
    val updateTime: String
) {
    override fun toString(): String {
        return "$partnerAccount-$contacts-$createDevCount"+"台"
    }
}