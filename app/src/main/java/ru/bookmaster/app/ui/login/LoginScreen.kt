package ru.bookmaster.app.ui.login

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
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
                if (state.isCalling) "Позвоните на номер ниже\nдля входа в аккаунт"
                else "Введите номер телефона\nдля входа или регистрации",
                color = Color(0xFF94A3B8), fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))

            if (!state.isCalling) {
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
                TextButton(onClick = onRegisterClick) {
                    Text("Регистрация", color = Color(0xFF38BDF8))
                }
            } else {
                Text("Номер для звонка", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(state.callPhone, style = MaterialTheme.typography.headlineLarge, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 32.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${state.callPhone}"))
                        context.startActivity(intent)
                    },
                    Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📞 Позвонить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text("После звонка вернитесь в приложение\nдля автоматического входа", color = Color(0xFF64748B), fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Звонок бесплатный, даже в роуминге", color = Color(0xFF86EFAC), fontSize = 12.sp)
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