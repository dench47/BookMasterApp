package ru.bookmaster.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.ClientProfileResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun formatDateTime(isoString: String?): String {
    if (isoString == null) return "-"
    return try {
        val dateTime = LocalDateTime.parse(isoString.take(19))
        dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (e: Exception) {
        isoString
    }
}

private fun formatDate(isoString: String?): String {
    if (isoString == null) return "-"
    return try {
        isoString.take(10).split("-").let { parts ->
            if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}"
            else isoString
        }
    } catch (e: Exception) {
        isoString
    }
}

private fun formatMoney(value: Number?): String {
    if (value == null) return "0 ₽"
    return "%,.0f ₽".format(value.toDouble()).replace(",", " ")
}

private fun timeOfDayLabel(value: String?): String = when (value) {
    "morning" -> "🌅 Утро"
    "afternoon" -> "🌤 День"
    "evening" -> "🌙 Вечер"
    else -> "—"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onBack: () -> Unit,
    viewModel: ClientDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(clientId) {
        viewModel.loadProfile(clientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.profile?.name ?: "Загрузка...",
                         maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌ ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.loadProfile(clientId) }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            uiState.profile != null -> {
                val profile = uiState.profile!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ============================================================
                    // БЛОК 1: Базовая информация
                    // ============================================================
                    SectionHeader("👤 Базовая информация")
                    InfoCard {
                        InfoRow("Имя", profile.name)
                        InfoRow("Телефон", profile.phone)
                        if (profile.email != null) InfoRow("Email", profile.email)
                        if (profile.birthday != null) InfoRow("Дата рождения", formatDate(profile.birthday))
                        if (profile.gender != null) InfoRow("Пол", genderLabel(profile.gender))
                        if (profile.source != null) InfoRow("Источник", sourceLabel(profile.source))
                        if (profile.tags != null) InfoRow("Теги", profile.tags)
                        if (profile.notes != null) InfoRow("Заметки", profile.notes)
                        InfoRow("Клиент с", formatDate(profile.createdAt))
                        if (profile.avatarUrl != null) InfoRow("Аватар", "✅ есть")
                    }

                    // ============================================================
                    // БЛОК 2: Статус лояльности
                    // ============================================================
                    SectionHeader("🏅 Статус и лояльность")
                    InfoCard {
                        LoyaltyBadge(profile.loyaltyStatus)
                        Spacer(Modifier.height(8.dp))
                        InfoRow("Статус", loyaltyStatusLabel(profile.loyaltyStatus))
                        InfoRow("Всего визитов", "${profile.totalVisits}")
                        InfoRow("За месяц", "${profile.visitsThisMonth}")
                        InfoRow("За год", "${profile.visitsThisYear}")
                        if (profile.firstVisitDate != null) InfoRow("Первый визит", formatDateTime(profile.firstVisitDate))
                        if (profile.lastVisitDate != null) InfoRow("Последний визит", formatDateTime(profile.lastVisitDate))
                        InfoRow("Дней с визита", if (profile.daysSinceLastVisit < 999) "${profile.daysSinceLastVisit}" else "—")
                        InfoRow("Отмен клиентом", "${profile.cancellationCount}")
                        InfoRow("Неявок", "${profile.noShowCount}")
                        InfoRow("Процент отмен", "%.0f%%".format(profile.cancellationRate * 100))
                        InfoRow("Push-уведомления", if (profile.pushEnabled) "✅ Включены" else "❌ Отключены")
                    }

                    // ============================================================
                    // БЛОК 3: Финансовая статистика
                    // ============================================================
                    SectionHeader("💰 Финансы")
                    InfoCard {
                        InfoRow("Всего потрачено", formatMoney(profile.totalSpent))
                        InfoRow("Средний чек", formatMoney(profile.averageBill))
                        InfoRow("В этом месяце", formatMoney(profile.spentThisMonth))
                        InfoRow("В этом году", formatMoney(profile.spentThisYear))
                    }

                    // ============================================================
                    // БЛОК 4: Предпочтения
                    // ============================================================
                    SectionHeader("⭐ Предпочтения")
                    InfoCard {
                        if (profile.favoriteMasterName != null) InfoRow("Любимый мастер", profile.favoriteMasterName)
                        else InfoRow("Любимый мастер", "—")
                        if (profile.favoriteServiceName != null) InfoRow("Любимая услуга", profile.favoriteServiceName)
                        else InfoRow("Любимая услуга", "—")
                        InfoRow("Предпочитает", timeOfDayLabel(profile.preferredTimeOfDay))
                    }

                    // ============================================================
                    // БЛОК 5: История записей
                    // ============================================================
                    SectionHeader("📅 История записей (${profile.appointments?.size ?: 0})")

                    if (profile.appointments.isNullOrEmpty()) {
                        InfoCard {
                            Text("Нет записей", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        profile.appointments!!.forEach { appointment ->
                            AppointmentHistoryCard(appointment)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ========== Компоненты ==========

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun LoyaltyBadge(status: String?) {
    val (bgColor, text) = when (status) {
        "vip" -> Color(0xFFFFD700).copy(alpha = 0.2f) to "⭐ VIP-клиент"
        "problematic" -> Color(0xFFFF4444).copy(alpha = 0.2f) to "⚠ Проблемный"
        "sleeping" -> Color(0xFFAAAAAA).copy(alpha = 0.2f) to "💤 Спящий"
        "new" -> Color(0xFF4CAF50).copy(alpha = 0.2f) to "🆕 Новый"
        else -> Color.Transparent to "✅ Обычный"
    }
    Text(
        text,
        modifier = Modifier
            .background(bgColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun AppointmentHistoryCard(appointment: AppointmentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                appointment.cancelled == true -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                appointment.completed == true -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                appointment.confirmed == true -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    appointment.serviceName ?: "—",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (appointment.price != null) {
                    Text("${appointment.price.toInt()} ₽",
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Text("👤 ${appointment.masterName ?: "—"}", style = MaterialTheme.typography.bodySmall)
            Text("📅 ${formatDateTime(appointment.startTime)}", style = MaterialTheme.typography.bodySmall)
            Text(
                when {
                    appointment.cancelled == true -> "❌ Отменена"
                    appointment.completed == true -> "✅ Выполнена"
                    appointment.confirmed == true -> "🟡 Подтверждена"
                    else -> "⏳ Ожидает"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    appointment.cancelled == true -> MaterialTheme.colorScheme.error
                    appointment.completed == true -> MaterialTheme.colorScheme.primary
                    appointment.confirmed == true -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ========== Вспомогательные функции ==========

private fun genderLabel(gender: String?): String = when (gender) {
    "male" -> "Мужской"
    "female" -> "Женский"
    "other" -> "Другой"
    else -> "—"
}

private fun sourceLabel(source: String?): String = when (source) {
    "instagram" -> "Instagram"
    "recommendation" -> "Рекомендация"
    "search" -> "Поиск"
    "other" -> "Другое"
    else -> "—"
}

private fun loyaltyStatusLabel(status: String?): String = when (status) {
    "vip" -> "VIP-клиент"
    "problematic" -> "Проблемный"
    "sleeping" -> "Спящий"
    "new" -> "Новый"
    else -> "Обычный"
}