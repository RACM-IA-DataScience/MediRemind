package com.mediremind.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mediremind.app.ui.screens.auth.LoginScreen
import com.mediremind.app.ui.screens.home.HomeScreen
import com.mediremind.app.ui.screens.medications.AddEditMedicationScreen
import com.mediremind.app.ui.screens.medications.DrugSearchScreen
import com.mediremind.app.ui.screens.medications.MedicationListScreen
import com.mediremind.app.ui.screens.pharmacies.PharmaciesScreen
import com.mediremind.app.ui.screens.places.PlacesScreen
import com.mediremind.app.ui.screens.history.HistoryScreen
import com.mediremind.app.ui.viewmodels.AuthViewModel
import com.mediremind.app.ui.viewmodels.LocationViewModel
import com.mediremind.app.ui.viewmodels.MedicationViewModel
import com.mediremind.app.ui.viewmodels.ReminderViewModel

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    medicationViewModel: MedicationViewModel,
    reminderViewModel: ReminderViewModel,
    locationViewModel: LocationViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    val shouldShowBottomBar = currentDestination?.route in BottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    BottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    medicationViewModel = medicationViewModel,
                    reminderViewModel = reminderViewModel,
                    onNavigateToAddMedication = {
                        navController.navigate(Screen.AddEditMedication.createRoute())
                    },
                    onNavigateToPharmacies = {
                        navController.navigate(Screen.Pharmacies.route)
                    },
                    onNavigateToPlaces = {
                        navController.navigate(Screen.Places.route)
                    },
                    onNavigateToDrugDatabase = {
                        navController.navigate(Screen.DrugDatabase.route)
                    }
                )
            }

            composable(Screen.Medications.route) {
                MedicationListScreen(
                    medicationViewModel = medicationViewModel,
                    reminderViewModel = reminderViewModel,
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddEditMedication.createRoute())
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.AddEditMedication.createRoute(id))
                    },
                    onNavigateToOpenFda = {
                        navController.navigate(Screen.DrugDatabase.route)
                    }
                )
            }

            composable(
                route = Screen.AddEditMedication.route,
                arguments = listOf(
                    navArgument("medId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val medId = backStackEntry.arguments?.getLong("medId") ?: 0L
                AddEditMedicationScreen(
                    medicationId = medId,
                    medicationViewModel = medicationViewModel,
                    reminderViewModel = reminderViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DrugDatabase.route) {
                DrugSearchScreen(
                    medicationViewModel = medicationViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Pharmacies.route) {
                PharmaciesScreen(
                    locationViewModel = locationViewModel,
                    onNavigateToPlaces = {
                        navController.navigate(Screen.Places.route)
                    }
                )
            }

            composable(Screen.Places.route) {
                PlacesScreen(
                    locationViewModel = locationViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    reminderViewModel = reminderViewModel
                )
            }
        }
    }
}
