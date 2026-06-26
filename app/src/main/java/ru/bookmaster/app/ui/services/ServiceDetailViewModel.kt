package ru.bookmaster.app.ui.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.UpdateServiceRequest
import ru.bookmaster.app.util.TokenManager

data class ServiceDetailUiState(
    val serviceId: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val duration: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

class ServiceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadService(serviceId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getService(serviceId, "Bearer $token")
                if (response.isSuccessful) {
                    val service = response.body()!!
                    _uiState.value = ServiceDetailUiState(
                        serviceId = service.id,
                        name = service.name,
                        description = service.description ?: "",
                        price = service.price.toInt().toString(),
                        duration = service.durationMinutes.toString(),
                        isLoading = false
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
                    error = e.localizedMessage ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updatePrice(value: String) {
        if (value.isEmpty() || value.matches(Regex("^[0-9.]*$"))) {
            _uiState.value = _uiState.value.copy(price = value)
        }
    }

    fun updateDuration(value: String) {
        if (value.isEmpty() || value.matches(Regex("^[0-9]*$"))) {
            _uiState.value = _uiState.value.copy(duration = value)
        }
    }

    fun saveService(onSuccess: () -> Unit) {  // ← добавить колбэк
        val state = _uiState.value
        val priceVal = state.price.toDoubleOrNull()
        val durationVal = state.duration.toIntOrNull()
        if (state.name.isBlank() || priceVal == null || durationVal == null) {
            _uiState.value = _uiState.value.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.updateService(
                    id = state.serviceId,
                    body = UpdateServiceRequest(
                        name = state.name,
                        description = state.description.takeIf { it.isNotBlank() },
                        price = priceVal,
                        durationMinutes = durationVal
                    ),
                    token = "Bearer $token"
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSuccess()  // ← вызываем колбэк
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Ошибка сохранения: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.localizedMessage ?: "Ошибка сохранения"
                )
            }
        }
    }
    fun deleteService() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.deleteService(_uiState.value.serviceId, "Bearer $token")
            } catch (_: Exception) { }
        }
    }
}