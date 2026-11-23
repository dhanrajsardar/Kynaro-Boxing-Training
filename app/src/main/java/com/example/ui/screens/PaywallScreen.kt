package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoachPose
import com.example.model.OnboardingAnswers
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

enum class KynaroSubscriptionPlan(
    val id: String,
    val title: String,
    val billedPriceText: String,
    val monthlyEquivalentText: String,
    val badgeText: String?,
    val hasFreeTrial: Boolean
) {
    ANNUAL(
        id = "kynaro_pro_annual_3999",
        title = "ANNUAL CAMP",
        billedPriceText = "$39.99 / year",
        monthlyEquivalentText = "$3.33 / mo",
        badgeText = "BEST VALUE • SAVE 67%",
        hasFreeTrial = true
    ),
    MONTHLY(
        id = "kynaro_pro_monthly_999",
        title = "MONTHLY",
        billedPriceText = "$9.99 / month",
        monthlyEquivalentText = "Billed monthly",
        badgeText = null,
        hasFreeTrial = false
    )
}

/**
 * KYNARO Hard Paywall (Placed at the end of Onboarding)
 * Built on 2026 Health & Fitness conversion benchmarks:
 * - Hard paywall immediately after personalized 30-day camp reveal
 * - Transparent 3-step 7-Day Free Trial Timeline (reduces billing anxiety)
 * - 2 clear plans: Annual ($39.99/yr = $3.33/mo, SAVE 67%, 7-day trial) vs Monthly ($9.99/mo)
 * - Google Play Billing transparent charge disclosure
 */
@Composable
fun PaywallScreen(
    answers: OnboardingAnswers,
    onBack: () -> Unit,
    onStartSubscription: (KynaroSubscriptionPlan) -> Unit,
    onRestorePurchases: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedPlan by rememberSaveable { mutableStateOf(KynaroSubscriptionPlan.ANNUAL) }
    val isSouthpaw = answers.strongHand == "Left-handed"
    val fighterDisplay = answers.fighterName.ifBlank { "Your" }

    KynaroScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Scrollable Top + Middle Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 10.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Back to review camp (Hard Paywall — no skip/dismiss bypass)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = KynaroTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1A120A))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "KYNARO PRO ACCESS",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            color = KynaroOrange
                        )
                    }

                    Spacer(modifier = Modifier.width(38.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live 8-Combo Pro Showcase Reel (Cycles through 1-2, 1-1-2, 1-2-3, 1-2-3-2, 1-2-5-2, 1-2-3-4, 2-3-2, 1-6-3-2)
                val showcaseCombos = remember {
                    listOf(
                        Triple("1–2", "Jab → Cross", listOf(CoachPose.JAB, CoachPose.CROSS)),
                        Triple("1–1–2", "Jab → Jab → Cross", listOf(CoachPose.JAB, CoachPose.JAB, CoachPose.CROSS)),
                        Triple("1–2–3", "Jab → Cross → Lead Hook", listOf(CoachPose.JAB, CoachPose.CROSS, CoachPose.JAB_CROSS_HOOK)),
                        Triple("1–2–3–2", "Jab → Cross → Hook → Cross", listOf(CoachPose.JAB, CoachPose.CROSS, CoachPose.JAB_CROSS_HOOK, CoachPose.CROSS)),
                        Triple("1–2–5–2", "Jab → Cross → Lead Uppercut → Cross", listOf(CoachPose.JAB, CoachPose.CROSS, CoachPose.LEAD_UPPERCUT, CoachPose.CROSS)),
                        Triple("1–2–3–4", "Jab → Cross → Lead Hook → Rear Hook", listOf(CoachPose.JAB, CoachPose.CROSS, CoachPose.JAB_CROSS_HOOK, CoachPose.REAR_HOOK)),
                        Triple("2–3–2", "Cross → Lead Hook → Cross", listOf(CoachPose.CROSS, CoachPose.JAB_CROSS_HOOK, CoachPose.CROSS)),
                        Triple("1–6–3–2", "Jab → Rear Uppercut → Lead Hook → Cross", listOf(CoachPose.JAB, CoachPose.UPPERCUT, CoachPose.JAB_CROSS_HOOK, CoachPose.CROSS))
                    )
                }
                var comboIdx by remember { mutableIntStateOf(0) }
                var stepInCombo by remember { mutableIntStateOf(0) }
                val activeCombo = showcaseCombos[comboIdx]
                val activeShowcasePose = activeCombo.third[stepInCombo % activeCombo.third.size]

                LaunchedEffect(comboIdx, stepInCombo) {
                    delay(850L)
                    if (stepInCombo + 1 < activeCombo.third.size) {
                        stepInCombo++
                    } else {
                        stepInCombo = 0
                        comboIdx = (comboIdx + 1) % showcaseCombos.size
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF24170D), KynaroCardBg))
                        )
                        .border(1.dp, KynaroOrange.copy(alpha = 0.40f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CoachCharacter3DView(
                        pose = activeShowcasePose,
                        isSouthpaw = isSouthpaw,
                        showFloorRing = true,
                        showAnatomicalHighlights = true,
                        allowCameraOrbit = false,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Live Combo Badge Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp, start = 10.dp, end = 10.dp)
                            .clip(CircleShape)
                            .background(KynaroBlack.copy(alpha = 0.85f))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.55f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeCombo.first,
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = KynaroOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeCombo.second,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = KynaroTextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personalized Headline
                Text(
                    text = if (answers.fighterName.isNotBlank()) {
                        "UNLOCK ${fighterDisplay.uppercase()}'S\n30-DAY BOXING CAMP"
                    } else {
                        "UNLOCK YOUR\n30-DAY BOXING CAMP"
                    },
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    color = KynaroTextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${if (isSouthpaw) "Southpaw" else "Orthodox"} • ${answers.ringGoal} • ${answers.daysPerWeekShort}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = KynaroOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3-Step Transparent Free Trial Timeline (Shown prominently for Annual Trial)
                if (selectedPlan.hasFreeTrial) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "HOW YOUR 7-DAY FREE TRIAL WORKS",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.3.sp,
                            color = KynaroTextSecondary
                        )
                        TrialTimelineRow(
                            icon = Icons.Default.LockOpen,
                            accent = KynaroOrange,
                            title = "Today — Instant Full Access ($0.00)",
                            desc = "Unlock your 30-day camp, all 12 360° Coach techniques & Heavy Bag Timer."
                        )
                        TrialTimelineRow(
                            icon = Icons.Default.NotificationsActive,
                            accent = KynaroYellow,
                            title = "Day 5 — Trial Reminder",
                            desc = "Get a reminder before your trial ends. Cancel in 2 taps if it's not for you."
                        )
                        TrialTimelineRow(
                            icon = Icons.Default.Star,
                            accent = KynaroGreen,
                            title = "Day 7 — Annual Plan Starts",
                            desc = "Billed $39.99/year ($3.33/mo). Keep building pro boxing form all year."
                        )
                    }
                } else {
                    // Compact Value List when Monthly is selected
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PaywallBenefitRow("Full 30-Day Personalized Boxing Camp")
                        PaywallBenefitRow("360° Real-Action Coach for All 12 Pro Techniques")
                        PaywallBenefitRow("Orthodox & Southpaw Mirroring + 0.5x Slow-Mo")
                        PaywallBenefitRow("Heavy Bag Round Timer & Live Combo Caller")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2-Plan Selector (Annual Pre-Selected + Monthly Anchor)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KynaroSubscriptionPlan.entries.forEach { plan ->
                        val isSelected = selectedPlan == plan
                        PaywallPlanCard(
                            plan = plan,
                            selected = isSelected,
                            onClick = { selectedPlan = plan }
                        )
                    }
                }
            }

            // Sticky Bottom Conversion CTA + Google Play Transparent Disclosure
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = KynaroGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedPlan.hasFreeTrial) {
                            "NO PAYMENT DUE TODAY • CANCEL ANYTIME"
                        } else {
                            "CANCEL ANYTIME IN GOOGLE PLAY"
                        },
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = KynaroGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                KynaroPrimaryButton(
                    text = if (selectedPlan.hasFreeTrial) {
                        "START 7-DAY FREE TRIAL"
                    } else {
                        "CONTINUE FOR $9.99 / MONTH"
                    },
                    onClick = { onStartSubscription(selectedPlan) },
                    testTag = "paywall_primary_cta"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (selectedPlan.hasFreeTrial) {
                        "7 days free, then $39.99/year ($3.33/mo). Auto-renews until cancelled."
                    } else {
                        "Billed $9.99/month via Google Play. Auto-renews until cancelled."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = KynaroTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Restore Purchase",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = KynaroTextSecondary,
                        modifier = Modifier
                            .clickable { onRestorePurchases() }
                            .padding(vertical = 4.dp)
                    )
                    Text(text = "•", color = KynaroTextMuted, fontSize = 11.sp)
                    Text(
                        text = "Terms",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = KynaroTextMuted
                    )
                    Text(text = "•", color = KynaroTextMuted, fontSize = 11.sp)
                    Text(
                        text = "Privacy",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = KynaroTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TrialTimelineRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = KynaroTextWhite
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = KynaroTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PaywallBenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(KynaroOrange.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = KynaroOrange,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = KynaroTextWhite
        )
    }
}

@Composable
private fun PaywallPlanCard(
    plan: KynaroSubscriptionPlan,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color(0xFF1B130A) else KynaroCardBg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) KynaroOrange else KynaroCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("plan_card_${plan.name.lowercase()}")
    ) {
        if (plan.badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KynaroOrange)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = plan.badgeText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = KynaroBlack
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (selected) KynaroOrange else Color.Transparent)
                        .border(
                            1.8.dp,
                            if (selected) KynaroOrange else KynaroTextMuted,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = KynaroBlack,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = plan.title,
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = if (plan.hasFreeTrial) "7-day free trial • ${plan.billedPriceText}" else plan.billedPriceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = plan.monthlyEquivalentText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (selected) KynaroOrange else KynaroTextWhite
                )
                if (plan.hasFreeTrial) {
                    Text(
                        text = "Billed yearly",
                        fontSize = 10.sp,
                        color = KynaroTextMuted
                    )
                }
            }
        }
    }
}
