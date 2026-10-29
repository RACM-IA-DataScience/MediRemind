package com.mediremind.app.data.local

import com.mediremind.app.data.local.entities.MedicationEntity

/**
 * Catálogo precargado de fármacos esenciales y de alta rotación para uso offline.
 * Cada fármaco incluye su nombre de referencia, principio activo (compuesto),
 * presentación farmacéutica, concentración, vía de administración y recomendaciones clínicas.
 */
object PreloadedMedications {
    fun getInitialMedications(): List<MedicationEntity> = listOf(
        // --- ANALGÉSICOS Y ANTIINFLAMATORIOS ---
        MedicationEntity(
            name = "Paracetamol (Tempra / Tylenol)",
            activeCompound = "Paracetamol (Acetaminofén)",
            presentation = "Tabletas",
            concentration = "500 mg",
            administrationRoute = "Oral",
            instructions = "Tomar con o sin alimentos. No superar 3g al día. Evitar bebidas alcohólicas.",
            currentStock = 20,
            unit = "pastillas",
            colorHex = "#2E7D32"
        ),
        MedicationEntity(
            name = "Ibuprofeno (Motrin / Advil)",
            activeCompound = "Ibuprofeno",
            presentation = "Cápsulas de gel",
            concentration = "400 mg",
            administrationRoute = "Oral",
            instructions = "Tomar estrictamente después de los alimentos o con leche para evitar irritación gástrica.",
            currentStock = 15,
            unit = "cápsulas",
            colorHex = "#E65100"
        ),
        MedicationEntity(
            name = "Naproxeno Sódico (Flanax)",
            activeCompound = "Naproxeno",
            presentation = "Tabletas",
            concentration = "550 mg",
            administrationRoute = "Oral",
            instructions = "Tomar cada 12 horas con suficiente agua tras ingerir alimentos.",
            currentStock = 12,
            unit = "pastillas",
            colorHex = "#0277BD"
        ),
        MedicationEntity(
            name = "Aspirina Protect",
            activeCompound = "Ácido Acetilsalicílico",
            presentation = "Tabletas con capa entérica",
            concentration = "100 mg",
            administrationRoute = "Oral",
            instructions = "Tomar preferentemente a la misma hora del día, con medio vaso de agua.",
            currentStock = 30,
            unit = "pastillas",
            colorHex = "#C2185B"
        ),

        // --- ANTIBIÓTICOS ---
        MedicationEntity(
            name = "Amoxicilina con Ácido Clavulánico (Augmentin)",
            activeCompound = "Amoxicilina / Ácido Clavulánico",
            presentation = "Tabletas",
            concentration = "875 mg / 125 mg",
            administrationRoute = "Oral",
            instructions = "Tomar al inicio de una comida. Completar los días prescritos por el médico aunque no haya síntomas.",
            currentStock = 14,
            unit = "pastillas",
            colorHex = "#6A1B9A"
        ),
        MedicationEntity(
            name = "Azitromicina (Zithromax)",
            activeCompound = "Azitromicina",
            presentation = "Tabletas",
            concentration = "500 mg",
            administrationRoute = "Oral",
            instructions = "Tomar 1 hora antes de las comidas o 2 horas después.",
            currentStock = 6,
            unit = "pastillas",
            colorHex = "#1565C0"
        ),
        MedicationEntity(
            name = "Ciprofloxacino",
            activeCompound = "Ciprofloxacino",
            presentation = "Tabletas",
            concentration = "500 mg",
            administrationRoute = "Oral",
            instructions = "Beber abundante agua durante el tratamiento. Evitar tomarlo simultáneamente con lácteos.",
            currentStock = 10,
            unit = "pastillas",
            colorHex = "#00838F"
        ),

        // --- SALUD CARDIOVASCULAR Y METABÓLICA ---
        MedicationEntity(
            name = "Losartán Potásico (Cozaar)",
            activeCompound = "Losartán",
            presentation = "Comprimidos recubiertos",
            concentration = "50 mg",
            administrationRoute = "Oral",
            instructions = "Tomar en ayunas o con alimentos, siempre a la misma hora para control de presión arterial.",
            currentStock = 30,
            unit = "pastillas",
            colorHex = "#AD1457"
        ),
        MedicationEntity(
            name = "Amlodipino (Norvasc)",
            activeCompound = "Besilato de Amlodipino",
            presentation = "Tabletas",
            concentration = "5 mg",
            administrationRoute = "Oral",
            instructions = "Monitorear presión arterial y no suspender bruscamente sin indicación médica.",
            currentStock = 30,
            unit = "pastillas",
            colorHex = "#283593"
        ),
        MedicationEntity(
            name = "Metformina (Glucophage)",
            activeCompound = "Clorhidrato de Metformina",
            presentation = "Tabletas de liberación prolongada",
            concentration = "850 mg",
            administrationRoute = "Oral",
            instructions = "Tomar durante o inmediatamente después del desayuno o la cena.",
            currentStock = 40,
            unit = "pastillas",
            colorHex = "#00695C"
        ),
        MedicationEntity(
            name = "Atorvastatina (Lipitor)",
            activeCompound = "Atorvastatina Cálcica",
            presentation = "Tabletas",
            concentration = "20 mg",
            administrationRoute = "Oral",
            instructions = "Tomar una vez al día por la noche antes de dormir.",
            currentStock = 28,
            unit = "pastillas",
            colorHex = "#4527A0"
        ),

        // --- SISTEMA GASTROINTESTINAL ---
        MedicationEntity(
            name = "Omeprazol (Losec)",
            activeCompound = "Omeprazol",
            presentation = "Cápsulas con microgránulos",
            concentration = "20 mg",
            administrationRoute = "Oral",
            instructions = "Tomar 30 minutos antes del desayuno con medio vaso de agua. Tragar entera sin masticar.",
            currentStock = 28,
            unit = "cápsulas",
            colorHex = "#37474F"
        ),
        MedicationEntity(
            name = "Pantoprazol",
            activeCompound = "Pantoprazol Magnésico",
            presentation = "Tabletas con capa entérica",
            concentration = "40 mg",
            administrationRoute = "Oral",
            instructions = "Ingerir por la mañana en ayunas sin triturar ni masticar.",
            currentStock = 14,
            unit = "pastillas",
            colorHex = "#4E342E"
        ),

        // --- SISTEMA RESPIRATORIO Y ALERGIAS ---
        MedicationEntity(
            name = "Salbutamol (Ventolin)",
            activeCompound = "Sulfato de Salbutamol",
            presentation = "Aerosol para inhalación",
            concentration = "100 mcg / disparo",
            administrationRoute = "Inhalatoria",
            instructions = "Agitar el inhalador antes de cada uso. Enjuagar la boca con agua tras la inhalación.",
            currentStock = 200,
            unit = "disparos",
            colorHex = "#0097A7"
        ),
        MedicationEntity(
            name = "Loratadina",
            activeCompound = "Loratadina",
            presentation = "Jarabe / Solución oral",
            concentration = "100 mg / 100 ml",
            administrationRoute = "Oral",
            instructions = "Medir con vaso dosificador. No produce somnolencia significativa.",
            currentStock = 120,
            unit = "ml",
            colorHex = "#558B2F"
        ),
        MedicationEntity(
            name = "Cetirizina (Zyrtec)",
            activeCompound = "Diclorhidrato de Cetirizina",
            presentation = "Tabletas",
            concentration = "10 mg",
            administrationRoute = "Oral",
            instructions = "Tomar una dosis única diaria preferentemente por la noche si causa somnolencia leve.",
            currentStock = 10,
            unit = "pastillas",
            colorHex = "#827717"
        )
    )
}
