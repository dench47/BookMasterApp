package ru.bookmaster.app.data.model

data class AppointmentResponse(
    val id: Long,
    val clientName: String,
    val clientPhone: String,
    val masterName: String,
    val serviceName: String,
    val startTime: String,
    val endTime: String,
    val confirmed: Boolean? = false,
    val cancelled: Boolean? = false,
    val salonNotified: Boolean? = false,
    val salonName: String? = null,
    val masterId: Long? = null,
    val serviceId: Long? = null,
    val cancellationReason: String? = null,
    val completed: Boolean? = false
)
