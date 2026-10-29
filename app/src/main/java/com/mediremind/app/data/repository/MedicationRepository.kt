package com.mediremind.app.data.repository

import com.mediremind.app.data.local.dao.MedicationDao
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.remote.OpenFdaApiService
import com.mediremind.app.data.remote.models.OpenFdaDrugDto
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val openFdaApiService: OpenFdaApiService = OpenFdaApiService.create()
) {

    fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    suspend fun getMedicationById(id: Long): MedicationEntity? = medicationDao.getMedicationById(id)

    fun searchLocalMedications(query: String): Flow<List<MedicationEntity>> =
        medicationDao.searchMedications(query)

    fun getLowStockMedications(): Flow<List<MedicationEntity>> =
        medicationDao.getLowStockMedications()

    suspend fun insertMedication(medication: MedicationEntity): Long =
        medicationDao.insertMedication(medication)

    suspend fun updateMedication(medication: MedicationEntity) =
        medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: MedicationEntity) =
        medicationDao.deleteMedication(medication)

    suspend fun decrementStock(medicationId: Long, amount: Int = 1) =
        medicationDao.decrementStock(medicationId, amount)

    /**
     * Consulta la API pública de OpenFDA para autocompletar fármacos internacionales.
     * Busca por generic_name o brand_name y transforma los resultados para fácil visualización.
     */
    suspend fun searchOnlineDrugs(query: String): Result<List<OnlineDrugItem>> {
        return try {
            val formattedQuery = "generic_name:\"$query\"+brand_name:\"$query\""
            val response = openFdaApiService.searchNdc(query = formattedQuery, limit = 15)
            val items = response.results?.map { dto ->
                dto.toOnlineDrugItem()
            } ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            // Reintento con búsqueda abierta si la búsqueda exacta entre comillas no devuelve datos
            try {
                val fallbackQuery = "(generic_name:$query* OR brand_name:$query*)"
                val response = openFdaApiService.searchNdc(query = fallbackQuery, limit = 15)
                val items = response.results?.map { it.toOnlineDrugItem() } ?: emptyList()
                Result.success(items)
            } catch (err: Exception) {
                Result.failure(err)
            }
        }
    }
}

/**
 * Modelo limpio para mostrar resultados devueltos por OpenFDA
 */
data class OnlineDrugItem(
    val brandName: String,
    val genericName: String,
    val dosageForm: String,
    val route: String,
    val activeIngredients: String,
    val productNdc: String
)

private fun OpenFdaDrugDto.toOnlineDrugItem(): OnlineDrugItem {
    val brand = brandName ?: brandNameBase ?: "Sin nombre comercial"
    val generic = genericName ?: substanceName?.firstOrNull() ?: "Compuesto no especificado"
    val form = dosageForm?.replace("_", " ")?.lowercase(Locale.ROOT)
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        ?: "No especificada"
    val routeStr = route?.joinToString(", ") ?: "Oral"
    val ingredients = activeIngredients?.joinToString(", ") {
        "${it.name ?: ""} ${it.strength ?: ""}".trim()
    }?.takeIf { it.isNotBlank() } ?: generic

    return OnlineDrugItem(
        brandName = brand,
        genericName = generic,
        dosageForm = form,
        route = routeStr,
        activeIngredients = ingredients,
        productNdc = productNdc ?: ""
    )
}
