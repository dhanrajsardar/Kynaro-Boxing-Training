package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.CoachPose
import com.example.model.OnboardingAnswers
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.CoachTransformationDualView
import com.example.ui.components.KynaroPillBadge
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.InterFontFamily
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

@Composable
fun PreparationFlowScreen(
    answers: OnboardingAnswers,
    onUpdateAnswers: (OnboardingAnswers) -> Unit,
    onBackToOnboarding: () -> Unit,
    onCompleteCampSetup: (OnboardingAnswers) -> Unit
) {
    // 6-stage preparation & camp reveal sequence matching the reference screenshots
    // 1: REAR HAND STRAIGHT Demo
    // 2: Get in frame (CameraX / Stance Guidance)
    // 3: LAND 3 PUNCHES (Interactive Pad Challenge)
    // 4: Your camp is ready (Fighter Name & Avatar selection)
    // 5: THIS IS YOU ON DEC 10 (30-Day Transformation Preview)
    // 6: BUILT FOR <USER NAME> (30-Day Fight Camp Grid & Unlock)
    var stage by rememberSaveable { mutableIntStateOf(1) }

    val handleBack: () -> Unit = {
        if (stage > 1) {
            stage--
        } else {
            onBackToOnboarding()
        }
    }

    BackHandler(onBack = handleBack)

    KynaroScreenBackground {
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
            label = "prep_stage_transition"
        ) { currentStage ->
            when (currentStage) {
                1 -> RearHandStraightDemoStage(
                    strongHand = answers.strongHand,
                    onGotIt = { stage = 2 },
                    onSkip = { stage = 2 },
                    onClose = handleBack
                )
                2 -> GetInFrameCameraStage(
                    onPositionedNext = { stage = 3 },
                    onSkip = { stage = 3 },
                    onClose = handleBack
                )
                3 -> Land3PunchesPadStage(
                    onCompleted3Punches = { stage = 4 }
                )
                4 -> FighterNameSetupStage(
                    answers = answers,
                    onSaveNameAndAvatar = { name, avatarIdx ->
                        val updated = answers.copy(fighterName = name, avatarIndex = avatarIdx)
                        onUpdateAnswers(updated)
                        stage = 5
                    }
                )
                5 -> TransformationPreviewStage(
                    answers = answers,
                    onStart30Days = { stage = 6 }
                )
                6 -> BuiltForUserCampGridStage(
                    answers = answers,
                    onUnlockCamp = {
                        onCompleteCampSetup(
                            answers.copy(
                                onboardingCompleted = true,
                                campUnlocked = true
                            )
                        )
                    }
                )
            }
        }
    }
}

/**
 * Stage 1: Recreates onboadingflow18_74d6b731.jpg ("REAR HAND STRAIGHT")
 */
@Composable
private fun RearHandStraightDemoStage(
    strongHand: String,
    onGotIt: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(30) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Close (X) on left, Circular 30 timer + 0 HITS pill on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(KynaroCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KynaroTextWhite
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(3.dp, KynaroYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countdown.toString(),
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = KynaroTextWhite
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "0 HITS",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = KynaroTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "REAR HAND\nSTRAIGHT",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (strongHand == "Left-handed") {
                "Southpaw Power Left Straight — Rotate rear hip & shoulder"
            } else {
                "Orthodox Power Right Cross — Rotate rear hip & shoulder"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        // 3D Coach Character Demonstrating Rear Hand Straight with Shoulder & Glove Highlights
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = CoachPose.CROSS,
                showAnatomicalHighlights = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        KynaroPrimaryButton(
            text = "I GOT IT",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KynaroBlack,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = onGotIt,
            testTag = "i_got_it_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Skip for now",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            modifier = Modifier
                .clickable(onClick = onSkip)
                .padding(8.dp)
                .testTag("skip_demo_button")
        )
    }
}

/**
 * Stage 2: Recreates onboadingflow19_cbbc83a4.jpg ("YOUR FIRST ROUND - Get in frame.")
 * Provides real CameraX preview + pose/stance framing overlay + graceful permission fallback.
 */
@Composable
private fun GetInFrameCameraStage(
    onPositionedNext: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraActive by rememberSaveable { mutableStateOf(false) }
    var permissionDeniedMessage by rememberSaveable { mutableStateOf(false) }
    var stanceLockProgress by remember { mutableIntStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            cameraActive = true
            permissionDeniedMessage = false
        } else {
            permissionDeniedMessage = true
        }
    }

    LaunchedEffect(cameraActive) {
        if (cameraActive) {
            while (stanceLockProgress < 100) {
                delay(28)
                stanceLockProgress = (stanceLockProgress + 2).coerceAtMost(100)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background 3D coach legs/stance visible softly behind the modal card
        CoachCharacter3DView(
            pose = CoachPose.IDLE_GUARD,
            showAnatomicalHighlights = false,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.25f }
        )

        if (!cameraActive) {
            // Modal Card matching onboadingflow19_cbbc83a4.jpg
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(28.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "YOUR FIRST ROUND",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.5.sp,
                            color = KynaroYellow
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Get in frame.",
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KynaroTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Pose Framing Guide Box matching reference
                    StanceFramingGuideIllustration(
                        isLocked = false,
                        modifier = Modifier.size(230.dp)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF171407))
                                .border(1.dp, KynaroRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsMma,
                                contentDescription = null,
                                tint = KynaroRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Guard up. Punch when a pad lights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KynaroTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = KynaroTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nothing is recorded or uploaded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KynaroTextMuted
                        )
                    }

                    if (permissionDeniedMessage) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF171407))
                                .border(1.dp, KynaroYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Camera permission declined. You can use Interactive Pad Mode below or enable camera anytime.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KynaroYellow,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    KynaroPrimaryButton(
                        text = if (permissionDeniedMessage) "START INTERACTIVE STANCE MODE" else "OPEN CAMERA",
                        onClick = {
                            if (permissionDeniedMessage) {
                                cameraActive = true
                            } else if (hasCameraPermission) {
                                cameraActive = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        testTag = "open_camera_button"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KynaroTextSecondary,
                        modifier = Modifier
                            .clickable(onClick = onSkip)
                            .padding(8.dp)
                            .testTag("skip_camera_button")
                    )
                }
            }
        } else {
            // Live CameraX Preview + Pose Alignment HUD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = KynaroTextWhite
                        )
                    }
                    KynaroPillBadge(
                        text = if (stanceLockProgress >= 100) "STANCE LOCKED IN FRAME" else "ALIGNING GUARD... $stanceLockProgress%",
                        borderColor = if (stanceLockProgress >= 100) KynaroGreen else KynaroYellow,
                        textColor = if (stanceLockProgress >= 100) KynaroGreen else KynaroYellow
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(KynaroCardBg)
                        .border(
                            width = 2.dp,
                            color = if (stanceLockProgress >= 100) KynaroGreen else KynaroRed,
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        CameraPreviewComposable(modifier = Modifier.fillMaxSize())
                    } else {
                        CoachCharacter3DView(
                            pose = CoachPose.IDLE_GUARD,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 0.6f }
                        )
                    }

                    // Pose estimation overlay frame
                    StanceFramingGuideIllustration(
                        isLocked = stanceLockProgress >= 100,
                        modifier = Modifier.fillMaxSize(0.78f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                KynaroPrimaryButton(
                    text = if (stanceLockProgress >= 100) "START FIRST ROUND" else "LOCK STANCE & CONTINUE",
                    onClick = onPositionedNext,
                    testTag = "confirm_frame_button"
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewComposable(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            runCatching {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun StanceFramingGuideIllustration(
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val guideColor = if (isLocked) KynaroGreen else KynaroRed
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cornerLen = w * 0.14f
        val strokeW = 3.5.dp.toPx()

        // 4 Framing Corner Brackets
        // Top-left
        drawLine(guideColor, Offset(12f, 12f), Offset(12f + cornerLen, 12f), strokeW, StrokeCap.Round)
        drawLine(guideColor, Offset(12f, 12f), Offset(12f, 12f + cornerLen), strokeW, StrokeCap.Round)
        // Top-right
        drawLine(guideColor, Offset(w - 12f, 12f), Offset(w - 12f - cornerLen, 12f), strokeW, StrokeCap.Round)
        drawLine(guideColor, Offset(w - 12f, 12f), Offset(w - 12f, 12f + cornerLen), strokeW, StrokeCap.Round)
        // Bottom-left
        drawLine(guideColor, Offset(12f, h - 12f), Offset(12f + cornerLen, h - 12f), strokeW, StrokeCap.Round)
        drawLine(guideColor, Offset(12f, h - 12f), Offset(12f, h - 12f - cornerLen), strokeW, StrokeCap.Round)
        // Bottom-right
        drawLine(guideColor, Offset(w - 12f, h - 12f), Offset(w - 12f - cornerLen, h - 12f), strokeW, StrokeCap.Round)
        drawLine(guideColor, Offset(w - 12f, h - 12f), Offset(w - 12f, h - 12f - cornerLen), strokeW, StrokeCap.Round)

        // Human Fighter Silhouette in center
        val silColor = Color(0xFF3E414C)
        val cx = w * 0.5f
        // Head
        drawCircle(silColor, radius = w * 0.085f, center = Offset(cx, h * 0.25f))
        // Torso
        drawRoundRect(
            color = silColor,
            topLeft = Offset(cx - w * 0.09f, h * 0.35f),
            size = Size(w * 0.18f, h * 0.28f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        // Left & Right Guard Gloves
        val leftGlove = Offset(cx - w * 0.21f, h * 0.34f)
        val rightGlove = Offset(cx + w * 0.21f, h * 0.34f)
        drawCircle(silColor, radius = w * 0.065f, center = leftGlove)
        drawCircle(silColor, radius = w * 0.065f, center = rightGlove)

        // Legs
        drawLine(silColor, Offset(cx - w * 0.05f, h * 0.62f), Offset(cx - w * 0.08f, h * 0.86f), strokeWidth = 14.dp.toPx())
        drawLine(silColor, Offset(cx + w * 0.05f, h * 0.62f), Offset(cx + w * 0.08f, h * 0.86f), strokeWidth = 14.dp.toPx())

        // Dashed Red Target Circles on Head, Left Glove, Right Glove + Waist Line
        val dashStyle = Stroke(
            width = 2.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
        drawCircle(guideColor, radius = w * 0.115f, center = Offset(cx, h * 0.25f), style = dashStyle)
        drawCircle(guideColor, radius = w * 0.095f, center = leftGlove, style = dashStyle)
        drawCircle(guideColor, radius = w * 0.095f, center = rightGlove, style = dashStyle)

        // Waist alignment dashed line
        val waistY = h * 0.61f
        drawLine(
            color = guideColor,
            start = Offset(cx - w * 0.19f, waistY),
            end = Offset(cx + w * 0.19f, waistY),
            strokeWidth = 2.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
        drawLine(guideColor, Offset(cx - w * 0.19f, waistY - 12f), Offset(cx - w * 0.19f, waistY + 12f), 2.5.dp.toPx())
        drawLine(guideColor, Offset(cx + w * 0.19f, waistY - 12f), Offset(cx + w * 0.19f, waistY + 12f), 2.5.dp.toPx())
    }
}

/**
 * Stage 3: Recreates onboadingflow22_8f2dc8b9.jpg & onboadingflow23_b6785966.jpg
 * ("UNLOCK YOUR FIGHTER OFFER — LAND 3 PUNCHES")
 */
@Composable
private fun Land3PunchesPadStage(
    onCompleted3Punches: () -> Unit
) {
    var punchesLanded by remember { mutableIntStateOf(0) }
    val punchCallouts = listOf("READY", "JAB!", "CROSS!", "KNOCKOUT HOOK!")
    val meterScores = listOf(0, 35, 70, 100)
    val currentScore = meterScores[punchesLanded.coerceIn(0, 3)]

    val animatedFill by animateFloatAsState(
        targetValue = (currentScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pad_meter_fill"
    )

    val padScale by animateFloatAsState(
        targetValue = if (punchesLanded > 0) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pad_bounce"
    )

    LaunchedEffect(punchesLanded) {
        if (punchesLanded >= 3) {
            delay(650)
            onCompleted3Punches()
        }
    }

    val handlePadHit: () -> Unit = {
        if (punchesLanded < 3) {
            punchesLanded++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KynaroPillBadge(text = "UNLOCK YOUR FIGHTER OFFER")

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "LAND 3 PUNCHES",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cut your first year by 40%. The pad's up. Swing.",
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Interactive Area: Left Vertical Power Meter + Center Red Concentric Pad
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Vertical Power Bar (00 -> 35 -> 70 -> 100) matching reference
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = String.format("%02d", currentScore),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = KynaroTextWhite
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.height(300.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Vertical Thermometer Track
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Color(0xFF1A1B23)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedFill)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(KynaroRed, Color(0xFFD9A300))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Tick marks
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (tick in 9 downTo 0) {
                            val isActiveTick = (tick * 10) <= currentScore
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(2.5.dp)
                                    .clip(CircleShape)
                                    .background(if (isActiveTick) KynaroYellow else Color(0xFF353744))
                            )
                        }
                    }
                }
            }

            // Center Target Pad & Callout
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Dynamic Callout ("JAB!", "CROSS!", "KNOCKOUT HOOK!")
                Text(
                    text = if (punchesLanded > 0) punchCallouts[punchesLanded.coerceAtMost(3)] else "",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    color = KynaroRed,
                    modifier = Modifier.height(46.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Concentric Red Boxing Pad matching onboadingflow22 & 23
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .graphicsLayer {
                            scaleX = padScale
                            scaleY = padScale
                        }
                        .clip(CircleShape)
                        .clickable { handlePadHit() }
                        .testTag("interactive_boxing_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.minDimension / 2f

                        // Outer glow (Championship Gold & Obsidian)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    KynaroRed.copy(alpha = 0.95f),
                                    Color(0xFFD9A300),
                                    Color(0xFF4D3900)
                                ),
                                center = Offset(cx, cy),
                                radius = r
                            ),
                            radius = r
                        )
                        // Concentric rings
                        drawCircle(
                            color = KynaroBlack.copy(alpha = 0.25f),
                            radius = r * 0.94f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = KynaroBlack.copy(alpha = 0.20f),
                            radius = r * 0.72f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = KynaroBlack.copy(alpha = 0.20f),
                            radius = r * 0.48f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.SportsMma,
                        contentDescription = "Hit the Pad",
                        tint = KynaroBlack,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                if (punchesLanded == 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF171407))
                            .border(1.dp, KynaroRed.copy(alpha = 0.55f), CircleShape)
                            .clickable { handlePadHit() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                            .testTag("hit_the_pad_button")
                    ) {
                        Text(
                            text = "HIT THE PAD",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.2.sp,
                            color = KynaroRed
                        )
                    }
                } else {
                    val remaining = (3 - punchesLanded).coerceAtLeast(0)
                    Text(
                        text = if (remaining > 0) "$remaining MORE" else "3 PUNCHES LANDED!",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp,
                        color = if (remaining > 0) KynaroTextSecondary else KynaroYellow
                    )
                }
            }
        }
    }
}

/**
 * Stage 4: Recreates onboadingflow20_82168575.jpg ("Your camp is ready. What should your coach call you?")
 */
@Composable
private fun FighterNameSetupStage(
    answers: OnboardingAnswers,
    onSaveNameAndAvatar: (String, Int) -> Unit
) {
    var nameInput by rememberSaveable { mutableStateOf(answers.fighterName) }
    var selectedAvatar by rememberSaveable { mutableIntStateOf(answers.avatarIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 3D Coach Character Throwing a Jab with Motion Blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(255.dp),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = CoachPose.JAB,
                cropUpperBody = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your camp is ready.",
            style = MaterialTheme.typography.displaySmall,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your coach will guide every mission, move by move.",
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "What should your coach call you?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KynaroTextWhite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fighter Name Input Field with 0/20 counter matching reference
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (nameInput.isEmpty()) {
                    Text(
                        text = "Enter fighter name",
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
                        fontSize = 18.sp,
                        color = KynaroTextWhite
                    ),
                    cursorBrush = SolidColor(KynaroRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fighter_name_input")
                )
            }
            Text(
                text = "${nameInput.length}/20",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 14.sp,
                color = KynaroTextMuted
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3 Selectable Fighter Avatars matching onboadingflow20_82168575.jpg
        val avatars = listOf(
            Triple(Icons.Default.SportsMma, KynaroRed, "Glove"),
            Triple(Icons.Default.LocalFireDepartment, KynaroYellow, "Striker"),
            Triple(Icons.Default.Shield, Color(0xFF60A5FA), "Sergeant")
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatars.forEachIndexed { idx, (icon, tint, label) ->
                val isSelected = selectedAvatar == idx
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(KynaroCardElevated)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) KynaroRed else KynaroCardBorder,
                            shape = CircleShape
                        )
                        .clickable { selectedAvatar = idx }
                        .testTag("avatar_option_$label"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        KynaroPrimaryButton(
            text = "CONTINUE",
            enabled = nameInput.trim().isNotEmpty(),
            onClick = {
                onSaveNameAndAvatar(nameInput.trim().ifEmpty { "Champion" }, selectedAvatar)
            },
            testTag = "camp_name_continue_button"
        )
    }
}

/**
 * Stage 5: Recreates onboadingflow21_afe6b9b7.jpg ("THIS IS YOU ON DEC 10")
 */
@Composable
private fun TransformationPreviewStage(
    answers: OnboardingAnswers,
    onStart30Days: () -> Unit
) {
    val displayFighterName = answers.fighterName.ifBlank { "FIGHTER" }.uppercase()
    val targetDateLabel = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 30)
        java.text.SimpleDateFormat("MMM d", java.util.Locale.US).format(cal.time).uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "$displayFighterName  •  YOUR NEXT 30 DAYS",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.6.sp,
            color = KynaroRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "THIS IS YOU ON\n$targetDateLabel",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            color = KynaroTextWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your coach builds it with you, round by round.",
            style = MaterialTheme.typography.bodyLarge,
            color = KynaroTextSecondary
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Side-by-Side TODAY LV 1 vs DAY 30 LV 3 3D Coach Cards
        CoachTransformationDualView(isSilhouetteMode = true)

        Spacer(modifier = Modifier.height(24.dp))

        // Level Progression Line Chart (LV1 -> LV2 -> LV3 across W1..W4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .padding(horizontal = 6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height - 26.dp.toPx()

                // Vertical Week Grid Lines
                val weekX = listOf(w * 0.24f, w * 0.46f, w * 0.68f, w * 0.90f)
                weekX.forEach { x ->
                    drawLine(
                        color = Color(0xFF1F212B),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Bottom baseline
                drawLine(
                    color = Color(0xFF252834),
                    start = Offset(0f, h),
                    end = Offset(w, h),
                    strokeWidth = 1.5.dp.toPx()
                )

                val pStart = Offset(w * 0.04f, h * 0.95f)
                val pMid = Offset(w * 0.36f, h * 0.62f)
                val pEnd = Offset(w * 0.92f, h * 0.14f)

                // Area fill under progression line
                val fillPath = Path().apply {
                    moveTo(pStart.x, h)
                    lineTo(pStart.x, pStart.y)
                    lineTo(pMid.x, pMid.y)
                    lineTo(pEnd.x, pEnd.y)
                    lineTo(pEnd.x, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            KynaroYellow.copy(alpha = 0.25f),
                            KynaroRed.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )

                // Progression Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(KynaroRed, KynaroYellow)
                    ),
                    start = pStart,
                    end = pEnd,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Nodes LV1, LV2, LV3
                drawCircle(color = KynaroTextMuted, radius = 5.dp.toPx(), center = pStart)
                drawCircle(color = KynaroYellow, radius = 6.dp.toPx(), center = pMid)
                drawCircle(color = KynaroYellow, radius = 8.dp.toPx(), center = pEnd)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("W1", "W2", "W3", "W4").forEach { wk ->
                    Text(
                        text = wk,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = KynaroTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "${answers.sessionsCount30Days} sessions • ${answers.sessionMinutesInt} min each",
            style = MaterialTheme.typography.titleMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        KynaroPrimaryButton(
            text = "START MY 30 DAYS",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = KynaroBlack,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = onStart30Days,
            testTag = "start_my_30_days_button"
        )
    }
}

/**
 * Stage 6: Recreates home_70c69470.jpg & home__2__a9c03557.jpg ("YOUR FIGHT CAMP IS READY - BUILT FOR <USER NAME>")
 */
@Composable
private fun BuiltForUserCampGridStage(
    answers: OnboardingAnswers,
    onUnlockCamp: () -> Unit
) {
    val displayUser = answers.fighterName.ifBlank { "FIGHTER" }.uppercase()
    var selectedPreviewDay by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top 3D Coach Character in Victory / Ready Pose
        Box(
            modifier = Modifier
                .size(125.dp),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = CoachPose.VICTORY_RAISE,
                showFloorRing = false,
                allowCameraOrbit = false,
                modifier = Modifier.fillMaxSize()
            )
        }

        KynaroPillBadge(text = "YOUR FIGHT CAMP IS READY")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "BUILT FOR $displayUser",
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            color = KynaroTextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "30 days, shaped by your answers. Your coach is warmed up. Step into the ring.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Personalized Schedule Pills (e.g. "3 days/week", "20-min missions")
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(answers.daysPerWeekShort, answers.sessionShortLabel).forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KynaroTextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // YOUR 30 DAYS Calendar Card (1..30) matching home_70c69470.jpg
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR 30 DAYS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.4.sp,
                    color = KynaroTextSecondary
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardElevated)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = KynaroYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "READY",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = KynaroYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 rows x 6 columns = 30 days
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 1..6) {
                        val dayNum = row * 6 + col
                        val isDaySelected = dayNum == selectedPreviewDay
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDaySelected) Color(0xFF191507) else KynaroCardElevated
                                )
                                .border(
                                    width = if (isDaySelected) 1.8.dp else 1.dp,
                                    color = if (isDaySelected) KynaroRed else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPreviewDay = dayNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDaySelected) KynaroRed else KynaroTextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MISSION 1 Card matching home__2__a9c03557.jpg
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroRed.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF171407)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsMma,
                    contentDescription = null,
                    tint = KynaroRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MISSION $selectedPreviewDay",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = KynaroRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (selectedPreviewDay == 1) "Stance & Jab Foundations" else "Day $selectedPreviewDay Coached Fight Round",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KynaroTextWhite
                )
            }
            Text(
                text = "${answers.sessionMinutesInt} min • coached",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                color = KynaroTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // THE 3-PHASE PERIODIZED CURRICULUM
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "30-DAY FIGHT CAMP PHASES",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = KynaroRed,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            CampPhaseRow(
                phase = "PHASE 1 (DAYS 1-10)",
                title = "Biomechanics & Kinetic Chain Base",
                desc = "Master the 1-6 punch system, hip rotation torque, and guard discipline."
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampPhaseRow(
                phase = "PHASE 2 (DAYS 11-20)",
                title = "Counter-Striking & Head Movement",
                desc = "Slips, rolls, pull counters, and high-velocity combination flow."
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampPhaseRow(
                phase = "PHASE 3 (DAYS 21-30)",
                title = "Championship Conditioning & IQ",
                desc = "Heavy bag endurance rounds, ring generalship, and graduation fight."
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // THE GRAND SLAM VALUE OFFER STACK ($675 VALUE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF121008))
                .border(1.dp, KynaroRed.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THE GRAND SLAM FIGHTER SYSTEM",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = KynaroRed,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroRed)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$675 VALUE",
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = KynaroBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ValueOfferItem("30-Day Periodized Fight Camp", "$240 Value")
            ValueOfferItem("3D AI Coach 360° Biomechanics Vault", "$195 Value")
            ValueOfferItem("Pro Round Timer & Live Combo Caller", "$90 Value")
            ValueOfferItem("Strike Rate & Calorie Metronome", "$80 Value")
            ValueOfferItem("Worldwide Fighter League & Belt System", "$70 Value")

            Spacer(modifier = Modifier.height(14.dp))

            // 30-Day Real Sweat Guarantee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07241A))
                    .border(1.dp, KynaroGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = KynaroGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "30-DAY REAL SWEAT GUARANTEE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KynaroGreen
                    )
                    Text(
                        text = "Commit for 30 days. If your speed, power, and reflexes haven't visibly transformed, get a 100% full refund.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = KynaroTextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        KynaroPrimaryButton(
            text = "UNLOCK MY FIGHT CAMP",
            onClick = onUnlockCamp,
            testTag = "unlock_my_fight_camp_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Restore purchases / Sign in matching home_70c69470.jpg
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Restore Purchases",
                style = MaterialTheme.typography.bodySmall,
                color = KynaroTextSecondary,
                modifier = Modifier
                    .clickable { onUnlockCamp() }
                    .padding(4.dp)
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = KynaroTextMuted
            )
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = KynaroTextWhite,
                modifier = Modifier
                    .clickable { onUnlockCamp() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun CampPhaseRow(phase: String, title: String, desc: String) {
    Column {
        Text(
            text = phase,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = KynaroYellow
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = KynaroTextWhite
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = KynaroTextSecondary
        )
    }
}

@Composable
private fun ValueOfferItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = KynaroRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = KynaroTextWhite
            )
        }
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = KynaroTextSecondary
        )
    }
}
