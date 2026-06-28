package ru.bookmaster.app.ui.masters

import android.annotation.SuppressLint
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
import java.time.LocalTime
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TimePickerLayoutType




@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDetailScreen(
    masterId: Long,
    onBack: () -> Unit,
    viewModel: MasterDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddBreakDialog by remember { mutableStateOf(false) }

    LaunchedEffect(masterId) {
        viewModel.loadMaster(masterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Редактирование сотрудника",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        Button(onClick = { viewModel.loadMaster(masterId) }) {
                            Text("Повторить")
                        }
                    }
                }
            } else {
                // ===== 1. Личные данные =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👤 Личные данные", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    uiState.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                TextButton(onClick = { /* TODO: загрузить фото */ }) {
                                    Text("Загрузить фото", color = Color(0xFF38BDF8))
                                }
                                if (uiState.photo.isNotBlank()) {
                                    Text("Фото загружено", color = Color(0xFF86EFAC), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("ФИО *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updatePhone(it) },
                            label = { Text("Телефон") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("Описание") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== 2. Услуги =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💇 Услуги", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.services.isEmpty()) {
                            Text("Нет доступных услуг", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            uiState.services.forEach { service ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = service.assigned,
                                        onCheckedChange = { viewModel.toggleServiceAssignment(service.id) }
                                    )
                                    Text(
                                        "${service.name} — ${service.price.toInt()} ₽ (${service.durationMinutes} мин)",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

// ===== 3. График работы =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🕐 График работы", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

                        uiState.weekDays.forEachIndexed { index, day ->
                            var showStartPicker by remember { mutableStateOf(false) }
                            var showEndPicker by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dayNames.getOrElse(day.dayOfWeek - 1) { day.dayName },
                                    color = if (day.isWorking) Color(0xFFE2E8F0) else Color(0xFF64748B),
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                if (day.isWorking) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = day.workStart,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable { showStartPicker = true }
                                        )
                                        Text(" - ", color = Color(0xFF94A3B8))
                                        Text(
                                            text = day.workEnd,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable { showEndPicker = true }
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Выходной",
                                        color = Color(0xFF64748B),
                                        fontSize = 16.sp
                                    )
                                }

                                Switch(
                                    checked = day.isWorking,
                                    onCheckedChange = { viewModel.toggleWeekDay(day.dayOfWeek) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF38BDF8),
                                        checkedTrackColor = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        uncheckedThumbColor = Color(0xFF64748B),
                                        uncheckedTrackColor = Color(0xFF334155)
                                    )
                                )
                            }

                            if (showStartPicker) {
                                MaterialTimePickerDialog(
                                    initialHour = day.workStart.split(":").firstOrNull()?.toIntOrNull() ?: 9,
                                    initialMinute = day.workStart.split(":").lastOrNull()?.toIntOrNull() ?: 0,
                                    onDismiss = { showStartPicker = false },
                                    onConfirm = { hour, minute ->
                                        val formattedTime = String.format("%02d:%02d", hour, minute)
                                        viewModel.updateWeekDay(day.dayOfWeek, "workStart", formattedTime)
                                        showStartPicker = false
                                    }
                                )
                            }

                            if (showEndPicker) {
                                MaterialTimePickerDialog(
                                    initialHour = day.workEnd.split(":").firstOrNull()?.toIntOrNull() ?: 18,
                                    initialMinute = day.workEnd.split(":").lastOrNull()?.toIntOrNull() ?: 0,
                                    onDismiss = { showEndPicker = false },
                                    onConfirm = { hour, minute ->
                                        val formattedTime = String.format("%02d:%02d", hour, minute)
                                        viewModel.updateWeekDay(day.dayOfWeek, "workEnd", formattedTime)
                                        showEndPicker = false
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
                        }
                    }
                }
                // ===== 4. Выходные дни (календарь) =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📅 Выходные дни", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.prevMonth() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color(0xFF38BDF8))
                                }
                                Text(
                                    viewModel.getMonthLabel(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { viewModel.nextMonth() }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Вперед", tint = Color(0xFF38BDF8))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            days.forEach { day ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                            }
                        }

                        uiState.calendarDays.chunked(7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                week.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .background(
                                                if (day.isWorking == false) Color(0xFF7F1D1D).copy(alpha = 0.3f)
                                                else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = !day.empty) {
                                                if (!day.empty) viewModel.toggleDayOff(day.date)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day.empty) {
                                            // Пустая ячейка
                                        } else {
                                            Text(
                                                day.label,
                                                color = if (day.isWorking == false) Color(0xFFFCA5A5) else Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== 5. Настройки онлайн-записи =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚙️ Настройки онлайн-записи", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Шаг времени (мин):", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            OutlinedTextField(
                                value = uiState.timeStep.toString(),
                                onValueChange = { viewModel.updateTimeStep(it) },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ограничение записи:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            var expanded by remember { mutableStateOf(false) }
                            val options = listOf("none", "today_tomorrow", "today", "12h", "4h", "3h", "2h", "1h")
                            val labels = mapOf(
                                "none" to "Нет ограничений",
                                "today_tomorrow" to "Сегодня + завтра",
                                "today" to "Сегодня",
                                "12h" to "Менее 12 часов",
                                "4h" to "Менее 4 часов",
                                "3h" to "Менее 3 часов",
                                "2h" to "Менее 2 часов",
                                "1h" to "Менее 1 часа"
                            )
                            Box {
                                OutlinedButton(onClick = { expanded = true }) {
                                    Text(labels[uiState.bookingLimit] ?: uiState.bookingLimit, color = Color.White)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(labels[option] ?: option, color = Color.White) },
                                            onClick = {
                                                viewModel.updateBookingLimit(option)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Прилипание времени:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Switch(
                                checked = uiState.stickTime,
                                onCheckedChange = { viewModel.updateStickTime(it) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== 6. Перерывы =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("☕ Перерывы", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { showAddBreakDialog = true }) {
                                Text("+ Добавить", color = Color(0xFF38BDF8))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.breaks.isEmpty()) {
                            Text("Нет перерывов", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            uiState.breaks.forEach { b ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${b.label} ${b.startTime}-${b.endTime} ${b.description ?: ""}",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 13.sp
                                    )
                                    IconButton(onClick = { viewModel.deleteBreak(b.id) }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFCA5A5))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== 7. Кнопка сохранения =====
                Button(
                    onClick = { viewModel.saveAllSchedule(onBack) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0F172A))
                    } else {
                        Text("💾 Сохранить", color = Color(0xFF0F172A))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ===== 8. Кнопка удаления =====
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Удалить сотрудника", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // ===== Диалог удаления =====
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить сотрудника?", color = Color.White) },
            text = {},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMaster()
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Удалить", color = Color(0xFFFCA5A5))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // ===== Диалог добавления перерыва =====
    if (showAddBreakDialog) {
        AddBreakDialog(
            onDismiss = { showAddBreakDialog = false },
            onAdd = { dayOfWeek, breakDate, startTime, endTime, description ->
                viewModel.addBreak(dayOfWeek, breakDate, startTime, endTime, description)
                showAddBreakDialog = false
            }
        )
    }
}

// ===== Диалог добавления перерыва =====
@Composable
fun AddBreakDialog(
    onDismiss: () -> Unit,
    onAdd: (dayOfWeek: Int?, breakDate: String?, startTime: String, endTime: String, description: String?) -> Unit
) {
    var breakType by remember { mutableStateOf("all") }
    var selectedDay by remember { mutableStateOf(1) }
    var breakDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("13:00") }
    var endTime by remember { mutableStateOf("14:00") }
    var description by remember { mutableStateOf("") }

    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить перерыв", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = breakType == "all",
                        onClick = { breakType = "all" },
                        label = { Text("Все дни", color = if (breakType == "all") Color.White else Color(0xFF94A3B8)) }
                    )
                    FilterChip(
                        selected = breakType == "weekday",
                        onClick = { breakType = "weekday" },
                        label = { Text("День недели", color = if (breakType == "weekday") Color.White else Color(0xFF94A3B8)) }
                    )
                    FilterChip(
                        selected = breakType == "date",
                        onClick = { breakType = "date" },
                        label = { Text("Дата", color = if (breakType == "date") Color.White else Color(0xFF94A3B8)) }
                    )
                }

                if (breakType == "weekday") {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        days.forEachIndexed { index, name ->
                            FilterChip(
                                selected = selectedDay == index + 1,
                                onClick = { selectedDay = index + 1 },
                                label = { Text(name, color = if (selectedDay == index + 1) Color.White else Color(0xFF94A3B8)) }
                            )
                        }
                    }
                }

                if (breakType == "date") {
                    OutlinedTextField(
                        value = breakDate,
                        onValueChange = { breakDate = it },
                        label = { Text("Дата (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Начало") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Конец") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
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
                    when (breakType) {
                        "all" -> onAdd(null, null, startTime, endTime, description.takeIf { it.isNotBlank() })
                        "weekday" -> onAdd(selectedDay, null, startTime, endTime, description.takeIf { it.isNotBlank() })
                        "date" -> {
                            if (breakDate.isNotBlank()) {
                                onAdd(null, breakDate, startTime, endTime, description.takeIf { it.isNotBlank() })
                            }
                        }
                    }
                }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = state,
                    layoutType = TimePickerLayoutType.Vertical
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = Color(0xFF94A3B8))
                    }
                    TextButton(
                        onClick = {
                            onConfirm(state.hour, state.minute)
                            onDismiss()
                        }
                    ) {
                        Text("Готово", color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}
