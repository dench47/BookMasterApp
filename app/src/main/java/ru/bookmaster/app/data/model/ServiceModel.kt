package ru.bookmaster.app.data.model

data class ServiceModel(
    val id: Long,
    val name: String,
    val description: String? = null,
    val price: Double,
    val durationMinutes: Int,
    val active: Boolean = true
)