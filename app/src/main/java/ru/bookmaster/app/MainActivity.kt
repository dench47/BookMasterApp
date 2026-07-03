package ru.bookmaster.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.bookmaster.app.ui.cabinet.CabinetScreen
import ru.bookmaster.app.ui.cabinet.CabinetViewModel
import ru.bookmaster.app.ui.clients.ClientDetailScreen
import ru.bookmaster.app.ui.clients.ClientsScreen
import ru.bookmaster.app.ui.home.AppointmentsScreen
import ru.bookmaster.app.ui.home.DayAppointmentsScreen
import ru.bookmaster.app.ui.home.HomeScreen
import ru.bookmaster.app.ui.login.LoginScreen
import ru.bookmaster.app.ui.masters.MasterDetailScreen
import ru.bookmaster.app.ui.masters.MastersScreen
import ru.bookmaster.app.ui.premium.PremiumScreen
import ru.bookmaster.app.ui.register.RegisterScreen
import ru.bookmaster.app.ui.services.ServiceDetailScreen
import ru.bookmaster.app.ui.services.ServicesScreen
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

        val tokenManager = TokenManager(this)
        val hasToken = runBlocking { tokenManager.token.first() != null }
        val startDestination = if (hasToken) "main" else "login"

        setContent {
            BookMasterTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableStateOf(0) }

                // Создаём общий экземпляр CabinetViewModel для всех экранов
                val cabinetViewModel: CabinetViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onRegisterClick = { phone ->
                                navController.navigate("register?phone=$phone")
                            }
                        )
                    }

                    composable(
                        "register?phone={phone}",
                        arguments = listOf(navArgument("phone") { defaultValue = "" })
                    ) { backStackEntry ->
                        val phone = backStackEntry.arguments?.getString("phone") ?: ""
                        RegisterScreen(
                            initialPhone = phone,
                            onRegisterSuccess = {
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onLoginClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        "day_appointments/{date}",
                        arguments = listOf(navArgument("date") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val date = backStackEntry.arguments?.getString("date") ?: ""
                        DayAppointmentsScreen(
                            dateString = date,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("all_appointments") {
                        AppointmentsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("main") {
                        MainScreen(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onLogout = {
                                lifecycleScope.launch {
                                    TokenManager(this@MainActivity).clear()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToClientDetail = { clientId ->
                                navController.navigate("client_detail/$clientId")
                            },
                            onNavigateToServices = {
                                navController.navigate("services")
                            },
                            onNavigateToMasters = {
                                navController.navigate("masters")
                            },
                            onNavigateToPremium = { navController.navigate("premium") },
                            onNavigateToDayAppointments = { date ->
                                navController.navigate("day_appointments/$date")
                            },
                            onNavigateToAllAppointments = {
                                navController.navigate("all_appointments")
                            },
                            cabinetViewModel = cabinetViewModel
                        )
                    }

                    composable("services") {
                        ServicesScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToServiceDetail = { serviceId ->
                                navController.navigate("service_detail/$serviceId")
                            }
                        )
                    }

                    composable(
                        "service_detail/{serviceId}",
                        arguments = listOf(navArgument("serviceId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val serviceId = backStackEntry.arguments?.getLong("serviceId") ?: 0L
                        ServiceDetailScreen(
                            serviceId = serviceId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("masters") {
                        MastersScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToMasterDetail = { masterId ->
                                navController.navigate("master_detail/$masterId")
                            }
                        )
                    }

                    composable(
                        "master_detail/{masterId}",
                        arguments = listOf(navArgument("masterId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val masterId = backStackEntry.arguments?.getLong("masterId") ?: 0L
                        MasterDetailScreen(
                            masterId = masterId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        "client_detail/{clientId}",
                        arguments = listOf(navArgument("clientId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val clientId = backStackEntry.arguments?.getLong("clientId") ?: 0L
                        ClientDetailScreen(
                            clientId = clientId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Экран Premium
                    composable("premium") {
                        PremiumScreen(
                            onBack = { navController.popBackStack() },
                            onPremiumActivated = {
                                cabinetViewModel.refresh()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onLogout: () -> Unit,
    onNavigateToClientDetail: (Long) -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToMasters: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToDayAppointments: (String) -> Unit = {},
    onNavigateToAllAppointments: () -> Unit = {},
    cabinetViewModel: CabinetViewModel
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Записи") },
                    label = { Text("Записи") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Клиенты") },
                    label = { Text("Клиенты") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Кабинет") },
                    label = { Text("Кабинет") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(
                onNavigateToClients = {
                    onTabSelected(1)
                },
                onNavigateToMasters = onNavigateToMasters,
                onShareWebLink = {
                    // TODO
                },
                onNavigateToDayAppointments = onNavigateToDayAppointments,
                onNavigateToAllAppointments = onNavigateToAllAppointments
            )
            1 -> ClientsScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToDetail = onNavigateToClientDetail,
                onNavigateToPremium = onNavigateToPremium
            )
            2 -> CabinetScreen(
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout,
                onNavigateToClients = { onTabSelected(1) },
                onNavigateToMasters = onNavigateToMasters,
                onNavigateToServices = onNavigateToServices,
                onShareWebLink = { /* TODO */ },
                onNavigateToPremium = onNavigateToPremium,
                viewModel = cabinetViewModel
            )
        }
    }
}