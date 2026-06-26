package ru.bookmaster.app.data.model

data class UpdateServiceRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val durationMinutes: Int
)