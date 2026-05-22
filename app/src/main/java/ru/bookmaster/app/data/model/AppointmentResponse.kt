package ru.bookmaster.app.data.model

data class AppointmentResponse(
    val id: Long,
    val clientName: String,
    val clientPhone: String,
    val masterName: String,
    val serviceName: String,
    val startTime: String,
    val endTime: String,
    val confirmed: Boolean?,
    val cancelled: Boolean?
)