package ru.bookmaster.app.ui.finances

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate

data class FinancesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val todayRevenue: String = "0",
    val todayNetProfit: String = "0",
    val monthRevenue: String = "0",
    val monthNetProfit: String = "0",
    val topServices: List<Pair<String, Long>> = emptyList(),
    val topMasters: List<Pair<String, Long>> = emptyList(),
    val dailyRevenue: List<Pair<String, Double>> = emptyList()
)

class FinancesViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(FinancesUiState())
    val uiState = _uiState.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun init(companyId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""

                // Dashboard
                val dashboardResp = api.getDashboard(companyId, "Bearer $token")
                val todayRevenue = dashboardResp.body()?.todayRevenue?.toInt()?.toString() ?: "0"
                val todayNetProfit = dashboardResp.body()?.todayNetProfit?.toInt()?.toString() ?: "0"

                // Месячная выручка по дням
                val monthStart = LocalDate.now().withDayOfMonth(1)
                val monthEnd = LocalDate.now()
                val revenueResp = api.getRevenueByDay(companyId, monthStart.toString(), monthEnd.toString(), "Bearer $token")
                val revenueMap = revenueResp.body()?.get("dailyRevenue") as? List<Map<String, Any>> ?: emptyList()
                val totalMonth = revenueMap.sumOf { (it["revenue"] as? Number)?.toDouble() ?: 0.0 }
                val dailyList = revenueMap.map {
                    (it["date"]?.toString()?.take(10) ?: "") to ((it["revenue"] as? Number)?.toDouble() ?: 0.0)
                }

                // Top services
                val topServicesResp = api.getTopServices(companyId, 5, "Bearer $token")
                val topServicesMap = topServicesResp.body()?.get("topServices") as? List<Map<String, Any>> ?: emptyList()
                val topServicesList = topServicesMap.map {
                    (it["serviceName"]?.toString() ?: "") to ((it["count"] as? Number)?.toLong() ?: 0)
                }

                // Top masters
                val topMastersResp = api.getTopMasters(companyId, 5, "Bearer $token")
                val topMastersMap = topMastersResp.body()?.get("topMasters") as? List<Map<String, Any>> ?: emptyList()
                val topMastersList = topMastersMap.map {
                    (it["masterName"]?.toString() ?: "") to ((it["count"] as? Number)?.toLong() ?: 0)
                }

                val monthNetProfit = dashboardResp.body()?.monthNetProfit?.toInt()?.toString() ?: "0"

                _uiState.value = FinancesUiState(
                    isLoading = false,
                    todayRevenue = todayRevenue,
                    todayNetProfit = todayNetProfit,
                    monthRevenue = totalMonth.toInt().toString(),
                    monthNetProfit = monthNetProfit,
                    topServices = topServicesList,
                    topMasters = topMastersList,
                    dailyRevenue = dailyList
                )
            } catch (e: Exception) {
                _uiState.value = FinancesUiState(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}