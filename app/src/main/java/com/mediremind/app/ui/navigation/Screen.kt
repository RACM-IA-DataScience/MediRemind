package com.mediremind.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Iniciar Sesión")
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Medications : Screen("medications", "Medicinas", Icons.Default.Medication)
    object AddEditMedication : Screen("add_edit_medication?medId={medId}", "Agregar Medicina") {
        fun createRoute(medId: Long = 0L) = "add_edit_medication?medId=$medId"
    }
    object DrugDatabase : Screen("drug_database", "Catálogo Fármacos", Icons.Default.LocalPharmacy)
    object Pharmacies : Screen("pharmacies", "Farmacias", Icons.Default.LocationOn)
    object Places : Screen("places", "Geovallas")
    object History : Screen("history", "Historial", Icons.Default.DateRange)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Medications,
    Screen.Pharmacies,
    Screen.History
)
