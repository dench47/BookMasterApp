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
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.TokenManager

data class AppointmentsUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentResponse> = emptyList(),
    val masters: List<Master> = emptyList(),
    val error: String? = null,
    val selectedTab: Int = 0
)

class AppointmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAllAppointments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L

                val appointmentsDeferred = async {
                    try { api.getAppointments(companyId, "Bearer $token") } catch (e: Exception) { null }
                }
                val mastersDeferred = async {
                    try { api.getMasters(companyId, "Bearer $token") } catch (e: Exception) { null }
                }

                val appointmentsResponse = appointmentsDeferred.await()
                val mastersResponse = mastersDeferred.await()

                val appointments = appointmentsResponse?.body() ?: emptyList()
                val masters = mastersResponse?.body() ?: emptyList()

                _uiState.value = AppointmentsUiState(
                    isLoading = false,
                    appointments = appointments,
                    masters = masters
                )
            } catch (e: Exception) {
                _uiState.value = AppointmentsUiState(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun confirmAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.confirmAppointment(appointmentId, "Bearer $token")
                loadAllAppointments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.cancelAppointment(appointmentId, "Bearer $token")
                loadAllAppointments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                loadAllAppointments()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}