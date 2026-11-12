package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import androidx.compose.ui.graphics.Path
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
import com.example.ui.theme.KynaroDeepBurgundy
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

private const val MESH_COLS = 24
private const val MESH_ROWS = 34
private const val VERTEX_COUNT = (MESH_COLS + 1) * (MESH_ROWS + 1)

private object CoachBitmapCache {
    @Volatile
    private var cachedBitmap: Bitmap? = null

    fun getBitmap(context: android.content.Context): Bitmap {
        cachedBitmap?.let { return it }
        synchronized(this) {
            cachedBitmap?.let { return it }
            val opts = BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            return BitmapFactory.decodeResource(
                context.resources,
                R.drawable.coach_character,
                opts
            ).also { cachedBitmap = it }
        }
    }
}

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
    showInteractiveHint: Boolean = false
) {
    val context = LocalContext.current
    val fullBitmap = remember(context) { CoachBitmapCache.getBitmap(context) }

    var userYawDrag by remember { mutableFloatStateOf(0f) }
    var userPitchDrag by remember { mutableFloatStateOf(0f) }
    var localTapPunchCounter by remember { mutableIntStateOf(0) }

    val totalPunchTriggers = punchTriggerCount + localTapPunchCounter

    val cycleDurationMs = remember(pose, speed) {
        val base = when (pose) {
            CoachPose.IDLE_GUARD -> 2000
            CoachPose.JAB -> 980
            CoachPose.CROSS -> 1060
            CoachPose.ONE_TWO_COMBO -> 1280
            CoachPose.JAB_CROSS_HOOK -> 1540
            CoachPose.SPEED_HANDS -> 580
            CoachPose.UPPERCUT -> 1140
            CoachPose.SLIP_DEFENSE -> 1360
            CoachPose.FOOTWORK -> 1180
            CoachPose.VICTORY_RAISE -> 1800
        }
        (base / speed.speedMultiplier).toInt().coerceIn(360, 3000)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coach_interactive_mesh")

    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_mesh_phase"
    )

    val actionPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "action_mesh_phase"
    )

    val tapBurstImpulse by animateFloatAsState(
        targetValue = if (totalPunchTriggers % 2 == 1) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tap_punch_impulse"
    )

    val smoothCameraYaw by animateFloatAsState(
        targetValue = pose.cameraYaw * 0.45f + userYawDrag,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "smooth_camera_yaw"
    )

    val smoothCameraPitch by animateFloatAsState(
        targetValue = pose.cameraPitch * 0.45f + userPitchDrag,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f),
        label = "smooth_camera_pitch"
    )

    val meshVerts = remember { FloatArray(VERTEX_COUNT * 2) }
    val meshColors = remember { IntArray(VERTEX_COUNT) }
    val camera3D = remember { Camera() }
    val matrix3D = remember { Matrix() }
    val mainPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    }
    val shadowPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = PorterDuffColorFilter(
                android.graphics.Color.argb(120, 0, 0, 0),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    remember(silhouetteTint) {
        if (silhouetteTint != null) {
            mainPaint.colorFilter = PorterDuffColorFilter(
                silhouetteTint.toArgb(),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            mainPaint.colorFilter = null
        }
        true
    }

    val pointerModifier = if (allowCameraOrbit) {
        Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        userYawDrag = (userYawDrag + dragAmount.x * 0.18f).coerceIn(-38f, 38f)
                        userPitchDrag = (userPitchDrag - dragAmount.y * 0.12f).coerceIn(-16f, 18f)
                    },
                    onDragEnd = {
                        userYawDrag = 0f
                        userPitchDrag = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        localTapPunchCounter++
                    }
                )
            }
    } else {
        Modifier
    }

    val effectivePhase = if (isPaused) 0.28f else actionPhase
    val breathSin = if (isPaused) 0f else sin(breathPhase)
    val tapImpulseWave = sin(tapBurstImpulse * PI.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier.then(pointerModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW * 0.5f

            val kinematics = evaluateMeshKinematics(
                pose = pose,
                phase = effectivePhase,
                breathSin = breathSin,
                tapImpulse = tapImpulseWave,
                tapTriggerIndex = totalPunchTriggers
            )

            val strikeIntensity = max(kinematics.leadPunchExt, kinematics.rearPunchExt)

            // 1. Minimalist Architectural Stage Spotlight (Championship Gold & Obsidian)
            if (silhouetteTint == null) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroRed.copy(alpha = 0.16f + strikeIntensity * 0.12f),
                            KynaroDeepBurgundy.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(
                            centerX + smoothCameraYaw * 1.4f,
                            canvasH * (if (cropUpperBody) 0.48f else 0.40f)
                        ),
                        radius = canvasW.coerceAtLeast(canvasH) * 0.52f
                    ),
                    radius = canvasW.coerceAtLeast(canvasH) * 0.52f,
                    center = Offset(
                        centerX + smoothCameraYaw * 1.4f,
                        canvasH * (if (cropUpperBody) 0.48f else 0.40f)
                    )
                )
            }

            // 2. Calculate Character Placement & Aspect Ratio preserving exact pixel proportions
            val bmpW = fullBitmap.width.toFloat()
            val bmpH = fullBitmap.height.toFloat()
            val aspect = bmpW / bmpH

            val targetCharH = if (cropUpperBody) {
                canvasH * 1.62f
            } else {
                canvasH * 0.92f
            }
            val targetCharW = (targetCharH * aspect).coerceAtMost(
                if (cropUpperBody) canvasW * 1.45f else canvasW * 0.95f
            )
            val finalCharH = targetCharW / aspect

            val charLeft = centerX - (targetCharW * 0.47f)
            val charTop = if (cropUpperBody) {
                canvasH * 0.02f
            } else {
                (canvasH - finalCharH) * 0.5f
            }

            // 3. Draw 3D Perspective Ring Floor & Footwork Telemetry
            if (showFloorRing && !cropUpperBody && silhouetteTint == null) {
                val floorCenterY = charTop + finalCharH * 0.915f
                val ringW = targetCharW * 1.15f
                val ringH = finalCharH * 0.13f

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KynaroRed.copy(alpha = 0.22f + strikeIntensity * 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, floorCenterY),
                        radius = ringW * 0.58f
                    ),
                    topLeft = Offset(centerX - ringW * 0.5f, floorCenterY - ringH * 0.5f),
                    size = Size(ringW, ringH)
                )
                drawOval(
                    color = KynaroRed.copy(alpha = 0.35f + strikeIntensity * 0.18f),
                    topLeft = Offset(centerX - ringW * 0.44f, floorCenterY - ringH * 0.40f),
                    size = Size(ringW * 0.88f, ringH * 0.80f),
                    style = Stroke(width = 1.6.dp.toPx())
                )

                val shoeLeftX = charLeft + targetCharW * 0.19f + kinematics.footworkShiftX * targetCharW
                val shoeLeftY = charTop + finalCharH * 0.865f
                val shoeRightX = charLeft + targetCharW * 0.80f + kinematics.footworkShiftX * targetCharW
                val shoeRightY = charTop + finalCharH * 0.928f

                drawOval(
                    color = Color.Black.copy(alpha = 0.72f),
                    topLeft = Offset(shoeLeftX - targetCharW * 0.13f, shoeLeftY - finalCharH * 0.022f),
                    size = Size(targetCharW * 0.26f, finalCharH * 0.048f)
                )
                drawOval(
                    color = Color.Black.copy(alpha = 0.72f),
                    topLeft = Offset(shoeRightX - targetCharW * 0.13f, shoeRightY - finalCharH * 0.022f),
                    size = Size(targetCharW * 0.26f, finalCharH * 0.048f)
                )

                // If FOOTWORK or CROSS, highlight foot pivot ring
                if (pose == CoachPose.FOOTWORK || pose == CoachPose.CROSS) {
                    val pivotX = if (pose == CoachPose.CROSS) shoeRightX else shoeLeftX
                    val pivotY = if (pose == CoachPose.CROSS) shoeRightY else shoeLeftY
                    drawOval(
                        color = KynaroRed.copy(alpha = 0.70f),
                        topLeft = Offset(pivotX - targetCharW * 0.14f, pivotY - finalCharH * 0.024f),
                        size = Size(targetCharW * 0.28f, finalCharH * 0.052f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )
                }
            }

            // 4. Populate 24x34 Articulated Skeletal Mesh (`meshVerts` & `meshColors`)
            // Pure mesh articulation — ZERO cropped sub-bitmap zooming!
            val yawNorm = (smoothCameraYaw / 40f).coerceIn(-1f, 1f)
            val pitchNorm = (smoothCameraPitch / 22f).coerceIn(-1f, 1f)

            var vIdx = 0
            var cIdx = 0
            for (row in 0..MESH_ROWS) {
                val v = row.toFloat() / MESH_ROWS.toFloat()

                val groundAnchorWeight = ((0.88f - v) / 0.24f).coerceIn(0f, 1f)
                val upperBodyWeight = ((0.58f - v) / 0.34f).coerceIn(0f, 1f)
                val headNeckWeight = ((0.20f - v) / 0.14f).coerceIn(0f, 1f)
                val chestRibWeight = gaussianWeight(v, center = 0.28f, sigma = 0.095f)
                val waistCoreWeight = gaussianWeight(v, center = 0.40f, sigma = 0.11f)
                val kneeBendWeight = gaussianWeight(v, center = 0.67f, sigma = 0.10f)

                for (col in 0..MESH_COLS) {
                    val u = col.toFloat() / MESH_COLS.toFloat()
                    val relU = u - 0.455f

                    val leadArmInfluence = gaussian2D(
                        u, v,
                        cu = 0.285f, cv = 0.235f,
                        su = 0.105f, sv = 0.105f
                    )
                    val rearArmInfluence = gaussian2D(
                        u, v,
                        cu = 0.555f, cv = 0.225f,
                        su = 0.115f, sv = 0.105f
                    )
                    val torsoDepth = gaussian2D(
                        u, v,
                        cu = 0.455f, cv = 0.28f,
                        su = 0.16f, sv = 0.18f
                    )
                    val vertexZ = (torsoDepth * 0.35f + leadArmInfluence * 0.65f + rearArmInfluence * 0.55f)

                    val bulkFactor = (physiqueBulkMultiplier - 1f) * (chestRibWeight * 0.55f + waistCoreWeight * 1.15f)
                    var du = relU * bulkFactor

                    val breathExpand = breathSin * 0.011f * chestRibWeight
                    du += relU * (breathExpand * 2.2f)
                    var dv = -abs(breathSin) * 0.0045f * chestRibWeight -
                        breathSin * 0.0035f * headNeckWeight

                    dv += kinematics.bodyBounceY * groundAnchorWeight
                    du += kinematics.footworkShiftX * (0.35f + 0.65f * groundAnchorWeight)
                    du += relU * (kinematics.kneeDip * 0.08f * kneeBendWeight)

                    du += kinematics.slipOffsetX * (upperBodyWeight * 0.85f + headNeckWeight * 0.35f)
                    dv += abs(kinematics.slipOffsetX) * 0.18f * upperBodyWeight

                    val totalYaw = yawNorm + kinematics.torsoTwistYaw
                    du += vertexZ * totalYaw * 0.048f * groundAnchorWeight
                    du -= relU * abs(totalYaw) * 0.035f * upperBodyWeight
                    dv += vertexZ * pitchNorm * 0.028f * groundAnchorWeight

                    if (leadArmInfluence > 0.01f) {
                        du += kinematics.leadArmDeltaU * leadArmInfluence
                        dv += kinematics.leadArmDeltaV * leadArmInfluence
                    }

                    if (rearArmInfluence > 0.01f) {
                        du += kinematics.rearArmDeltaU * rearArmInfluence
                        dv += kinematics.rearArmDeltaV * rearArmInfluence
                    }

                    if (headNeckWeight > 0.01f) {
                        du += kinematics.headDeltaU * headNeckWeight
                        dv += kinematics.headDeltaV * headNeckWeight
                    }

                    val screenX = charLeft + (u + du) * targetCharW
                    val screenY = charTop + (v + dv) * finalCharH

                    meshVerts[vIdx++] = screenX
                    meshVerts[vIdx++] = screenY

                    if (silhouetteTint != null) {
                        meshColors[cIdx++] = silhouetteTint.toArgb()
                    } else {
                        val normalX = (relU * 2.4f).coerceIn(-1f, 1f)
                        val shadeDelta = (-normalX * totalYaw * 18f).toInt().coerceIn(-22, 16)
                        val channel = (255 + shadeDelta).coerceIn(228, 255)
                        meshColors[cIdx++] = android.graphics.Color.argb(255, channel, channel, channel)
                    }
                }
            }

            // 5. Render the Deformed Pixel-Exact Character Mesh via Native Canvas + 3D Camera
            drawIntoCanvas { composeCanvas ->
                val native = composeCanvas.nativeCanvas
                native.save()

                val pivotX = charLeft + targetCharW * 0.46f
                val pivotY = charTop + finalCharH * 0.44f
                camera3D.save()
                camera3D.setLocation(0f, 0f, -18f)
                camera3D.rotateY(smoothCameraYaw * 0.32f)
                camera3D.rotateX(-smoothCameraPitch * 0.25f)
                camera3D.getMatrix(matrix3D)
                camera3D.restore()

                matrix3D.preTranslate(-pivotX, -pivotY)
                matrix3D.postTranslate(pivotX, pivotY)
                native.concat(matrix3D)

                if (silhouetteTint == null) {
                    native.save()
                    native.translate(
                        -smoothCameraYaw * 0.35f + 4.dp.toPx(),
                        6.dp.toPx()
                    )
                    native.drawBitmapMesh(
                        fullBitmap,
                        MESH_COLS,
                        MESH_ROWS,
                        meshVerts,
                        0,
                        null,
                        0,
                        shadowPaint
                    )
                    native.restore()
                }

                native.drawBitmapMesh(
                    fullBitmap,
                    MESH_COLS,
                    MESH_ROWS,
                    meshVerts,
                    0,
                    if (silhouetteTint == null) meshColors else null,
                    0,
                    mainPaint
                )

                native.restore()
            }

            // 6. Minimalist 3D Biomechanical Motion Vectors & Kinetic Chain Telemetry HUD
            // Clearly illustrates exact technique mechanics (trajectory, hip torque, slip angle)
            if (showAnatomicalHighlights && silhouetteTint == null) {
                val leadShoulder = Offset(
                    x = charLeft + (0.245f + kinematics.slipOffsetX) * targetCharW,
                    y = charTop + (0.225f + kinematics.bodyBounceY) * finalCharH
                )
                val rearShoulder = Offset(
                    x = charLeft + (0.605f + kinematics.slipOffsetX) * targetCharW,
                    y = charTop + (0.215f + kinematics.bodyBounceY) * finalCharH
                )
                val leadGlove = Offset(
                    x = charLeft + (0.302f + kinematics.leadArmDeltaU * 1.15f + kinematics.slipOffsetX) * targetCharW,
                    y = charTop + (0.202f + kinematics.leadArmDeltaV * 1.15f + kinematics.bodyBounceY) * finalCharH
                )
                val rearGlove = Offset(
                    x = charLeft + (0.528f + kinematics.rearArmDeltaU * 1.15f + kinematics.slipOffsetX) * targetCharW,
                    y = charTop + (0.190f + kinematics.rearArmDeltaV * 1.15f + kinematics.bodyBounceY) * finalCharH
                )
                val hipCenter = Offset(
                    x = charLeft + (0.455f + kinematics.footworkShiftX * 0.5f) * targetCharW,
                    y = charTop + (0.455f + kinematics.bodyBounceY) * finalCharH
                )

                // Lead Hand Strike Vector (#1 Jab / #3 Lead Hook)
                if (kinematics.leadPunchExt > 0.14f) {
                    val ext = kinematics.leadPunchExt
                    val strikeTarget = Offset(
                        x = leadGlove.x + targetCharW * 0.16f * ext,
                        y = leadGlove.y - finalCharH * 0.045f * ext
                    )
                    // Kinetic chain line: Shoulder -> Glove -> Strike Apex
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                KynaroRed.copy(alpha = 0.75f * ext),
                                KynaroTextWhite.copy(alpha = 0.95f * ext)
                            ),
                            start = leadShoulder,
                            end = strikeTarget
                        ),
                        start = leadShoulder,
                        end = strikeTarget,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Impact Target Ring at strike apex
                    drawCircle(
                        color = KynaroRed.copy(alpha = 0.85f * ext),
                        radius = (8f + ext * 16f).dp.toPx(),
                        center = strikeTarget,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = KynaroTextWhite.copy(alpha = 0.95f * ext),
                        radius = 4.dp.toPx(),
                        center = strikeTarget
                    )
                }

                // Rear Hand Power Vector (#2 Cross / #6 Rear Uppercut)
                if (kinematics.rearPunchExt > 0.14f) {
                    val ext = kinematics.rearPunchExt
                    val strikeTarget = if (pose == CoachPose.UPPERCUT) {
                        Offset(
                            x = rearGlove.x - targetCharW * 0.08f * ext,
                            y = rearGlove.y - finalCharH * 0.12f * ext
                        )
                    } else {
                        Offset(
                            x = rearGlove.x - targetCharW * 0.22f * ext,
                            y = rearGlove.y - finalCharH * 0.035f * ext
                        )
                    }

                    // Hip-to-Shoulder Torque Line
                    drawLine(
                        color = KynaroRed.copy(alpha = 0.45f * ext),
                        start = hipCenter,
                        end = rearShoulder,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Power Vector Line: Rear Shoulder -> Rear Glove -> Strike Apex
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                KynaroRed.copy(alpha = 0.85f * ext),
                                KynaroTextWhite.copy(alpha = 0.95f * ext)
                            ),
                            start = rearShoulder,
                            end = strikeTarget
                        ),
                        start = rearShoulder,
                        end = strikeTarget,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = KynaroRed.copy(alpha = 0.90f * ext),
                        radius = (10f + ext * 18f).dp.toPx(),
                        center = strikeTarget,
                        style = Stroke(width = 2.2.dp.toPx())
                    )
                    drawCircle(
                        color = KynaroTextWhite.copy(alpha = 0.95f * ext),
                        radius = 4.5.dp.toPx(),
                        center = strikeTarget
                    )
                }

                // Slip / Roll Evasion Centerline Reference
                if (pose == CoachPose.SLIP_DEFENSE) {
                    val headTop = charTop + finalCharH * 0.06f
                    val chestBottom = charTop + finalCharH * 0.36f
                    // Vertical Centerline Axis
                    drawLine(
                        color = KynaroTextWhite.copy(alpha = 0.35f),
                        start = Offset(centerX, headTop),
                        end = Offset(centerX, chestBottom),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                    // Current Head Position Off-Centerline Indicator
                    val currentHeadCenter = Offset(
                        x = charLeft + (0.44f + kinematics.slipOffsetX + kinematics.headDeltaU) * targetCharW,
                        y = charTop + (0.12f + kinematics.bodyBounceY) * finalCharH
                    )
                    drawCircle(
                        color = KynaroGreen.copy(alpha = 0.75f),
                        radius = 18.dp.toPx(),
                        center = currentHeadCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        if (showInteractiveHint && allowCameraOrbit) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KynaroBlack.copy(alpha = 0.78f))
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DRAG TO ROTATE 3D • TAP COACH TO STRIKE",
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

private class MeshKinematics(
    val bodyBounceY: Float,
    val footworkShiftX: Float,
    val kneeDip: Float,
    val slipOffsetX: Float,
    val torsoTwistYaw: Float,
    val leadArmDeltaU: Float,
    val leadArmDeltaV: Float,
    val rearArmDeltaU: Float,
    val rearArmDeltaV: Float,
    val headDeltaU: Float,
    val headDeltaV: Float,
    val leadPunchExt: Float,
    val rearPunchExt: Float,
    val leadFistRotationDeg: Float,
    val rearFistRotationDeg: Float
)

private fun evaluateMeshKinematics(
    pose: CoachPose,
    phase: Float,
    breathSin: Float,
    tapImpulse: Float,
    tapTriggerIndex: Int
): MeshKinematics {
    val twoPiPhase = phase * (2f * PI).toFloat()

    var bodyBounceY = sin(twoPiPhase) * 0.008f
    var footworkShiftX = 0f
    var kneeDip = max(0f, sin(twoPiPhase)) * 0.12f
    var slipOffsetX = 0f
    var torsoTwistYaw = 0f

    var leadArmDeltaU = 0f
    var leadArmDeltaV = sin(twoPiPhase + 0.5f) * 0.005f
    var rearArmDeltaU = 0f
    var rearArmDeltaV = cos(twoPiPhase) * 0.005f

    var headDeltaU = 0f
    var headDeltaV = 0f

    var leadPunchExt = 0f
    var rearPunchExt = 0f
    var leadFistRotationDeg = 0f
    var rearFistRotationDeg = 0f

    when (pose) {
        CoachPose.IDLE_GUARD -> {
            bodyBounceY = sin(twoPiPhase) * 0.010f
            footworkShiftX = cos(twoPiPhase) * 0.008f
            torsoTwistYaw = sin(twoPiPhase) * 0.14f
            leadArmDeltaU = cos(twoPiPhase) * 0.006f
            leadArmDeltaV = sin(twoPiPhase) * 0.008f
            rearArmDeltaU = -cos(twoPiPhase) * 0.005f
            rearArmDeltaV = -sin(twoPiPhase) * 0.007f
        }

        CoachPose.JAB -> {
            val ext = punchCurve(phase, peakAt = 0.34f)
            leadPunchExt = ext
            leadArmDeltaU = 0.068f * ext
            leadArmDeltaV = -0.034f * ext
            leadFistRotationDeg = -18f * ext
            rearArmDeltaU = -0.012f * ext
            rearArmDeltaV = -0.010f * ext
            torsoTwistYaw = -0.48f * ext
            headDeltaU = -0.012f * ext
            headDeltaV = 0.008f * ext
        }

        CoachPose.CROSS -> {
            val ext = punchCurve(phase, peakAt = 0.38f)
            rearPunchExt = ext
            rearArmDeltaU = -0.082f * ext
            rearArmDeltaV = -0.032f * ext
            rearFistRotationDeg = 22f * ext
            leadArmDeltaU = 0.014f * ext
            leadArmDeltaV = -0.012f * ext
            torsoTwistYaw = 0.64f * ext
            footworkShiftX = -0.014f * ext
            headDeltaU = -0.016f * ext
            headDeltaV = 0.010f * ext
        }

        CoachPose.ONE_TWO_COMBO -> {
            if (phase < 0.48f) {
                val localP = phase / 0.48f
                val ext = punchCurve(localP, peakAt = 0.38f)
                leadPunchExt = ext
                leadArmDeltaU = 0.064f * ext
                leadArmDeltaV = -0.032f * ext
                leadFistRotationDeg = -16f * ext
                torsoTwistYaw = -0.44f * ext
            } else {
                val localP = (phase - 0.48f) / 0.52f
                val ext = punchCurve(localP, peakAt = 0.40f)
                rearPunchExt = ext
                rearArmDeltaU = -0.078f * ext
                rearArmDeltaV = -0.030f * ext
                rearFistRotationDeg = 20f * ext
                torsoTwistYaw = 0.60f * ext
                footworkShiftX = -0.012f * ext
            }
        }

        CoachPose.JAB_CROSS_HOOK -> {
            when {
                phase < 0.32f -> {
                    val ext = punchCurve(phase / 0.32f, peakAt = 0.40f)
                    leadPunchExt = ext
                    leadArmDeltaU = 0.062f * ext
                    leadArmDeltaV = -0.030f * ext
                    leadFistRotationDeg = -15f * ext
                    torsoTwistYaw = -0.40f * ext
                }
                phase < 0.64f -> {
                    val ext = punchCurve((phase - 0.32f) / 0.32f, peakAt = 0.40f)
                    rearPunchExt = ext
                    rearArmDeltaU = -0.076f * ext
                    rearArmDeltaV = -0.028f * ext
                    rearFistRotationDeg = 20f * ext
                    torsoTwistYaw = 0.58f * ext
                }
                else -> {
                    val hookP = (phase - 0.64f) / 0.36f
                    val ext = punchCurve(hookP, peakAt = 0.44f)
                    val sweepArc = sin(hookP * PI.toFloat())
                    leadPunchExt = ext
                    leadArmDeltaU = (-0.036f * (1f - hookP) + 0.088f * hookP) * sweepArc
                    leadArmDeltaV = -0.026f * ext
                    leadFistRotationDeg = -38f * ext
                    torsoTwistYaw = -0.68f * ext
                    slipOffsetX = 0.020f * ext
                }
            }
        }

        CoachPose.SPEED_HANDS -> {
            val leftWave = max(0f, sin(twoPiPhase))
            val rightWave = max(0f, -sin(twoPiPhase))
            leadPunchExt = leftWave
            rearPunchExt = rightWave
            leadArmDeltaU = 0.056f * leftWave
            leadArmDeltaV = -0.026f * leftWave
            leadFistRotationDeg = -14f * leftWave
            rearArmDeltaU = -0.064f * rightWave
            rearArmDeltaV = -0.026f * rightWave
            rearFistRotationDeg = 16f * rightWave
            torsoTwistYaw = (rightWave - leftWave) * 0.42f
            bodyBounceY = sin(twoPiPhase * 2f) * 0.006f
        }

        CoachPose.UPPERCUT -> {
            if (phase < 0.32f) {
                val load = sin((phase / 0.32f) * (PI / 2f).toFloat())
                bodyBounceY = 0.026f * load
                kneeDip = 0.38f * load
                slipOffsetX = 0.018f * load
                rearArmDeltaV = 0.036f * load
                rearArmDeltaU = 0.016f * load
            } else {
                val driveP = (phase - 0.32f) / 0.68f
                val ext = punchCurve(driveP, peakAt = 0.36f)
                bodyBounceY = -0.020f * ext
                rearPunchExt = ext
                rearArmDeltaU = -0.056f * ext
                rearArmDeltaV = -0.074f * ext
                rearFistRotationDeg = -28f * ext
                torsoTwistYaw = 0.60f * ext
                headDeltaV = -0.012f * ext
            }
        }

        CoachPose.SLIP_DEFENSE -> {
            val slipWave = sin(twoPiPhase)
            val duckWave = abs(slipWave)
            slipOffsetX = 0.054f * slipWave
            bodyBounceY = 0.024f * duckWave
            kneeDip = 0.30f * duckWave
            torsoTwistYaw = slipWave * 0.46f
            leadArmDeltaU = 0.014f * duckWave
            leadArmDeltaV = -0.014f * duckWave
            rearArmDeltaU = -0.014f * duckWave
            rearArmDeltaV = -0.014f * duckWave
            headDeltaU = 0.022f * slipWave
        }

        CoachPose.FOOTWORK -> {
            val stepWave = sin(twoPiPhase)
            val bounceWave = abs(cos(twoPiPhase))
            footworkShiftX = 0.042f * stepWave
            bodyBounceY = -0.022f * bounceWave
            kneeDip = 0.24f * (1f - bounceWave)
            torsoTwistYaw = stepWave * 0.28f
            leadArmDeltaV = -0.012f * bounceWave
            rearArmDeltaV = -0.012f * bounceWave
        }

        CoachPose.VICTORY_RAISE -> {
            val lift = 0.5f + 0.5f * sin(twoPiPhase)
            bodyBounceY = -0.014f * lift
            leadArmDeltaU = -0.032f * lift
            leadArmDeltaV = -0.062f * lift
            rearArmDeltaU = 0.032f * lift
            rearArmDeltaV = -0.062f * lift
            leadPunchExt = 0.35f * lift
            rearPunchExt = 0.35f * lift
        }
    }

    if (tapImpulse > 0.01f) {
        if (tapTriggerIndex % 2 == 1) {
            leadPunchExt = max(leadPunchExt, tapImpulse)
            leadArmDeltaU += 0.064f * tapImpulse
            leadArmDeltaV -= 0.032f * tapImpulse
            leadFistRotationDeg -= 18f * tapImpulse
            torsoTwistYaw -= 0.42f * tapImpulse
        } else {
            rearPunchExt = max(rearPunchExt, tapImpulse)
            rearArmDeltaU -= 0.076f * tapImpulse
            rearArmDeltaV -= 0.030f * tapImpulse
            rearFistRotationDeg += 20f * tapImpulse
            torsoTwistYaw += 0.52f * tapImpulse
        }
    }

    return MeshKinematics(
        bodyBounceY = bodyBounceY,
        footworkShiftX = footworkShiftX,
        kneeDip = kneeDip,
        slipOffsetX = slipOffsetX,
        torsoTwistYaw = torsoTwistYaw,
        leadArmDeltaU = leadArmDeltaU,
        leadArmDeltaV = leadArmDeltaV,
        rearArmDeltaU = rearArmDeltaU,
        rearArmDeltaV = rearArmDeltaV,
        headDeltaU = headDeltaU,
        headDeltaV = headDeltaV,
        leadPunchExt = leadPunchExt.coerceIn(0f, 1f),
        rearPunchExt = rearPunchExt.coerceIn(0f, 1f),
        leadFistRotationDeg = leadFistRotationDeg,
        rearFistRotationDeg = rearFistRotationDeg
    )
}

private fun punchCurve(t: Float, peakAt: Float = 0.36f): Float {
    val p = t.coerceIn(0f, 1f)
    return if (p < peakAt) {
        val u = p / peakAt
        sin(u * (PI / 2.0).toFloat())
    } else {
        val u = (p - peakAt) / (1f - peakAt)
        val c = cos(u * (PI / 2.0).toFloat())
        c * c
    }
}

private fun gaussianWeight(x: Float, center: Float, sigma: Float): Float {
    val d = (x - center) / sigma
    return exp(-0.5f * d * d)
}

private fun gaussian2D(
    u: Float,
    v: Float,
    cu: Float,
    cv: Float,
    su: Float,
    sv: Float
): Float {
    val du = (u - cu) / su
    val dv = (v - cv) / sv
    return exp(-0.5f * (du * du + dv * dv))
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
                        silhouetteTint = Color(0xFF3E4251),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 52.dp)
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "DAY 1",
                            style = MaterialTheme.typography.labelMedium,
                            color = KynaroTextWhite,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "LV 1",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = KynaroTextSecondary
                        )
                        Text(
                            text = "Foundation stance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KynaroTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    KynaroRed.copy(alpha = 0.22f),
                                    KynaroCardBg
                                )
                            )
                        )
                        .border(1.dp, KynaroRed.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    CoachCharacter3DView(
                        pose = CoachPose.ONE_TWO_COMBO,
                        showFloorRing = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 52.dp)
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "DAY 30",
                            style = MaterialTheme.typography.labelMedium,
                            color = KynaroRed,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "LV 3",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = KynaroRed
                        )
                        Text(
                            text = "Sharp combinations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KynaroTextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(KynaroRed)
                    .border(3.dp, KynaroBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    color = KynaroBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CoachCharacter3DView(
                        pose = CoachPose.IDLE_GUARD,
                        showFloorRing = false,
                        showAnatomicalHighlights = false,
                        physiqueBulkMultiplier = 1.15f,
                        modifier = Modifier.fillMaxSize(0.95f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DAY 1 BASE",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    color = KynaroTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                KynaroRed.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CoachCharacter3DView(
                        pose = CoachPose.ONE_TWO_COMBO,
                        showFloorRing = false,
                        physiqueBulkMultiplier = 1.0f,
                        modifier = Modifier.fillMaxSize(0.95f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DAY 30 PEAK",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    color = KynaroRed
                )
            }
        }
    }
}

@Composable
fun CoachDrillThumbnail(
    pose: CoachPose,
    size: Dp = 108.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        KynaroRed.copy(alpha = 0.25f),
                        KynaroCardElevated,
                        KynaroBlack
                    )
                )
            )
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        CoachCharacter3DView(
            pose = pose,
            showFloorRing = false,
            allowCameraOrbit = false,
            cropUpperBody = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
        )
    }
}
