package ru.bookmaster.app.ui.premium

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager

data class PremiumUiState(
    val isLoading: Boolean = false,
    val isPremiumActive: Boolean = false,
    val error: String? = null
)

class PremiumViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState = _uiState.asStateFlow()

    fun activatePremium() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L

                val response = api.activatePremium(companyId, "Bearer $token")

                if (response.isSuccessful) {
                    // Обновляем локальные данные о премиуме
                    val prefs = getApplication<Application>().getSharedPreferences("premium_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_premium", true).apply()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPremiumActive = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка активации: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Ошибка соединения"
                )
            }
        }
    }
}