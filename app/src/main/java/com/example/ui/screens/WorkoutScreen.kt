package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OnboardingAnswers
import com.example.model.WorkoutExerciseStep
import com.example.model.WorkoutSpeed
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.CoachTransformationDualView
import com.example.ui.components.KynaroPillBadge
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

private enum class WorkoutCenterViewMode {
    COACH_3D,
    TRANSFORMATION
}

@Composable
fun WorkoutScreen(
    workoutTitle: String,
    missionDay: Int,
    steps: List<WorkoutExerciseStep>,
    answers: OnboardingAnswers,
    onCompleteWorkout: (
        durationSeconds: Int,
        kcalBurned: Int,
        punchesLanded: Int,
        roundsCompleted: Int,
        xpEarned: Int
    ) -> Unit,
    onExitWorkout: () -> Unit
) {
    BackHandler(onBack = onExitWorkout)

    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentStep = steps.getOrElse(currentStepIndex) { steps.first() }

    var isPaused by rememberSaveable { mutableStateOf(false) }
    var isRestingBetweenRounds by rememberSaveable { mutableStateOf(false) }
    var restSecondsRemaining by rememberSaveable { mutableIntStateOf(15) }
    var isWorkoutCompleted by rememberSaveable { mutableStateOf(false) }

    var selectedSpeed by rememberSaveable { mutableStateOf(WorkoutSpeed.NORMAL) }
    var viewMode by rememberSaveable(currentStepIndex) {
        mutableStateOf(WorkoutCenterViewMode.COACH_3D)
    }

    var stepSecondsLeft by rememberSaveable(currentStepIndex) {
        mutableIntStateOf(currentStep.durationSeconds)
    }
    var totalElapsedSeconds by rememberSaveable { mutableIntStateOf(0) }
    var totalSessionRemainingSeconds by rememberSaveable {
        mutableIntStateOf(answers.sessionMinutesInt * 60)
    }

    var currentStepPunches by rememberSaveable(currentStepIndex) { mutableIntStateOf(0) }
    var totalPunchesLanded by rememberSaveable { mutableIntStateOf(0) }
    var punchTriggerCount by remember { mutableIntStateOf(0) }
    var coachFeedbackBanner by remember { mutableStateOf("HANDS HIGH • CHIN TUCKED") }

    val estimatedKcal = remember(totalElapsedSeconds, totalPunchesLanded, selectedSpeed) {
        ((totalElapsedSeconds * 0.22f * selectedSpeed.kcalMultiplier) + (totalPunchesLanded * 0.35f)).toInt()
    }

    // Main Workout & Rest Countdown Loop
    LaunchedEffect(isPaused, isRestingBetweenRounds, isWorkoutCompleted, currentStepIndex, selectedSpeed) {
        while (!isPaused && !isWorkoutCompleted) {
            delay(1000L)
            if (isRestingBetweenRounds) {
                if (restSecondsRemaining > 1) {
                    restSecondsRemaining--
                } else {
                    isRestingBetweenRounds = false
                    if (currentStepIndex < steps.lastIndex) {
                        currentStepIndex++
                    } else {
                        isWorkoutCompleted = true
                    }
                }
            } else {
                totalElapsedSeconds++
                if (totalSessionRemainingSeconds > 0) totalSessionRemainingSeconds--
                // Auto-log rhythm punches as the user trains along with the 3D coach
                if (totalElapsedSeconds % 2 == 0) {
                    currentStepPunches++
                    totalPunchesLanded++
                }
                if (stepSecondsLeft > 1) {
                    stepSecondsLeft--
                } else {
                    if (currentStepIndex < steps.lastIndex) {
                        restSecondsRemaining = 12
                        isRestingBetweenRounds = true
                    } else {
                        isWorkoutCompleted = true
                    }
                }
            }
        }
    }

    val triggerManualPunch = {
        currentStepPunches++
        totalPunchesLanded++
        punchTriggerCount++
        val callouts = when (answers.coachStyle) {
            "The Drill Sergeant" -> listOf("HARDER! SNAP IT BACK!", "GOOD TORQUE! KEEP FIRING!", "DON'T DROP THAT GUARD!")
            "The Hype Man" -> listOf("BOOM! CLEAN SHOT!", "PURE LIGHTNING! LET'S GO!", "UNSTOPPABLE COMBO!")
            else -> listOf("PERFECT EXTENSION!", "CLEAN HIP ROTATION!", "SHARP RETRACTION!")
        }
        coachFeedbackBanner = callouts[totalPunchesLanded % callouts.size]
    }

    KynaroScreenBackground {
        if (isWorkoutCompleted) {
            val xpEarned = 450 + (totalPunchesLanded * 5)
            WorkoutCompletedCelebrationView(
                workoutTitle = workoutTitle,
                missionDay = missionDay,
                kcalBurned = estimatedKcal,
                punchesLanded = totalPunchesLanded,
                roundsCompleted = steps.size,
                xpEarned = xpEarned,
                onFinishAndSave = {
                    onCompleteWorkout(
                        totalElapsedSeconds.coerceAtLeast(60),
                        estimatedKcal,
                        totalPunchesLanded,
                        steps.size,
                        xpEarned
                    )
                }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Center Stage: 3D Coach Character / Pad Challenge / Transformation View
                AnimatedContent(
                    targetState = Triple(currentStep, viewMode, isRestingBetweenRounds),
                    transitionSpec = { fadeIn(tween(240)).togetherWith(fadeOut(tween(240))) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 68.dp, bottom = 148.dp),
                    label = "workout_center_stage"
                ) { (step, mode, resting) ->
                    when {
                        resting -> {
                            RestRoundOverlayView(
                                restSecondsLeft = restSecondsRemaining,
                                nextStep = steps.getOrNull(currentStepIndex + 1) ?: step,
                                onAdd15Seconds = { restSecondsRemaining += 15 },
                                onSkipRest = {
                                    isRestingBetweenRounds = false
                                    if (currentStepIndex < steps.lastIndex) {
                                        currentStepIndex++
                                    } else {
                                        isWorkoutCompleted = true
                                    }
                                }
                            )
                        }

                        mode == WorkoutCenterViewMode.TRANSFORMATION -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CoachTransformationDualView(isSilhouetteMode = false)
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { triggerManualPunch() },
                                contentAlignment = Alignment.Center
                            ) {
                                CoachCharacter3DView(
                                    pose = step.pose,
                                    speed = selectedSpeed,
                                    isPaused = isPaused,
                                    showAnatomicalHighlights = true,
                                    punchTriggerCount = punchTriggerCount,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Coach Live Cue / Feedback Pill near bottom of 3D stage
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp, start = 24.dp, end = 24.dp)
                                        .clip(CircleShape)
                                        .background(KynaroCardBg.copy(alpha = 0.9f))
                                        .border(1.dp, KynaroRed.copy(alpha = 0.45f), CircleShape)
                                        .clickable { triggerManualPunch() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .testTag("workout_punch_action_pill")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SportsMma,
                                            contentDescription = null,
                                            tint = KynaroYellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$coachFeedbackBanner • TAP TO LAND PUNCH ($currentStepPunches/${step.targetPunches})",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = KynaroTextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Top Header Bar matching unnamed__1__38e8d827.webp & unnamed__4__63d5fa85.webp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onExitWorkout,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .testTag("workout_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Workout",
                            tint = KynaroTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentStep.title,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp,
                            color = KynaroTextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentStep.subtitle,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.1.sp,
                            color = KynaroTextMuted
                        )
                    }

                    Text(
                        text = formatMinutesSeconds(totalSessionRemainingSeconds),
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = KynaroTextWhite
                    )
                }

                // 3. Left Floating Telemetry Stack (KCAL card, ROUND / TEACH card, TIME card, Mode Switcher)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 74.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KCAL Card matching reference
                    Column(
                        modifier = Modifier
                            .width(82.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KynaroCardBg.copy(alpha = 0.92f))
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            tint = KynaroRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$estimatedKcal",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = KynaroTextWhite
                        )
                        Text(
                            text = if (viewMode == WorkoutCenterViewMode.TRANSFORMATION) "EST. KCAL" else "KCAL",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            color = KynaroRed
                        )
                    }

                    // TEACH or ROUND X/Y Card matching unnamed__4 & unnamed__1
                    if (currentStep.isTeachStep) {
                        Box(
                            modifier = Modifier
                                .width(82.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KynaroCardBg.copy(alpha = 0.92f))
                                .border(1.dp, KynaroCardBorder, RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "TEACH",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = KynaroYellow
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .width(82.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(KynaroCardBg.copy(alpha = 0.92f))
                                .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ROUND",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = KynaroYellow
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentStepIndex + 1}/${steps.size}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = KynaroTextWhite
                            )
                        }
                    }

                    // Step Countdown Timer Card ("TIME 0:28")
                    Column(
                        modifier = Modifier
                            .width(82.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KynaroCardBg.copy(alpha = 0.92f))
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!currentStep.isTeachStep) {
                            Text(
                                text = "TIME",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = KynaroYellow
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = formatMinutesSeconds(stepSecondsLeft),
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = KynaroTextWhite
                        )
                    }

                    // View Mode Switcher Pill (3D / FIT)
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1608))
                            .border(1.dp, KynaroRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewMode = when (viewMode) {
                                    WorkoutCenterViewMode.COACH_3D -> WorkoutCenterViewMode.TRANSFORMATION
                                    WorkoutCenterViewMode.TRANSFORMATION -> WorkoutCenterViewMode.COACH_3D
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("workout_mode_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (viewMode) {
                                WorkoutCenterViewMode.COACH_3D -> "FIT VIEW"
                                WorkoutCenterViewMode.TRANSFORMATION -> "3D COACH"
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = KynaroRed
                        )
                    }
                }

                // 4. Right Vertical Exercise Node Timeline matching unnamed__1__38e8d827.webp
                WorkoutRightVerticalTimeline(
                    totalSteps = steps.size,
                    currentStepIndex = currentStepIndex,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 18.dp)
                        .height(280.dp)
                        .width(24.dp)
                )

                // 5. Bottom Controls: Previous (<), Play/Pause Red Circle (||), Next (>) + Speed Selector Bar (LIGHT | NORMAL | BEAST | SLOW)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: <   [ || ]   >
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Step Button (<)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KynaroCardBg)
                                .border(1.dp, KynaroCardBorder, CircleShape)
                                .clickable {
                                    if (currentStepIndex > 0) {
                                        isRestingBetweenRounds = false
                                        currentStepIndex--
                                    }
                                }
                                .testTag("workout_prev_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Exercise",
                                tint = KynaroTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Central Championship Gold Circle Pause / Resume Button
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(KynaroRed)
                                .clickable { isPaused = !isPaused }
                                .testTag("workout_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Resume Workout" else "Pause Workout",
                                tint = KynaroBlack,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Next / Complete Step Button (>)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KynaroCardBg)
                                .border(1.dp, KynaroCardBorder, CircleShape)
                                .clickable {
                                    isRestingBetweenRounds = false
                                    if (currentStepIndex < steps.lastIndex) {
                                        currentStepIndex++
                                    } else {
                                        isWorkoutCompleted = true
                                    }
                                }
                                .testTag("workout_next_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Next Exercise",
                                tint = KynaroTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2: Speed / Intensity Selector Bar (LIGHT | NORMAL | BEAST | SLOW)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WorkoutSpeed.entries.forEach { speedOption ->
                            val isSelected = selectedSpeed == speedOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) KynaroRed else Color.Transparent)
                                    .clickable { selectedSpeed = speedOption }
                                    .padding(vertical = 10.dp)
                                    .testTag("speed_option_${speedOption.label.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = speedOption.label,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = if (isSelected) KynaroBlack else KynaroTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutRightVerticalTimeline(
    totalSteps: Int,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val count = totalSteps.coerceAtLeast(2)
        val stepGap = size.height / (count - 1).toFloat()

        // Background track line
        drawLine(
            color = Color(0xFF2A2D3A),
            start = Offset(cx, 0f),
            end = Offset(cx, size.height),
            strokeWidth = 2.dp.toPx()
        )

        // Active red line up to current step
        val activeEndY = (currentStepIndex * stepGap).coerceAtMost(size.height)
        drawLine(
            color = KynaroRed,
            start = Offset(cx, 0f),
            end = Offset(cx, activeEndY),
            strokeWidth = 2.5.dp.toPx()
        )

        for (i in 0 until count) {
            val y = i * stepGap
            when {
                i == 0 && currentStepIndex > 0 -> {
                    drawCircle(color = KynaroRed, radius = 5.dp.toPx(), center = Offset(cx, y))
                }
                i < currentStepIndex -> {
                    drawCircle(color = KynaroGreen, radius = 5.dp.toPx(), center = Offset(cx, y))
                }
                i == currentStepIndex -> {
                    drawCircle(color = KynaroBlack, radius = 8.dp.toPx(), center = Offset(cx, y))
                    drawCircle(
                        color = KynaroRed,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, y),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
                else -> {
                    drawCircle(color = KynaroBlack, radius = 5.dp.toPx(), center = Offset(cx, y))
                    drawCircle(
                        color = Color(0xFF3E4252),
                        radius = 5.dp.toPx(),
                        center = Offset(cx, y),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun RestRoundOverlayView(
    restSecondsLeft: Int,
    nextStep: WorkoutExerciseStep,
    onAdd15Seconds: () -> Unit,
    onSkipRest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KynaroPillBadge(text = "ACTIVE RECOVERY • BREATHE")
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "0:${String.format("%02d", restSecondsLeft)}",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 64.sp,
            color = KynaroYellow
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "UP NEXT: ${nextStep.title}",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = KynaroTextWhite
        )
        Spacer(modifier = Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KynaroCardElevated)
                    .border(1.dp, KynaroCardBorder, CircleShape)
                    .clickable(onClick = onAdd15Seconds)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "+15s REST",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = KynaroTextWhite
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KynaroRed)
                    .clickable(onClick = onSkipRest)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("skip_rest_button")
            ) {
                Text(
                    text = "SKIP REST ›",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = KynaroBlack
                )
            }
        }
    }
}

@Composable
private fun WorkoutCompletedCelebrationView(
    workoutTitle: String,
    missionDay: Int,
    kcalBurned: Int,
    punchesLanded: Int,
    roundsCompleted: Int,
    xpEarned: Int,
    onFinishAndSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KynaroPillBadge(text = "ROUND COMPLETE • +$xpEarned XP")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "MISSION ACCOMPLISHED",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$workoutTitle • Day $missionDay",
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = com.example.model.CoachPose.VICTORY_RAISE,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStatCol("PUNCHES", "$punchesLanded", KynaroTextWhite)
            SummaryStatCol("BURNED", "$kcalBurned kcal", KynaroRed)
            SummaryStatCol("ROUNDS", "$roundsCompleted/$roundsCompleted", KynaroGreen)
            SummaryStatCol("REWARD", "+$xpEarned XP", KynaroYellow)
        }

        Spacer(modifier = Modifier.height(20.dp))

        KynaroPrimaryButton(
            text = "SAVE PROGRESS & CONTINUE",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KynaroBlack,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = onFinishAndSave,
            testTag = "finish_workout_save_button"
        )
    }
}

@Composable
private fun SummaryStatCol(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = KynaroTextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = valueColor
        )
    }
}

private fun formatMinutesSeconds(totalSeconds: Int): String {
    val mins = (totalSeconds / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return String.format("%d:%02d", mins, secs)
}
