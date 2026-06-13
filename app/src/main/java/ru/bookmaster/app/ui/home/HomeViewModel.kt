package ru.bookmaster.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate

data class HomeUiState(
    val companyName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    // Блок "Сегодня"
    val todayDate: String = "",
    val todayAppointments: Int = 0,
    val todayRevenue: String = "0",
    // Блок "Неделя"
    val weekStats: List<WeekDayStat> = emptyList(),
    // Блок "Клиенты"
    val totalClients: Int = 0,
    val newClientsThisMonth: Int = 0,
    val sleepingClients: Int = 0,
    // Блок "Сотрудники"
    val totalMasters: Int = 0,
    val activeMasters: Int = 0,
    // Premium
    val isPremium: Boolean = false,
    val webBookingUrl: String = "",
    val isMaster: Boolean = false


)

data class WeekDayStat(
    val dayOfWeek: String,
    val dayOfWeekShort: String,
    val date: String,
    val appointments: Int,
    val revenue: String
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val prefs = application.getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val companyType = tokenManager.companyType.first() ?: "salon"
                val companyName = tokenManager.companyName.first() ?: ""
                val isPremium = prefs.getBoolean("is_premium", false)

                // Параллельно загружаем все данные
                val todayDeferred = async { api.getTodayStats(companyId, "Bearer $token") }
                val weekDeferred = async { api.getWeekStats(companyId, getWeekStart(), "Bearer $token") }
                val clientsStatsDeferred = async { api.getClientsStats(companyId, "Bearer $token") }
                val mastersStatsDeferred = async { api.getMastersStats(companyId, "Bearer $token") }

                val todayResponse = todayDeferred.await()
                val weekResponse = weekDeferred.await()
                val clientsStatsResponse = clientsStatsDeferred.await()
                val mastersStatsResponse = mastersStatsDeferred.await()

                val todayData = if (todayResponse.isSuccessful) todayResponse.body() else emptyMap()
                val weekData = if (weekResponse.isSuccessful) weekResponse.body() else emptyList<Map<String, Any>>()
                val clientsData = if (clientsStatsResponse.isSuccessful) clientsStatsResponse.body() else emptyMap()
                val mastersData = if (mastersStatsResponse.isSuccessful) mastersStatsResponse.body() else emptyMap()

                _uiState.value = HomeUiState(
                    companyName = companyName,
                    isLoading = false,
                    todayDate = todayData?.get("dayOfWeek") as? String ?: "",
                    todayAppointments = (todayData?.get("totalAppointments") as? Number)?.toInt() ?: 0,
                    todayRevenue = formatRevenue((todayData?.get("totalRevenue") as? Number)?.toDouble() ?: 0.0),
                    weekStats = parseWeekStats(weekData),
                    totalClients = (clientsData?.get("totalClients") as? Number)?.toInt() ?: 0,
                    newClientsThisMonth = (clientsData?.get("newClientsThisMonth") as? Number)?.toInt() ?: 0,
                    sleepingClients = (clientsData?.get("sleepingClients") as? Number)?.toInt() ?: 0,
                    totalMasters = (mastersData?.get("totalMasters") as? Number)?.toInt() ?: 0,
                    activeMasters = (mastersData?.get("activeMasters") as? Number)?.toInt() ?: 0,
                    isPremium = isPremium,
                    webBookingUrl = "http://your-server.com/salon/$companyId",
                    isMaster = companyType == "master"

                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Ошибка загрузки"
                )
            }
        }
    }

    private fun getWeekStart(): String {
        val today = LocalDate.now()
        val monday = today.with(java.time.DayOfWeek.MONDAY)
        return monday.toString()
    }

    private fun parseWeekStats(data: List<Map<String, Any>>?): List<WeekDayStat> {
        if (data == null) return emptyList()
        return data.mapNotNull { day ->
            try {
                WeekDayStat(
                    dayOfWeek = day["dayOfWeek"] as? String ?: "",
                    dayOfWeekShort = day["dayOfWeekShort"] as? String ?: "",
                    date = day["date"] as? String ?: "",
                    appointments = (day["appointments"] as? Number)?.toInt() ?: 0,
                    revenue = formatRevenue((day["revenue"] as? Number)?.toDouble() ?: 0.0)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun formatRevenue(amount: Double): String {
        return String.format("%.0f ₽", amount)
    }

    fun refresh() {
        loadAllData()
    }
}