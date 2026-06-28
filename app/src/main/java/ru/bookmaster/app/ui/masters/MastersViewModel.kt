package ru.bookmaster.app.ui.masters

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.TokenManager

data class MastersUiState(
    val masters: List<Master> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPremium: Boolean = false
)

class MastersViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val prefs = application.getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MastersUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMasters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val isPremium = prefs.getBoolean("is_premium", false)

                val response = api.getMasters(companyId, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        masters = response.body() ?: emptyList(),
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

    fun addMaster(name: String, phone: String?, specialization: String?) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L

                val response = api.addMaster(
                    body = mapOf(
                        "name" to name,
                        "phone" to (phone ?: ""),
                        "specialization" to (specialization ?: ""),
                        "workStart" to "09:00",
                        "workEnd" to "18:00",
                        "companyId" to companyId
                    ),
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    loadMasters()
                } else {
                    val errorMsg = if (response.code() == 403) {
                        "Достигнут лимит сотрудников. Подключите Premium."
                    } else {
                        "Ошибка добавления сотрудника"
                    }
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка")
            }
        }
    }

    fun toggleActive(masterId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.toggleMasterActive(masterId, "Bearer $token")
                if (response.isSuccessful) {
                    // Просто обновляем статус
                    val currentMasters = _uiState.value.masters.toMutableList()
                    val index = currentMasters.indexOfFirst { it.id == masterId }
                    if (index != -1) {
                        val old = currentMasters[index]
                        currentMasters[index] = old.copy(active = !old.active)
                        _uiState.value = _uiState.value.copy(masters = currentMasters)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка изменения статуса: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка")
            }
        }
    }

    fun deleteMaster(masterId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.deleteMaster(masterId, "Bearer $token")
                loadMasters()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка удаления")
            }
        }
    }
}