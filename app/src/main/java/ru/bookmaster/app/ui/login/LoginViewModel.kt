package ru.bookmaster.app.ui.login

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
import ru.bookmaster.app.data.model.LoginRequest
import ru.bookmaster.app.util.TokenManager
import androidx.core.content.edit

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val response = api.login(LoginRequest(state.email, state.password))
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
                        error = "Неверный email или пароль"
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
                android.util.Log.d("FCM_TOKEN", "Token: $fcmToken")
                if (fcmToken != null) {
                    val api = RetrofitClient.instance
                    val response = api.registerDeviceToken(
                        mapOf("token" to fcmToken),
                        "Bearer $authToken"
                    )
                    android.util.Log.d("FCM_TOKEN", "Server response: ${response.code()}")
                } else {
                    android.util.Log.d("FCM_TOKEN", "Token is null!")
                }
            } catch (e: Exception) {
                android.util.Log.e("FCM_TOKEN", "Error: ${e.message}", e)
            }
        }
    }}