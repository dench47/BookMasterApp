package ru.bookmaster.app.data.model

data class LoginResponse(
    val token: String,
    val company: CompanyResponse
)

data class CompanyResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val premium: Boolean?,
    val premiumExpiry: String?,
    val type: String?,
    val maxServices: Int?,
    val maxBookingDays: Int?,
    val remindersEnabled: Boolean?,
    val createdAt: String?
)