package ru.bookmaster.app.ui.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager
import java.net.HttpURLConnection
import java.net.URL
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
    val isMaster: Boolean = false,
    val isServerError: Boolean = false,
    val serverErrorMessage: String? = null
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

    private var retryJob: Job? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAllData()
        startRetryOnConnection()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isServerError = false)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val companyType = tokenManager.companyType.first() ?: "salon"
                val companyName = tokenManager.companyName.first() ?: ""
                val isPremium = prefs.getBoolean("is_premium", false)

                // Параллельно загружаем все данные с обработкой ошибок внутри каждого запроса
                val todayDeferred = async {
                    try {
                        api.getTodayStats(companyId, "Bearer $token")
                    } catch (e: Exception) {
                        null
                    }
                }
                val weekDeferred = async {
                    try {
                        api.getWeekStats(companyId, getWeekStart(), "Bearer $token")
                    } catch (e: Exception) {
                        null
                    }
                }
                val clientsStatsDeferred = async {
                    try {
                        api.getClientsStats(companyId, "Bearer $token")
                    } catch (e: Exception) {
                        null
                    }
                }
                val mastersStatsDeferred = async {
                    try {
                        api.getMastersStats(companyId, "Bearer $token")
                    } catch (e: Exception) {
                        null
                    }
                }

                val todayResponse = todayDeferred.await()
                val weekResponse = weekDeferred.await()
                val clientsStatsResponse = clientsStatsDeferred.await()
                val mastersStatsResponse = mastersStatsDeferred.await()

                // Если все запросы вернули null - сервер недоступен
                if (todayResponse == null && weekResponse == null && clientsStatsResponse == null && mastersStatsResponse == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isServerError = true,
                        serverErrorMessage = "Сервер недоступен. Проверьте подключение к интернету."
                    )
                    return@launch
                }

                val todayData = todayResponse?.body() ?: emptyMap()
                val weekData = weekResponse?.body() ?: emptyList<Map<String, Any>>()
                val clientsData = clientsStatsResponse?.body() ?: emptyMap()
                val mastersData = mastersStatsResponse?.body() ?: emptyMap()

                _uiState.value = HomeUiState(
                    companyName = companyName,
                    isLoading = false,
                    todayDate = todayData["dayOfWeek"] as? String ?: "",
                    todayAppointments = (todayData["totalAppointments"] as? Number)?.toInt() ?: 0,
                    todayRevenue = formatRevenue((todayData["totalRevenue"] as? Number)?.toDouble() ?: 0.0),
                    weekStats = parseWeekStats(weekData),
                    totalClients = (clientsData["totalClients"] as? Number)?.toInt() ?: 0,
                    newClientsThisMonth = (clientsData["newClientsThisMonth"] as? Number)?.toInt() ?: 0,
                    sleepingClients = (clientsData["sleepingClients"] as? Number)?.toInt() ?: 0,
                    totalMasters = (mastersData["totalMasters"] as? Number)?.toInt() ?: 0,
                    activeMasters = (mastersData["activeMasters"] as? Number)?.toInt() ?: 0,
                    isPremium = isPremium,
                    webBookingUrl = "http://your-server.com/salon/$companyId",
                    isMaster = companyType == "master"
                )
            } catch (e: java.net.SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Сервер не отвечает. Проверьте подключение."
                )
            } catch (e: java.net.ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Нет связи с сервером. Проверьте интернет."
                )
            } catch (e: java.net.UnknownHostException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Не удалось найти сервер. Проверьте подключение."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Ошибка соединения. Попробуйте позже."
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

    @SuppressLint("DefaultLocale")
    private fun formatRevenue(amount: Double): String {
        return String.format("%.0f ₽", amount)
    }

    fun refresh() {
        loadAllData()
    }

    private fun startRetryOnConnection() {
        retryJob?.cancel()
        retryJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                if (!isConnected()) continue
                if (_uiState.value.isServerError) {
                    loadAllData()
                }
            }
        }
    }

    private fun isConnected(): Boolean {
        return try {
            val baseUrl = RetrofitClient.BASE_URL
            val url = URL("$baseUrl/actuator/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.connect()
            connection.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }
}