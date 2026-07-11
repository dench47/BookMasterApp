package ru.bookmaster.app.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.ClientProfileResponse
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate

data class ClientDetailUiState(
    val profile: ClientProfileResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ClientDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadProfile(clientId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getClientProfile(clientId, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = ClientDetailUiState(
                        profile = response.body(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = ClientDetailUiState(
                        isLoading = false,
                        error = "Ошибка: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ClientDetailUiState(
                    isLoading = false,
                    error = e.localizedMessage ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun updateClient(clientId: Long, fields: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.updateClient(clientId, fields, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                    loadProfile(clientId)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun updateNotes(clientId: Long, notes: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.updateClientNotes(clientId, mapOf("notes" to notes), "Bearer $token")
                loadProfile(clientId)
            } catch (_: Exception) { }
        }
    }
}