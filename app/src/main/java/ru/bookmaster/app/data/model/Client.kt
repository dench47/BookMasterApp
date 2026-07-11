package ru.bookmaster.app.data.model

import com.google.gson.annotations.SerializedName

// ========== Модели для списка клиентов ==========

data class Client(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("birthday")
    val birthday: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("tags")
    val tags: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("totalVisits")
    val totalVisits: Int = 0,

    @SerializedName("totalSpent")
    val totalSpent: Double = 0.0,

    @SerializedName("lastVisit")
    val lastVisit: String? = null,

    @SerializedName("cancellationCount")
    val cancellationCount: Int = 0,

    @SerializedName("daysSinceLastVisit")
    val daysSinceLastVisit: Int = 999,

    @SerializedName("loyaltyStatus")
    val loyaltyStatus: String? = null
)

data class ClientsResponse(
    @SerializedName("clients")
    val clients: List<Client>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("size")
    val size: Int = 20,

    @SerializedName("totalPages")
    val totalPages: Int = 1,

    @SerializedName("isPremium")
    val isPremium: Boolean = true,

    @SerializedName("message")
    val message: String? = null
)

// ========== Модели для детальной карточки клиента (CRM-профиль) ==========

data class ClientProfileResponse(
    // Блок 1
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("birthday") val birthday: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,

    // Блок 2
    @SerializedName("totalVisits") val totalVisits: Int = 0,
    @SerializedName("visitsThisMonth") val visitsThisMonth: Int = 0,
    @SerializedName("visitsThisYear") val visitsThisYear: Int = 0,
    @SerializedName("firstVisitDate") val firstVisitDate: String? = null,
    @SerializedName("lastVisitDate") val lastVisitDate: String? = null,
    // Используем AppointmentResponse из отдельного файла
    @SerializedName("appointments") val appointments: List<AppointmentResponse>? = null,

    // Блок 3
    @SerializedName("totalSpent") val totalSpent: Number? = null,
    @SerializedName("averageBill") val averageBill: Number? = null,
    @SerializedName("spentThisMonth") val spentThisMonth: Number? = null,
    @SerializedName("spentThisYear") val spentThisYear: Number? = null,

    // Блок 4
    @SerializedName("favoriteMasterName") val favoriteMasterName: String? = null,
    @SerializedName("favoriteServiceName") val favoriteServiceName: String? = null,
    @SerializedName("preferredTimeOfDay") val preferredTimeOfDay: String? = null,

    // Блок 5
    @SerializedName("pushEnabled") val pushEnabled: Boolean = false,
    @SerializedName("cancellationCount") val cancellationCount: Int = 0,
    @SerializedName("noShowCount") val noShowCount: Int = 0,
    @SerializedName("cancellationRate") val cancellationRate: Double = 0.0,
    @SerializedName("daysSinceLastVisit") val daysSinceLastVisit: Int = 999,
    @SerializedName("loyaltyStatus") val loyaltyStatus: String? = null
)

// ========== Модель для старого детального эндпоинта (для совместимости) ==========

data class ClientDetailResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("history") val history: List<AppointmentHistory>,
    @SerializedName("totalVisits") val totalVisits: Int,
    @SerializedName("totalSpent") val totalSpent: Double
)

data class AppointmentHistory(
    @SerializedName("id") val id: Long,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("masterName") val masterName: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("confirmed") val confirmed: Boolean,
    @SerializedName("cancelled") val cancelled: Boolean,
    @SerializedName("price") val price: Double
)