package ru.bookmaster.app.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Client(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("totalVisits")
    val totalVisits: Int = 0,

    @SerializedName("totalSpent")
    val totalSpent: Double = 0.0,

    @SerializedName("lastVisit")
    val lastVisit: String? = null
)

data class ClientsResponse(
    @SerializedName("clients")
    val clients: List<Client>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("size")
    val size: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("isPremium")
    val isPremium: Boolean,

    @SerializedName("message")
    val message: String? = null
)

data class ClientDetailResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("history")
    val history: List<AppointmentHistory>,

    @SerializedName("totalVisits")
    val totalVisits: Int,

    @SerializedName("totalSpent")
    val totalSpent: Double
)

data class AppointmentHistory(
    @SerializedName("id")
    val id: Long,

    @SerializedName("serviceName")
    val serviceName: String,

    @SerializedName("masterName")
    val masterName: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("confirmed")
    val confirmed: Boolean,

    @SerializedName("cancelled")
    val cancelled: Boolean,

    @SerializedName("price")
    val price: Double
)