package com.roche.ota.model.response

data class UserResponseX(
    val code: Int,
    val message: String,
    val result: Result,
    val success: Boolean,
    val timestamp: Long
)