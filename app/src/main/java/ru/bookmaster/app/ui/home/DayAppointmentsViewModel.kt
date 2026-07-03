package ru.bookmaster.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.TokenManager

data class DayAppointmentsUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentResponse> = emptyList(),
    val masters: List<Master> = emptyList(),
    val error: String? = null
)

class DayAppointmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(DayAppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAppointmentsForDate(dateString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val response = api.getAppointmentsByDate(companyId, dateString, "Bearer $token")
                val appointments = response.body() ?: emptyList()

                // Загружаем мастеров для редактирования
                val mastersResponse = try {
                    api.getMasters(companyId, "Bearer $token")
                } catch (e: Exception) { null }
                val masters = mastersResponse?.body() ?: emptyList()

                _uiState.value = DayAppointmentsUiState(
                    isLoading = false,
                    appointments = appointments,
                    masters = masters
                )
            } catch (e: Exception) {
                _uiState.value = DayAppointmentsUiState(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun confirmAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.confirmAppointment(appointmentId, "Bearer $token")
                val currentDate = _uiState.value.appointments.firstOrNull()?.startTime?.take(10)
                if (currentDate != null) loadAppointmentsForDate(currentDate)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.cancelAppointment(appointmentId, "Bearer $token")
                val currentDate = _uiState.value.appointments.firstOrNull()?.startTime?.take(10)
                if (currentDate != null) loadAppointmentsForDate(currentDate)
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
                val currentDate = _uiState.value.appointments.firstOrNull()?.startTime?.take(10)
                if (currentDate != null) loadAppointmentsForDate(currentDate)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}