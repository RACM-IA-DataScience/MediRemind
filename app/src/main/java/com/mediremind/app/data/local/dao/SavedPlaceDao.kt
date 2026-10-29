package com.mediremind.app.data.local.dao

import androidx.room.*
import com.mediremind.app.data.local.entities.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY id ASC")
    fun getAllPlaces(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places WHERE isEnabled = 1")
    suspend fun getActivePlacesSync(): List<SavedPlaceEntity>

    @Query("SELECT * FROM saved_places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: Long): SavedPlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: SavedPlaceEntity): Long

    @Update
    suspend fun updatePlace(place: SavedPlaceEntity)

    @Delete
    suspend fun deletePlace(place: SavedPlaceEntity)
}
