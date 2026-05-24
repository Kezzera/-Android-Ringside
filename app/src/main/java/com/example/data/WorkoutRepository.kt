package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val profileDao: WorkoutProfileDao) {
    val allProfiles: Flow<List<WorkoutProfile>> = profileDao.getAllProfiles()

    suspend fun getProfile(id: Long): WorkoutProfile? = profileDao.getProfileById(id)

    suspend fun insert(profile: WorkoutProfile) = profileDao.insertProfile(profile)

    suspend fun delete(id: Long) = profileDao.deleteProfileById(id)
}
