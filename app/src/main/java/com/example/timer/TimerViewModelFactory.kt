package com.example.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.WorkoutProfileDao

class TimerViewModelFactory(
    private val profileDao: WorkoutProfileDao,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(profileDao, AppFeedbackManager(applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
