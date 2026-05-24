package com.example.timer

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkoutProfile
import com.example.data.WorkoutProfileDao
import com.example.timer.AppFeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerPhase {
    PREPARATION, ROUND, REST, FINISHED, IDLE
}

data class TimerState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val currentRound: Int = 1,
    val totalRounds: Int = 1,
    val timeRemainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val profile: WorkoutProfile? = null
)

class TimerViewModel(
    private val profileDao: WorkoutProfileDao,
    private val feedbackManager: AppFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()
    
    private val _profiles = MutableStateFlow<List<WorkoutProfile>>(emptyList())
    val profiles: StateFlow<List<WorkoutProfile>> = _profiles.asStateFlow()

    private var timerJob: Job? = null
    
    // For precise timing
    private var targetTimeNanos: Long = 0L
    private var pausedTimeRemainingNanos: Long = 0L
    private var hasPlayedWarning = false

    init {
        viewModelScope.launch {
            profileDao.getAllProfiles().collect { list ->
                // Ensure there's a default profile
                if (list.isEmpty()) {
                    val defaultProfiles = listOf(
                        WorkoutProfile(
                            name = "Classic Boxing",
                            prepTimeSeconds = 10,
                            roundTimeSeconds = 180, // 3 min
                            restTimeSeconds = 60,
                            warningTimeSeconds = 10,
                            numberOfRounds = 12
                        ),
                        WorkoutProfile(
                            name = "Mixed Martial Arts (MMA)",
                            prepTimeSeconds = 10,
                            roundTimeSeconds = 300, // 5 min
                            restTimeSeconds = 60,
                            warningTimeSeconds = 10,
                            numberOfRounds = 3
                        ),
                        WorkoutProfile(
                            name = "Brazilian Jiu-Jitsu (BJJ)",
                            prepTimeSeconds = 10,
                            roundTimeSeconds = 300, // 5 min
                            restTimeSeconds = 0,
                            warningTimeSeconds = 10,
                            numberOfRounds = 1
                        ),
                        WorkoutProfile(
                            name = "Collegiate Wrestling",
                            prepTimeSeconds = 10,
                            roundTimeSeconds = 120, // 2 min
                            restTimeSeconds = 30, // Usually periods are continuous or minimal rest, but let's give 30s
                            warningTimeSeconds = 10,
                            numberOfRounds = 3
                        ),
                        WorkoutProfile(
                            name = "High-Intensity Interval Training",
                            prepTimeSeconds = 10,
                            roundTimeSeconds = 45,
                            restTimeSeconds = 15,
                            warningTimeSeconds = 5,
                            numberOfRounds = 10
                        )
                    )
                    defaultProfiles.forEach { profileDao.insertProfile(it) }
                } else {
                    _profiles.value = list
                    if (_uiState.value.profile == null) {
                        _uiState.value = _uiState.value.copy(profile = list.first())
                    }
                }
            }
        }
    }

    fun selectProfile(profile: WorkoutProfile) {
        if (_uiState.value.isRunning) return
        _uiState.value = TimerState(profile = profile, totalRounds = profile.numberOfRounds)
    }

    fun startTimer() {
        val currentState = _uiState.value
        val profile = currentState.profile ?: return

        if (currentState.isRunning) return

        if (currentState.phase == TimerPhase.IDLE || currentState.phase == TimerPhase.FINISHED) {
            // Fresh start
            _uiState.value = currentState.copy(
                phase = TimerPhase.PREPARATION,
                currentRound = 1,
                timeRemainingMillis = profile.prepTimeSeconds * 1000L,
                isRunning = true
            )
            hasPlayedWarning = false
            feedbackManager.playRoundEndSignal() // Preparation beep
            startPreciseCountdown(profile.prepTimeSeconds * 1_000_000_000L)
        } else if (pausedTimeRemainingNanos > 0) {
            // Resume from pause
            _uiState.value = currentState.copy(isRunning = true)
            startPreciseCountdown(pausedTimeRemainingNanos)
            pausedTimeRemainingNanos = 0L
        }
    }

    fun pauseTimer() {
        if (!_uiState.value.isRunning) return
        timerJob?.cancel()
        timerJob = null
        val remainingNanos = targetTimeNanos - System.nanoTime()
        pausedTimeRemainingNanos = remainingNanos.coerceAtLeast(0L)
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            timeRemainingMillis = pausedTimeRemainingNanos / 1_000_000L
        )
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        pausedTimeRemainingNanos = 0L
        val profile = _uiState.value.profile
        _uiState.value = TimerState(profile = profile, totalRounds = profile?.numberOfRounds ?: 1)
    }

    private fun startPreciseCountdown(durationNanos: Long) {
        timerJob?.cancel()
        targetTimeNanos = System.nanoTime() + durationNanos

        timerJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (true) {
                val now = System.nanoTime()
                val remainingNanos = targetTimeNanos - now

                if (remainingNanos <= 0) {
                    _uiState.value = _uiState.value.copy(timeRemainingMillis = 0L)
                    transitionToNextPhase()
                    break
                }

                val remainingMillis = remainingNanos / 1_000_000L
                _uiState.value = _uiState.value.copy(timeRemainingMillis = remainingMillis)
                
                checkWarnings(remainingMillis)

                // Update frequency ~100ms (10 fps), relying on UI animation tween for smooth 60fps interpolation
                delay(100)
            }
        }
    }
    
    private fun checkWarnings(remainingMillis: Long) {
        val currentPhase = _uiState.value.phase
        val profile = _uiState.value.profile ?: return
        
        if (currentPhase == TimerPhase.ROUND && !hasPlayedWarning) {
            if (remainingMillis <= profile.warningTimeSeconds * 1000L) {
                hasPlayedWarning = true
                feedbackManager.playWarningSignal()
            }
        } else if (currentPhase == TimerPhase.REST && !hasPlayedWarning) {
            if (remainingMillis <= 10000L) { // 10s warning before rest ends
                hasPlayedWarning = true
                feedbackManager.playRestEndWarningSignal()
            }
        }
    }

    private fun transitionToNextPhase() {
        val currentState = _uiState.value
        val profile = currentState.profile ?: return
        hasPlayedWarning = false

        when (currentState.phase) {
            TimerPhase.PREPARATION -> {
                feedbackManager.playRoundStartSignal()
                _uiState.value = currentState.copy(phase = TimerPhase.ROUND)
                startPreciseCountdown(profile.roundTimeSeconds * 1_000_000_000L)
            }
            TimerPhase.ROUND -> {
                feedbackManager.playRoundEndSignal()
                if (currentState.currentRound >= profile.numberOfRounds) {
                    _uiState.value = currentState.copy(phase = TimerPhase.FINISHED, isRunning = false)
                } else {
                    _uiState.value = currentState.copy(phase = TimerPhase.REST)
                    startPreciseCountdown(profile.restTimeSeconds * 1_000_000_000L)
                }
            }
            TimerPhase.REST -> {
                feedbackManager.playRoundStartSignal()
                _uiState.value = currentState.copy(
                    phase = TimerPhase.ROUND,
                    currentRound = currentState.currentRound + 1
                )
                startPreciseCountdown(profile.roundTimeSeconds * 1_000_000_000L)
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedbackManager.release()
    }
}
