package ru.bookmaster.app.ui.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.DashboardResponse
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.BookMasterMessagingService
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate
import androidx.core.content.edit

data class HomeUiState(
    val companyName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val todayDate: String = "",
    val todayAppointments: Int = 0,
    val todayConfirmedAppointments: Int = 0,
    val todayRevenue: String = "0",
    val todayActualAppointments: Int = 0,
    val todayActualRevenue: String = "0",
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
    val cancelledAppointments: List<AppointmentResponse> = emptyList(),
    val waitingListEntries: List<Map<String, Any>> = emptyList(),
    val newEventsCount: Int = 0,
    val cancelledByClientCount: Int = 0,
    val waitingListCount: Int = 0,
    val totalEventsCount: Int = 0,
    val isPendingSheetVisible: Boolean = false,
    val masters: List<Master> = emptyList(),
    val showPlanned: Boolean = false,
    val isRevenueVisible: Boolean = true
)

data class WeekDayStat(
    val dayOfWeek: String,
    val dayOfWeekShort: String,
    val date: String,
    val appointments: Int,
    val revenue: String,
    val actualAppointments: Int = 0,
    val actualRevenue: String = "0 ₽"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val premiumPrefs = application.getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)
    private val appPrefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            BookMasterMessagingService.KEY_NEW_EVENTS_COUNT -> loadAllData()
            BookMasterMessagingService.KEY_CANCELLED_COUNT -> {
                val count = appPrefs.getInt(BookMasterMessagingService.KEY_CANCELLED_COUNT, 0)
                _uiState.value = _uiState.value.copy(
                    cancelledByClientCount = count,
                    totalEventsCount = _uiState.value.newEventsCount + count
                )
            }
        }
    }

    init {
        appPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        loadAllData()
    }

    override fun onCleared() {
        super.onCleared()
        appPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    fun toggleRevenueMode() {
        _uiState.value = _uiState.value.copy(showPlanned = !_uiState.value.showPlanned)
    }

    fun toggleRevenueVisibility() {
        _uiState.value = _uiState.value.copy(isRevenueVisible = !_uiState.value.isRevenueVisible)
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

                // ЕДИНСТВЕННЫЙ ЗАПРОС вместо 7
                val dashboardResponse = try {
                    api.getDashboard(companyId, "Bearer $token")
                } catch (_: Exception) { null }

                if (dashboardResponse == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isServerError = true,
                        serverErrorMessage = "Сервер недоступен. Проверьте подключение к интернету."
                    )
                    return@launch
                }

                val dashboard = dashboardResponse.body()

                if (dashboard == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isServerError = true,
                        serverErrorMessage = "Сервер недоступен. Проверьте подключение к интернету."
                    )
                    return@launch
                }

                val pendingList = dashboard.pendingAppointments ?: emptyList()
                val cancelledList = dashboard.cancelledAppointments ?: emptyList()
                val waitingList = dashboard.waitingListEntries ?: emptyList()
                val cancelledCount = appPrefs.getInt(BookMasterMessagingService.KEY_CANCELLED_COUNT, 0)
                val waitingListCount = waitingList.size
                val newEventsCount = pendingList.size + waitingListCount

                // Мастеров преобразуем в модель Master
                val mastersList = dashboard.masters?.mapNotNull { masterMap ->
                    try {
                        Master(
                            id = (masterMap["id"] as? Number)?.toLong() ?: return@mapNotNull null,
                            name = masterMap["name"] as? String ?: "",
                            active = (masterMap["active"] as? Boolean) ?: true
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()

                _uiState.value = HomeUiState(
                    companyName = companyName,
                    isLoading = false,
                    todayDate = dashboard.todayDayOfWeek,
                    todayAppointments = dashboard.todayAppointments,
                    todayConfirmedAppointments = dashboard.todayConfirmedAppointments,
                    todayRevenue = formatRevenue(dashboard.todayRevenue?.toDouble() ?: 0.0),
                    todayActualAppointments = dashboard.todayActualAppointments,
                    todayActualRevenue = formatRevenue(dashboard.todayActualRevenue?.toDouble() ?: 0.0),
                    weekStats = parseWeekStats(dashboard.weekStats),
                    totalClients = dashboard.totalClients,
                    newClientsThisMonth = dashboard.newClientsThisMonth,
                    sleepingClients = dashboard.sleepingClients,
                    totalMasters = dashboard.totalMasters,
                    activeMasters = dashboard.activeMasters,
                    isPremium = isPremium,
                    webBookingUrl = "http://your-server.com/salon/$companyId",
                    isMaster = companyType == "master",
                    pendingAppointments = pendingList,
                    cancelledAppointments = cancelledList,
                    waitingListEntries = waitingList,
                    newEventsCount = newEventsCount,
                    cancelledByClientCount = cancelledCount,
                    waitingListCount = waitingListCount,
                    totalEventsCount = newEventsCount + cancelledCount,
                    isPendingSheetVisible = _uiState.value.isPendingSheetVisible,
                    masters = mastersList,
                    showPlanned = _uiState.value.showPlanned,
                    isRevenueVisible = _uiState.value.isRevenueVisible
                )
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Сервер не отвечает. Проверьте подключение."
                )
            } catch (_: java.net.ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isServerError = true,
                    serverErrorMessage = "Нет связи с сервером. Проверьте интернет."
                )
            } catch (_: java.net.UnknownHostException) {
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
        loadEventsData()
    }

    private fun loadEventsData() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: return@launch

                // Единый dashboard-запрос для событий
                val dashboardResponse = try {
                    api.getDashboard(companyId, "Bearer $token")
                } catch (_: Exception) { null }

                val dashboard = dashboardResponse?.body()

                val pendingList = dashboard?.pendingAppointments ?: _uiState.value.pendingAppointments
                val cancelledList = dashboard?.cancelledAppointments ?: _uiState.value.cancelledAppointments
                val cancelledCount = appPrefs.getInt(BookMasterMessagingService.KEY_CANCELLED_COUNT, 0)

                _uiState.value = _uiState.value.copy(
                    pendingAppointments = pendingList,
                    cancelledAppointments = cancelledList,
                    newEventsCount = pendingList.size,
                    cancelledByClientCount = cancelledCount,
                    totalEventsCount = pendingList.size + cancelledCount
                )
            } catch (_: Exception) { }
        }
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
                loadEventsData()
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
                loadEventsData()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }


    fun checkAndShowPendingFromNotification() {
        val showSheet = appPrefs.getBoolean("show_pending_sheet", false)

        if (showSheet) {
            appPrefs.edit { putBoolean("show_pending_sheet", false) }
            appPrefs.edit { putInt(BookMasterMessagingService.KEY_NEW_EVENTS_COUNT, 0) }

            viewModelScope.launch {
                loadAllData()
                while (_uiState.value.isLoading) {
                    kotlinx.coroutines.delay(50)
                }
                _uiState.value = _uiState.value.copy(
                    isPendingSheetVisible = true,
                    cancelledByClientCount = 0,
                    totalEventsCount = _uiState.value.newEventsCount
                )
            }
        }
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
                    revenue = formatRevenue((day["revenue"] as? Number)?.toDouble() ?: 0.0),
                    actualAppointments = (day["completed"] as? Number)?.toInt() ?: 0,
                    actualRevenue = formatRevenue((day["actualRevenue"] as? Number)?.toDouble() ?: 0.0)
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

    fun dismissCancelledAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.markAppointmentViewed(appointmentId, "Bearer $token")

                val updatedCancelled = _uiState.value.cancelledAppointments.filter { it.id != appointmentId }
                // Синхронизируем SharedPreferences с реальным состоянием
                appPrefs.edit { putInt(BookMasterMessagingService.KEY_CANCELLED_COUNT, updatedCancelled.size) }

                _uiState.value = _uiState.value.copy(
                    cancelledAppointments = updatedCancelled,
                    cancelledByClientCount = updatedCancelled.size,
                    totalEventsCount = _uiState.value.newEventsCount + updatedCancelled.size
                )
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun dismissWaitingListEntry(entryId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.markWaitingListViewed(entryId, "Bearer $token")

                val updatedWaiting = _uiState.value.waitingListEntries.filter {
                    (it["id"] as? Number)?.toLong() != entryId
                }
                val waitingCount = updatedWaiting.size
                val newEventsCount = _uiState.value.pendingAppointments.size + waitingCount

                _uiState.value = _uiState.value.copy(
                    waitingListEntries = updatedWaiting,
                    waitingListCount = waitingCount,
                    newEventsCount = newEventsCount,
                    totalEventsCount = newEventsCount + _uiState.value.cancelledByClientCount
                )
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
