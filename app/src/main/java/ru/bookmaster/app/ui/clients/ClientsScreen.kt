package ru.bookmaster.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bookmaster.app.data.model.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: ClientsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Поиск + иконка фильтра
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { value ->
                    searchQuery = value
                    viewModel.searchClients(value)
                },
                placeholder = { Text("Поиск по имени или телефону") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = { showFilterSheet = true }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Фильтры и сортировка",
                    tint = MaterialTheme.colorScheme.primary
                )
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.clients) { client ->
                    ClientCard(client) { onNavigateToDetail(client.id) }
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

    // Фильтр-модалка
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Сортировка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                val sorts = listOf(
                    "name" to "По имени",
                    "totalVisits" to "По визитам",
                    "totalSpent" to "По сумме",
                    "lastVisit" to "По дате визита",
                    "daysSinceLastVisit" to "Давно не был"
                )
                sorts.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.sortBy == key,
                            onClick = {
                                viewModel.sortClients(key, if (key == "name") "asc" else "desc")
                                showFilterSheet = false
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: Client, onClick: () -> Unit) {
    val initials = client.name
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercase() ?: "" }
        .joinToString("")

    val avatarBg = when (client.loyaltyStatus) {
        "vip" -> Color(0xFFFFD700)
        "new" -> Color(0xFF4CAF50)
        "problematic" -> Color(0xFFFF4444)
        "sleeping" -> Color(0xFFAAAAAA)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Имя
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    client.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}