package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.sp
import com.example.timer.TimerPhase
import com.example.timer.TimerState
import com.example.timer.TimerViewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.WorkoutProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    var showProfileSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar() },
        bottomBar = {
            TimerControls(
                isRunning = uiState.isRunning,
                phase = uiState.phase,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onStop = { viewModel.stopTimer() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer Display
            TimerCircle(
                timeRemainingMillis = uiState.timeRemainingMillis,
                totalTimeMillis = getTotalTimeForPhase(uiState),
                phase = uiState.phase,
                currentRound = uiState.currentRound,
                totalRounds = uiState.totalRounds
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Quick Stats Grid
            QuickStatsGrid(uiState = uiState)

            Spacer(modifier = Modifier.weight(1f))
            
            ActiveSessionBanner(uiState = uiState, onClick = { showProfileSheet = true })
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "Select Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(profiles) { profile ->
                    ProfileListItem(
                        profile = profile, 
                        isSelected = uiState.profile?.id == profile.id,
                        onClick = {
                            viewModel.selectProfile(profile)
                            showProfileSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileListItem(profile: WorkoutProfile, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${profile.numberOfRounds} rounds • ${profile.roundTimeSeconds / 60}:${String.format("%02d", profile.roundTimeSeconds % 60)} work • ${profile.restTimeSeconds / 60}:${String.format("%02d", profile.restTimeSeconds % 60)} rest",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) textColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
        Text(
            text = "Ringside Interval",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "JD", 
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimerControls(
    isRunning: Boolean,
    phase: TimerPhase,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stopEnabled = phase != TimerPhase.IDLE && phase != TimerPhase.FINISHED
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    1.dp, 
                    if (stopEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant, 
                    CircleShape
                )
                .clickable(enabled = stopEnabled, onClick = onStop)
                .testTag("stop_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Stop, 
                contentDescription = "Stop", 
                tint = if (stopEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = phase != TimerPhase.FINISHED, onClick = if (isRunning) onPause else onStart)
                .testTag("play_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Start",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    1.dp, 
                    MaterialTheme.colorScheme.outlineVariant, // In real app, implement skip logic
                    CircleShape
                )
                .clickable(enabled = false, onClick = { /* Skip */ }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext, 
                contentDescription = "Skip", 
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
fun TimerCircle(
    timeRemainingMillis: Long,
    totalTimeMillis: Long,
    phase: TimerPhase,
    currentRound: Int,
    totalRounds: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTimeMillis > 0) {
        (timeRemainingMillis.toFloat() / totalTimeMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "progress"
    )

    val progressColor = getPhaseColor(phase)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val size = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arch
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inside display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val phaseLabel = when (phase) {
                TimerPhase.PREPARATION -> "PREP"
                TimerPhase.ROUND -> "WORK"
                TimerPhase.REST -> "REST"
                TimerPhase.FINISHED -> "DONE"
                TimerPhase.IDLE -> "READY"
            }
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = progressColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            val seconds = (timeRemainingMillis / 1000) % 60
            val minutes = (timeRemainingMillis / 1000) / 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            AnimatedContent(
                targetState = timeString,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "time_display"
            ) { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        letterSpacing = (-2).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Round $currentRound / $totalRounds",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun QuickStatsGrid(uiState: TimerState) {
    val profile = uiState.profile ?: return
    
    val totalSeconds = (profile.roundTimeSeconds + profile.restTimeSeconds) * profile.numberOfRounds - profile.restTimeSeconds + profile.prepTimeSeconds
    val totalMinutes = totalSeconds / 60
    val totalSecsLeft = totalSeconds % 60
    
    val restString = "${profile.restTimeSeconds / 60}:${String.format("%02d", profile.restTimeSeconds % 60)}"
    val warnString = "0:${String.format("%02d", profile.warningTimeSeconds)}"
    val totalString = "${totalMinutes}:${String.format("%02d", totalSecsLeft)}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "REST", 
            value = restString,
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "WARN", 
            value = warnString,
            highlighted = true,
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "TOTAL", 
            value = totalString,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatBox(label: String, value: String, highlighted: Boolean = false, modifier: Modifier = Modifier) {
    val bgColor = if (highlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val labelColor = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = labelColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ActiveSessionBanner(uiState: TimerState, onClick: () -> Unit) {
    val profileName = uiState.profile?.name ?: "Boxing Intervals"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = uiState.phase == TimerPhase.IDLE || uiState.phase == TimerPhase.FINISHED, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        
        Column(modifier = Modifier
            .padding(start = 12.dp)
            .weight(1f)) {
            Text(
                text = "Active Session",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = profileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun getPhaseColor(phase: TimerPhase): Color {
    return when (phase) {
        TimerPhase.PREPARATION -> MaterialTheme.colorScheme.tertiary
        TimerPhase.ROUND -> MaterialTheme.colorScheme.primary
        TimerPhase.REST -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getTotalTimeForPhase(state: TimerState): Long {
    val profile = state.profile ?: return 1L
    return when (state.phase) {
        TimerPhase.PREPARATION -> profile.prepTimeSeconds * 1000L
        TimerPhase.ROUND -> profile.roundTimeSeconds * 1000L
        TimerPhase.REST -> profile.restTimeSeconds * 1000L
        else -> 1L
    }
}
