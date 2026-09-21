package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FactCheckDao {
    @Query("SELECT * FROM fact_checks ORDER BY timestamp DESC")
    fun getAllFactChecks(): Flow<List<FactCheckEntity>>

    @Query("SELECT * FROM fact_checks WHERE id = :id LIMIT 1")
    suspend fun getFactCheckById(id: Long): FactCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactCheck(entity: FactCheckEntity): Long

    @Query("DELETE FROM fact_checks WHERE id = :id")
    suspend fun deleteFactCheckById(id: Long)

    @Query("DELETE FROM fact_checks")
    suspend fun clearAll()
}
