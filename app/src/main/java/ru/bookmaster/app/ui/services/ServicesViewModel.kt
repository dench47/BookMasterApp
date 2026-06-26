package ru.bookmaster.app.ui.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.ServiceModel
import ru.bookmaster.app.util.TokenManager

data class ServicesUiState(
    val services: List<ServiceModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPremium: Boolean = false
)

class ServicesViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val prefs = application.getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val isPremium = prefs.getBoolean("is_premium", false)

                val response = api.getServices(companyId, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        services = response.body() ?: emptyList(),
                        isLoading = false,
                        isPremium = isPremium
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

    fun addService(name: String, description: String?, price: Double, duration: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""

                val response = api.addService(
                    body = mapOf(
                        "name" to name,
                        "description" to (description ?: ""),
                        "price" to price,
                        "durationMinutes" to duration,
                        "active" to true
                    ),
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    loadServices()
                } else {
                    val errorMsg = if (response.code() == 403) {
                        "Достигнут лимит услуг. Подключите Premium."
                    } else {
                        "Ошибка добавления услуги: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка добавления")
            }
        }
    }

    fun toggleActive(serviceId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.toggleServiceActive(serviceId, "Bearer $token")
                if (response.isSuccessful) {
                    // Обновляем только изменённую услугу в списке
                    val updatedService = response.body()
                    val currentServices = _uiState.value.services.toMutableList()
                    val index = currentServices.indexOfFirst { it.id == serviceId }
                    if (index != -1 && updatedService != null) {
                        // Преобразуем Map в ServiceModel
                        val newService = ServiceModel(
                            id = (updatedService["id"] as? Number)?.toLong() ?: serviceId,
                            name = updatedService["name"] as? String ?: "",
                            description = updatedService["description"] as? String,
                            price = (updatedService["price"] as? Number)?.toDouble() ?: 0.0,
                            durationMinutes = (updatedService["durationMinutes"] as? Number)?.toInt() ?: 0,
                            active = updatedService["active"] as? Boolean ?: false
                        )
                        currentServices[index] = newService
                        _uiState.value = _uiState.value.copy(services = currentServices)
                    }
                } else {
                    val errorMsg = if (response.code() == 403) {
                        "Достигнут лимит активных услуг. Подключите Premium."
                    } else {
                        "Ошибка изменения статуса: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка изменения статуса")
            }
        }
    }

    fun deleteService(serviceId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.deleteService(serviceId, "Bearer $token")
                if (response.isSuccessful) {
                    loadServices()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Ошибка удаления услуги: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка удаления")
            }
        }
    }
}