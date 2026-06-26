package ru.bookmaster.app.ui.services

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onBack: () -> Unit,
    onNavigateToServiceDetail: (Long) -> Unit,
    viewModel: ServicesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Услуги",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌ ${uiState.error}", color = Color(0xFFFCA5A5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadServices() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (uiState.services.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💇", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Услуг пока нет", color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Добавить услугу")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.services,
                        key = { it.id }  // ← ключ для правильного обновления
                    ) { service ->
                        ServiceCard(
                            service = service,
                            onToggleActive = {
                                viewModel.toggleActive(service.id)  // ← только для этой услуги
                            },
                            onClick = { onNavigateToServiceDetail(service.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, description, price, duration ->
                viewModel.addService(name, description, price, duration)
                showAddDialog = false
            },
            isPremium = uiState.isPremium,
            currentCount = uiState.services.size
        )
    }
}

@Composable
fun ServiceCard(
    service: ru.bookmaster.app.data.model.ServiceModel,
    onToggleActive: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (service.active) Color(0xFF1E293B) else Color(0xFF0F172A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (service.active) "●" else "○",
                        color = if (service.active) Color(0xFF86EFAC) else Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        service.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (!service.description.isNullOrBlank()) {
                    Text(
                        service.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "${service.price.toInt()} ₽",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${service.durationMinutes} мин",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }
            TextButton(
                onClick = onToggleActive,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (service.active) Color(0xFF86EFAC) else Color(0xFF38BDF8)
                )
            ) {
                Text(if (service.active) "Активна" else "Активировать")
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, description: String?, price: Double, duration: Int) -> Unit,
    isPremium: Boolean,
    currentCount: Int
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая услуга", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isPremium && currentCount >= 3) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF713F12).copy(alpha = 0.3f))
                    ) {
                        Text(
                            "🔒 Достигнут лимит (3 услуги). Подключите Premium.",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFFCD34D),
                            fontSize = 13.sp
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Длительность (мин) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = price.toDoubleOrNull()
                    val durationVal = duration.toIntOrNull()
                    if (name.isNotBlank() && priceVal != null && durationVal != null) {
                        onAdd(name, description.takeIf { it.isNotBlank() }, priceVal, durationVal)
                    }
                },
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null && duration.toIntOrNull() != null
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}