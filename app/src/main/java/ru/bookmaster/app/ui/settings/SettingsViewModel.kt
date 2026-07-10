package ru.bookmaster.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager

data class SettingsUiState(
    val companyId: Long = 0L,
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val companyName = tokenManager.companyName.first() ?: ""
                val email = tokenManager.email.first() ?: ""

                val companyResponse = api.getCompany(companyId, "Bearer $token")
                val companyData = if (companyResponse.isSuccessful) companyResponse.body() else null

                _uiState.value = SettingsUiState(
                    companyId = companyId,
                    displayName = companyName,
                    email = email,
                    phone = companyData?.get("phone") as? String ?: "",
                    address = companyData?.get("address") as? String ?: "",
                    createdAt = formatDate(companyData?.get("createdAt") as? String),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun saveData(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = _uiState.value.companyId

                val body = mutableMapOf<String, Any>()
                body["name"] = name
                body["phone"] = phone
                body["email"] = email
                body["address"] = address

                val response = api.updateCompany(companyId, body, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        displayName = name,
                        phone = phone,
                        email = email,
                        address = address,
                        isSaving = false,
                        saveSuccess = true
                    )
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

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    private fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val dateTime = java.time.LocalDateTime.parse(isoString.take(19))
            dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (_: Exception) {
            try {
                val date = java.time.LocalDate.parse(isoString.take(10))
                date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } catch (_: Exception) {
                isoString
            }
        }
    }
}