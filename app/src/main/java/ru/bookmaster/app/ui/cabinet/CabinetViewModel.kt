package ru.bookmaster.app.ui.cabinet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager

data class CabinetUiState(
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: String = "",
    val isMaster: Boolean = false,
    val totalClients: Int = 0,
    val totalMasters: Int = 0,
    val servicesCount: Int = 0,
    val webBookingUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CabinetViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(CabinetUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val companyId = tokenManager.companyId.first() ?: 0L
                val companyName = tokenManager.companyName.first() ?: ""
                val email = tokenManager.email.first() ?: ""
                val companyType = tokenManager.companyType.first() ?: "salon"

                // Загружаем данные компании
                val companyResponse = api.getCompany(companyId, "Bearer $token")
                val companyData = if (companyResponse.isSuccessful) companyResponse.body() else null

                // Загружаем статистику для цифр
                val statsResponse = api.getStats(companyId, "Bearer $token")
                val statsData = if (statsResponse.isSuccessful) statsResponse.body() else null

                _uiState.value = CabinetUiState(
                    displayName = companyName,
                    email = email,
                    phone = companyData?.get("phone") as? String ?: "",
                    address = companyData?.get("address") as? String ?: "",
                    createdAt = companyData?.get("createdAt") as? String ?: "",
                    isMaster = companyType == "master",
                    totalClients = (statsData?.get("totalClients") as? Number)?.toInt() ?: 0,
                    totalMasters = (statsData?.get("totalMasters") as? Number)?.toInt() ?: 0,
                    servicesCount = (statsData?.get("servicesCount") as? Number)?.toInt() ?: 0,
                    webBookingUrl = "http://your-server.com/salon/$companyId",
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
}