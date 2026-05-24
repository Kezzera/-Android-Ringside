package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_profiles")
data class WorkoutProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val prepTimeSeconds: Int,
    val roundTimeSeconds: Int,
    val restTimeSeconds: Int,
    val warningTimeSeconds: Int, // e.g. 10 sec before round ends
    val numberOfRounds: Int
)
