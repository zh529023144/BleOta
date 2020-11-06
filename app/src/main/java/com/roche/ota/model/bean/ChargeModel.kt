package com.roche.ota.model.bean

/**
 * 创建日期：2020/1/8 on 17:05
 * 描述:
 * 作者:张伦欢
 */
data class ChargeModel(
    val id: Int,
    val name: String


) {
    override fun toString(): String {
        return name
    }
}