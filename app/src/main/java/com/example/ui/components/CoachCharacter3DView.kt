package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CoachPose
import com.example.model.WorkoutSpeed
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// =========================================================================================
// KYNARO PIXEL-EXACT PHOTOREALISTIC 360° & REAL ACTION BOXING COACH ENGINE
// Uses the exact official KYNARO marketing Coach Character across:
// - 8 true 360° turnaround studio views (Front, 45°, Right Side, 135°, Back 180°, 225°, Left Side, 315°)
// - 10 real boxing action keyframes (Guard, Full-Extension Jab, Rear-Pivot Cross, Lead Hook Load/Hit,
//   Knee-Dip Uppercut Load/Drive, Slip Defense, Low Bob & Weave Roll, Step-Slide Footwork)
// - Smooth keyframe cross-dissolve, snappy anticipation/recoil curves, Orthodox/Southpaw flip,
//   and 3D perspective boxing ring mat + kinetic trajectory overlays.
// =========================================================================================

private object CoachSpriteBank {
    private val cache = ConcurrentHashMap<Int, Bitmap>()

    val turnaroundIds = intArrayOf(
        R.drawable.coach_360_0, // 0° Front
        R.drawable.coach_360_1, // 45° Front-Right
        R.drawable.coach_360_2, // 90° Right Side
        R.drawable.coach_360_3, // 135° Back-Right
        R.drawable.coach_360_4, // 180° Back
        R.drawable.coach_360_5, // 225° Back-Left
        R.drawable.coach_360_6, // 270° Left Side
        R.drawable.coach_360_7  // 315° Front-Left
    )

    fun get(context: Context, resId: Int): Bitmap {
        return cache.getOrPut(resId) {
            val opts = BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, resId, opts)
        }
    }
}

private data class ActionFrameState(
    val primaryResId: Int,
    val secondaryResId: Int,
    val blendAlpha: Float,
    val shiftXFrac: Float,
    val shiftYFrac: Float,
    val scaleBoost: Float,
    val strikePower: Float,
    val impactNormX: Float,
    val impactNormY: Float,
    val phaseLabel: String,
    val phaseStepIndex: Int
)

@Composable
fun CoachCharacter3DView(
    pose: CoachPose,
    modifier: Modifier = Modifier,
    speed: WorkoutSpeed = WorkoutSpeed.NORMAL,
    isPaused: Boolean = false,
    showAnatomicalHighlights: Boolean = true,
    showFloorRing: Boolean = true,
    allowCameraOrbit: Boolean = true,
    cropUpperBody: Boolean = false,
    punchTriggerCount: Int = 0,
    physiqueBulkMultiplier: Float = 1.0f,
    silhouetteTint: Color? = null,
    showInteractiveHint: Boolean = false,
    isSouthpaw: Boolean = false,
    externalYawDeg: Float? = null,
    onPhaseChanged: ((String, Int) -> Unit)? = null
) {
    val context = LocalContext.current

    var userYawDeg by remember { mutableFloatStateOf(0f) }
    var localTapPunchCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(externalYawDeg) {
        if (externalYawDeg != null) {
            userYawDeg = externalYawDeg
        }
    }

    val totalPunchTriggers = punchTriggerCount + localTapPunchCounter

    val cycleDurationMs = remember(pose, speed) {
        val base = when (pose) {
            CoachPose.IDLE_GUARD -> 1900
            CoachPose.JAB -> 1050
            CoachPose.CROSS -> 1150
            CoachPose.ONE_TWO_COMBO -> 1480
            CoachPose.JAB_CROSS_HOOK -> 1850
            CoachPose.SPEED_HANDS -> 680
            CoachPose.UPPERCUT -> 1280
            CoachPose.SLIP_DEFENSE -> 1500
            CoachPose.FOOTWORK -> 1320
            CoachPose.REAR_HOOK -> 1180
            CoachPose.LEAD_UPPERCUT -> 1240
            CoachPose.HIGH_GUARD_SHELL -> 1600
            CoachPose.PULL_COUNTER -> 1360
            CoachPose.CHECK_HOOK_PIVOT -> 1380
            CoachPose.VICTORY_RAISE -> 1700
        }
        (base / speed.speedMultiplier).toInt().coerceIn(380, 3400)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coach_pixel_exact_action")

    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_phase"
    )

    val actionPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "action_phase"
    )

    val tapBurstImpulse by animateFloatAsState(
        targetValue = if (totalPunchTriggers % 2 == 1) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tap_impulse"
    )

    val smoothYawDeg by animateFloatAsState(
        targetValue = userYawDeg,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 260f),
        label = "smooth_yaw"
    )

    val paintPrimary = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val paintSecondary = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val srcRect = remember { Rect() }
    val dstRect = remember { RectF() }

    val pointerModifier = if (allowCameraOrbit) {
        Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    userYawDeg = (userYawDeg + dragAmount.x * 0.45f) % 360f
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { localTapPunchCounter++ })
            }
    } else {
        Modifier
    }

    val effectivePhase = if (isPaused) 0.36f else actionPhase
    val breathSin = if (isPaused) 0f else sin(breathPhase)
    val tapWave = sin(tapBurstImpulse * PI.toFloat()).coerceIn(0f, 1f)

    // Normalize yaw into [0, 360)
    val normYaw = ((smoothYawDeg % 360f) + 360f) % 360f
    // If user rotated away from front (> 22 deg), show true 360° photorealistic turnaround!
    val isOrbitInspecting = normYaw > 22f && normYaw < 338f

    val actionState = remember(pose, effectivePhase, breathSin, tapWave, totalPunchTriggers, isOrbitInspecting, normYaw) {
        if (isOrbitInspecting) {
            val idxF = (normYaw / 360f) * 8f
            val idx0 = idxF.toInt() % 8
            val idx1 = (idx0 + 1) % 8
            val frac = idxF - idxF.toInt()
            val angleDeg = normYaw.toInt()
            ActionFrameState(
                primaryResId = CoachSpriteBank.turnaroundIds[idx0],
                secondaryResId = CoachSpriteBank.turnaroundIds[idx1],
                blendAlpha = frac,
                shiftXFrac = 0f,
                shiftYFrac = breathSin * 0.008f,
                scaleBoost = 1.0f,
                strikePower = 0f,
                impactNormX = 0.5f,
                impactNormY = 0.3f,
                phaseLabel = "360° FORM VIEW • ${angleDeg}°",
                phaseStepIndex = (idx0 / 2).coerceIn(0, 3)
            )
        } else {
            evaluateRealActionFrames(pose, effectivePhase, breathSin, tapWave, totalPunchTriggers)
        }
    }

    LaunchedEffect(actionState.phaseLabel, actionState.phaseStepIndex) {
        onPhaseChanged?.invoke(actionState.phaseLabel, actionState.phaseStepIndex)
    }

    Box(
        modifier = modifier.then(pointerModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            val cx = w * 0.5f

            // 1. Studio Spotlight Aura
            if (silhouetteTint == null) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroOrange.copy(alpha = 0.16f + actionState.strikePower * 0.14f),
                            Color(0xFF1A120A).copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(cx, h * 0.44f),
                        radius = max(w, h) * 0.52f
                    ),
                    radius = max(w, h) * 0.52f,
                    center = Offset(cx, h * 0.44f)
                )
            }

            // 2. Perspective Boxing Ring Mat & Ground Shadow
            val spriteAspect = 460f / 500f
            val targetH = if (cropUpperBody) h * 1.48f else h * 0.92f
            val targetW = min(targetH * spriteAspect, if (cropUpperBody) w * 1.38f else w * 0.94f)
            val finalH = (targetW / spriteAspect) * actionState.scaleBoost
            val finalW = targetW * actionState.scaleBoost * physiqueBulkMultiplier

            val shiftX = actionState.shiftXFrac * finalW * (if (isSouthpaw) -1f else 1f)
            val shiftY = actionState.shiftYFrac * finalH
            val drawLeft = cx - finalW * 0.5f + shiftX
            val drawTop = if (cropUpperBody) h * 0.02f + shiftY else (h - finalH) * 0.5f + shiftY

            if (showFloorRing && !cropUpperBody && silhouetteTint == null) {
                val floorY = drawTop + finalH * 0.945f
                val ringW = finalW * 1.12f
                val ringH = finalH * 0.12f
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroOrange.copy(alpha = 0.22f + actionState.strikePower * 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(cx, floorY),
                        radius = ringW * 0.55f
                    ),
                    topLeft = Offset(cx - ringW * 0.5f, floorY - ringH * 0.5f),
                    size = Size(ringW, ringH)
                )
                drawOval(
                    color = KynaroOrange.copy(alpha = 0.40f + actionState.strikePower * 0.2f),
                    topLeft = Offset(cx - ringW * 0.44f, floorY - ringH * 0.40f),
                    size = Size(ringW * 0.88f, ringH * 0.80f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Ground contact shadow beneath shoes
                drawOval(
                    color = Color.Black.copy(alpha = 0.65f),
                    topLeft = Offset(cx - finalW * 0.38f + shiftX, floorY - finalH * 0.03f),
                    size = Size(finalW * 0.76f, finalH * 0.06f)
                )
            }

            // 3. Draw Pixel-Exact Coach Character Sprites (Smooth Cross-Dissolve + Southpaw Mirroring)
            val bmp1 = CoachSpriteBank.get(context, actionState.primaryResId)
            val bmp2 = CoachSpriteBank.get(context, actionState.secondaryResId)

            val filter = silhouetteTint?.let {
                PorterDuffColorFilter(it.toArgb(), PorterDuff.Mode.SRC_IN)
            }
            paintPrimary.colorFilter = filter
            paintSecondary.colorFilter = filter

            dstRect.set(drawLeft, drawTop, drawLeft + finalW, drawTop + finalH)

            drawIntoCanvas { composeCanvas ->
                val native = composeCanvas.nativeCanvas
                native.save()
                if (isSouthpaw) {
                    native.scale(-1f, 1f, cx, h * 0.5f)
                }

                val activeBmp = if (actionState.blendAlpha >= 0.36f) bmp2 else bmp1
                paintPrimary.alpha = 255
                srcRect.set(0, 0, activeBmp.width, activeBmp.height)
                native.drawBitmap(activeBmp, srcRect, dstRect, paintPrimary)
                native.restore()
            }

            // 4. Kinetic Strike Impact Ring & Trajectory Vector (Only during active punches)
            if (showAnatomicalHighlights && silhouetteTint == null && actionState.strikePower > 0.22f) {
                val ext = actionState.strikePower
                val normX = if (isSouthpaw) (1f - actionState.impactNormX) else actionState.impactNormX
                val impactPt = Offset(drawLeft + normX * finalW, drawTop + actionState.impactNormY * finalH)
                val originPt = Offset(drawLeft + (if (isSouthpaw) 0.55f else 0.45f) * finalW, drawTop + 0.25f * finalH)

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, KynaroOrange.copy(alpha = 0.85f * ext), Color.White.copy(alpha = 0.95f * ext)),
                        start = originPt,
                        end = impactPt
                    ),
                    start = originPt,
                    end = impactPt,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = KynaroOrange.copy(alpha = 0.90f * ext),
                    radius = (10f + 18f * ext).dp.toPx(),
                    center = impactPt,
                    style = Stroke(width = 2.2.dp.toPx())
                )
                drawCircle(
                    color = KynaroYellow.copy(alpha = 0.95f * ext),
                    radius = 4.dp.toPx(),
                    center = impactPt
                )
            }

            // 5. Slip Centerline Reference
            if (showAnatomicalHighlights && silhouetteTint == null && pose == CoachPose.SLIP_DEFENSE && !isOrbitInspecting) {
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(cx, drawTop + finalH * 0.05f),
                    end = Offset(cx, drawTop + finalH * 0.42f),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            }
        }

        if (showInteractiveHint && allowCameraOrbit) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KynaroBlack.copy(alpha = 0.82f))
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DRAG 360° TO INSPECT • TAP TO PUNCH",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KynaroTextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private fun evaluateRealActionFrames(
    pose: CoachPose,
    phase: Float,
    breathSin: Float,
    tapImpulse: Float,
    tapIndex: Int
): ActionFrameState {
    val guard = R.drawable.coach_act_guard
    val jabMid = R.drawable.coach_act_jab_mid
    val jab = R.drawable.coach_act_jab
    val crossMid = R.drawable.coach_act_cross_mid
    val cross = R.drawable.coach_act_cross
    val hookLoad = R.drawable.coach_act_hook_load
    val hookHit = R.drawable.coach_act_hook_hit
    val rearHook = R.drawable.coach_act_rear_hook
    val upperLoad = R.drawable.coach_act_upper_load
    val upperLead = R.drawable.coach_act_upper_lead
    val upperHit = R.drawable.coach_act_upper_hit
    val highGuard = R.drawable.coach_act_high_guard
    val slipL = R.drawable.coach_act_slip_l
    val slipR = R.drawable.coach_act_slip_r
    val roll = R.drawable.coach_act_roll
    val foot = R.drawable.coach_act_footwork
    val pivotHook = R.drawable.coach_act_pivot_hook

    if (tapImpulse > 0.25f) {
        val isJabTap = tapIndex % 2 == 1
        return ActionFrameState(
            primaryResId = if (isJabTap) jabMid else crossMid,
            secondaryResId = if (isJabTap) jab else cross,
            blendAlpha = tapImpulse.coerceIn(0f, 1f),
            shiftXFrac = if (isJabTap) 0.025f * tapImpulse else 0.035f * tapImpulse,
            shiftYFrac = -0.01f * tapImpulse,
            scaleBoost = 1f + 0.025f * tapImpulse,
            strikePower = tapImpulse,
            impactNormX = if (isJabTap) 0.767f else 0.804f,
            impactNormY = if (isJabTap) 0.208f else 0.191f,
            phaseLabel = if (isJabTap) "SNAP LEAD JAB!" else "POWER REAR CROSS!",
            phaseStepIndex = if (isJabTap) 1 else 2
        )
    }

    val bounceY = sin(phase * 2f * PI.toFloat()) * 0.009f + breathSin * 0.004f

    return when (pose) {
        CoachPose.IDLE_GUARD -> ActionFrameState(
            guard, R.drawable.coach_360_1, 0.1f, cos(phase * 2f * PI.toFloat()) * 0.01f, bounceY, 1f, 0f, 0.5f, 0.25f,
            "HIGH GUARD • BALANCED BOUNCE", 0
        )

        CoachPose.JAB -> {
            val ext = punchCurve(phase, 0.36f)
            when {
                phase < 0.14f -> ActionFrameState(guard, jabMid, 0.1f, 0.005f, bounceY * 0.4f, 1f, 0f, 0.629f, 0.207f, "1. PUSH OFF REAR BALL", 0)
                phase < 0.26f -> ActionFrameState(jabMid, jabMid, 1f, 0.018f, 0f, 1.01f, 0.45f, 0.629f, 0.207f, "2. SNAP LEAD JAB", 1)
                phase < 0.54f -> ActionFrameState(jab, jab, 1f, 0.030f * ext, 0f, 1.022f, ext, 0.767f, 0.208f, "3. SHOULDER CHIN SHIELD", 2)
                phase < 0.72f -> ActionFrameState(jabMid, jabMid, 1f, 0.014f, 0f, 1.01f, 0.25f, 0.629f, 0.207f, "4. FAST GUARD RETURN", 3)
                else -> ActionFrameState(guard, guard, 0f, 0f, bounceY * 0.4f, 1f, 0f, 0.5f, 0.25f, "4. FAST GUARD RETURN", 3)
            }
        }

        CoachPose.CROSS -> {
            val ext = punchCurve(phase, 0.38f)
            when {
                phase < 0.14f -> ActionFrameState(guard, crossMid, 0.1f, 0f, bounceY * 0.4f, 1f, 0f, 0.679f, 0.210f, "1. PIVOT REAR HEEL 45°", 0)
                phase < 0.28f -> ActionFrameState(crossMid, crossMid, 1f, 0.020f, 0f, 1.012f, 0.5f, 0.679f, 0.210f, "2. DRIVE HIP & CROSS", 1)
                phase < 0.56f -> ActionFrameState(cross, cross, 1f, 0.036f * ext, 0f, 1.025f, ext, 0.804f, 0.191f, "3. LEAD HAND ON TEMPLE", 2)
                phase < 0.74f -> ActionFrameState(crossMid, crossMid, 1f, 0.016f, 0f, 1.01f, 0.25f, 0.679f, 0.210f, "4. RESET STANCE", 3)
                else -> ActionFrameState(guard, guard, 0f, 0f, bounceY * 0.4f, 1f, 0f, 0.5f, 0.25f, "4. RESET STANCE", 3)
            }
        }

        CoachPose.ONE_TWO_COMBO -> {
            when {
                phase < 0.14f -> ActionFrameState(jabMid, jabMid, 1f, 0.014f, 0f, 1.01f, 0.35f, 0.629f, 0.207f, "1. LEAD JAB — MEASURE", 0)
                phase < 0.40f -> ActionFrameState(jab, jab, 1f, 0.028f, 0f, 1.02f, 0.92f, 0.767f, 0.208f, "1. LEAD JAB — MEASURE", 1)
                phase < 0.54f -> ActionFrameState(crossMid, crossMid, 1f, 0.020f, 0f, 1.012f, 0.45f, 0.679f, 0.210f, "2. REAR CROSS — POWER", 2)
                phase < 0.80f -> ActionFrameState(cross, cross, 1f, 0.036f, 0f, 1.025f, 0.96f, 0.804f, 0.191f, "2. REAR CROSS — POWER", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, bounceY * 0.4f, 1f, 0f, 0.5f, 0.25f, "3. SNAP TO HIGH GUARD", 3)
            }
        }

        CoachPose.JAB_CROSS_HOOK -> {
            when {
                phase < 0.26f -> ActionFrameState(jab, jab, 1f, 0.025f, 0f, 1.015f, 0.88f, 0.767f, 0.208f, "1. JAB — SET DISTANCE", 0)
                phase < 0.52f -> ActionFrameState(cross, cross, 1f, 0.034f, 0f, 1.02f, 0.92f, 0.804f, 0.191f, "2. CROSS — LOAD LEAD HIP", 1)
                phase < 0.66f -> ActionFrameState(hookLoad, hookLoad, 1f, 0.010f, 0.01f, 1f, 0.2f, 0.5f, 0.25f, "3. LEAD HOOK — 90° PIVOT", 2)
                phase < 0.88f -> ActionFrameState(hookHit, hookHit, 1f, 0.030f, 0f, 1.022f, 0.95f, 0.774f, 0.198f, "3. LEAD HOOK — 90° PIVOT", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "4. GUARD RESET", 3)
            }
        }

        CoachPose.REAR_HOOK -> {
            when {
                phase < 0.22f -> ActionFrameState(guard, guard, 0f, 0f, bounceY * 0.4f, 1f, 0f, 0.5f, 0.25f, "1. LOAD REAR HIP", 0)
                phase < 0.36f -> ActionFrameState(crossMid, crossMid, 1f, 0.016f, 0f, 1.01f, 0.4f, 0.679f, 0.210f, "2. PIVOT REAR FOOT", 1)
                phase < 0.72f -> ActionFrameState(rearHook, rearHook, 1f, 0.028f, 0f, 1.025f, 0.95f, 0.653f, 0.223f, "3. 90° REAR HOOK IMPACT", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "4. HIGH GUARD RESET", 3)
            }
        }

        CoachPose.LEAD_UPPERCUT -> {
            when {
                phase < 0.34f -> ActionFrameState(upperLoad, upperLoad, 1f, 0f, 0.032f, 1f, 0f, 0.5f, 0.3f, "1. DIP KNEES & LOAD LEAD", 0)
                phase < 0.74f -> ActionFrameState(upperLead, upperLead, 1f, 0.018f, -0.020f, 1.025f, 0.95f, 0.694f, 0.078f, "2. DRIVE LEAD UPPERCUT UP", 1)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "3. SNAP BACK TO CHIN", 3)
            }
        }

        CoachPose.UPPERCUT -> {
            when {
                phase < 0.34f -> ActionFrameState(upperLoad, upperLoad, 1f, 0f, 0.035f, 1f, 0f, 0.5f, 0.3f, "1. DIP KNEES & LOAD REAR", 0)
                phase < 0.74f -> ActionFrameState(upperHit, upperHit, 1f, 0.015f, -0.022f, 1.025f, 0.95f, 0.438f, 0.053f, "2. EXPLODE REAR UPPERCUT", 1)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "3. RETURN TO CHIN", 3)
            }
        }

        CoachPose.SPEED_HANDS -> {
            when {
                phase < 0.25f -> ActionFrameState(jab, jab, 1f, 0.022f, 0f, 1.015f, 0.85f, 0.767f, 0.208f, "RAPID STRAIGHT FLURRY", 1)
                phase < 0.50f -> ActionFrameState(crossMid, crossMid, 1f, 0.012f, 0f, 1.01f, 0.4f, 0.679f, 0.210f, "RAPID STRAIGHT FLURRY", 1)
                phase < 0.75f -> ActionFrameState(cross, cross, 1f, 0.028f, 0f, 1.015f, 0.85f, 0.804f, 0.191f, "RAPID STRAIGHT FLURRY", 2)
                else -> ActionFrameState(jabMid, jabMid, 1f, 0.012f, 0f, 1.01f, 0.4f, 0.629f, 0.207f, "RAPID STRAIGHT FLURRY", 2)
            }
        }

        CoachPose.SLIP_DEFENSE -> {
            when {
                phase < 0.30f -> ActionFrameState(slipL, slipL, 1f, -0.035f, 0.018f, 1f, 0f, 0.5f, 0.25f, "1. SLIP LEFT • LOAD LEAD", 0)
                phase < 0.58f -> ActionFrameState(roll, roll, 1f, 0f, 0.042f, 1f, 0f, 0.5f, 0.25f, "2. BOB & WEAVE U-ROLL", 1)
                phase < 0.84f -> ActionFrameState(slipR, slipR, 1f, 0.035f, 0.018f, 1f, 0f, 0.5f, 0.25f, "3. SLIP RIGHT • LOAD REAR", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "4. EYES UP • GUARD HIGH", 3)
            }
        }

        CoachPose.HIGH_GUARD_SHELL -> {
            if (phase < 0.32f) {
                ActionFrameState(guard, guard, 0f, 0f, bounceY * 0.5f, 1f, 0f, 0.5f, 0.25f, "1. READ INCOMING FLURRY", 0)
            } else {
                ActionFrameState(highGuard, highGuard, 1f, -0.01f, 0.012f, 1.01f, 0f, 0.5f, 0.25f, "2. GLOVES ON TEMPLES • ELBOWS IN", 2)
            }
        }

        CoachPose.PULL_COUNTER -> {
            when {
                phase < 0.34f -> ActionFrameState(slipL, slipL, 1f, -0.042f, 0.010f, 1f, 0f, 0.5f, 0.25f, "1. PULL WEIGHT TO REAR LEG", 0)
                phase < 0.48f -> ActionFrameState(crossMid, crossMid, 1f, 0.015f, 0f, 1.01f, 0.5f, 0.679f, 0.210f, "2. SPRING OFF REAR CALF", 1)
                phase < 0.80f -> ActionFrameState(cross, cross, 1f, 0.038f, 0f, 1.028f, 0.96f, 0.804f, 0.191f, "3. LASER COUNTER CROSS!", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "4. RESET HIGH GUARD", 3)
            }
        }

        CoachPose.FOOTWORK -> {
            val stepWave = sin(phase * 2f * PI.toFloat())
            val active = if (abs(stepWave) > 0.35f) foot else guard
            ActionFrameState(
                active, active, 1f, 0.055f * stepWave, -0.018f * abs(cos(phase * 2f * PI.toFloat())), 1f, 0f, 0.5f, 0.25f,
                if (stepWave >= 0f) "STEP LEAD FOOT FORWARD" else "SLIDE REAR FOOT BACK",
                if (stepWave >= 0f) 1 else 2
            )
        }

        CoachPose.CHECK_HOOK_PIVOT -> {
            when {
                phase < 0.26f -> ActionFrameState(foot, foot, 1f, 0.02f, 0f, 1f, 0f, 0.5f, 0.25f, "1. PLANT LEAD BALL OF FOOT", 0)
                phase < 0.72f -> ActionFrameState(pivotHook, pivotHook, 1f, -0.032f, 0f, 1.022f, 0.95f, 0.703f, 0.179f, "2. 90° PIVOT + CHECK HOOK!", 2)
                else -> ActionFrameState(guard, guard, 0f, 0f, 0f, 1f, 0f, 0.5f, 0.25f, "3. NEW DOMINANT ANGLE", 3)
            }
        }

        CoachPose.VICTORY_RAISE -> ActionFrameState(
            upperHit, upperHit, 1f, 0f, -0.015f, 1.015f, 0f, 0.5f, 0.15f, "ROUND COMPLETE", 3
        )
    }
}

private fun punchCurve(t: Float, peakAt: Float = 0.36f): Float {
    val p = t.coerceIn(0f, 1f)
    return if (p < peakAt) {
        sin((p / peakAt) * (PI / 2.0).toFloat())
    } else {
        val c = cos(((p - peakAt) / (1f - peakAt)) * (PI / 2.0).toFloat())
        c * c
    }
}

@Composable
fun CoachTransformationDualView(
    modifier: Modifier = Modifier,
    isSilhouetteMode: Boolean = false
) {
    if (isSilhouetteMode) {
        Box(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    CoachCharacter3DView(
                        pose = CoachPose.IDLE_GUARD,
                        showFloorRing = false,
                        showAnatomicalHighlights = false,
                        allowCameraOrbit = false,
                        silhouetteTint = Color(0xFF3E4251),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 52.dp)
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Text("DAY 1", style = MaterialTheme.typography.labelMedium, color = KynaroTextWhite, letterSpacing = 1.5.sp)
                        Text("FUNDAMENTALS", fontFamily = OswaldFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = KynaroTextSecondary)
                        Text("Stance & basic guard", style = MaterialTheme.typography.bodySmall, color = KynaroTextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.radialGradient(listOf(KynaroOrange.copy(alpha = 0.22f), KynaroCardBg)))
                        .border(1.dp, KynaroOrange.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    CoachCharacter3DView(
                        pose = CoachPose.ONE_TWO_COMBO,
                        showFloorRing = false,
                        allowCameraOrbit = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 52.dp)
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Text("DAY 30", style = MaterialTheme.typography.labelMedium, color = KynaroOrange, letterSpacing = 1.5.sp)
                        Text("FIGHT READY", fontFamily = OswaldFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = KynaroOrange)
                        Text("Sharp combos & slips", style = MaterialTheme.typography.bodySmall, color = KynaroTextWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(KynaroOrange)
                    .border(3.dp, KynaroBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("›", color = KynaroBlack, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-2).dp))
            }
        }
    } else {
        Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.height(230.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CoachCharacter3DView(pose = CoachPose.IDLE_GUARD, showFloorRing = false, showAnatomicalHighlights = false, allowCameraOrbit = false, modifier = Modifier.fillMaxSize(0.95f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("DAY 1 STANCE", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = KynaroTextSecondary)
            }
            Box(modifier = Modifier.width(1.dp).height(200.dp).background(Brush.verticalGradient(listOf(Color.Transparent, KynaroOrange.copy(alpha = 0.6f), Color.Transparent))))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.height(230.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CoachCharacter3DView(pose = CoachPose.ONE_TWO_COMBO, showFloorRing = false, allowCameraOrbit = false, modifier = Modifier.fillMaxSize(0.95f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("DAY 30 SHARP", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 15.sp, color = KynaroOrange)
            }
        }
    }
}

@Composable
fun CoachDrillThumbnail(
    pose: CoachPose,
    size: Dp = 104.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(KynaroOrange.copy(alpha = 0.22f), KynaroCardElevated, KynaroBlack)))
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        CoachCharacter3DView(
            pose = pose,
            showFloorRing = false,
            showAnatomicalHighlights = false,
            allowCameraOrbit = false,
            cropUpperBody = true,
            modifier = Modifier.fillMaxSize().padding(top = 4.dp)
        )
    }
}
