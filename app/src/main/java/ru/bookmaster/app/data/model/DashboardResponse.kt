package ru.bookmaster.app.data.model

data class DashboardResponse(
    val todayDate: String = "",
    val todayDayOfWeek: String = "",
    val todayAppointments: Int = 0,
    val todayConfirmedAppointments: Int = 0,
    val todayActualAppointments: Int = 0,
    val todayPendingAppointments: Int = 0,
    val todayRevenue: Number? = null,
    val todayActualRevenue: Number? = null,
    val weekStats: List<Map<String, Any>>? = null,
    val totalClients: Int = 0,
    val newClientsThisMonth: Int = 0,
    val sleepingClients: Int = 0,
    val totalMasters: Int = 0,
    val activeMasters: Int = 0,
    val pendingAppointments: List<AppointmentResponse>? = null,
    val cancelledAppointments: List<AppointmentResponse>? = null,
    val masters: List<Map<String, Any>>? = null,
    val todayNetProfit: Number? = null,
    val monthNetProfit: Number? = null
)
