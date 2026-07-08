package ru.bookmaster.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
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

private fun isAppointmentPassed(startTime: String): Boolean {
    return try {
        val dateTime = LocalDateTime.parse(startTime.take(19))
        dateTime.isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }
}

private fun getStartTimeOrNull(startTime: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(startTime.take(19))
    } catch (e: Exception) {
        null
    }
}

private fun getCancellationReasonText(reason: String?): String {
    return when (reason) {
        "SALON" -> "❌ Отменено салоном"
        "CLIENT" -> "❌ Отменено клиентом"
        "AUTO" -> "❌ Автоотмена — салон не успел подтвердить"
        else -> "❌ Отменено"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Будущие", "Прошедшие", "Отменённые")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAllAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание", fontSize = 16.sp) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                title,
                                color = if (pagerState.currentPage == index) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Будущие: только подтверждённые, не отменённые, в будущем; ближайшие — сверху
                        AppointmentsList(
                            appointments = uiState.appointments
                                .filter { a ->
                                    val startTime = getStartTimeOrNull(a.startTime)
                                    a.confirmed == true && a.cancelled == false &&
                                            startTime != null && startTime.isAfter(LocalDateTime.now())
                                }
                                .sortedBy { getStartTimeOrNull(it.startTime) },
                            showActions = true,
                            onConfirm = { viewModel.confirmAppointment(it) },
                            onCancel = { viewModel.cancelAppointment(it) },
                            onEdit = { id, masterId, startTime -> viewModel.editAppointment(id, masterId, startTime) },
                            masters = uiState.masters,
                            isLoading = uiState.isLoading
                        )
                    }
                    1 -> {
                        // Прошедшие: только подтверждённые, не отменённые, в прошлом; свежие — сверху
                        AppointmentsList(
                            appointments = uiState.appointments
                                .filter { a ->
                                    val startTime = getStartTimeOrNull(a.startTime)
                                    a.confirmed == true && a.cancelled == false &&
                                            startTime != null && !startTime.isAfter(LocalDateTime.now())
                                }
                                .sortedByDescending { getStartTimeOrNull(it.startTime) },
                            showActions = false,
                            onConfirm = {},
                            onCancel = {},
                            onEdit = { id, masterId, startTime -> viewModel.editAppointment(id, masterId, startTime) },
                            masters = uiState.masters,
                            isLoading = uiState.isLoading
                        )
                    }
                    2 -> {
                        // Отменённые: все cancelled == true; свежие отмены — сверху
                        CancelledAppointmentsList(
                            appointments = uiState.appointments
                                .filter { a -> a.cancelled == true }
                                .sortedByDescending { getStartTimeOrNull(it.startTime) },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentsList(
    appointments: List<AppointmentResponse>,
    showActions: Boolean,
    onConfirm: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onEdit: (Long, Long?, String?) -> Unit,
    masters: List<Master>,
    isLoading: Boolean
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF38BDF8))
            }
        }
        appointments.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventNote,
                        tint = Color(0xFF94A3B8),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Нет записей",
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
                items(appointments) { appointment ->
                    AppointmentsListItem(
                        appointment = appointment,
                        showActions = showActions,
                        onConfirm = { onConfirm(appointment.id) },
                        onCancel = { onCancel(appointment.id) },
                        onEdit = { masterId, startTime -> onEdit(appointment.id, masterId, startTime) },
                        masters = masters
                    )
                }
            }
        }
    }
}

@Composable
fun CancelledAppointmentsList(
    appointments: List<AppointmentResponse>,
    isLoading: Boolean
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF38BDF8))
            }
        }
        appointments.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CancelScheduleSend,
                        tint = Color(0xFF94A3B8),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Нет отменённых записей",
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
                items(appointments) { appointment ->
                    CancelledAppointmentListItem(
                        appointment = appointment
                    )
                }
            }
        }
    }
}

@Composable
fun CancelledAppointmentListItem(
    appointment: AppointmentResponse
) {
    val cancellationText = getCancellationReasonText(appointment.cancellationReason)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Cancel,
                        tint = Color(0xFFEF4444),
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        cancellationText.removePrefix("❌ "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
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
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    tint = Color(0xFFFCA5A5),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    cancellationText,
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AppointmentsListItem(
    appointment: AppointmentResponse,
    showActions: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: (Long?, String?) -> Unit,
    masters: List<Master> = emptyList()
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
        isConfirmed && isAppointmentPassed(appointment.startTime) -> "Завершено"
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

            if (showActions && isPending) {
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