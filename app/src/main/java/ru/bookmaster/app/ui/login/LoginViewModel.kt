package ru.bookmaster.app.ui.login

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager

data class LoginUiState(
    val phone: String = "+7",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val shouldNavigateToRegister: Boolean = false
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
                val response = api.loginByPhone(mapOf("phone" to phone))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(
                        token = body.token,
                        email = body.company.email,
                        companyName = body.company.name,
                        companyId = body.company.id,
                        isPremium = body.company.premium == true,
                        maxServices = body.company.maxServices ?: 3,
                        maxBookingDays = body.company.maxBookingDays ?: 7,
                        remindersEnabled = body.company.remindersEnabled == true
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    sendFcmToken(body.token)
                } else {
                    // Салона нет — переход на регистрацию
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shouldNavigateToRegister = true,
                        phone = phone
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shouldNavigateToRegister = true,
                    phone = phone
                )
            }
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