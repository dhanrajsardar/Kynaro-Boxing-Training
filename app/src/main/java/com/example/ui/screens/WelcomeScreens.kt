package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.example.ui.theme.KynaroRed
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
            // Top Row: Official KYNARO Icon + Brand Mark & Language Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KynaroBrandMark(iconSize = 34.dp, fontSizeSp = 22)

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
                            style = MaterialTheme.typography.bodyMedium,
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

            // Prominent Interactive 3D Coach Character (Original Guard Stance)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                contentAlignment = Alignment.Center
            ) {
                CoachCharacter3DView(
                    pose = CoachPose.IDLE_GUARD,
                    showAnatomicalHighlights = false,
                    allowCameraOrbit = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KYNARO AI COACH Pill Badge
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF161307))
                    .border(1.dp, KynaroRed.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = KynaroRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = KynaroTextWhite, fontWeight = FontWeight.Bold)) {
                            append("KYNARO ")
                        }
                        withStyle(SpanStyle(color = KynaroRed, fontWeight = FontWeight.Bold)) {
                            append("AI COACH")
                        }
                    },
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Headline
            Text(
                text = "Your coach can\nsee you punch.",
                style = MaterialTheme.typography.displaySmall,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KynaroTextWhite
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Feature Rows — Minimalist Championship Gold, Emerald & Stark White
            WelcomeFeatureRow(
                icon = Icons.Default.SportsMma,
                iconTint = KynaroRed,
                iconBoxBg = Color(0xFF171407),
                iconBoxBorder = KynaroRed.copy(alpha = 0.45f),
                label = "START YOUR BOXING JOURNEY"
            )
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureRow(
                icon = Icons.Default.TrackChanges,
                iconTint = KynaroGreen,
                iconBoxBg = Color(0xFF071F17),
                iconBoxBorder = KynaroGreen.copy(alpha = 0.35f),
                label = "30-day fight camp"
            )
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureRow(
                icon = Icons.Default.FitnessCenter,
                iconTint = KynaroTextWhite,
                iconBoxBg = Color(0xFF14161E),
                iconBoxBorder = KynaroCardBorder,
                label = "A 3D coach on every drill"
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(22.dp))

            // Primary GET STARTED Button (Championship Gold with Jet Black text)
            KynaroPrimaryButton(
                text = "GET STARTED",
                onClick = onStartOnboarding,
                testTag = "get_started_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sign in row
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = KynaroTextSecondary)) {
                            append("Have a camp? ")
                        }
                        withStyle(SpanStyle(color = KynaroRed, fontWeight = FontWeight.Bold)) {
                            append("Sign in")
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
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg)
                .border(1.dp, iconBoxBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            color = KynaroTextWhite,
            maxLines = 1,
            softWrap = false
        )
    }
}
