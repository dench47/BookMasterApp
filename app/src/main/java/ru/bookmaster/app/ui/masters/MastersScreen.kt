package ru.bookmaster.app.ui.masters

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
fun MastersScreen(
    onBack: () -> Unit,
    onNavigateToMasterDetail: (Long) -> Unit,
    viewModel: MastersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMasters()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Сотрудники",
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
                        Button(onClick = { viewModel.loadMasters() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (uiState.masters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Сотрудников пока нет", color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Добавить сотрудника")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.masters,
                        key = { it.id }
                    ) { master ->
                        MasterCard(
                            master = master,
                            onToggleActive = {
                                viewModel.toggleActive(master.id)
                            },
                            onClick = { onNavigateToMasterDetail(master.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMasterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone, specialization ->
                viewModel.addMaster(name, phone, specialization)
                showAddDialog = false
            },
            isPremium = uiState.isPremium,
            currentCount = uiState.masters.size
        )
    }
}

@Composable
fun MasterCard(
    master: ru.bookmaster.app.data.model.Master,
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
            containerColor = if (master.active) Color(0xFF1E293B) else Color(0xFF0F172A)
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
                        if (master.active) "●" else "○",
                        color = if (master.active) Color(0xFF86EFAC) else Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        master.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (!master.specialization.isNullOrBlank()) {
                    Text(
                        master.specialization,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
                if (!master.phone.isNullOrBlank()) {
                    Text(
                        "📞 ${master.phone}",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
            TextButton(
                onClick = onToggleActive,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (master.active) Color(0xFF86EFAC) else Color(0xFF38BDF8)
                )
            ) {
                Text(if (master.active) "Активен" else "Активировать")
            }
        }
    }
}

@Composable
fun AddMasterDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String?, specialization: String?) -> Unit,
    isPremium: Boolean,
    currentCount: Int
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый сотрудник", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isPremium && currentCount >= 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF713F12).copy(alpha = 0.3f))
                    ) {
                        Text(
                            "🔒 Достигнут лимит (1 сотрудник). Подключите Premium.",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFFCD34D),
                            fontSize = 13.sp
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Специализация") },
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
                    if (name.isNotBlank()) {
                        onAdd(
                            name,
                            phone.takeIf { it.isNotBlank() },
                            specialization.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = name.isNotBlank()
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