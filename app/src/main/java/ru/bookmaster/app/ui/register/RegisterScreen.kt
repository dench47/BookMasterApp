package ru.bookmaster.app.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("BookMaster", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Регистрация", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Название *") },
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email *") },
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Телефон") },
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Пароль *") },
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::register,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Зарегистрироваться", color = Color(0xFF0F172A), fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onLoginClick) {
            Text("Уже есть аккаунт? Войти", color = Color(0xFF38BDF8), fontSize = 16.sp)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = Color(0xFF38BDF8),
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF94A3B8),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF64748B)
)