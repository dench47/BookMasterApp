package ru.bookmaster.app.ui.register

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.RegisterRequest
import ru.bookmaster.app.util.TokenManager
import androidx.core.content.edit

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun register() {
        val state = _uiState.value

        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Заполните обязательные поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val response = api.register(
                    RegisterRequest(
                        name = state.name,
                        email = state.email,
                        password = state.password,
                        phone = state.phone.ifBlank { null },
                        address = null
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(body.token, body.company.email, body.company.name, body.company.id)
                    // Сохраняем Premium-инфу
                    val prefs = getApplication<Application>().getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
                    prefs.edit {
                        putBoolean("is_premium", body.company.premium == true)
                            .putInt("max_services", body.company.maxServices ?: 3)
                            .putInt("max_booking_days", body.company.maxBookingDays ?: 7)
                            .putBoolean("reminders_enabled", body.company.remindersEnabled == true)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    sendFcmToken(body.token)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка регистрации. Возможно, email уже занят."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка соединения: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun sendFcmToken(authToken: String) {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                if (fcmToken != null) {
                    api.registerDeviceToken(
                        mapOf("token" to fcmToken),
                        "Bearer $authToken"
                    )
                }
            } catch (_: Exception) { }
        }
    }
}