package ru.bookmaster.app.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String?,
    val phone: String?,
    val address: String?,
    val type: String? = "salon"
)