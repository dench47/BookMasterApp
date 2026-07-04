package ru.bookmaster.app.ui.clients

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun formatDateTime(isoString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoString.take(19))
        dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (e: Exception) {
        isoString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onBack: () -> Unit,
    viewModel: ClientDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(clientId) {
        viewModel.loadClient(clientId)
    }

    LaunchedEffect(uiState.client?.notes) {
        notes = uiState.client?.notes ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.client?.name ?: "Загрузка...")
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("❌ ${uiState.error}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { viewModel.loadClient(clientId) }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                uiState.client != null -> {
                    val client = uiState.client!!

                    // Информация о клиенте
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📞 ${client.phone}", style = MaterialTheme.typography.bodyLarge)
                            client.email?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📧 $it", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("📊 ${client.totalVisits} записей", fontWeight = FontWeight.Bold)
                                Text("💰 ${client.totalSpent.toInt()} ₽", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Заметки
                    Text("📝 Заметки", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Добавьте заметку о клиенте...") },
                        minLines = 3,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.updateNotes(client.id, notes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить заметки")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // История записей
                    Text("📅 История записей (${client.history.size})", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(client.history) { appointment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        appointment.cancelled -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        appointment.confirmed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(appointment.serviceName, fontWeight = FontWeight.Bold)
                                        Text("${appointment.price.toInt()} ₽", color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("👤 ${appointment.masterName}", style = MaterialTheme.typography.bodySmall)
                                    Text("📅 ${formatDateTime(appointment.startTime)}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        when {
                                            appointment.cancelled -> "❌ Отменена"
                                            appointment.confirmed -> "✅ Подтверждена"
                                            else -> "⏳ Ожидает"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            appointment.cancelled -> MaterialTheme.colorScheme.error
                                            appointment.confirmed -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}