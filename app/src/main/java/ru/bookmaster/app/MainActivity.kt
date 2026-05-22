package ru.bookmaster.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.bookmaster.app.ui.home.HomeScreen
import ru.bookmaster.app.ui.login.LoginScreen
import ru.bookmaster.app.ui.register.RegisterScreen
import ru.bookmaster.app.ui.theme.BookMasterTheme
import ru.bookmaster.app.util.TokenManager

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Проверяем, есть ли сохранённый токен
        val tokenManager = TokenManager(this)
        val hasToken = runBlocking { tokenManager.token.first() != null }
        val startScreen = if (hasToken) "home" else "login"

        setContent {
            BookMasterTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startScreen) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                            onLoginClick = { navController.popBackStack() }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            onLogout = { navController.navigate("login") { popUpTo(0) { inclusive = true } } }
                        )
                    }
                }
            }
        }
    }
}