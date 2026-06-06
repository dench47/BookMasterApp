package ru.bookmaster.app.ui.login

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager

data class LoginUiState(
    val phone: String = "+7",
    val callPhone: String = "",
    val isLoading: Boolean = false,
    val isCalling: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, error = null)
    }

    fun login() {
        val phone = _uiState.value.phone.replace(Regex("[^0-9+]"), "")
        if (phone.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Введите номер телефона")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = api.requestCallCheck(mapOf("phone" to phone))
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    if (body["status"] == "ok") {
                        if (body["already_verified"] == true) {
                            // Уже верифицирован — пробуем войти
                            loginByPhone(phone)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                callPhone = body["call_phone"]?.toString() ?: "",
                                isCalling = true
                            )
                            startAutoCheck(phone)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = body["message"]?.toString() ?: "Ошибка"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка: ${e.message}")
            }
        }
    }

    private fun startAutoCheck(phone: String) {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 40 && !_uiState.value.isSuccess) {
                delay(3000)
                attempts++
                try {
                    val response = api.checkCallStatus(mapOf("phone" to phone))
                    if (response.isSuccessful) {
                        val body = response.body() ?: emptyMap()
                        if (body["verified"] == true) {
                            loginByPhone(phone)
                            return@launch
                        }
                    }
                } catch (_: Exception) { }
            }
            if (!_uiState.value.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    error = "Время истекло. Попробуйте снова."
                )
            }
        }
    }

    private suspend fun loginByPhone(phone: String) {
        try {
            val response = api.loginByPhone(mapOf("phone" to phone))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveAuthData(body.token, body.company.email, body.company.name, body.company.id)
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
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Аккаунт не найден. Зарегистрируйтесь.")
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка: ${e.message}")
        }
    }

    private fun sendFcmToken(authToken: String) {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                if (fcmToken != null) {
                    api.registerDeviceToken(mapOf("token" to fcmToken), "Bearer $authToken")
                }
            } catch (_: Exception) { }
        }
    }
}