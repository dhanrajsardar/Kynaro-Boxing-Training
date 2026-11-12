package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroDeepBurgundy
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun KynaroScreenBackground(
    modifier: Modifier = Modifier,
    topGlowColor: Color = KynaroDeepBurgundy,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KynaroBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Minimalist architectural Obsidian & Championship Gold top spotlight
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        topGlowColor,
                        Color(0xFF0A0A0D),
                        KynaroBlack,
                        KynaroBlack
                    ),
                    startY = 0f,
                    endY = size.height * 0.60f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KynaroRed.copy(alpha = 0.085f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.14f),
                    radius = size.width * 0.78f
                ),
                radius = size.width * 0.78f,
                center = Offset(size.width * 0.5f, size.height * 0.14f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            content = content
        )
    }
}

@Composable
fun KynaroBrandMark(
    iconSize: Dp = 32.dp,
    fontSizeSp: Int = 22
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_kynaro_logo),
            contentDescription = "KYNARO Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, KynaroRed.copy(alpha = 0.6f), RoundedCornerShape(9.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = KynaroTextWhite, fontWeight = FontWeight.ExtraBold)) {
                    append("KYN")
                }
                withStyle(SpanStyle(color = KynaroRed, fontWeight = FontWeight.ExtraBold)) {
                    append("ARO")
                }
            },
            fontFamily = OswaldFontFamily,
            fontSize = fontSizeSp.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun KynaroPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = KynaroRed,
    contentColor: Color = KynaroBlack,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "primary_action_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (enabled) containerColor else Color(0xFF181920),
        animationSpec = tween(200),
        label = "btn_bg"
    )
    val txtColor by animateColorAsState(
        targetValue = if (enabled) contentColor else KynaroTextMuted,
        animationSpec = tween(200),
        label = "btn_txt"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (enabled) 1.dp else 0.dp,
                color = if (enabled) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides txtColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.4.sp,
                    color = txtColor
                )
            }
        }
    }
}

@Composable
fun OnboardingProgressHeader(
    currentStep: Int,
    totalSteps: Int = 17,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0.05f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "onboarding_progress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("onboarding_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = KynaroTextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 8.dp)
                .clip(CircleShape)
                .background(Color(0xFF181A22))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(KynaroRed)
            )
        }

        Text(
            text = String.format("%02d/%02d", currentStep, totalSteps),
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = KynaroTextMuted,
            modifier = Modifier.padding(start = 6.dp, end = 8.dp)
        )
    }
}

@Composable
fun SelectableOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    customIconContent: (@Composable () -> Unit)? = null,
    testTag: String = "option_card_$title"
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) KynaroRed else KynaroCardBorder,
        animationSpec = tween(180),
        label = "card_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF17150A) else KynaroCardBg,
        animationSpec = tween(180),
        label = "card_bg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(
                width = if (selected) 1.8.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (customIconContent != null || icon != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) KynaroRed.copy(alpha = 0.18f) else KynaroCardElevated),
                contentAlignment = Alignment.Center
            ) {
                if (customIconContent != null) {
                    customIconContent()
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) KynaroRed else KynaroTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = KynaroTextWhite
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) KynaroRed else Color.Transparent)
                .border(
                    width = 1.8.dp,
                    color = if (selected) KynaroRed else Color(0xFF333645),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = KynaroBlack,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun KynaroPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = KynaroRed.copy(alpha = 0.50f),
    textColor: Color = KynaroRed,
    backgroundColor: Color = Color(0xFF171407)
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
            color = textColor
        )
    }
}
