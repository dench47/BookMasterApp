package ru.bookmaster.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

    // BottomSheet для ожидающих записей
    if (uiState.isPendingSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hidePendingSheet() },
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) {
            PendingAppointmentsSheetContent(
                appointments = uiState.pendingAppointments.filter { !it.confirmed!! && !it.cancelled!! },
                masters = uiState.masters,
                onConfirm = { viewModel.confirmAppointment(it) },
                onCancel = { viewModel.cancelAppointment(it) },
                onEdit = { id, masterId, startTime -> viewModel.editAppointment(id, masterId, startTime) },
                onMarkAllViewed = { viewModel.markAllViewed() },
                onDismiss = { viewModel.hidePendingSheet() }
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
                    onFreeSlotsClick = { /* TODO */ },
                    onScheduleClick = { onNavigateToAllAppointments() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PendingAppointmentsCard(
                    newEventsCount = uiState.newEventsCount,
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
    appointments: List<AppointmentResponse>,
    masters: List<Master>,
    onConfirm: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onEdit: (Long, Long?, String?) -> Unit,
    onMarkAllViewed: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ожидающие записи",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (appointments.isNotEmpty()) {
                TextButton(onClick = {
                    onMarkAllViewed()
                    onDismiss()
                }) {
                    Text("Отметить все", color = Color(0xFF38BDF8))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${appointments.size} записей ожидают подтверждения",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет ожидающих записей", color = Color(0xFF94A3B8))
            }
        } else {
            appointments.forEach { appointment ->
                PendingAppointmentItem(
                    appointment = appointment,
                    masters = masters,
                    onConfirm = { onConfirm(appointment.id) },
                    onCancel = { onCancel(appointment.id) },
                    onEdit = { masterId, startTime -> onEdit(appointment.id, masterId, startTime) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PendingAppointmentItem(
    appointment: AppointmentResponse,
    masters: List<Master>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: (Long?, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    appointment.clientName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Row {
                    // Кнопка редактирования
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFCD34D).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Ожидает",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color(0xFFFCD34D),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о записи
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    tint = Color(0xFF94A3B8),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    appointment.clientPhone,
                    color = Color(0xFF94A3B8),
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
                    Icons.Default.Schedule,
                    tint = Color(0xFF94A3B8),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    appointment.startTime,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки действий
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
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Отменить", fontSize = 12.sp)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Подтвердить", fontSize = 12.sp)
                }
            }
        }
    }

    // Диалог редактирования
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    appointment: AppointmentResponse,
    masters: List<Master>,
    onDismiss: () -> Unit,
    onSave: (Long?, String?) -> Unit
) {
    var selectedMasterId by remember { mutableStateOf<Long?>(appointment.masterId) }
    var selectedTime by remember { mutableStateOf(appointment.startTime.take(16)) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text("Редактировать запись", color = Color.White) },
        text = {
            Column {
                // Клиент
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${appointment.clientName} • ${appointment.clientPhone}", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Услуга
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, tint = Color(0xFF94A3B8), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appointment.serviceName, color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Выбор мастера
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

                // Время
                Text("Время", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    placeholder = { Text("ГГГГ-ММ-ДДTЧЧ:ММ", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    singleLine = true
                )
                Text("Формат: 2026-07-04T14:30", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedMasterId, if (selectedTime != appointment.startTime.take(16)) selectedTime else null) },
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
