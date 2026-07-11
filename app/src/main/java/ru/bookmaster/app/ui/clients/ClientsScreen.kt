package ru.bookmaster.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bookmaster.app.data.model.Client

@Composable
fun ClientsScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
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
            SortChip("Визиты", uiState.sortBy == "totalVisits") {
                viewModel.sortClients("totalVisits", "desc")
            }
            SortChip("Сумма", uiState.sortBy == "totalSpent") {
                viewModel.sortClients("totalSpent", "desc")
            }
            SortChip("Посл.визит", uiState.sortBy == "lastVisit") {
                viewModel.sortClients("lastVisit", "desc")
            }
            SortChip("Давно не был", uiState.sortBy == "daysSinceLastVisit") {
                viewModel.sortClients("daysSinceLastVisit", "desc")
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
                    OutlinedButton(
                        onClick = onNavigateToPremium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("❌", fontSize = 48.sp)
                    Text(
                        uiState.error ?: "Сервер недоступен",
                        color = Color(0xFFFCA5A5),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.loadClients() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("Повторить")
                    }
                }
            }
            return
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
    val statusColor = when (client.loyaltyStatus) {
        "vip" -> Color(0xFFFFD700)
        "problematic" -> Color(0xFFFF4444)
        "sleeping" -> Color(0xFFAAAAAA)
        "new" -> Color(0xFF4CAF50)
        else -> Color.Transparent
    }

    val statusLabel = when (client.loyaltyStatus) {
        "vip" -> "⭐ VIP"
        "problematic" -> "⚠ Проблемный"
        "sleeping" -> "💤 Спящий"
        "new" -> "🆕 Новый"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    client.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (statusLabel.isNotEmpty()) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("📞 ${client.phone}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📊 ${client.totalVisits} зап.", style = MaterialTheme.typography.bodySmall)
                if (isPremium) {
                    Text("💰 ${client.totalSpent.toInt()} ₽", style = MaterialTheme.typography.bodySmall)
                    Text("❌ ${client.cancellationCount} отмен", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                client.lastVisit?.let {
                    Text("📅 ${it.take(10).split("-").reversed().joinToString(".")}",
                        style = MaterialTheme.typography.bodySmall)
                }
                if (client.daysSinceLastVisit < 999) {
                    Text(
                        "🚫 ${client.daysSinceLastVisit} дн. назад",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (client.daysSinceLastVisit > 30) Color(0xFFFF4444) else Color.Unspecified
                    )
                }
            }
        }
    }
}