package ru.bookmaster.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun formatDateTime(isoString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoString.take(19))
        dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (e: Exception) {
        isoString
    }
}

private fun isAppointmentPassed(startTime: String): Boolean {
    return try {
        val dateTime = LocalDateTime.parse(startTime.take(19))
        dateTime.isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayAppointmentsScreen(
    dateString: String,
    onBack: () -> Unit,
    viewModel: DayAppointmentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(dateString) {
        viewModel.loadAppointmentsForDate(dateString)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val date = try {
                            LocalDate.parse(dateString)
                        } catch (e: Exception) { null }
                        if (date != null) {
                            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
                            Text(
                                "$dayOfWeek, ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                fontSize = 16.sp
                            )
                        } else {
                            Text(dateString, fontSize = 16.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF38BDF8)
                    )
                }
                uiState.appointments.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventBusy,
                                tint = Color(0xFF94A3B8),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Нет записей на этот день",
                                color = Color(0xFF94A3B8),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "${uiState.appointments.size} записей",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.appointments) { appointment ->
                            DayAppointmentItem(
                                appointment = appointment,
                                masters = uiState.masters,
                                onConfirm = { viewModel.confirmAppointment(appointment.id) },
                                onCancel = { viewModel.cancelAppointment(appointment.id) },
                                onEdit = { masterId, startTime -> viewModel.editAppointment(appointment.id, masterId, startTime) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayAppointmentItem(
    appointment: AppointmentResponse,
    masters: List<Master> = emptyList(),
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: (Long?, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val isPending = !appointment.confirmed!! && !appointment.cancelled!!
    val isConfirmed = appointment.confirmed!!
    val isCancelled = appointment.cancelled!!

    val statusColor = when {
        isCancelled -> Color(0xFFEF4444)
        isConfirmed -> Color(0xFF22C55E)
        else -> Color(0xFFFCD34D)
    }
    val statusText = when {
        isCancelled -> "Отменено"
        isConfirmed -> "Подтверждено"
        else -> "Ожидает"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        tint = Color(0xFF38BDF8),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formatDateTime(appointment.startTime),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Row {
                    // Кнопка редактирования — только для непрошедших записей
                    if (!isAppointmentPassed(appointment.startTime)) {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                tint = Color(0xFF94A3B8),
                                contentDescription = "Редактировать",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = statusColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    tint = Color(0xFF94A3B8),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${appointment.clientName} • ${appointment.clientPhone}",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Category,
                    tint = Color(0xFF94A3B8),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${appointment.serviceName} • ${appointment.masterName}",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Отменить", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Подтвердить", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditAppointmentDialog(
            appointment = appointment,
            masters = masters,
            onDismiss = { showEditDialog = false },
            onSave = { masterId, startTime ->
                onEdit(masterId, startTime)
                showEditDialog = false
            }
        )
    }
}