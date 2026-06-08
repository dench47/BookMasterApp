package ru.bookmaster.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bookmaster.app.data.model.Client

@Composable
fun ClientsScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ClientsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    Column(modifier = modifier.fillMaxSize()) {



        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchClients(it)
            },
            placeholder = { Text("Поиск по имени или телефону") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            singleLine = true
        )

        // Кнопки сортировки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SortChip("Имя", uiState.sortBy == "name") {
                viewModel.sortClients("name", if (uiState.sortDir == "asc") "desc" else "asc")
            }
            SortChip("Записи", uiState.sortBy == "totalVisits") {
                viewModel.sortClients("totalVisits", "desc")
            }
            SortChip("Сумма", uiState.sortBy == "totalSpent") {
                viewModel.sortClients("totalSpent", "desc")
            }
            SortChip("Визит", uiState.sortBy == "lastVisit") {
                viewModel.sortClients("lastVisit", "desc")
            }
        }

        // Premium баннер
        if (!uiState.isPremium && uiState.clients.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF713F12).copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("🔒 Полный доступ к клиентской базе — только в Premium",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(onClick = { /* TODO */ }) {
                        Text("Подключить Premium")
                    }
                }
            }
        }

        // Список клиентов
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.loadClients() }) { Text("Повторить") }
                }
            }
        } else if (uiState.clients.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Клиентов пока нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.clients) { client ->
                    ClientCard(client, uiState.isPremium) { onNavigateToDetail(client.id) }
                }
                if (uiState.isPremium && uiState.hasMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            if (uiState.isLoadingMore) CircularProgressIndicator()
                            else TextButton(onClick = { viewModel.loadMore() }) { Text("Загрузить ещё") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.background(
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
        )
    ) {
        Text(
            label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
    }
}

@Composable
fun ClientCard(client: Client, isPremium: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(client.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("📞 ${client.phone}", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📊 ${client.totalVisits} зап.", style = MaterialTheme.typography.bodySmall)
                if (isPremium) {
                    Text("💰 ${client.totalSpent.toInt()} ₽", style = MaterialTheme.typography.bodySmall)
                }
                client.lastVisit?.let {
                    Text("📅 ${it.take(10).split("-").reversed().joinToString(".")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}