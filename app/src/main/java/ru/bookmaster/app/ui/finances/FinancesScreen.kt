package ru.bookmaster.app.ui.finances

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    companyId: Long,
    onBack: () -> Unit,
    viewModel: FinancesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(companyId) {
        viewModel.init(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Финансы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Карточки: сегодня и месяц
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Сегодня", "Выручка", "${uiState.todayRevenue} ₽", Color(0xFF38BDF8), Modifier.weight(1f))
                        StatCard("Сегодня", "Чистая", "${uiState.todayNetProfit} ₽", Color(0xFF4CAF50), Modifier.weight(1f))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Месяц", "Выручка", "${uiState.monthRevenue} ₽", Color(0xFF38BDF8), Modifier.weight(1f))
                        StatCard("Месяц", "Чистая", "${uiState.monthNetProfit} ₽", Color(0xFF4CAF50), Modifier.weight(1f))
                    }
                }

                // График выручки по дням
                if (uiState.dailyRevenue.isNotEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Выручка по дням", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                                    val maxVal = uiState.dailyRevenue.maxOf { it.second }.let { if (it == 0.0) 1.0 else it }
                                    val stepX = size.width / (uiState.dailyRevenue.size - 1).coerceAtLeast(1)
                                    var prevX = 0f
                                    var prevY = 0f
                                    uiState.dailyRevenue.forEachIndexed { index, pair ->
                                        val x = stepX * index
                                        val y = size.height - (pair.second / maxVal * size.height).toFloat()
                                        if (index > 0) {
                                            drawLine(Color(0xFF38BDF8), Offset(prevX, prevY), Offset(x, y), strokeWidth = 3f)
                                        }
                                        drawCircle(Color(0xFF38BDF8), 4f, Offset(x, y))
                                        prevX = x; prevY = y
                                    }
                                }
                            }
                        }
                    }
                }

                // Топ-5 услуг
                if (uiState.topServices.isNotEmpty()) {
                    item { SectionTitle("Топ услуг") }
                    items(uiState.topServices) { (name, count) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("$count зап.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Топ-5 мастеров
                if (uiState.topMasters.isNotEmpty()) {
                    item { SectionTitle("Топ мастеров") }
                    items(uiState.topMasters) { (name, count) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("$count зап.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(period: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(12.dp)) {
            Text(period, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
}