package ru.bookmaster.app.ui.cabinet

import android.app.Application
import android.content.Context
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
    val error: String? = null,
    val isPremium: Boolean = false
)

class CabinetViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(CabinetUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
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

                // 1. Данные компании
                val companyResponse = api.getCompany(companyId, "Bearer $token")
                val companyData = if (companyResponse.isSuccessful) companyResponse.body() else null

                // 2. Статистика по клиентам (totalClients)
                val clientsStatsResponse = api.getClientsStats(companyId, "Bearer $token")
                val clientsStatsData = if (clientsStatsResponse.isSuccessful) clientsStatsResponse.body() else null

                // 3. Статистика по мастерам (totalMasters)
                val mastersStatsResponse = api.getMastersStats(companyId, "Bearer $token")
                val mastersStatsData = if (mastersStatsResponse.isSuccessful) mastersStatsResponse.body() else null

                // 4. Список услуг (servicesCount)
                val servicesResponse = api.getServices(companyId, "Bearer $token")
                val services = servicesResponse.body()
                val servicesCount = services?.size ?: 0 // <--- Исправлено!

                // 5. Premium статус
                val prefs = getApplication<Application>().getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
                val isPremium = prefs.getBoolean("is_premium", false)

                _uiState.value = CabinetUiState(
                    displayName = companyName,
                    email = email,
                    phone = companyData?.get("phone") as? String ?: "",
                    address = companyData?.get("address") as? String ?: "",
                    createdAt = companyData?.get("createdAt") as? String ?: "",
                    isMaster = companyType == "master",
                    totalClients = (clientsStatsData?.get("totalClients") as? Number)?.toInt() ?: 0,
                    totalMasters = (mastersStatsData?.get("totalMasters") as? Number)?.toInt() ?: 0,
                    servicesCount = servicesCount,
                    webBookingUrl = "http://your-server.com/salon/$companyId",
                    isLoading = false,
                    isPremium = isPremium
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