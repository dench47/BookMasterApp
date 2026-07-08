package ru.bookmaster.app.ui.cabinet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabinetScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToMasters: () -> Unit,
    onNavigateToServices: () -> Unit,
    onShareWebLink: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: CabinetViewModel = viewModel() // <--- Будет передан из MainActivity
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Кабинет",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
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
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Блок 1: Услуги
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToServices() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Услуги", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${uiState.servicesCount} услуги", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Блок 3: Клиенты
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToClients() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Клиенты", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${uiState.totalClients} клиентов", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Блок 4: Сотрудники
            if (!uiState.isMaster) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMasters() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Сотрудники", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${uiState.totalMasters} сотрудников", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Блок 5: Веб-страничка
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Веб-страничка записи", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(uiState.webBookingUrl, color = Color(0xFF38BDF8), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onShareWebLink) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = Color(0xFF38BDF8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Блок 6: Premium статус
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isPremium) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (uiState.isPremium) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (uiState.isPremium) Color(0xFF38BDF8) else Color(0xFFFCD34D)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (uiState.isPremium) "Premium активен" else "Бесплатный тариф",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (uiState.isPremium) "Доступны все функции" else "Ограниченные возможности",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (!uiState.isPremium) {
                        Button(
                            onClick = onNavigateToPremium,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Подключить", color = Color(0xFF0F172A), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Блок 7: Уведомления
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Уведомления", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text("Скоро", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка выхода
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти", color = Color.White)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp
        )
        Text(
            value ?: "-",
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}