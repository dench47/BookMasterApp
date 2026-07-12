package ru.bookmaster.app.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun categoryLabel(category: String): String = when (category) {
    "RENT" -> "Аренда"
    "ADVERTISING" -> "Реклама"
    "MATERIALS" -> "Материалы"
    "SALARY" -> "Зарплата"
    else -> "Прочее"
}

private fun categoryIcon(category: String) = when (category) {
    "RENT" -> Icons.Default.House
    "ADVERTISING" -> Icons.Default.Campaign
    "MATERIALS" -> Icons.Default.Inventory
    "SALARY" -> Icons.Default.Person
    else -> Icons.Default.ShoppingCart
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    companyId: Long,
    onBack: () -> Unit,
    viewModel: ExpensesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(companyId) {
        viewModel.init(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расходы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddForm() }) {
                        Icon(Icons.Default.Add, "Добавить расход")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                }
                uiState.expenses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет расходов", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    // Сумма за месяц
                    val total = uiState.expenses.sumOf { it.amount }
                    Card(
                        Modifier.fillMaxWidth().padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            "Всего за период: %,.0f ₽".format(total),
                            Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    LazyColumn(Modifier.weight(1f)) {
                        items(uiState.expenses) { expense ->
                            Card(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        categoryIcon(expense.category),
                                        null,
                                        Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(categoryLabel(expense.category), fontWeight = FontWeight.Bold)
                                        if (expense.description.isNotBlank()) Text(
                                            expense.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            formatDate(expense.date),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "%,.0f ₽".format(expense.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { viewModel.deleteExpense(expense.id) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Удалить",
                                            Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Диалог добавления
        if (uiState.showAddForm) {
            AddExpenseDialog(
                onDismiss = { viewModel.hideAddForm() },
                onAdd = { amount, cat, desc, date -> viewModel.addExpense(amount, cat, desc, date) },
                isSaving = uiState.savingExpense
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit,
    isSaving: Boolean
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("OTHER") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }

    val categories = listOf("RENT", "ADVERTISING", "MATERIALS", "SALARY", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить расход") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(amount, { amount = it }, label = { Text("Сумма") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Описание") }, singleLine = true)
                OutlinedTextField(date, { date = it }, label = { Text("Дата (YYYY-MM-DD)") }, singleLine = true)

                Text("Категория:", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(categoryLabel(cat), fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(amount, category, description, date) }, enabled = !isSaving && amount.isNotBlank()) {
                if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun formatDate(dateStr: String): String {
    return try {
        val parts = dateStr.take(10).split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else dateStr
    } catch (e: Exception) { dateStr }
}