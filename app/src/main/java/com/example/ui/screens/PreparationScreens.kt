package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoachPose
import com.example.model.OnboardingAnswers
import com.example.ui.components.BoxingAudioEngine
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.KynaroPillBadge
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroOrangeDark
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

/**
 * Clean 5-Stage Preparation Flow (Camera stage removed — 100% parity with KYNARO v2 Preview):
 * Stage 1: REAR HAND STRAIGHT (Live pixel-exact Coach demo)
 * Stage 2: LAND 3 PUNCHES (Interactive heavy pad tap challenge)
 * Stage 3: YOUR CAMP IS READY (Clean Fighter Name input — zero legacy avatar circles)
 * Stage 4: YOUR 30-DAY PROGRESSION (Day 1 Fundamentals -> Day 30 Fight Ready)
 * Stage 5: BUILT FOR <NAME> (Personalized summary -> flows directly into Hard Paywall)
 */
@Composable
fun PreparationFlowScreen(
    answers: OnboardingAnswers,
    onUpdateAnswers: (OnboardingAnswers) -> Unit,
    onBackToOnboarding: () -> Unit,
    onCompleteCampSetup: (OnboardingAnswers) -> Unit
) {
    var stage by rememberSaveable { mutableIntStateOf(1) }
    val isSouthpaw = answers.strongHand == "Left-handed"

    val handleBack: () -> Unit = {
        if (stage > 1) stage-- else onBackToOnboarding()
    }
    BackHandler(onBack = handleBack)

    KynaroScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified V2 Top Progress Header (Steps 18..22 of 22)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = handleBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = KynaroTextWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PREP • $stage OF 5",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            color = KynaroOrange
                        )
                        Text(
                            text = "${17 + stage} / 22",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = KynaroTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1E28))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(((17 + stage) / 22f).coerceIn(0.1f, 1f))
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(KynaroOrange, KynaroYellow)))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            AnimatedContent(
                targetState = stage,
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
                label = "prep_v2_stage"
            ) { currentStage ->
                when (currentStage) {
                    1 -> PrepRearCrossDemoStage(
                        isSouthpaw = isSouthpaw,
                        onContinue = { stage = 2 }
                    )
                    2 -> PrepLand3PunchesStage(
                        onContinue = { stage = 3 }
                    )
                    3 -> PrepFighterNameStage(
                        initialName = answers.fighterName,
                        onSaveName = { name ->
                            onUpdateAnswers(answers.copy(fighterName = name))
                            stage = 4
                        }
                    )
                    4 -> PrepProgressionStage(
                        onContinue = { stage = 5 }
                    )
                    5 -> PrepBuiltForYouStage(
                        answers = answers,
                        onUnlockCamp = {
                            onCompleteCampSetup(answers.copy(onboardingCompleted = true))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrepRearCrossDemoStage(
    isSouthpaw: Boolean,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "REAR HAND STRAIGHT",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Watch your Coach pivot the rear foot & drive the cross.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.radialGradient(listOf(Color(0xFF24170D), KynaroBlack)))
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = CoachPose.CROSS,
                isSouthpaw = isSouthpaw,
                showFloorRing = true,
                showAnatomicalHighlights = true,
                allowCameraOrbit = true,
                showInteractiveHint = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        KynaroPrimaryButton(
            text = "GOT IT — TRY 3 PUNCHES",
            onClick = onContinue,
            testTag = "prep_demo_continue"
        )
    }
}

@Composable
private fun PrepLand3PunchesStage(
    onContinue: () -> Unit
) {
    var hits by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        Text(
            text = "LAND 3 PUNCHES",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap the heavy target pad 3 times to lock in your timing.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(34.dp))

        Box(
            modifier = Modifier
                .size(172.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(KynaroOrange, KynaroOrangeDark)))
                .border(4.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable {
                    BoxingAudioEngine.playStrikeTick()
                    hits = (hits + 1).coerceAtMost(3)
                    if (hits >= 3) onContinue()
                }
                .testTag("prep_pad_target"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$hits / 3",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp,
                    color = KynaroBlack
                )
                Text(
                    text = "TAP TO STRIKE",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = KynaroBlack
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        KynaroPrimaryButton(
            text = if (hits >= 3) "CONTINUE" else "SKIP CHALLENGE",
            onClick = onContinue,
            testTag = "prep_pad_continue"
        )
    }
}

@Composable
private fun PrepFighterNameStage(
    initialName: String,
    onSaveName: (String) -> Unit
) {
    var nameInput by rememberSaveable { mutableStateOf(initialName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Your camp is ready.",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            color = KynaroTextWhite
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "What should your Coach call you in the ring?",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(KynaroCardBg)
                .border(1.5.dp, KynaroOrange.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            if (nameInput.isBlank()) {
                Text(
                    text = "Enter your fighter name",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KynaroTextMuted
                )
            }
            BasicTextField(
                value = nameInput,
                onValueChange = { if (it.length <= 20) nameInput = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = KynaroTextWhite
                ),
                cursorBrush = SolidColor(KynaroOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fighter_name_input")
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        KynaroPrimaryButton(
            text = "SAVE FIGHTER NAME",
            onClick = { onSaveName(nameInput.trim().ifBlank { "Fighter" }) },
            testTag = "save_fighter_name_button"
        )
    }
}

@Composable
private fun PrepProgressionStage(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "YOUR 30-DAY PROGRESSION",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "From Day 1 stance foundations to Day 30 sharp combinations.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "DAY 1",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = KynaroTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "FUNDAMENTALS",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = KynaroTextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balanced guard, crisp jab & stance discipline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KynaroTextSecondary,
                    lineHeight = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A120A))
                    .border(1.5.dp, KynaroOrange.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "DAY 30",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = KynaroOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "FIGHT READY",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = KynaroOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fluid 3-punch combos, slips, rolls & ring angles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KynaroTextWhite,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        KynaroPrimaryButton(
            text = "SEE MY 30-DAY CAMP",
            onClick = onContinue,
            testTag = "prep_progression_continue"
        )
    }
}

@Composable
private fun PrepBuiltForYouStage(
    answers: OnboardingAnswers,
    onUnlockCamp: () -> Unit
) {
    val displayName = answers.fighterName.ifBlank { "FIGHTER" }.uppercase()
    val stanceLabel = if (answers.strongHand == "Left-handed") "Southpaw" else "Orthodox"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        KynaroPillBadge(text = "30-DAY PERSONALIZED PLAN")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "BUILT FOR $displayName",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            color = KynaroTextWhite
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your personalized 30-day boxing camp is ready to start.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrepSummaryRow("Goal", answers.ringGoal)
            PrepSummaryRow("Stance", stanceLabel)
            PrepSummaryRow("Coach Persona", answers.coachStyle)
            PrepSummaryRow("Schedule", "${answers.daysPerWeekShort} • ${answers.sessionMinutesInt} min")
            PrepSummaryRow("Curriculum", "30 Days • 12 Pro Techniques", highlightValue = true)
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(20.dp))

        KynaroPrimaryButton(
            text = "UNLOCK MY 30-DAY CAMP",
            onClick = onUnlockCamp,
            testTag = "unlock_my_30_day_camp_button"
        )
    }
}

@Composable
private fun PrepSummaryRow(label: String, value: String, highlightValue: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlightValue) KynaroOrange else KynaroTextWhite
        )
    }
}
