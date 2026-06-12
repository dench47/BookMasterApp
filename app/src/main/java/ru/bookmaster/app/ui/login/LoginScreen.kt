package ru.bookmaster.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    LaunchedEffect(state.shouldNavigateToRegister) {
        if (state.shouldNavigateToRegister) {
            onRegisterClick(state.phone)
        }
    }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(80.dp).background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Text("📱", fontSize = 40.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text("BookMaster", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Введите номер телефона\nдля входа",
                color = Color(0xFF94A3B8), fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                state.phone, viewModel::onPhoneChange,
                label = { Text("Номер телефона") },
                placeholder = { Text("+79001234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF475569),
                    focusedLabelColor = Color(0xFF38BDF8), unfocusedLabelColor = Color(0xFF94A3B8),
                    cursorColor = Color(0xFF38BDF8)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.login() },
                Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Войти", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { onRegisterClick(state.phone) }) {
                Text("Регистрация", color = Color(0xFF38BDF8))
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp)) {
                    Text(state.error!!, Modifier.padding(12.dp), color = Color(0xFFFCA5A5), fontSize = 14.sp)
                }
            }
        }
    }
}