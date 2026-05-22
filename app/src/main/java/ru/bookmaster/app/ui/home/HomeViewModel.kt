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
import ru.bookmaster.app.util.TokenManager

data class HomeUiState(
    val companyName: String = "",
    val appointments: List<AppointmentResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 1L
                val companyName = tokenManager.companyName.first() ?: ""

                _uiState.value = _uiState.value.copy(companyName = companyName)

                val response = api.getAppointments(companyId, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = response.body() ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.localizedMessage}"
                )
            }
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
}