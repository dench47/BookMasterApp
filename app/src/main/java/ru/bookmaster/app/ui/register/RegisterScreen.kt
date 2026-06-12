package ru.bookmaster.app.ui.register

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    initialPhone: String = "",
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher для звонка
    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    // Launcher для запроса разрешения
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${uiState.callPhone}"))
            callLauncher.launch(intent)
        }
    }

    LaunchedEffect(initialPhone) {
        if (initialPhone.isNotBlank() && uiState.phone == "+7") {
            viewModel.onPhoneChange(initialPhone)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isCalling) {
            // Экран звонка
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(80.dp).background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("📞", fontSize = 40.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("Подтверждение номера", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Позвоните на номер ниже\nдля подтверждения регистрации", color = Color(0xFF94A3B8), fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))

                Text(uiState.callPhone, style = MaterialTheme.typography.headlineLarge, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 32.sp)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${uiState.callPhone}"))
                            callLauncher.launch(intent)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    },
                    Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📞 Позвонить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text("После звонка вернитесь в приложение\nдля завершения регистрации", color = Color(0xFF64748B), fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        } else {
            // Форма регистрации
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(80.dp).background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("💼", fontSize = 40.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("BookMaster", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Регистрация салона или мастера", color = Color(0xFF94A3B8), fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = uiState.type == "salon",
                        onClick = { viewModel.onTypeChange("salon") },
                        label = { Text("Салон") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A)
                        )
                    )
                    FilterChip(
                        selected = uiState.type == "master",
                        onClick = { viewModel.onTypeChange("master") },
                        label = { Text("Мастер") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A)
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(if (uiState.type == "master") "Ваше имя *" else "Название салона *") },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email *") },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Телефон") },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (uiState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp)) {
                        Text(uiState.error!!, Modifier.padding(12.dp), color = Color(0xFFFCA5A5), fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = viewModel::register,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text("Зарегистрироваться", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onLoginClick) {
                    Text("Уже есть аккаунт? Войти", color = Color(0xFF38BDF8), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF38BDF8),
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF475569),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8)
)