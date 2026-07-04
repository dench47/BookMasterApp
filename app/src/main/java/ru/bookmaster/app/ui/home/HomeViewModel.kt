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
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.BookMasterMessagingService
import ru.bookmaster.app.util.TokenManager
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

data class HomeUiState(
    val companyName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val todayDate: String = "",
    val todayAppointments: Int = 0,
    val todayRevenue: String = "0",
    val weekStats: List<WeekDayStat> = emptyList(),
    val totalClients: Int = 0,
    val newClientsThisMonth: Int = 0,
    val sleepingClients: Int = 0,
    val totalMasters: Int = 0,
    val activeMasters: Int = 0,
    val isPremium: Boolean = false,
    val webBookingUrl: String = "",
    val isMaster: Boolean = false,
    val isServerError: Boolean = false,
    val serverErrorMessage: String? = null,
    val pendingAppointments: List<AppointmentResponse> = emptyList(),
    val newEventsCount: Int = 0,
    val isPendingSheetVisible: Boolean = false,
    val masters: List<Master> = emptyList()
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
    private val premiumPrefs = application.getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)
    private val appPrefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    private var retryJob: Job? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == BookMasterMessagingService.KEY_NEW_EVENTS_COUNT) {
            loadAllData()
        }
    }

    init {
        appPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        loadAllData()
        startRetryOnConnection()
    }

    override fun onCleared() {
        super.onCleared()
        appPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isServerError = false)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val companyType = tokenManager.companyType.first() ?: "salon"
                val companyName = tokenManager.companyName.first() ?: ""
                val isPremium = premiumPrefs.getBoolean("is_premium", false)

                val todayDeferred = async {
                    try { api.getTodayStats(companyId, "Bearer $token") } catch (e: Exception) { null }
                }
                val weekDeferred = async {
                    try { api.getWeekStats(companyId, getWeekStart(), "Bearer $token") } catch (e: Exception) { null }
                }
                val clientsStatsDeferred = async {
                    try { api.getClientsStats(companyId, "Bearer $token") } catch (e: Exception) { null }
                }
                val mastersStatsDeferred = async {
                    try { api.getMastersStats(companyId, "Bearer $token") } catch (e: Exception) { null }
                }
                val pendingDeferred = async {
                    try { api.getPendingAppointments(companyId, "Bearer $token") } catch (e: Exception) { null }
                }
                val mastersDeferred = async {
                    try { api.getMasters(companyId, "Bearer $token") } catch (e: Exception) { null }
                }

                val todayResponse = todayDeferred.await()
                val weekResponse = weekDeferred.await()
                val clientsStatsResponse = clientsStatsDeferred.await()
                val mastersStatsResponse = mastersStatsDeferred.await()
                val pendingResponse = pendingDeferred.await()
                val mastersResponse = mastersDeferred.await()

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
                val mastersList = mastersResponse?.body() ?: emptyList()

                val pendingList = pendingResponse?.body() ?: emptyList()
                val newEventsCount = pendingList.size

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
                    isMaster = companyType == "master",
                    pendingAppointments = pendingList,
                    newEventsCount = newEventsCount,
                    isPendingSheetVisible = _uiState.value.isPendingSheetVisible,
                    masters = mastersList
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

    fun showPendingSheet() {
        _uiState.value = _uiState.value.copy(isPendingSheetVisible = true)
    }

    fun hidePendingSheet() {
        _uiState.value = _uiState.value.copy(isPendingSheetVisible = false)
    }

    fun confirmAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.confirmAppointment(appointmentId, "Bearer $token")
                loadAllData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.cancelAppointment(appointmentId, "Bearer $token")
                loadAllData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun editAppointment(appointmentId: Long, masterId: Long?, startTime: String?) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val body = mutableMapOf<String, Any>()
                if (masterId != null) body["masterId"] = masterId
                if (startTime != null) body["startTime"] = startTime
                if (body.isNotEmpty()) {
                    api.editAppointment(appointmentId, body, "Bearer $token")
                }
                loadAllData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun markAllViewed() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                api.markAppointmentsViewed(companyId, "Bearer $token")
                loadAllData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun checkAndShowPendingFromNotification() {
        val hasNew = appPrefs.getBoolean("has_new_appointment", false)

        if (hasNew) {
            appPrefs.edit().putBoolean("has_new_appointment", false).apply()
            appPrefs.edit().putInt(BookMasterMessagingService.KEY_NEW_EVENTS_COUNT, 0).apply()

            viewModelScope.launch {
                loadAllData()
                while (_uiState.value.isLoading) {
                    kotlinx.coroutines.delay(50)
                }
                _uiState.value = _uiState.value.copy(isPendingSheetVisible = true)
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
            } catch (_: Exception) { null }
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
        } catch (e: Exception) { false }
    }
}