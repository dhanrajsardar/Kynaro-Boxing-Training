package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompletedCampDayEntity
import com.example.data.KynaroRepository
import com.example.data.WorkoutSessionEntity
import com.example.model.BoxingCategory
import com.example.model.BoxingDrill
import com.example.model.BoxingTechnique
import com.example.model.BoxingTechniqueRepository
import com.example.model.CampMission
import com.example.model.CoachPose
import com.example.model.KynaroCurriculum
import com.example.model.OnboardingAnswers
import com.example.model.WorkoutSpeed
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.CoachDrillThumbnail
import com.example.ui.components.KynaroBrandMark
import com.example.ui.components.KynaroPillBadge
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroBlue
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroOrange
import com.example.ui.theme.KynaroOrangeDark
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

enum class MainNavTab(val label: String) {
    TRAIN("Train"),
    COACH_LAB("Coach Lab"),
    DRILLS("Drills"),
    PROGRESS("Progress")
}

@Composable
fun MainTrainingScreen(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    workoutSessions: List<WorkoutSessionEntity>,
    completedCampDays: List<CompletedCampDayEntity>,
    activePlan: KynaroSubscriptionPlan = KynaroSubscriptionPlan.ANNUAL,
    onOpenPaywall: () -> Unit = {},
    onStartMission: (CampMission) -> Unit,
    onStartDrill: (BoxingDrill) -> Unit,
    onRetakeOnboarding: () -> Unit,
    onUpdateAnswers: (OnboardingAnswers) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainNavTab.TRAIN) }
    var showHeavyBagTimerScreen by rememberSaveable { mutableStateOf(false) }

    val allMissions = remember(answers) { KynaroCurriculum.get30DayCampMissions(answers) }
    val allDrills = remember { KynaroCurriculum.getPracticeDrills() }
    val activeDay = stats.currentCampDay.coerceIn(1, 30)
    val todaysMission = allMissions.getOrElse(activeDay - 1) { allMissions.first() }
    val isSouthpaw = answers.strongHand == "Left-handed"

    BackHandler(enabled = selectedTab != MainNavTab.TRAIN || showHeavyBagTimerScreen) {
        if (showHeavyBagTimerScreen) {
            showHeavyBagTimerScreen = false
        } else {
            selectedTab = MainNavTab.TRAIN
        }
    }

    KynaroScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 86.dp)
            ) {
                when (selectedTab) {
                    MainNavTab.TRAIN -> TrainHomeTab(
                        answers = answers,
                        stats = stats,
                        isSouthpaw = isSouthpaw,
                        todaysMission = todaysMission,
                        allMissions = allMissions,
                        completedDaysSet = completedCampDays.map { it.dayNumber }.toSet(),
                        onToggleStance = {
                            val nextHand = if (isSouthpaw) "Right-handed" else "Left-handed"
                            onUpdateAnswers(answers.copy(strongHand = nextHand))
                        },
                        onStartMission = onStartMission,
                        onOpenCoachLab = { selectedTab = MainNavTab.COACH_LAB },
                        onOpenTimer = { showHeavyBagTimerScreen = true }
                    )

                    MainNavTab.COACH_LAB -> CoachLabTab(
                        isSouthpaw = isSouthpaw,
                        onToggleStance = {
                            val nextHand = if (isSouthpaw) "Right-handed" else "Left-handed"
                            onUpdateAnswers(answers.copy(strongHand = nextHand))
                        },
                        onLaunchTechniqueDrill = { technique ->
                            val matched = allDrills.firstOrNull { it.pose == technique.pose } ?: allDrills.first()
                            onStartDrill(matched)
                        }
                    )

                    MainNavTab.DRILLS -> DrillsTab(
                        drills = allDrills,
                        onStartDrill = onStartDrill,
                        onOpenHeavyBagTimer = { showHeavyBagTimerScreen = true },
                        onOpenCoachLab = { selectedTab = MainNavTab.COACH_LAB }
                    )

                    MainNavTab.PROGRESS -> ProgressProfileTab(
                        answers = answers,
                        stats = stats,
                        activePlan = activePlan,
                        workoutSessions = workoutSessions,
                        completedCampDays = completedCampDays,
                        onOpenPaywall = onOpenPaywall,
                        onRetakeOnboarding = onRetakeOnboarding,
                        onUpdateAnswers = onUpdateAnswers
                    )
                }
            }

            // Clean 4-Tab Floating Bottom Bar + Center Round Timer Glove Button
            KynaroFloatingBottomNav(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onCenterGloveClick = { showHeavyBagTimerScreen = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )

            // Fullscreen Heavy Bag & Shadowboxing Round Timer with Live 3D Coach
            if (showHeavyBagTimerScreen) {
                HeavyBagRoundTimerScreen(
                    isSouthpaw = isSouthpaw,
                    onClose = { showHeavyBagTimerScreen = false }
                )
            }
        }
    }
}

// =========================================================================================
// TAB 1: TRAIN — Clean, Structured Daily Training Hub (Zero Clutter, Zero Fake Data)
// =========================================================================================
@Composable
private fun TrainHomeTab(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    isSouthpaw: Boolean,
    todaysMission: CampMission,
    allMissions: List<CampMission>,
    completedDaysSet: Set<Int>,
    onToggleStance: () -> Unit,
    onStartMission: (CampMission) -> Unit,
    onOpenCoachLab: () -> Unit,
    onOpenTimer: () -> Unit
) {
    var expandedWeekIndex by rememberSaveable { mutableIntStateOf(((stats.currentCampDay - 1) / 7).coerceIn(0, 3)) }
    var livePhaseLabel by remember { mutableStateOf("BALANCED GUARD") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Clean Top Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KynaroBrandMark(iconSize = 28.dp, fontSizeSp = 20)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Stance Switcher Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .clickable { onToggleStance() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSouthpaw) "SOUTHPAW" else "ORTHODOX",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = KynaroTextSecondary
                        )
                    }

                    // Active Streak Pill (Real Data)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1A1107))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = KynaroOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.streakDays}d",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = KynaroOrange
                        )
                    }
                }
            }
        }

        // 2. Hero Card: Full-Body Live 3D Coach + Today's Session
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF17120D), KynaroCardBg)
                        )
                    )
                    .border(1.dp, KynaroOrange.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KynaroPillBadge(text = "DAY ${todaysMission.day} OF 30 • TODAY'S SESSION")
                    Text(
                        text = "${todaysMission.durationMinutes} MIN",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = KynaroTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = todaysMission.title.uppercase(),
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    color = KynaroTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${todaysMission.roundsCount} coached rounds • ${answers.ringGoal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Full-Body 3D Coach Preview Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1E1712), KynaroBlack)
                            )
                        )
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CoachCharacter3DView(
                        pose = todaysMission.focusPose,
                        isSouthpaw = isSouthpaw,
                        showFloorRing = true,
                        showAnatomicalHighlights = true,
                        allowCameraOrbit = true,
                        onPhaseChanged = { label, _ -> livePhaseLabel = label },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Live Biomechanical Phase Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(KynaroBlack.copy(alpha = 0.78f))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = livePhaseLabel,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = KynaroOrange
                        )
                    }

                    // 360 Drag Hint
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .clip(CircleShape)
                            .background(KynaroBlack.copy(alpha = 0.72f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DRAG 360° TO INSPECT COACH",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = KynaroTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                KynaroPrimaryButton(
                    text = "START TODAY'S SESSION",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = KynaroBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { onStartMission(todaysMission) },
                    testTag = "start_mission_button"
                )
            }
        }

        // 3. Quick Studio Actions (2 Clean Side-by-Side Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStudioCard(
                    title = "3D COACH LAB",
                    subtitle = "360° Punches 1–6 & Defense",
                    icon = Icons.Default.AccessibilityNew,
                    accent = KynaroOrange,
                    onClick = onOpenCoachLab,
                    modifier = Modifier.weight(1f)
                )
                QuickStudioCard(
                    title = "ROUND TIMER",
                    subtitle = "Heavy Bag & Combo Caller",
                    icon = Icons.Default.Timer,
                    accent = KynaroYellow,
                    onClick = onOpenTimer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Structured 4-Week Boxing Curriculum (Clean Accordion — No 30-Row Mess!)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30-DAY BOXING PATH",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroTextSecondary
                    )
                    Text(
                        text = "${completedDaysSet.size}/30 DONE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = KynaroOrange
                    )
                }

                val phases = listOf(
                    Triple("PHASE 1 • DAYS 1–7", "Stance, Guard & Straight Punches", 1..7),
                    Triple("PHASE 2 • DAYS 8–14", "Hooks, Uppercuts & 3-Punch Flow", 8..14),
                    Triple("PHASE 3 • DAYS 15–21", "Head Movement, Slips & Counters", 15..21),
                    Triple("PHASE 4 • DAYS 22–30", "Ring Footwork & Fight Conditioning", 22..30)
                )

                phases.forEachIndexed { idx, (tag, title, dayRange) ->
                    val phaseMissions = allMissions.filter { it.day in dayRange }
                    val doneInPhase = phaseMissions.count { it.day in completedDaysSet }
                    val isExpanded = expandedWeekIndex == idx

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(KynaroCardBg)
                            .border(
                                1.dp,
                                if (isExpanded) KynaroOrange.copy(alpha = 0.45f) else KynaroCardBorder,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { expandedWeekIndex = if (isExpanded) -1 else idx }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tag,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.1.sp,
                                    color = KynaroOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KynaroTextWhite
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$doneInPhase/${phaseMissions.size}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (doneInPhase == phaseMissions.size) KynaroGreen else KynaroTextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Phase",
                                    tint = KynaroTextSecondary
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                phaseMissions.forEach { mission ->
                                    val isDone = mission.day in completedDaysSet
                                    val isCurrent = mission.day == stats.currentCampDay
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KynaroCardElevated)
                                            .border(
                                                1.dp,
                                                if (isCurrent) KynaroOrange else KynaroCardBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onStartMission(mission) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isDone -> KynaroGreen
                                                        isCurrent -> KynaroOrange
                                                        else -> KynaroCardBg
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDone) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Done",
                                                    tint = KynaroBlack,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "${mission.day}",
                                                    fontFamily = SpaceGroteskFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isCurrent) KynaroBlack else KynaroTextSecondary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mission.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = KynaroTextWhite
                                            )
                                            Text(
                                                text = "${mission.durationMinutes} min • ${mission.roundsCount} rounds",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KynaroTextMuted
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Start",
                                            tint = if (isCurrent) KynaroOrange else KynaroTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStudioCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(KynaroCardBg)
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = KynaroTextWhite
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = KynaroTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =========================================================================================
// TAB 2: COACH LAB — Interactive 360° Boxing Academy (Where Users Actually Learn Boxing!)
// =========================================================================================
@Composable
private fun CoachLabTab(
    isSouthpaw: Boolean,
    onToggleStance: () -> Unit,
    onLaunchTechniqueDrill: (BoxingTechnique) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(BoxingCategory.PUNCH) }
    val filteredTechniques = remember(selectedCategory) {
        BoxingTechniqueRepository.allTechniques.filter { it.category == selectedCategory }
    }
    var selectedTechId by rememberSaveable { mutableStateOf(BoxingTechniqueRepository.allTechniques.first().id) }
    val activeTech = remember(selectedTechId, filteredTechniques) {
        filteredTechniques.firstOrNull { it.id == selectedTechId } ?: filteredTechniques.first()
    }

    var cameraYawTarget by remember { mutableFloatStateOf(-22f) }
    var activeAngleLabel by remember { mutableStateOf("3/4") }
    var isSlowMo by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showVectors by remember { mutableStateOf(true) }
    var detailSubTab by remember { mutableIntStateOf(0) } // 0 = Form Cues, 1 = Fix Mistakes
    var livePhaseLabel by remember { mutableStateOf("1. STANCE & GUARD") }
    var livePhaseIdx by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "360° BOXING ACADEMY",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroOrange
                    )
                    Text(
                        text = "COACH LAB",
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 32.sp,
                        color = KynaroTextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroOrange.copy(alpha = 0.5f), CircleShape)
                        .clickable { onToggleStance() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (isSouthpaw) "SOUTHPAW" else "ORTHODOX",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = KynaroOrange
                    )
                }
            }
        }

        // Category Switcher (Punches 1-6 | Defense | Footwork)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoxingCategory.entries.forEach { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) KynaroOrange else KynaroCardBg)
                            .border(
                                1.dp,
                                if (isSel) KynaroOrange else KynaroCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedCategory = cat
                                val firstInCat = BoxingTechniqueRepository.allTechniques.first { it.category == cat }
                                selectedTechId = firstInCat.id
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (cat) {
                                BoxingCategory.PUNCH -> "PUNCHES 1–6"
                                BoxingCategory.DEFENSE -> "DEFENSE"
                                BoxingCategory.FOOTWORK -> "FOOTWORK"
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSel) KynaroBlack else KynaroTextSecondary
                        )
                    }
                }
            }
        }

        // Technique Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTechniques, key = { it.id }) { tech ->
                    val isSel = tech.id == activeTech.id
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSel) Color(0xFF22160A) else KynaroCardBg)
                            .border(
                                1.dp,
                                if (isSel) KynaroOrange else KynaroCardBorder,
                                CircleShape
                            )
                            .clickable { selectedTechId = tech.id }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${tech.canonicalNumber}",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = KynaroOrange
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tech.name,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSel) KynaroTextWhite else KynaroTextSecondary
                        )
                    }
                }
            }
        }

        // Interactive 360° 3D Coach Stage + Camera Angle Presets + Controls
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                    .padding(14.dp)
            ) {
                // Live Action Phase Indicator Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) KynaroYellow else KynaroGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = livePhaseLabel,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = KynaroTextWhite
                        )
                    }

                    // 4-step live phase dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0..3) {
                            Box(
                                modifier = Modifier
                                    .width(if (i == livePhaseIdx) 16.dp else 6.dp)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(if (i == livePhaseIdx) KynaroOrange else KynaroCardBorder)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1F1711), KynaroBlack)
                            )
                        )
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(18.dp))
                ) {
                    CoachCharacter3DView(
                        pose = activeTech.pose,
                        speed = if (isSlowMo) WorkoutSpeed.SLOW else WorkoutSpeed.NORMAL,
                        isPaused = isPaused,
                        isSouthpaw = isSouthpaw,
                        externalYawDeg = cameraYawTarget,
                        showAnatomicalHighlights = showVectors,
                        showFloorRing = true,
                        allowCameraOrbit = true,
                        onPhaseChanged = { label, idx ->
                            livePhaseLabel = label
                            livePhaseIdx = idx
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Quick 360° Camera Angle Presets (Front / 3/4 / Side / Back)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .clip(CircleShape)
                            .background(KynaroBlack.copy(alpha = 0.85f))
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val presets = listOf(
                            "FRONT" to 0f,
                            "3/4" to -28f,
                            "SIDE" to 90f,
                            "BACK" to 180f
                        )
                        presets.forEach { (label, deg) ->
                            val isSel = activeAngleLabel == label
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSel) KynaroOrange else Color.Transparent)
                                    .clickable {
                                        activeAngleLabel = label
                                        cameraYawTarget = deg
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSel) KynaroBlack else KynaroTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Control Strip: Play/Pause, 0.5x Slow-Mo, Kinetic Vectors Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabControlChip(
                        icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (isPaused) "PLAY" else "PAUSE",
                        active = isPaused,
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.weight(1f)
                    )
                    LabControlChip(
                        icon = Icons.Default.SlowMotionVideo,
                        label = if (isSlowMo) "0.5x ON" else "0.5x SLOW",
                        active = isSlowMo,
                        onClick = { isSlowMo = !isSlowMo },
                        modifier = Modifier.weight(1f)
                    )
                    LabControlChip(
                        icon = Icons.Default.Visibility,
                        label = if (showVectors) "VECTORS" else "CLEAN",
                        active = showVectors,
                        onClick = { showVectors = !showVectors },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Technique Breakdown Card (Form Cues vs Fix Mistakes)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTech.name.uppercase(),
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = KynaroTextWhite
                        )
                        Text(
                            text = activeTech.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = KynaroTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-tabs: FORM CUES | FIX MISTAKES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KynaroCardElevated)
                        .padding(4.dp)
                ) {
                    listOf("FORM CUES", "FIX MISTAKES").forEachIndexed { idx, label ->
                        val isSel = detailSubTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSel) KynaroCardBg else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSel) KynaroOrange.copy(alpha = 0.45f) else Color.Transparent,
                                    RoundedCornerShape(9.dp)
                                )
                                .clickable { detailSubTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSel) KynaroOrange else KynaroTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (detailSubTab == 0) {
                    // Kinetic Chain Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A120A))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "KINETIC CHAIN",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = KynaroOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeTech.kineticChainSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = KynaroTextWhite,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    activeTech.checkpoints.forEach { cp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KynaroGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = cp.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KynaroTextWhite
                                )
                                Text(
                                    text = cp.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KynaroTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    // Amateur Flaw vs Pro Correction
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1D110E))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COMMON MISTAKE",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeTech.amateurFlaw,
                                style = MaterialTheme.typography.bodySmall,
                                color = KynaroTextWhite,
                                lineHeight = 18.sp
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A1F17))
                                .border(1.dp, KynaroGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = KynaroGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PRO FIX",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = KynaroGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeTech.proFix,
                                style = MaterialTheme.typography.bodySmall,
                                color = KynaroTextWhite,
                                lineHeight = 18.sp
                            )
                        }

                        Text(
                            text = "When to use: ${activeTech.tacticalUsage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KynaroTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                KynaroPrimaryButton(
                    text = "PRACTICE ${activeTech.name.uppercase()}",
                    onClick = { onLaunchTechniqueDrill(activeTech) },
                    testTag = "coach_lab_practice_button"
                )
            }
        }
    }
}

@Composable
private fun LabControlChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) KynaroOrange.copy(alpha = 0.18f) else KynaroCardElevated)
            .border(
                1.dp,
                if (active) KynaroOrange else KynaroCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) KynaroOrange else KynaroTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (active) KynaroOrange else KynaroTextSecondary
        )
    }
}

// =========================================================================================
// TAB 3: DRILLS — Guided Drills & Heavy Bag Round Timer
// =========================================================================================
@Composable
private fun DrillsTab(
    drills: List<BoxingDrill>,
    onStartDrill: (BoxingDrill) -> Unit,
    onOpenHeavyBagTimer: () -> Unit,
    onOpenCoachLab: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredDrills = remember(selectedFilter, drills) {
        when (selectedFilter) {
            "PUNCHES" -> drills.filter { it.pose == CoachPose.JAB || it.pose == CoachPose.CROSS || it.pose == CoachPose.UPPERCUT }
            "COMBOS" -> drills.filter { it.pose == CoachPose.ONE_TWO_COMBO || it.pose == CoachPose.JAB_CROSS_HOOK || it.pose == CoachPose.SPEED_HANDS }
            "DEFENSE" -> drills.filter { it.pose == CoachPose.SLIP_DEFENSE || it.pose == CoachPose.IDLE_GUARD }
            "FOOTWORK" -> drills.filter { it.pose == CoachPose.FOOTWORK }
            else -> drills
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "GUIDED BOXING SESSIONS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    color = KynaroOrange
                )
                Text(
                    text = "DRILLS & ROUNDS",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 32.sp,
                    color = KynaroTextWhite
                )
            }
        }

        // Pro Heavy Bag & Shadowboxing Round Timer Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1F150B), KynaroCardBg))
                    )
                    .border(1.dp, KynaroOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onOpenHeavyBagTimer() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(KynaroOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = KynaroBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HEAVY BAG & SHADOW TIMER",
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = "Live 3D Coach combo callouts, gym bells & 10s warnings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Timer",
                    tint = KynaroOrange
                )
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "PUNCHES", "COMBOS", "DEFENSE", "FOOTWORK").forEach { chip ->
                    val isSel = chip == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSel) KynaroOrange else KynaroCardBg)
                            .border(
                                1.dp,
                                if (isSel) KynaroOrange else KynaroCardBorder,
                                CircleShape
                            )
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = chip,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSel) KynaroBlack else KynaroTextSecondary
                        )
                    }
                }
            }
        }

        items(filteredDrills, key = { it.id }) { drill ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                    .clickable { onStartDrill(drill) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoachDrillThumbnail(pose = drill.pose, size = 94.dp)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drill.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KynaroTextWhite
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = drill.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(KynaroCardElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${drill.durationMinutes} MIN • ${drill.level.uppercase()}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = KynaroTextSecondary
                            )
                        }
                        Text(
                            text = "START ›",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KynaroOrange
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================================
// TAB 4: PROGRESS — Real Fighter Stats, Settings & Workout Log (Zero Fake Data)
// =========================================================================================
@Composable
private fun ProgressProfileTab(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    activePlan: KynaroSubscriptionPlan,
    workoutSessions: List<WorkoutSessionEntity>,
    completedCampDays: List<CompletedCampDayEntity>,
    onOpenPaywall: () -> Unit,
    onRetakeOnboarding: () -> Unit,
    onUpdateAnswers: (OnboardingAnswers) -> Unit
) {
    val context = LocalContext.current
    val totalPunches = remember(workoutSessions) { workoutSessions.sumOf { it.punchesLanded } }
    val totalMinutes = remember(workoutSessions) { workoutSessions.sumOf { it.durationSeconds } / 60 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FIGHTER LOG",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroOrange
                    )
                    Text(
                        text = answers.fighterName.ifBlank { "FIGHTER" }.uppercase(),
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = KynaroTextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, CircleShape)
                        .clickable { onRetakeOnboarding() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("retake_onboarding_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Edit Setup",
                            tint = KynaroOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Setup",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = KynaroOrange
                        )
                    }
                }
            }
        }

        // Real Stats Grid (Only real completed data)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RealStatBox("SESSIONS", "${workoutSessions.size}", Modifier.weight(1f))
                RealStatBox("PUNCHES", "$totalPunches", Modifier.weight(1f))
                RealStatBox("MINUTES", "$totalMinutes", Modifier.weight(1f))
                RealStatBox("CAMP", "${completedCampDays.size}/30", Modifier.weight(1f))
            }
        }

        // KYNARO PRO Membership & Subscription Management Card (Google Play Compliant)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1C1309), KynaroCardBg)))
                    .border(1.dp, KynaroOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KYNARO PRO MEMBERSHIP",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = KynaroOrange
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(KynaroGreen.copy(alpha = 0.16f))
                            .border(1.dp, KynaroGreen.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (activePlan.hasFreeTrial) "7-DAY TRIAL ACTIVE" else "PRO ACTIVE",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = KynaroGreen
                        )
                    }
                }

                ProfileDetailRow("Current Plan", "${activePlan.title} (${activePlan.billedPriceText})")
                ProfileDetailRow("Billing", "Google Play Subscriptions")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KynaroCardElevated)
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenPaywall() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CHANGE PLAN",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KynaroTextWhite
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KynaroOrange.copy(alpha = 0.15f))
                            .border(1.dp, KynaroOrange.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .clickable {
                                val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=com.kynaro")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MANAGE / CANCEL",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KynaroOrange
                        )
                    }
                }
            }
        }

        // Personalized Settings Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TRAINING PROFILE",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = KynaroOrange
                )
                ProfileDetailRow("Goal", answers.ringGoal)
                ProfileDetailRow("Experience", answers.boxedBefore)
                ProfileDetailRow("Schedule", "${answers.daysPerWeek} • ${answers.sessionDuration}")
                ProfileDetailRow("Stance", if (answers.strongHand == "Left-handed") "Southpaw" else "Orthodox")
                ProfileDetailRow("Coach Persona", answers.coachStyle)
                ProfileDetailRow("Gear", answers.gear.joinToString(", "))
            }
        }

        item {
            Text(
                text = "COMPLETED SESSIONS (${workoutSessions.size})",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = KynaroTextSecondary
            )
        }

        if (workoutSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No completed sessions yet. Finish your first coached round to start tracking your punches and time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KynaroTextSecondary
                    )
                }
            }
        } else {
            items(workoutSessions, key = { it.id }) { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KynaroTextWhite
                        )
                        Text(
                            text = "${session.punchesLanded} punches • ${session.roundsCompleted} rounds • ${session.kcalBurned} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = KynaroTextSecondary
                        )
                    }
                    Text(
                        text = "${(session.durationSeconds / 60).coerceAtLeast(1)}m",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = KynaroOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun RealStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KynaroCardBg)
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = KynaroTextWhite
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            color = KynaroTextMuted
        )
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = KynaroTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = KynaroTextWhite
        )
    }
}

// =========================================================================================
// FLOATING BOTTOM NAV BAR (4 Clean Tabs + Central Orange Round Timer Button)
// =========================================================================================
@Composable
private fun KynaroFloatingBottomNav(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit,
    onCenterGloveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF12141C).copy(alpha = 0.96f))
                .border(1.dp, KynaroCardBorder, CircleShape)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                tab = MainNavTab.TRAIN,
                icon = Icons.Default.Home,
                selected = selectedTab == MainNavTab.TRAIN,
                onClick = { onSelectTab(MainNavTab.TRAIN) }
            )
            BottomNavItem(
                tab = MainNavTab.COACH_LAB,
                icon = Icons.Default.AccessibilityNew,
                selected = selectedTab == MainNavTab.COACH_LAB,
                onClick = { onSelectTab(MainNavTab.COACH_LAB) }
            )

            Spacer(modifier = Modifier.width(62.dp))

            BottomNavItem(
                tab = MainNavTab.DRILLS,
                icon = Icons.Default.FitnessCenter,
                selected = selectedTab == MainNavTab.DRILLS,
                onClick = { onSelectTab(MainNavTab.DRILLS) }
            )
            BottomNavItem(
                tab = MainNavTab.PROGRESS,
                icon = Icons.Default.PersonOutline,
                selected = selectedTab == MainNavTab.PROGRESS,
                onClick = { onSelectTab(MainNavTab.PROGRESS) }
            )
        }

        // Elevated Center Boxing Glove Timer Button
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(colors = listOf(KynaroOrange, KynaroOrangeDark))
                )
                .border(3.dp, KynaroBlack, CircleShape)
                .clickable { onCenterGloveClick() }
                .testTag("bottom_nav_quick_workout_glove"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsMma,
                contentDescription = "Round Timer",
                tint = KynaroBlack,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: MainNavTab,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("nav_tab_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.label,
            tint = if (selected) KynaroOrange else KynaroTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp,
            color = if (selected) KynaroOrange else KynaroTextMuted
        )
    }
}
