package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoachPose
import com.example.ui.components.BoxingAudioEngine
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroOrangeDark
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

enum class TimerPreset(
    val title: String,
    val workSec: Int,
    val restSec: Int,
    val totalRounds: Int,
    val description: String
) {
    CHAMPIONSHIP("Championship", 180, 60, 5, "3:00 work • 1:00 rest"),
    AMATEUR("Olympic Boxing", 120, 60, 3, "2:00 work • 1:00 rest"),
    HIIT_BLITZ("Power Blitz", 60, 30, 6, "1:00 work • 0:30 rest"),
    SPEED_BURNOUT("Speed Burnout", 30, 15, 8, "0:30 work • 0:15 rest")
}

enum class RoundState {
    CONFIG,
    GET_READY,
    WORK,
    REST,
    FINISHED
}

data class ComboCallout(
    val calloutText: String,
    val punchTokens: List<String>,
    val pose: CoachPose
)

private val tacticalCombos = listOf(
    ComboCallout("1 - 2!", listOf("1 JAB", "2 CROSS"), CoachPose.ONE_TWO_COMBO),
    ComboCallout("1 - 2 - 3!", listOf("1 JAB", "2 CROSS", "3 HOOK"), CoachPose.JAB_CROSS_HOOK),
    ComboCallout("1 - 1 - 2!", listOf("1 JAB", "1 JAB", "2 CROSS"), CoachPose.JAB),
    ComboCallout("SLIP - 2!", listOf("SLIP", "2 CROSS"), CoachPose.SLIP_DEFENSE),
    ComboCallout("1 - 2 - 6!", listOf("1 JAB", "2 CROSS", "6 UPPERCUT"), CoachPose.UPPERCUT),
    ComboCallout("SPEED FLURRY!", listOf("1 JAB", "2 CROSS", "1 JAB", "2 CROSS"), CoachPose.SPEED_HANDS),
    ComboCallout("STEP - PIVOT!", listOf("STEP IN", "90° PIVOT"), CoachPose.FOOTWORK)
)

@Composable
fun HeavyBagRoundTimerScreen(
    isSouthpaw: Boolean = false,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedPreset by remember { mutableStateOf(TimerPreset.CHAMPIONSHIP) }
    var currentRoundState by remember { mutableStateOf(RoundState.CONFIG) }
    var currentRoundNumber by remember { mutableIntStateOf(1) }
    var secondsRemaining by remember { mutableIntStateOf(selectedPreset.workSec) }
    var isPaused by remember { mutableStateOf(false) }
    var isComboCallerEnabled by remember { mutableStateOf(true) }
    var currentComboIndex by remember { mutableIntStateOf(0) }
    var totalPunchesLanded by remember { mutableIntStateOf(0) }
    var estCaloriesBurned by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentRoundState, isPaused, secondsRemaining) {
        if (isPaused) return@LaunchedEffect

        when (currentRoundState) {
            RoundState.GET_READY -> {
                delay(1000)
                if (secondsRemaining > 1) {
                    secondsRemaining -= 1
                } else {
                    currentRoundState = RoundState.WORK
                    secondsRemaining = selectedPreset.workSec
                    BoxingAudioEngine.playRoundStartBell(scope)
                }
            }
            RoundState.WORK -> {
                if (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining -= 1
                    estCaloriesBurned += 0.28f
                    totalPunchesLanded += 2

                    if (secondsRemaining == 10) {
                        BoxingAudioEngine.playTenSecondWarning(scope)
                    }

                    if (isComboCallerEnabled && secondsRemaining % 5 == 0 && secondsRemaining > 5) {
                        currentComboIndex = (currentComboIndex + 1) % tacticalCombos.size
                        BoxingAudioEngine.playComboChime(scope)
                    }
                } else {
                    BoxingAudioEngine.playRoundEndBell(scope)
                    if (currentRoundNumber < selectedPreset.totalRounds) {
                        currentRoundState = RoundState.REST
                        secondsRemaining = selectedPreset.restSec
                    } else {
                        currentRoundState = RoundState.FINISHED
                    }
                }
            }
            RoundState.REST -> {
                if (secondsRemaining > 0) {
                    delay(1000)
                    secondsRemaining -= 1
                    if (secondsRemaining == 5) {
                        BoxingAudioEngine.playTenSecondWarning(scope)
                    }
                } else {
                    currentRoundNumber += 1
                    currentRoundState = RoundState.WORK
                    secondsRemaining = selectedPreset.workSec
                    BoxingAudioEngine.playRoundStartBell(scope)
                }
            }
            else -> {}
        }
    }

    KynaroScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .testTag("close_round_timer_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = KynaroTextWhite)
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1A120A))
                        .border(1.dp, KynaroOrange.copy(alpha = 0.45f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsMma,
                        contentDescription = null,
                        tint = KynaroOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HEAVY BAG & SHADOWBOXING",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KynaroOrange,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { BoxingAudioEngine.playRoundStartBell(scope) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test Bell", tint = KynaroYellow)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentRoundState == RoundState.CONFIG) {
                Text(
                    text = "PRO ROUND TIMER",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    color = KynaroTextWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Real boxing rounds with live 3D Coach combo demos & gym bell cues.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                TimerPreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color(0xFF1B1209) else KynaroCardBg)
                            .border(
                                1.dp,
                                if (isSelected) KynaroOrange else KynaroCardBorder,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                selectedPreset = preset
                                secondsRemaining = preset.workSec
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title.uppercase(),
                                fontFamily = OswaldFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSelected) KynaroOrange else KynaroTextWhite
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = KynaroTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) KynaroOrange else Color(0xFF1E212B))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${preset.totalRounds} ROUNDS",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) KynaroBlack else KynaroTextWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = KynaroOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE 3D COMBO CALLER",
                                fontFamily = OswaldFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = KynaroTextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3D Coach demonstrates every called combo in real time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KynaroTextSecondary
                        )
                    }
                    Switch(
                        checked = isComboCallerEnabled,
                        onCheckedChange = { isComboCallerEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KynaroTextWhite,
                            checkedTrackColor = KynaroOrange
                        )
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                KynaroPrimaryButton(
                    text = "START TRAINING ROUNDS",
                    onClick = {
                        currentRoundState = RoundState.GET_READY
                        secondsRemaining = 3
                        currentRoundNumber = 1
                        totalPunchesLanded = 0
                        estCaloriesBurned = 0f
                        BoxingAudioEngine.playStrikeTick()
                    },
                    testTag = "start_heavy_bag_rounds_button"
                )
            } else if (currentRoundState == RoundState.FINISHED) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ROUNDS COMPLETE",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = KynaroTextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${selectedPreset.totalRounds} Rounds Completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard("EST. STRIKES", "$totalPunchesLanded", KynaroOrange, Modifier.weight(1f))
                    StatMetricCard("CALORIES", "${estCaloriesBurned.toInt()} kcal", KynaroYellow, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(28.dp))
                KynaroPrimaryButton(text = "DONE", onClick = onClose)
            } else {
                val isWork = currentRoundState == RoundState.WORK
                val isRest = currentRoundState == RoundState.REST
                val isGetReady = currentRoundState == RoundState.GET_READY
                val activeCombo = tacticalCombos[currentComboIndex]

                // Stage Badge + Clock Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                when {
                                    isWork -> KynaroOrange
                                    isRest -> Color(0xFF1E3A8A)
                                    else -> KynaroYellow
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                isGetReady -> "GET READY"
                                isWork -> "ROUND $currentRoundNumber / ${selectedPreset.totalRounds}"
                                isRest -> "REST & RECOVER"
                                else -> ""
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isRest) KynaroTextWhite else KynaroBlack
                        )
                    }

                    val mins = secondsRemaining / 60
                    val secs = secondsRemaining % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 46.sp,
                        color = when {
                            isWork && secondsRemaining <= 10 -> KynaroOrange
                            isWork -> KynaroTextWhite
                            isRest -> Color(0xFF60A5FA)
                            else -> KynaroYellow
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LIVE 3D COACH PERFORMING THE CURRENT COMBO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF1F160F), KynaroBlack))
                        )
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CoachCharacter3DView(
                        pose = if (isWork && isComboCallerEnabled) activeCombo.pose else CoachPose.IDLE_GUARD,
                        isPaused = isPaused,
                        isSouthpaw = isSouthpaw,
                        showFloorRing = true,
                        showAnatomicalHighlights = isWork,
                        allowCameraOrbit = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isWork && isComboCallerEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroOrange.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = activeCombo.calloutText,
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = KynaroTextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeCombo.punchTokens.forEach { token ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(KynaroOrange)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = token,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = KynaroBlack
                                    )
                                }
                            }
                        }
                    }
                } else if (isRest) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F1A2E))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Deep nose inhale, slow mouth exhale. Shake out shoulders — next round starts on the bell.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF93C5FD),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentRoundState = RoundState.CONFIG
                            secondsRemaining = selectedPreset.workSec
                            isPaused = false
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = KynaroTextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(KynaroOrange, KynaroOrangeDark)))
                            .clickable { isPaused = !isPaused }
                            .testTag("timer_pause_resume_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = KynaroBlack,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = { BoxingAudioEngine.playRoundStartBell(scope) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.SportsMma, contentDescription = "Bell", tint = KynaroYellow)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KynaroCardBg)
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = KynaroTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = OswaldFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
