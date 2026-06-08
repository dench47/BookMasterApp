package ru.bookmaster.app.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.ClientDetailResponse
import ru.bookmaster.app.util.TokenManager

data class ClientDetailUiState(
    val client: ClientDetailResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ClientDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadClient(clientId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getClientDetail(clientId, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = ClientDetailUiState(
                        client = response.body(),
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

    fun updateNotes(clientId: Long, notes: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.updateClientNotes(clientId, mapOf("notes" to notes), "Bearer $token")
                if (response.isSuccessful) {
                    loadClient(clientId)
                }
            } catch (_: Exception) { }
        }
    }
}