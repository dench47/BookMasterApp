package ru.bookmaster.app.ui.expenses

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

data class ExpenseItem(
    val id: Long,
    val amount: Double = 0.0,
    val category: String = "OTHER",
    val description: String = "",
    val date: String = ""
)

data class ExpensesUiState(
    val expenses: List<ExpenseItem> = emptyList(),
    val companyId: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddForm: Boolean = false,
    val savingExpense: Boolean = false
)

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    fun init(companyId: Long) {
        _uiState.value = _uiState.value.copy(companyId = companyId)
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getExpenses(
                    "Bearer $token",
                    _uiState.value.companyId
                )
                if (response.isSuccessful) {
                    val rawList = response.body() ?: emptyList()
                    val list = rawList.map { map ->
                        ExpenseItem(
                            id = (map["id"] as? Number)?.toLong() ?: 0,
                            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                            category = map["category"]?.toString() ?: "OTHER",
                            description = map["description"]?.toString() ?: "",
                            date = map["date"]?.toString() ?: ""
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        expenses = list,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = "Ошибка: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.localizedMessage
                )
            }
        }
    }

    fun showAddForm() {
        _uiState.value = _uiState.value.copy(showAddForm = true)
    }

    fun hideAddForm() {
        _uiState.value = _uiState.value.copy(showAddForm = false)
    }

    fun addExpense(amount: String, category: String, description: String, date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(savingExpense = true)
            try {
                val token = tokenManager.token.first() ?: ""
                api.addExpense(
                    "Bearer $token",
                    mapOf(
                        "amount" to amount.toDouble(),
                        "category" to category,
                        "description" to description,
                        "date" to date
                    )
                )
                _uiState.value = _uiState.value.copy(savingExpense = false, showAddForm = false)
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(savingExpense = false)
            }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.deleteExpense("Bearer $token", id)
                loadExpenses()
            } catch (_: Exception) {}
        }
    }
}