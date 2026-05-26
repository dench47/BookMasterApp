package ru.bookmaster.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import ru.bookmaster.app.util.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.companyName, style = MaterialTheme.typography.titleMedium)
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                TextButton(
                    onClick = {
                        runBlocking { TokenManager(context).clear() }
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Карточка Premium
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isPremium) Color(0xFF14532D).copy(alpha = 0.3f)
                    else Color(0xFF713F12).copy(alpha = 0.3f)
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (uiState.isPremium) "👑 Premium активен"
                        else "🆓 Бесплатный тариф",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isPremium) Color(0xFF86EFAC) else Color(0xFFFCD34D)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Мастеров: ${if (uiState.isPremium) "безлимит" else "1"} • " +
                                "Услуг: ${if (uiState.isPremium) "безлимит" else "${uiState.maxServices}"} • " +
                                "Запись: ${if (uiState.isPremium) "безлимит" else "${uiState.maxBookingDays} дн"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!uiState.isPremium) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Напоминания салону: ❌",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("📅 Записи клиентов", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.loadData() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.error != null) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("❌", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.loadData() }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                } else if (uiState.appointments.isEmpty() && !uiState.isLoading) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🙌", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Записей пока нет", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.loadData() }) {
                                Text("Обновить")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.appointments) { appointment ->
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        appointment.cancelled == true -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        appointment.confirmed == true -> Color(0xFF14532D).copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(appointment.clientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(appointment.startTime.substring(11, 16), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("📞 ${appointment.clientPhone}", style = MaterialTheme.typography.bodySmall)
                                    Text("💇 ${appointment.serviceName} • 👤 ${appointment.masterName}", style = MaterialTheme.typography.bodySmall)
                                    Text("📅 ${formatDate(appointment.startTime)}", style = MaterialTheme.typography.bodySmall)

                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        when {
                                            appointment.cancelled == true -> "❌ Отменена"
                                            appointment.confirmed == true -> "✅ Подтверждена"
                                            else -> "⏳ Ожидает"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            appointment.cancelled == true -> MaterialTheme.colorScheme.error
                                            appointment.confirmed == true -> Color(0xFF86EFAC)
                                            else -> Color(0xFFFCD34D)
                                        }
                                    )

                                    if (appointment.cancelled != true && appointment.confirmed != true) {
                                        Spacer(Modifier.height(8.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { viewModel.confirmAppointment(appointment.id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC))
                                            ) {
                                                Text("✓ Подтвердить")
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.cancelAppointment(appointment.id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("✕ Отменить")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateTime: String): String {
    val datePart = dateTime.take(10)
    val parts = datePart.split("-")
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}