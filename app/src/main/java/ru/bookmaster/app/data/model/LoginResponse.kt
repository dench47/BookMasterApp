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
    val createdAt: String?
)