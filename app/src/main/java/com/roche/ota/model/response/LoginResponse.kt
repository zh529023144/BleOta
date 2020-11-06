package com.roche.ota.model.response

data class LoginResponse(
    val code: Int,
    val message: String,
    val result: List<LoginResult>,
    val success: Boolean,
    val timestamp: Long
)

data class LoginResult(
    val contact: String,
    val token: String,
    val username: String
)