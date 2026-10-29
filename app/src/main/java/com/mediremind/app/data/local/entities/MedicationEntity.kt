package com.mediremind.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un medicamento registrado en la base de datos local.
 * Contiene información clínica detallada como el principio activo (compuesto),
 * la presentación farmacéutica, concentración y stock disponible.
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                          // Nombre comercial (ej. Tempra, Motrin, Amoxil)
    val activeCompound: String,                // Principio activo (ej. Paracetamol, Ibuprofeno, Amoxicilina)
    val presentation: String,                  // Presentación (Tableta, Cápsula, Jarabe, Gotas, Inhalador, etc.)
    val concentration: String,                 // Concentración (ej. 500 mg, 100 mg/5 ml, 50 mcg)
    val administrationRoute: String = "Oral",  // Vía de administración (Oral, Tópica, Inhalatoria, etc.)
    val instructions: String = "",             // Indicaciones (ej. Tomar con abundante agua después de comer)
    val currentStock: Int = 30,                // Cantidad restante en inventario
    val unit: String = "piezas",               // Unidad (pastillas, ml, dosis, gotas)
    val lowStockThreshold: Int = 5,            // Alerta cuando el stock sea menor o igual a este número
    val colorHex: String = "#00695C",          // Color distintivo para la interfaz
    val isCustom: Boolean = false,             // Si fue creado por el usuario o proviene del catálogo oficial
    val createdAt: Long = System.currentTimeMillis()
)
