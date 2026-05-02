package com.queryb.medlog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LabResultDao {

    @Insert
    suspend fun insert(result: LabResult)

    @Delete
    suspend fun delete(result: LabResult)

    @Query("SELECT * FROM lab_results WHERE userId = :userId ORDER BY date DESC")
    fun getAllResults(userId: String): Flow<List<LabResult>>

    @Query("SELECT * FROM lab_results WHERE userId = :userId ORDER BY date ASC")
    suspend fun getAllResultsSortedByDate(userId: String): List<LabResult>
}
