package ru.bookmaster.app.ui.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import ru.bookmaster.app.ui.theme.BookMasterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    onPremiumActivated: () -> Unit,
    viewModel: PremiumViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isPremiumActive) {
        if (uiState.isPremiumActive) {
            onPremiumActivated()
        }
    }

    BookMasterTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Подписка Premium", style = MaterialTheme.typography.titleMedium) },
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Расширьте возможности вашего бизнеса",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Подключите Premium и получите доступ ко всем функциям",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Бесплатный тариф
                PremiumCard(
                    title = "Бесплатный",
                    price = "0 ₽/мес",
                    features = listOf(
                        "До 1 сотрудника",
                        "Запись на 7 дней вперёд",
                        "До 3 услуг",
                        "Базовая поддержка"
                    ),
                    isPremium = false,
                    isSelected = false,
                    onSelect = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Premium тариф
                PremiumCard(
                    title = "Premium",
                    price = "499 ₽/мес",
                    features = listOf(
                        "✅ Безлимит сотрудников",
                        "✅ Запись на любой срок",
                        "✅ Безлимит услуг",
                        "✅ Напоминания клиентам",
                        "✅ Статистика и отчёты",
                        "✅ Приоритетная поддержка"
                    ),
                    isPremium = true,
                    isSelected = true,
                    onSelect = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.activatePremium() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF0F172A))
                    } else {
                        Text("Активировать Premium", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            uiState.error!!,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFFCA5A5),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCard(
    title: String,
    price: String,
    features: List<String>,
    isPremium: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFF1E293B) else Color(0xFF0F172A)
        ),
        border = if (isPremium) BorderStroke(2.dp, Color(0xFF38BDF8)) else BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isPremium) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                )
                if (isPremium) {
                    Text(
                        "Популярный",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .background(Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                price,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            features.forEach { feature ->
                Text(
                    feature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}