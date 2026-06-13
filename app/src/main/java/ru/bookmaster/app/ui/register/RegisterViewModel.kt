package ru.bookmaster.app.ui.register

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
import ru.bookmaster.app.data.model.RegisterRequest
import ru.bookmaster.app.util.TokenManager

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "+7",
    val type: String = "salon",
    val isLoading: Boolean = false,
    val isCalling: Boolean = false,
    val callPhone: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.value = _uiState.value.copy(name = name, error = null) }
    fun onEmailChange(email: String) { _uiState.value = _uiState.value.copy(email = email, error = null) }
    fun onPhoneChange(phone: String) { _uiState.value = _uiState.value.copy(phone = phone, error = null) }
    fun onTypeChange(type: String) { _uiState.value = _uiState.value.copy(type = type) }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank()) {
            _uiState.value = state.copy(error = "Заполните обязательные поля")
            return
        }

        // Начинаем верификацию
        startVerification()
    }

    private fun startVerification() {
        val phone = _uiState.value.phone.replace(Regex("[^0-9+]"), "")
        if (phone.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Введите номер телефона")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = api.requestCallCheck(mapOf("phone" to phone, "type" to "salon"))  // ← добавить type
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    if (body["status"] == "ok") {
                        if (body["already_verified"] == true) {
                            loginByPhone(phone)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isCalling = true,
                                callPhone = body["call_phone"]?.toString() ?: ""
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
                // Если уже зарегистрировались - выходим
                if (_uiState.value.isSuccess) return@launch
                try {
                    val response = api.checkCallStatus(mapOf("phone" to phone, "type" to "salon"))
                    if (response.isSuccessful) {
                        val body = response.body() ?: emptyMap()
                        if (body["verified"] == true) {
                            createCompany()
                            return@launch  // ← Выходим после createCompany
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

    private fun createCompany() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val response = api.register(
                    RegisterRequest(
                        name = state.name,
                        email = state.email,
                        password = state.phone,
                        phone = state.phone.ifBlank { null },
                        address = null,
                        type = state.type
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(
                        token = body.token,
                        email = body.company.email,
                        companyName = body.company.name,
                        companyId = body.company.id,
                        companyType = state.type,  // ← добавить эту строку
                        isPremium = body.company.premium == true,
                        maxServices = body.company.maxServices ?: 3,
                        maxBookingDays = body.company.maxBookingDays ?: 7,
                        remindersEnabled = body.company.remindersEnabled == true
                    )
                    val prefs = getApplication<Application>().getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
                    prefs.edit {
                        putBoolean("is_premium", body.company.premium == true)
                            .putInt("max_services", body.company.maxServices ?: 3)
                            .putInt("max_booking_days", body.company.maxBookingDays ?: 7)
                            .putBoolean("reminders_enabled", body.company.remindersEnabled == true)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isCalling = false, isSuccess = true)
                    sendFcmToken(body.token)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isCalling = false,
                        error = "Ошибка регистрации"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCalling = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    private suspend fun loginByPhone(phone: String) {
        try {
            val response = api.loginByPhone(mapOf("phone" to phone))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveAuthData(
                    token = body.token,
                    email = body.company.email,
                    companyName = body.company.name,
                    companyId = body.company.id,
                    companyType = body.company.type ?: "salon",  // ← добавить ?: "salon"
                    isPremium = body.company.premium == true,
                    maxServices = body.company.maxServices ?: 3,
                    maxBookingDays = body.company.maxBookingDays ?: 7,
                    remindersEnabled = body.company.remindersEnabled == true
                )
                val prefs = getApplication<Application>().getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putBoolean("is_premium", body.company.premium == true)
                        .putInt("max_services", body.company.maxServices ?: 3)
                        .putInt("max_booking_days", body.company.maxBookingDays ?: 7)
                        .putBoolean("reminders_enabled", body.company.remindersEnabled == true)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isCalling = false, isSuccess = true)
                sendFcmToken(body.token)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCalling = false,
                    error = "Ошибка входа"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isCalling = false,
                error = "Ошибка: ${e.message}"
            )
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