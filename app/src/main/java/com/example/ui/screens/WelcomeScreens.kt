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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoachPose
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.KynaroBrandMark
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun WelcomeScreen(
    onStartOnboarding: () -> Unit,
    onQuickSignIn: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    KynaroScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KynaroBrandMark(iconSize = 32.dp, fontSizeSp = 22)

                Box {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .clickable { languageMenuExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("language_selector"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedLanguage,
                            style = MaterialTheme.typography.bodySmall,
                            color = KynaroTextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Language",
                            tint = KynaroTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false }
                    ) {
                        listOf("English", "Español", "Français", "Deutsch").forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Full-Body 3D Articulated Coach Stage (Live 1-2 Combo Demo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF1E150E), KynaroBlack))
                    )
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                CoachCharacter3DView(
                    pose = CoachPose.ONE_TWO_COMBO,
                    showAnatomicalHighlights = true,
                    allowCameraOrbit = true,
                    showInteractiveHint = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1A120A))
                    .border(1.dp, KynaroOrange.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = KynaroOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = KynaroTextWhite, fontWeight = FontWeight.Bold)) {
                            append("KYNARO ")
                        }
                        withStyle(SpanStyle(color = KynaroOrange, fontWeight = FontWeight.Bold)) {
                            append("COACH")
                        }
                    },
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "START YOUR\nBOXING JOURNEY.",
                style = MaterialTheme.typography.displaySmall,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KynaroTextWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            WelcomeFeatureRow(
                icon = Icons.Default.AccessibilityNew,
                iconTint = KynaroOrange,
                iconBoxBg = Color(0xFF1A120A),
                iconBoxBorder = KynaroOrange.copy(alpha = 0.45f),
                label = "Real-action animated boxing coach"
            )
            Spacer(modifier = Modifier.height(10.dp))
            WelcomeFeatureRow(
                icon = Icons.Default.SportsMma,
                iconTint = KynaroGreen,
                iconBoxBg = Color(0xFF081F17),
                iconBoxBorder = KynaroGreen.copy(alpha = 0.35f),
                label = "Punches 1–6, slips, rolls & footwork"
            )
            Spacer(modifier = Modifier.height(10.dp))
            WelcomeFeatureRow(
                icon = Icons.Default.TrackChanges,
                iconTint = KynaroTextWhite,
                iconBoxBg = Color(0xFF141722),
                iconBoxBorder = KynaroCardBorder,
                label = "Structured 30-day boxing camp"
            )

            Spacer(modifier = Modifier.height(22.dp))

            KynaroPrimaryButton(
                text = "GET STARTED",
                onClick = onStartOnboarding,
                testTag = "get_started_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = KynaroTextSecondary)) {
                            append("Already training? ")
                        }
                        withStyle(SpanStyle(color = KynaroOrange, fontWeight = FontWeight.Bold)) {
                            append("Enter Camp")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onQuickSignIn() }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .testTag("welcome_sign_in_button")
                )
            }
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    iconBoxBg: Color,
    iconBoxBorder: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg)
                .border(1.dp, iconBoxBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = KynaroTextWhite,
            maxLines = 1
        )
    }
}
