package ru.bookmaster.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.ui.masters.CustomWheelTimePickerDialog
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private fun formatDateTime(isoString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoString.take(19))
        dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (_: Exception) {
        isoString
    }
}

private fun isAppointmentPassed(startTime: String): Boolean {
    return try {
        val dateTime = LocalDateTime.parse(startTime.take(19))
        dateTime.isBefore(LocalDateTime.now())
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToClients: () -> Unit,
    onNavigateToMasters: () -> Unit,
    onShareWebLink: () -> Unit,
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToFinances: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDayAppointments: (String) -> Unit = {},
    onNavigateToAllAppointments: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (uiState.isServerError) {
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
                    uiState.serverErrorMessage ?: "Сервер недоступен",
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.refresh() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Повторить")
                }
            }
        }
        return
    }

    // BottomSheet для событий
    if (uiState.isPendingSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hidePendingSheet() },
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) {
            PendingAppointmentsSheetContent(
                pendingAppointments = uiState.pendingAppointments.filter { !it.confirmed!! && !it.cancelled!! },
                cancelledAppointments = uiState.cancelledAppointments,
                onConfirm = { viewModel.confirmAppointment(it) },
                onCancel = { viewModel.cancelAppointment(it) },
                onDismissCancelled = { viewModel.dismissCancelledAppointment(it) }
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E293B),
                drawerContentColor = Color.White
            ) {
                Text(
                    "Меню",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                HorizontalDivider(color = Color(0xFF334155))

                NavigationDrawerItem(
                    label = { Text("Расписание", color = Color.White) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAllAppointments()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Сообщения", color = Color.White) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToMessages()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Уведомления клиентам", color = Color.White) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToNotifications()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Клиенты", color = Color.White) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToClients()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                if (!uiState.isMaster) {
                    NavigationDrawerItem(
                        label = { Text("Сотрудники", color = Color.White) },
                        icon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToMasters()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                        )
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Финансы и статистика", color = Color.White) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToFinances()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Расходы", color = Color.White) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToExpenses()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Галерея", color = Color.White) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToGallery()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Настройки", color = Color.White) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            uiState.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 85.dp)
            ) {
                TodayCard(
                    date = uiState.todayDate,
                    appointments = uiState.todayAppointments,
                    revenue = uiState.todayRevenue,
                    onFreeSlotsClick = { },
                    onScheduleClick = { onNavigateToAllAppointments() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PendingAppointmentsCard(
                    newEventsCount = uiState.totalEventsCount,
                    onClick = { viewModel.showPendingSheet() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                WeekStatsCard(
                    weekStats = uiState.weekStats,
                    totalWeekAppointments = uiState.weekStats.sumOf { it.appointments },
                    onDayClick = { date -> onNavigateToDayAppointments(date) }
                )
            }
        }
    }
}

@Composable
fun TodayCard(
    date: String,
    appointments: Int,
    revenue: String,
    onFreeSlotsClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                date,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$appointments",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("записей клиентов", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        revenue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC)
                    )
                    Text("выручка", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onFreeSlotsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("свободные окна")
                }
                OutlinedButton(
                    onClick = onScheduleClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("расписание")
                }
            }
        }
    }
}

@Composable
fun PendingAppointmentsCard(
    newEventsCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (newEventsCount > 0) Color(0xFF1E3A5F) else Color(0xFF0F172A)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Notifications,
                tint = if (newEventsCount > 0) Color(0xFFFCD34D) else Color(0xFF94A3B8),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$newEventsCount новых событий",
                color = if (newEventsCount > 0) Color.White else Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = if (newEventsCount > 0) FontWeight.Bold else FontWeight.Normal
            )
            if (newEventsCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    tint = Color(0xFF38BDF8),
                    contentDescription = "Подробнее"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingAppointmentsSheetContent(
    pendingAppointments: List<AppointmentResponse>,
    cancelledAppointments: List<AppointmentResponse>,
    onConfirm: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onDismissCancelled: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "События",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${pendingAppointments.size + cancelledAppointments.size} событий",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingAppointments.isEmpty() && cancelledAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет новых событий", color = Color(0xFF94A3B8))
            }
        } else {
            // Новые записи
            pendingAppointments.forEach { appointment ->
                PendingAppointmentItem(
                    appointment = appointment,
                    onConfirm = { onConfirm(appointment.id) },
                    onCancel = { onCancel(appointment.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Отмены клиентом
            cancelledAppointments.forEach { appointment ->
                key(appointment.id) {
                    CancelledAppointmentItem(
                        appointment = appointment,
                        onDismissed = { onDismissCancelled(appointment.id) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingAppointmentItem(
    appointment: AppointmentResponse,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var opened by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var totalShift by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val paddingPx = with(density) { 12.dp.toPx() }

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(end = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { opened = false; offsetX = 0f; onConfirm() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.onSizeChanged { size ->
                        totalShift = size.width.toFloat() + paddingPx * 2
                    }
                ) { Text("Подтвердить", fontSize = 13.sp, color = Color(0xFF0F172A)) }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { opened = false; offsetX = 0f; onCancel() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.onSizeChanged { size ->
                        if (size.width.toFloat() + paddingPx * 2 > totalShift) {
                            totalShift = size.width.toFloat() + paddingPx * 2
                        }
                    }
                ) { Text("Отменить", fontSize = 13.sp) }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.toInt().coerceIn(-totalShift.toInt(), 0), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -totalShift * 0.5f) {
                                opened = true
                                offsetX = -totalShift
                            } else {
                                opened = false
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {},
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-totalShift, 0f)
                        }
                    )
                }
                .clickable {
                    if (opened) {
                        opened = false
                        offsetX = 0f
                    } else {
                        opened = true
                        offsetX = -totalShift
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(appointment.clientName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFCD34D).copy(alpha = 0.2f)) {
                        Text("Ожидает", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFFFCD34D), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${appointment.serviceName} • ${appointment.masterName}", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatDateTime(appointment.startTime), color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelledAppointmentItem(
    appointment: AppointmentResponse,
    onDismissed: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismissed()
                false  // возвращаем false, не даём анимации SwipeToDismiss завершиться
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Скрыть",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Cancel,
                    tint = Color(0xFFFCA5A5),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "❌ ${appointment.clientName} отменил(а) запись",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${appointment.serviceName} • ${appointment.masterName}",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Text(
                        formatDateTime(appointment.startTime),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    appointment: AppointmentResponse,
    masters: List<Master>,
    onDismiss: () -> Unit,
    onSave: (Long?, String?) -> Unit
) {
    val initialDateTime = remember {
        try {
            LocalDateTime.parse(appointment.startTime.take(19))
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }
    var selectedMasterId by remember { mutableStateOf<Long?>(appointment.masterId) }
    var expanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text("Редактировать запись", color = Color.White) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${appointment.clientName} • ${appointment.clientPhone}", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appointment.serviceName, color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Мастер", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (masters.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = masters.find { it.id == selectedMasterId }?.name ?: appointment.masterName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            masters.forEach { master ->
                                DropdownMenuItem(
                                    text = { Text(master.name, color = Color.White) },
                                    onClick = {
                                        selectedMasterId = master.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(appointment.masterName, color = Color(0xFF94A3B8), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Дата и время", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedDate.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Дата", color = Color(0xFF94A3B8)) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Выбрать дату",
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color(0xFF334155),
                                disabledLabelColor = Color(0xFF94A3B8),
                                disabledTrailingIconColor = Color(0xFF38BDF8)
                            ),
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedTime.format(timeFormatter),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Время", color = Color(0xFF94A3B8)) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Выбрать время",
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color(0xFF334155),
                                disabledLabelColor = Color(0xFF94A3B8),
                                disabledTrailingIconColor = Color(0xFF38BDF8)
                            ),
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate()
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK", color = Color(0xFF38BDF8))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Отмена", color = Color(0xFF94A3B8))
                            }
                        },
                        colors = DatePickerDefaults.colors(containerColor = Color(0xFF1E293B))
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showTimePicker) {
                    CustomWheelTimePickerDialog(
                        initialValue = selectedTime.format(timeFormatter),
                        onDismiss = { showTimePicker = false },
                        onConfirm = { newTime ->
                            val parts = newTime.split(":").map { it.toIntOrNull() ?: 0 }
                            selectedTime = LocalTime.of(parts.getOrElse(0) { 0 }, parts.getOrElse(1) { 0 })
                            showTimePicker = false
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newDateTime = LocalDateTime.of(selectedDate, selectedTime)
                    val newStartTime = newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                    onSave(
                        selectedMasterId,
                        if (newStartTime != appointment.startTime.take(16)) newStartTime else null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun WeekStatsCard(
    weekStats: List<WeekDayStat>,
    totalWeekAppointments: Int,
    onDayClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Записи на неделе", color = Color.White, fontWeight = FontWeight.Bold)
                Text("$totalWeekAppointments записей", color = Color(0xFF38BDF8))
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (weekStats.isEmpty()) {
                Text(
                    "Нет данных на эту неделю",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekStats.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onDayClick(day.date) }
                        ) {
                            Text(
                                day.dayOfWeekShort,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (day.appointments > 0) Color(0xFF38BDF8).copy(alpha = 0.2f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day.date.split("-").last(),
                                    color = if (day.appointments > 0) Color(0xFF38BDF8) else Color.White,
                                    fontWeight = if (day.appointments > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Text(
                                "${day.appointments}",
                                color = Color(0xFF86EFAC),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}