package ru.bookmaster.app.data.model

import com.google.gson.annotations.SerializedName

data class AppointmentResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("clientName")
    val clientName: String? = null,

    @SerializedName("clientPhone")
    val clientPhone: String? = null,

    @SerializedName("masterName")
    val masterName: String? = null,

    @SerializedName("serviceName")
    val serviceName: String? = null,

    @SerializedName("startTime")
    val startTime: String? = null,

    @SerializedName("endTime")
    val endTime: String? = null,

    @SerializedName("confirmed")
    val confirmed: Boolean? = false,

    @SerializedName("cancelled")
    val cancelled: Boolean? = false,

    @SerializedName("completed")
    val completed: Boolean? = false,

    @SerializedName("salonNotified")
    val salonNotified: Boolean? = false,

    @SerializedName("salonName")
    val salonName: String? = null,

    @SerializedName("masterId")
    val masterId: Long? = null,

    @SerializedName("serviceId")
    val serviceId: Long? = null,

    @SerializedName("cancellationReason")
    val cancellationReason: String? = null,

    @SerializedName("price")
    val price: Double? = null
)