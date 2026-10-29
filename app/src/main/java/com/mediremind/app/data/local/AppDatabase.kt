package com.mediremind.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mediremind.app.data.local.dao.DoseHistoryDao
import com.mediremind.app.data.local.dao.MedicationDao
import com.mediremind.app.data.local.dao.ReminderDao
import com.mediremind.app.data.local.dao.SavedPlaceDao
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.local.entities.ReminderEntity
import com.mediremind.app.data.local.entities.SavedPlaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MedicationEntity::class,
        ReminderEntity::class,
        DoseHistoryEntity::class,
        SavedPlaceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun doseHistoryDao(): DoseHistoryDao
    abstract fun savedPlaceDao(): SavedPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar columna userId a dose_history con valor por defecto
                db.execSQL("ALTER TABLE dose_history ADD COLUMN userId TEXT NOT NULL DEFAULT 'guest_local'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_dose_history_userId ON dose_history(userId)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mediremind.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val medDao = database.medicationDao()
            val placeDao = database.savedPlaceDao()

            // Cargar medicamentos iniciales
            medDao.insertAll(PreloadedMedications.getInitialMedications())

            // Cargar lugares predeterminados de ejemplo (Casa y Trabajo)
            placeDao.insertPlace(
                SavedPlaceEntity(
                    id = 1,
                    name = "Casa",
                    address = "Mi Hogar (Ubicación de descanso)",
                    latitude = 19.432608,
                    longitude = -99.133209,
                    radiusMeters = 150f,
                    notifyOnEntry = true,
                    notifyOnExit = false,
                    isEnabled = true
                )
            )
            placeDao.insertPlace(
                SavedPlaceEntity(
                    id = 2,
                    name = "Trabajo",
                    address = "Oficina / Trabajo",
                    latitude = 19.427025,
                    longitude = -99.167665,
                    radiusMeters = 200f,
                    notifyOnEntry = false,
                    notifyOnExit = true,
                    isEnabled = true
                )
            )
        }
    }
}
