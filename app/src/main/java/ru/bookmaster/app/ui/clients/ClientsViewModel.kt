package ru.bookmaster.app.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.Client
import ru.bookmaster.app.util.TokenManager

data class ClientsUiState(
    val clients: List<Client> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isPremium: Boolean = false,
    val total: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val sortBy: String = "name",
    val sortDir: String = "asc",
    val hasMore: Boolean = false,
    val isServerError: Boolean = false

)

class ClientsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentSearch: String? = null

    fun loadClients(reset: Boolean = true) {
        viewModelScope.launch {
            if (reset) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, isServerError = false)
            }
            try {
                val token = tokenManager.token.first() ?: ""
                val state = _uiState.value
                val response = api.getClients(
                    token = "Bearer $token",
                    page = if (reset) 0 else state.currentPage + 1,
                    size = 20,
                    sortBy = state.sortBy,
                    sortDir = state.sortDir,
                    search = currentSearch
                )

                if (response.isSuccessful) {
                    val data = response.body()!!
                    val newClients = if (reset) data.clients else _uiState.value.clients + data.clients
                    _uiState.value = _uiState.value.copy(
                        clients = newClients,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        isServerError = false,
                        isPremium = data.isPremium,
                        total = data.total,
                        currentPage = data.page,
                        totalPages = data.totalPages,
                        hasMore = data.page < data.totalPages - 1
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isServerError = true,
                        error = "Ошибка загрузки: ${response.code()}"
                    )
                }
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isServerError = true,
                    error = "Сервер не отвечает. Проверьте подключение."
                )
            } catch (_: java.net.ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isServerError = true,
                    error = "Нет связи с сервером. Проверьте интернет."
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isServerError = true,
                    error = "Ошибка соединения. Попробуйте позже."
                )
            }
        }
    }
    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoadingMore) {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            loadClients(reset = false)
        }
    }

    fun sortClients(sortBy: String, sortDir: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy, sortDir = sortDir)
        loadClients(reset = true)
    }

    fun searchClients(query: String) {
        currentSearch = query.ifEmpty { null }
        loadClients(reset = true)
    }
}