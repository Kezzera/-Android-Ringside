package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutProfileDao {
    @Query("SELECT * FROM workout_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<WorkoutProfile>>

    @Query("SELECT * FROM workout_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): WorkoutProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: WorkoutProfile)

    @Query("DELETE FROM workout_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
}
