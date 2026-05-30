package ru.bookmaster.app.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.util.TokenManager

data class HomeUiState(
    val companyName: String = "",
    val appointments: List<AppointmentResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdate: Long = 0L,
    val isPremium: Boolean = false,
    val maxServices: Int = 3,
    val maxBookingDays: Int = 7,
    val remindersEnabled: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val prefs = application.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPremiumInfo()
        loadData()
    }

    private fun loadPremiumInfo() {
        _uiState.value = _uiState.value.copy(
            isPremium = prefs.getBoolean("is_premium", false),
            maxServices = prefs.getInt("max_services", 3),
            maxBookingDays = prefs.getInt("max_booking_days", 7),
            remindersEnabled = prefs.getBoolean("reminders_enabled", false)
        )
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                fetchAppointments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.localizedMessage}"
                )
            }
        }
    }

    private suspend fun fetchAppointments() {
        val token = tokenManager.token.first() ?: ""
        val companyId = tokenManager.companyId.first() ?: 1L
        val companyName = tokenManager.companyName.first() ?: ""

        val response = api.getAppointments(companyId, "Bearer $token")
        if (response.isSuccessful) {
            val now = java.time.LocalDateTime.now()
            val filtered = response.body()?.filter {
                val startTime = java.time.LocalDateTime.parse(it.startTime.substring(0, 19), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                startTime.isAfter(now)
            } ?: emptyList()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                appointments = filtered,
                companyName = companyName
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Ошибка загрузки: ${response.code()}"
            )
        }
    }

    fun confirmAppointment(id: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.confirmAppointment(id, "Bearer $token")
                loadData()
            } catch (_: Exception) { }
        }
    }

    fun cancelAppointment(id: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.cancelAppointment(id, "Bearer $token")
                loadData()
            } catch (_: Exception) { }
        }
    }

    fun onResume() {
        loadData()
    }
}