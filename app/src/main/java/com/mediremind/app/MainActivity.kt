package com.mediremind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mediremind.app.ui.navigation.NavGraph
import com.mediremind.app.ui.theme.MediRemindTheme
import com.mediremind.app.ui.viewmodels.AuthViewModel
import com.mediremind.app.ui.viewmodels.LocationViewModel
import com.mediremind.app.ui.viewmodels.MedicationViewModel
import com.mediremind.app.ui.viewmodels.ReminderViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MediRemindApp

        val authViewModel = ViewModelProvider(
            this,
            viewModelFactory { AuthViewModel(app.authRepository) }
        )[AuthViewModel::class.java]

        val medicationViewModel = ViewModelProvider(
            this,
            viewModelFactory { MedicationViewModel(app.medicationRepository) }
        )[MedicationViewModel::class.java]

        val reminderViewModel = ViewModelProvider(
            this,
            viewModelFactory { ReminderViewModel(app.reminderRepository) }
        )[ReminderViewModel::class.java]

        val locationViewModel = ViewModelProvider(
            this,
            viewModelFactory { LocationViewModel(app.locationRepository) }
        )[LocationViewModel::class.java]

        setContent {
            MediRemindTheme {
                NavGraph(
                    authViewModel = authViewModel,
                    medicationViewModel = medicationViewModel,
                    reminderViewModel = reminderViewModel,
                    locationViewModel = locationViewModel
                )
            }
        }
    }

    private inline fun <reified T : ViewModel> viewModelFactory(crossinline creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                return creator() as VM
            }
        }
    }
}
