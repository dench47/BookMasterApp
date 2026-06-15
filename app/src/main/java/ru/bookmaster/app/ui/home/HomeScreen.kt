
package ru.bookmaster.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
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

                // Расписание
                NavigationDrawerItem(
                    label = { Text("Расписание", color = Color.White) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSchedule()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )

                // Сообщения
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

                // Уведомления клиентам
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

                // Клиенты
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

                // Сотрудники
                if (!uiState.isMaster) {
                    NavigationDrawerItem(
                        label = { Text("Сотрудники", color = Color.White) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
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

                // Финансы и статистика
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

                // Расходы
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

                // Галерея
                NavigationDrawerItem(
                    label = { Text("Галерея", color = Color.White) },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToGallery()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f)
                    )
                )


                // Настройки
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
                    onScheduleClick = { /* TODO */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotificationsCard(newEventsCount = 0)

                Spacer(modifier = Modifier.height(16.dp))

                WeekStatsCard(
                    weekStats = uiState.weekStats,
                    totalWeekAppointments = uiState.weekStats.sumOf { it.appointments }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ClientsCard(
                    totalClients = uiState.totalClients,
                    newClientsThisMonth = uiState.newClientsThisMonth,
                    sleepingClients = uiState.sleepingClients,
                    onViewClick = onNavigateToClients
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.isMaster) {
                    MastersCard(
                        totalMasters = uiState.totalMasters,
                        activeMasters = uiState.activeMasters,
                        onAddClick = onNavigateToMasters
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                WebBookingCard(
                    url = uiState.webBookingUrl,
                    onShareClick = onShareWebLink
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
fun NotificationsCard(newEventsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Notifications, tint = Color(0xFFFCD34D), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("$newEventsCount новых событий", color = Color(0xFF94A3B8), fontSize = 14.sp)
        }
    }
}

@Composable
fun WeekStatsCard(
    weekStats: List<WeekDayStat>,
    totalWeekAppointments: Int
) {
    if (weekStats.isEmpty()) return

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekStats.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

@Composable
fun ClientsCard(
    totalClients: Int,
    newClientsThisMonth: Int,
    sleepingClients: Int,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$totalClients клиент(ов) в базе", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Смотреть →", color = Color(0xFF38BDF8), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("$newClientsThisMonth", color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                    Text("новых", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Column {
                    Text("$sleepingClients", color = Color(0xFFFCD34D), fontWeight = FontWeight.Bold)
                    Text("спящих", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MastersCard(
    totalMasters: Int,
    activeMasters: Int,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$totalMasters сотрудник(ов)", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Добавить →", color = Color(0xFF38BDF8), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Активных: $activeMasters", color = Color(0xFF86EFAC), fontSize = 14.sp)
        }
    }
}

@Composable
fun WebBookingCard(url: String, onShareClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ваша веб-страничка записи!", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(url, color = Color(0xFF38BDF8), fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("поделиться", color = Color(0xFF0F172A))
            }
        }
    }
}