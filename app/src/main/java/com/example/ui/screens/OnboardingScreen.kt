package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OnboardingAnswers
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.components.OnboardingProgressHeader
import com.example.ui.components.SelectableOptionCard
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    answers: OnboardingAnswers,
    onUpdateAnswers: (OnboardingAnswers) -> Unit,
    onBackToWelcome: () -> Unit,
    onFinishOnboardingQuestionnaire: () -> Unit
) {
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    val totalSteps = 17

    val handleBack: () -> Unit = {
        if (currentStep > 1) {
            currentStep--
        } else {
            onBackToWelcome()
        }
    }

    BackHandler(onBack = handleBack)

    val advanceStep: () -> Unit = {
        if (currentStep < totalSteps) {
            currentStep++
        } else {
            onFinishOnboardingQuestionnaire()
        }
    }

    val isCurrentStepValid = when (currentStep) {
        1 -> answers.ringGoal.isNotBlank()
        2 -> answers.boxedBefore.isNotBlank()
        3 -> answers.daysPerWeek.isNotBlank()
        4 -> answers.sessionDuration.isNotBlank()
        5 -> true
        6 -> answers.ageRange.isNotBlank()
        7 -> answers.heightValue > 0
        8 -> answers.weightValue > 0
        9 -> answers.fitnessLevel in 1..10
        10 -> answers.gear.isNotEmpty()
        11 -> answers.limitations.isNotEmpty()
        12 -> answers.stoppedBefore.isNotBlank()
        13 -> true
        14 -> answers.meaning.isNotBlank()
        15 -> answers.trainingFor.isNotBlank()
        16 -> answers.strongHand.isNotBlank()
        17 -> answers.coachStyle.isNotBlank()
        else -> true
    }

    KynaroScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingProgressHeader(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onBack = handleBack
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 2 } + fadeIn()).togetherWith(
                            slideOutHorizontally { -it / 2 } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { -it / 2 } + fadeIn()).togetherWith(
                            slideOutHorizontally { it / 2 } + fadeOut()
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step_content"
            ) { step ->
                when (step) {
                    1 -> Step1RingGoal(
                        selected = answers.ringGoal,
                        onSelect = { onUpdateAnswers(answers.copy(ringGoal = it)) }
                    )
                    2 -> Step2BoxedBefore(
                        selected = answers.boxedBefore,
                        onSelect = { onUpdateAnswers(answers.copy(boxedBefore = it)) }
                    )
                    3 -> Step3DaysPerWeek(
                        selected = answers.daysPerWeek,
                        onSelect = { onUpdateAnswers(answers.copy(daysPerWeek = it)) }
                    )
                    4 -> Step4SessionDuration(
                        selected = answers.sessionDuration,
                        onSelect = { onUpdateAnswers(answers.copy(sessionDuration = it)) }
                    )
                    5 -> Step5EncouragementSchedule(
                        onContinue = advanceStep
                    )
                    6 -> Step6AgeRange(
                        selected = answers.ageRange,
                        onSelect = { onUpdateAnswers(answers.copy(ageRange = it)) }
                    )
                    7 -> Step7HeightSelector(
                        heightValue = answers.heightValue,
                        heightUnit = answers.heightUnit,
                        onUpdate = { v, u ->
                            onUpdateAnswers(answers.copy(heightValue = v, heightUnit = u))
                        }
                    )
                    8 -> Step8WeightSelector(
                        weightValue = answers.weightValue,
                        weightUnit = answers.weightUnit,
                        onUpdate = { v, u ->
                            onUpdateAnswers(answers.copy(weightValue = v, weightUnit = u))
                        }
                    )
                    9 -> Step9FitnessLevel(
                        fitnessLevel = answers.fitnessLevel,
                        onSelect = { onUpdateAnswers(answers.copy(fitnessLevel = it)) }
                    )
                    10 -> Step10GearSelection(
                        selectedGear = answers.gear,
                        onToggleGear = { item ->
                            val updated = if (item == "Nothing yet") {
                                setOf("Nothing yet")
                            } else {
                                val base = answers.gear - "Nothing yet"
                                if (item in base && base.size > 1) base - item else base + item
                            }
                            onUpdateAnswers(answers.copy(gear = updated))
                        }
                    )
                    11 -> Step11LimitationsSelection(
                        selectedLimitations = answers.limitations,
                        onToggleLimitation = { item ->
                            val updated = if (item == "No limitations") {
                                setOf("No limitations")
                            } else {
                                val base = answers.limitations - "No limitations"
                                if (item in base && base.size > 1) base - item else base + item
                            }
                            onUpdateAnswers(answers.copy(limitations = updated))
                        }
                    )
                    12 -> Step12StoppedBefore(
                        selected = answers.stoppedBefore,
                        onSelect = { onUpdateAnswers(answers.copy(stoppedBefore = it)) }
                    )
                    13 -> Step13ThatsFixable(
                        stoppedBefore = answers.stoppedBefore,
                        onContinue = advanceStep
                    )
                    14 -> Step14Meaning(
                        selected = answers.meaning,
                        onSelect = { onUpdateAnswers(answers.copy(meaning = it)) }
                    )
                    15 -> Step15TrainingFor(
                        selected = answers.trainingFor,
                        onSelect = { onUpdateAnswers(answers.copy(trainingFor = it)) }
                    )
                    16 -> Step16StrongHand(
                        selected = answers.strongHand,
                        onSelect = { onUpdateAnswers(answers.copy(strongHand = it)) }
                    )
                    17 -> Step17CoachStyle(
                        selected = answers.coachStyle,
                        onSelect = { onUpdateAnswers(answers.copy(coachStyle = it)) }
                    )
                }
            }

            // Bottom CONTINUE button for all standard question screens (Steps 5 and 13 have their own GOT IT button)
            if (currentStep != 5 && currentStep != 13) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    KynaroPrimaryButton(
                        text = "CONTINUE",
                        enabled = isCurrentStepValid,
                        onClick = advanceStep,
                        testTag = "onboarding_continue_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KynaroTextWhite
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = KynaroTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Step1RingGoal(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        Triple("Learn to box", null, Icons.Default.SportsMma),
        Triple("Burn fat", null, Icons.Default.LocalFireDepartment),
        Triple("Get in fighting shape", null, Icons.Default.Bolt),
        Triple("Push past the usual", "Punch the stress out", Icons.Default.Favorite)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What brings you\nto the ring?")
        options.forEach { (title, sub, icon) ->
            SelectableOptionCard(
                title = title,
                subtitle = sub,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = icon
            )
        }
    }
}

@Composable
private fun Step2BoxedBefore(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "Brand new" to Icons.Default.Spa,
        "I've boxed" to Icons.Default.SportsMma
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("Have you boxed\nbefore?")
        options.forEach { (title, icon) ->
            SelectableOptionCard(
                title = title,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = icon
            )
        }
    }
}

@Composable
private fun Step3DaysPerWeek(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("2 days", "3 days", "4 days", "5+ days")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("How many days a\nweek\ncan you train?")
        options.forEach { title ->
            SelectableOptionCard(
                title = title,
                selected = selected == title,
                onClick = { onSelect(title) }
            )
        }
    }
}

@Composable
private fun Step4SessionDuration(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("10 minutes", "20 minutes", "30 minutes", "45+ minutes")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("How long is a good\nsession for you?")
        options.forEach { title ->
            SelectableOptionCard(
                title = title,
                selected = selected == title,
                onClick = { onSelect(title) }
            )
        }
    }
}

@Composable
private fun Step5EncouragementSchedule(onContinue: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glove_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glove_glow_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Glowing 3D Red & Yellow Boxing Glove Illustration matching onboadingflow6_be1cbc35.jpg
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroRed.copy(alpha = 0.35f),
                            KynaroRed.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.48f * glowScale
                    ),
                    radius = size.minDimension * 0.48f * glowScale
                )

                // Glove main dome (Championship Gold)
                drawRoundRect(
                    color = KynaroRed,
                    topLeft = Offset(cx - 46.dp.toPx(), cy - 58.dp.toPx()),
                    size = Size(92.dp.toPx(), 84.dp.toPx()),
                    cornerRadius = CornerRadius(42.dp.toPx(), 42.dp.toPx())
                )
                // Thumb curve
                drawRoundRect(
                    color = Color(0xFFD9A300),
                    topLeft = Offset(cx + 16.dp.toPx(), cy - 34.dp.toPx()),
                    size = Size(36.dp.toPx(), 48.dp.toPx()),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
                // Obsidian Black wrist cuff
                drawRoundRect(
                    color = Color(0xFF14161D),
                    topLeft = Offset(cx - 28.dp.toPx(), cy + 20.dp.toPx()),
                    size = Size(56.dp.toPx(), 34.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                // White monolith strap patch
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - 20.dp.toPx(), cy + 29.dp.toPx()),
                    size = Size(40.dp.toPx(), 14.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Good call.",
            style = MaterialTheme.typography.displaySmall,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "A set schedule is what makes it stick.",
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        KynaroPrimaryButton(
            text = "GOT IT",
            onClick = onContinue,
            testTag = "good_call_got_it_button"
        )
    }
}

@Composable
private fun Step6AgeRange(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("Under 25", "25 to 34", "35 to 44", "45+")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What's your age?")
        options.forEach { title ->
            SelectableOptionCard(
                title = title,
                selected = selected == title,
                onClick = { onSelect(title) }
            )
        }
    }
}

@Composable
private fun Step7HeightSelector(
    heightValue: Int,
    heightUnit: String,
    onUpdate: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionHeader("How tall are you?")

        Spacer(modifier = Modifier.weight(1f))

        // Unit Switcher (CM / FT-IN)
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, CircleShape)
                .padding(4.dp)
        ) {
            listOf("CM", "IN").forEach { unit ->
                val isSelected = heightUnit == unit
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) KynaroRed else Color.Transparent)
                        .clickable {
                            if (unit != heightUnit) {
                                val converted = if (unit == "IN") {
                                    (heightValue / 2.54f).toInt().coerceIn(48, 88)
                                } else {
                                    (heightValue * 2.54f).toInt().coerceIn(130, 230)
                                }
                                onUpdate(converted, unit)
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = unit,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) KynaroBlack else KynaroTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        NumberStepperRow(
            value = heightValue,
            unitLabel = heightUnit,
            minVal = if (heightUnit == "CM") 130 else 48,
            maxVal = if (heightUnit == "CM") 230 else 88,
            onValueChange = { onUpdate(it, heightUnit) }
        )

        Spacer(modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun Step8WeightSelector(
    weightValue: Int,
    weightUnit: String,
    onUpdate: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionHeader("What do you\nweigh?")

        Spacer(modifier = Modifier.weight(1f))

        // Unit Switcher (KG / LBS)
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, CircleShape)
                .padding(4.dp)
        ) {
            listOf("KG", "LBS").forEach { unit ->
                val isSelected = weightUnit == unit
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) KynaroRed else Color.Transparent)
                        .clickable {
                            if (unit != weightUnit) {
                                val converted = if (unit == "LBS") {
                                    (weightValue * 2.205f).toInt().coerceIn(85, 350)
                                } else {
                                    (weightValue / 2.205f).toInt().coerceIn(40, 160)
                                }
                                onUpdate(converted, unit)
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = unit,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) KynaroBlack else KynaroTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        NumberStepperRow(
            value = weightValue,
            unitLabel = weightUnit,
            minVal = if (weightUnit == "KG") 40 else 85,
            maxVal = if (weightUnit == "KG") 160 else 350,
            onValueChange = { onUpdate(it, weightUnit) }
        )

        Spacer(modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun NumberStepperRow(
    value: Int,
    unitLabel: String,
    minVal: Int,
    maxVal: Int,
    onValueChange: (Int) -> Unit
) {
    var holdingDelta by remember { mutableIntStateOf(0) }
    var currentVal by remember(value) { mutableIntStateOf(value) }

    LaunchedEffect(holdingDelta) {
        if (holdingDelta != 0) {
            delay(280)
            while (holdingDelta != 0) {
                val next = (currentVal + holdingDelta).coerceIn(minVal, maxVal)
                currentVal = next
                onValueChange(next)
                delay(75)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus Circle Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, CircleShape)
                    .pointerInput(minVal, maxVal) {
                        detectTapGestures(
                            onPress = {
                                val next = (currentVal - 1).coerceIn(minVal, maxVal)
                                currentVal = next
                                onValueChange(next)
                                holdingDelta = -1
                                tryAwaitRelease()
                                holdingDelta = 0
                            }
                        )
                    }
                    .testTag("stepper_minus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = KynaroTextWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Big Red Value Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.toString(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 68.sp,
                    color = KynaroRed
                )
                Text(
                    text = unitLabel,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = KynaroTextSecondary
                )
            }

            // Plus Circle Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, CircleShape)
                    .pointerInput(minVal, maxVal) {
                        detectTapGestures(
                            onPress = {
                                val next = (currentVal + 1).coerceIn(minVal, maxVal)
                                currentVal = next
                                onValueChange(next)
                                holdingDelta = 1
                                tryAwaitRelease()
                                holdingDelta = 0
                            }
                        )
                    }
                    .testTag("stepper_plus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = KynaroTextWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Hold to fast-forward",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextMuted
        )
    }
}

@Composable
private fun Step9FitnessLevel(fitnessLevel: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuestionHeader("How fit are you\nright now?")

        Spacer(modifier = Modifier.height(20.dp))

        // Big Red Number 1..10 matching onboadingflow9_e3f81125.jpg
        Text(
            text = fitnessLevel.toString(),
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 68.sp,
            color = KynaroRed
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 10 Interactive Segmented Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (level in 1..10) {
                val isFilled = level <= fitnessLevel
                val segmentBg = if (isFilled) {
                    val ratio = level / 10f
                    Color(
                        red = (0.55f + 0.45f * ratio).coerceIn(0f, 1f),
                        green = (0.42f + 0.36f * ratio).coerceIn(0f, 1f),
                        blue = 0.03f
                    )
                } else {
                    KynaroCardBg
                }
                val segmentBorder = if (isFilled) KynaroRed else KynaroCardBorder

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(segmentBg)
                        .border(1.2.dp, segmentBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelect(level) }
                        .testTag("fitness_level_$level")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Out of shape",
                style = MaterialTheme.typography.bodyMedium,
                color = KynaroTextMuted
            )
            Text(
                text = "Athlete",
                style = MaterialTheme.typography.bodyMedium,
                color = KynaroTextMuted
            )
        }
    }
}

@Composable
private fun Step10GearSelection(selectedGear: Set<String>, onToggleGear: (String) -> Unit) {
    val options = listOf(
        "Gloves" to Icons.Default.SportsMma,
        "Jump rope" to Icons.Default.FitnessCenter,
        "Heavy bag" to Icons.Default.Shield,
        "Nothing yet" to Icons.Default.Bolt
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What gear do you\nhave?")
        options.forEach { (title, icon) ->
            SelectableOptionCard(
                title = title,
                selected = title in selectedGear,
                onClick = { onToggleGear(title) },
                icon = icon
            )
        }
    }
}

@Composable
private fun Step11LimitationsSelection(
    selectedLimitations: Set<String>,
    onToggleLimitation: (String) -> Unit
) {
    val options = listOf(
        "No limitations" to "All clear",
        "Elbow" to null,
        "Shoulders" to null,
        "Wrists / hands" to null,
        "Knees" to null,
        "Lower back" to null
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        QuestionHeader("Anything we should\nwork around?")
        options.forEach { (title, sub) ->
            SelectableOptionCard(
                title = title,
                subtitle = sub,
                selected = title in selectedLimitations,
                onClick = { onToggleLimitation(title) },
                icon = if (title == "No limitations") Icons.Default.Shield else null
            )
        }
    }
}

@Composable
private fun Step12StoppedBefore(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "No time",
        "Lost motivation",
        "No real plan",
        "Results came too slow"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What's stopped you\nbefore?")
        options.forEach { title ->
            SelectableOptionCard(
                title = title,
                selected = selected == title,
                onClick = { onSelect(title) }
            )
        }
    }
}

@Composable
private fun Step13ThatsFixable(stoppedBefore: String, onContinue: () -> Unit) {
    val dynamicSubtitle = when (stoppedBefore) {
        "Lost motivation" -> "Your 3D coach tracks every streak and keeps you locked in."
        "No real plan" -> "30 days of structured rounds built around your exact level."
        "Results came too slow" -> "High-output boxing rounds torch calories from Day 1."
        else -> "Sessions fit the minutes you have."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Bullseye Target with Blue Dart matching onboadingflow13_39aa661f.jpg
        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroYellow.copy(alpha = 0.22f),
                            KynaroRed.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.48f
                    ),
                    radius = size.minDimension * 0.48f
                )

                // Concentric Target Rings
                drawCircle(color = KynaroRed, radius = 64.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 46.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = KynaroRed, radius = 30.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 15.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = KynaroRed, radius = 7.dp.toPx(), center = Offset(cx, cy))

                // Blue Dart hitting center
                drawLine(
                    color = Color(0xFF0EA5E9),
                    start = Offset(cx, cy),
                    end = Offset(cx + 46.dp.toPx(), cy - 42.dp.toPx()),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                val fletching = Path().apply {
                    moveTo(cx + 36.dp.toPx(), cy - 34.dp.toPx())
                    lineTo(cx + 56.dp.toPx(), cy - 52.dp.toPx())
                    lineTo(cx + 60.dp.toPx(), cy - 36.dp.toPx())
                    close()
                }
                drawPath(path = fletching, color = Color(0xFF0284C7))
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "That's fixable.",
            style = MaterialTheme.typography.displaySmall,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = dynamicSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        KynaroPrimaryButton(
            text = "GOT IT",
            containerColor = KynaroYellow,
            contentColor = KynaroBlack,
            onClick = onContinue,
            testTag = "thats_fixable_got_it_button"
        )
    }
}

@Composable
private fun Step14Meaning(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        Triple("Confidence", null, Icons.Default.WorkspacePremium),
        Triple("Discipline", null, Icons.Default.TrackChanges),
        Triple("Look better", "Look the part", Icons.Default.LocalFireDepartment),
        Triple("Handle yourself", null, Icons.Default.Shield)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What would getting\nthere\nmean to you?")
        options.forEach { (title, sub, icon) ->
            SelectableOptionCard(
                title = title,
                subtitle = sub,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = icon
            )
        }
    }
}

@Composable
private fun Step15TrainingFor(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        Triple("Summer body", null, Icons.Default.LocalFireDepartment),
        Triple("Amateur boxing", "An event", Icons.Default.Schedule),
        Triple("Fight-ready", null, Icons.Default.EmojiEvents),
        Triple("Just ready", "No deadline", Icons.Default.Favorite)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("Training for\nsomething?")
        options.forEach { (title, sub, icon) ->
            SelectableOptionCard(
                title = title,
                subtitle = sub,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = icon
            )
        }
    }
}

@Composable
private fun Step16StrongHand(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "Right-handed" to "Orthodox stance (Left lead, Right power cross)",
        "Left-handed" to "Southpaw stance (Right lead, Left power cross)"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("Which is your\nstrong hand?")
        options.forEach { (title, sub) ->
            SelectableOptionCard(
                title = title,
                subtitle = sub,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = Icons.Default.SportsMma
            )
        }
    }
}

@Composable
private fun Step17CoachStyle(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        Triple("The Drill Sergeant", "No excuses. Push me hard.", Icons.Default.LocalFireDepartment),
        Triple("The Hype Man", "Loud energy, big celebrations", Icons.Default.Bolt),
        Triple("The Technician", "Calm, precise, detail-obsessed", Icons.Default.TrackChanges)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestionHeader("What kind of coach\ndo you want?")
        options.forEach { (title, subtitle, icon) ->
            SelectableOptionCard(
                title = title,
                subtitle = subtitle,
                selected = selected == title,
                onClick = { onSelect(title) },
                icon = icon
            )
        }
    }
}
