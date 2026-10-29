package com.mediremind.app.data.local.dao

import androidx.room.*
import com.mediremind.app.data.local.entities.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Query("""
        SELECT * FROM medications 
        WHERE name LIKE '%' || :query || '%' 
           OR activeCompound LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """)
    fun searchMedications(query: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE currentStock <= lowStockThreshold")
    fun getLowStockMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET currentStock = MAX(0, currentStock - :amount) WHERE id = :medicationId")
    suspend fun decrementStock(medicationId: Long, amount: Int)

    @Query("UPDATE medications SET currentStock = currentStock + :amount WHERE id = :medicationId")
    suspend fun incrementStock(medicationId: Long, amount: Int)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getCount(): Int
}
