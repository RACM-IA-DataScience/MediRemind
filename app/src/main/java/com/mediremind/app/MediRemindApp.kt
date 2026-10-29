package com.mediremind.app

import android.app.Application
import com.mediremind.app.data.local.AppDatabase
import com.mediremind.app.data.repository.AuthRepository
import com.mediremind.app.data.repository.LocationRepository
import com.mediremind.app.data.repository.MedicationRepository
import com.mediremind.app.data.repository.ReminderRepository
import com.mediremind.app.notification.NotificationHelper

class MediRemindApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val authRepository by lazy { AuthRepository(this) }
    val medicationRepository by lazy { MedicationRepository(database.medicationDao()) }
    val reminderRepository by lazy {
        ReminderRepository(
            context = this,
            reminderDao = database.reminderDao(),
            medicationDao = database.medicationDao(),
            doseHistoryDao = database.doseHistoryDao(),
            authRepository = authRepository
        )
    }
    val locationRepository by lazy {
        LocationRepository(
            context = this,
            savedPlaceDao = database.savedPlaceDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Crear canales de notificación requeridos para alarmas y geovallas
        NotificationHelper.createNotificationChannels(this)
    }
}
