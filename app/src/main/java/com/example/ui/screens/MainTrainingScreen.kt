package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompletedCampDayEntity
import com.example.data.KynaroRepository
import com.example.data.WorkoutSessionEntity
import com.example.model.BoxingDrill
import com.example.model.CampMission
import com.example.model.CoachPose
import com.example.model.KynaroCurriculum
import com.example.model.OnboardingAnswers
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.CoachDrillThumbnail
import com.example.ui.components.CoachTransformationDualView
import com.example.ui.components.KynaroBrandMark
import com.example.ui.components.KynaroPillBadge
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.components.KynaroScreenBackground
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroCardElevated
import com.example.ui.theme.KynaroDeepBurgundy
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MainNavTab(val label: String) {
    HOME("Home"),
    LEAGUE("League"),
    DRILLS("Drills"),
    PROFILE("Profile")
}

@Composable
fun MainTrainingScreen(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    workoutSessions: List<WorkoutSessionEntity>,
    completedCampDays: List<CompletedCampDayEntity>,
    onStartMission: (CampMission) -> Unit,
    onStartDrill: (BoxingDrill) -> Unit,
    onRetakeOnboarding: () -> Unit,
    onUpdateAnswers: (OnboardingAnswers) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainNavTab.HOME) }
    val allMissions = remember(answers) { KynaroCurriculum.get30DayCampMissions(answers) }
    val allDrills = remember { KynaroCurriculum.getPracticeDrills() }
    val activeDay = stats.currentCampDay.coerceIn(1, 30)
    val todaysMission = allMissions.getOrElse(activeDay - 1) { allMissions.first() }
    var showCoachTipDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedTab != MainNavTab.HOME) {
        selectedTab = MainNavTab.HOME
    }

    if (showCoachTipDialog) {
        val styleQuote = when (answers.coachStyle) {
            "The Drill Sergeant" -> "No excuses today, ${answers.fighterName}. Chin down, hands up, and give me 100% on every round!"
            "The Hype Man" -> "LET'S GO ${answers.fighterName.uppercase()}! Day $activeDay is YOURS! Big combos, fast hands, huge energy!"
            else -> "Precision beats power, ${answers.fighterName}. Focus on full hip rotation and clean guard return today."
        }
        AlertDialog(
            onDismissRequest = { showCoachTipDialog = false },
            containerColor = KynaroCardBg,
            title = {
                Text(
                    text = answers.coachStyle.uppercase(),
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = KynaroYellow
                )
            },
            text = {
                Text(
                    text = styleQuote,
                    style = MaterialTheme.typography.bodyLarge,
                    color = KynaroTextWhite
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCoachTipDialog = false
                    onStartMission(todaysMission)
                }) {
                    Text("START MISSION", color = KynaroRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoachTipDialog = false }) {
                    Text("CLOSE", color = KynaroTextSecondary)
                }
            }
        )
    }

    var showTechniqueVaultSheet by rememberSaveable { mutableStateOf(false) }
    var showHeavyBagTimerScreen by rememberSaveable { mutableStateOf(false) }

    KynaroScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 86.dp)
            ) {
                when (selectedTab) {
                    MainNavTab.HOME -> HomeTabContent(
                        answers = answers,
                        stats = stats,
                        todaysMission = todaysMission,
                        allMissions = allMissions,
                        workoutSessions = workoutSessions,
                        completedDaysSet = completedCampDays.map { it.dayNumber }.toSet(),
                        onStartMission = onStartMission,
                        onOpenCoachNote = { showCoachTipDialog = true },
                        onOpenDrillsTab = { selectedTab = MainNavTab.DRILLS }
                    )
                    MainNavTab.DRILLS -> DrillsTabContent(
                        drills = allDrills,
                        onStartDrill = onStartDrill,
                        onOpenTechniqueVault = { showTechniqueVaultSheet = true },
                        onOpenHeavyBagTimer = { showHeavyBagTimerScreen = true }
                    )
                    MainNavTab.LEAGUE -> LeagueTabContent(
                        answers = answers,
                        stats = stats,
                        workoutSessions = workoutSessions,
                        onStartQuickRound = { onStartMission(todaysMission) }
                    )
                    MainNavTab.PROFILE -> ProfileTabContent(
                        answers = answers,
                        stats = stats,
                        workoutSessions = workoutSessions,
                        onRetakeOnboarding = onRetakeOnboarding,
                        onUpdateAnswers = onUpdateAnswers
                    )
                }
            }

            // Floating Dark Bottom Navigation Bar with Central Red Boxing Glove Button (matching unnamed__2__99fbef1b.webp)
            KynaroFloatingBottomNav(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onCenterGloveClick = { showHeavyBagTimerScreen = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )

            // Interactive 3D Biomechanics Masterclass Sheet
            if (showTechniqueVaultSheet) {
                TechniqueVaultSheet(
                    onDismiss = { showTechniqueVaultSheet = false },
                    onLaunchTechniqueDrill = { tech ->
                        showTechniqueVaultSheet = false
                        val matched = allDrills.firstOrNull { it.pose == tech.pose } ?: allDrills.first()
                        onStartDrill(matched)
                    }
                )
            }

            // Heavy Bag & Shadowboxing Pro Round Timer Fullscreen HUD
            if (showHeavyBagTimerScreen) {
                HeavyBagRoundTimerScreen(
                    onClose = { showHeavyBagTimerScreen = false }
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    todaysMission: CampMission,
    allMissions: List<CampMission>,
    workoutSessions: List<WorkoutSessionEntity>,
    completedDaysSet: Set<Int>,
    onStartMission: (CampMission) -> Unit,
    onOpenCoachNote: () -> Unit,
    onOpenDrillsTab: () -> Unit
) {
    var campDaysExpanded by rememberSaveable { mutableStateOf(false) }
    var showTransformationModal by rememberSaveable { mutableStateOf(false) }

    val (timeGreetingWithDate, todayWeekdayIndex) = remember {
        val cal = Calendar.getInstance(java.util.TimeZone.getDefault(), Locale.getDefault())
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greet = when {
            hour < 12 -> "GOOD MORNING"
            hour < 17 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
        val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(cal.time).uppercase()
        val mondayZeroIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 6
        }
        "$greet • $dateStr" to mondayZeroIdx
    }

    val completedWeekdaysThisWeek = remember(workoutSessions) {
        val nowCal = Calendar.getInstance()
        val currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = nowCal.get(Calendar.YEAR)
        workoutSessions.mapNotNull { session ->
            val sCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
            if (sCal.get(Calendar.YEAR) == currentYear &&
                sCal.get(Calendar.WEEK_OF_YEAR) == currentWeek
            ) {
                when (sCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    else -> 6
                }
            } else null
        }.toSet()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Official KYNARO Brand Header + Real-Time Greeting
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KynaroBrandMark(iconSize = 30.dp)
                    KynaroPillBadge(text = "PRO CAMP • DAY ${stats.currentCampDay}/30")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeGreetingWithDate,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = KynaroTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = answers.fighterName.ifBlank { "Champion" },
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KynaroTextWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KynaroCardBg)
                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(14.dp))
                            .clickable { onOpenCoachNote() }
                            .testTag("home_notification_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Coach Message",
                            tint = KynaroRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. TODAY'S MISSION Hero Card with 3D Boxing Coach
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF161307),
                                KynaroCardBg,
                                Color(0xFF1A1506)
                            )
                        )
                    )
                    .border(1.dp, KynaroRed.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                    ) {
                        // Left Column: TODAY'S MISSION badge + Mission title
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.58f)
                                .align(Alignment.CenterStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C1707))
                                    .border(1.dp, KynaroRed.copy(alpha = 0.55f), CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "TODAY'S MISSION • DAY ${todaysMission.day}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = KynaroRed
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = todaysMission.title.uppercase(),
                                fontFamily = OswaldFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                fontSize = 26.sp,
                                lineHeight = 29.sp,
                                maxLines = 2,
                                color = KynaroTextWhite
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${todaysMission.durationMinutes} min • ${answers.ringGoal}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KynaroTextSecondary
                            )
                        }

                        // Right Side: Prominent 3D Boxing Coach in Guard Stance
                        Box(
                            modifier = Modifier
                                .width(175.dp)
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                        ) {
                            CoachCharacter3DView(
                                pose = todaysMission.steps.firstOrNull()?.pose ?: CoachPose.IDLE_GUARD,
                                showFloorRing = false,
                                allowCameraOrbit = false,
                                cropUpperBody = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Championship Gold START MISSION Button
                    KynaroPrimaryButton(
                        text = "START MISSION",
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
        }

        // 3. STREAK / LEVEL / XP Card + Level Progress Bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // STREAK
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = KynaroRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "STREAK",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = KynaroTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.streakDays}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = KynaroTextWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KynaroTextMuted,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(52.dp)
                            .background(KynaroCardBorder)
                    )

                    // LEVEL
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Level",
                            tint = KynaroYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LEVEL",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = KynaroTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.level}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = KynaroTextWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(52.dp)
                            .background(KynaroCardBorder)
                    )

                    // XP
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "XP",
                            tint = KynaroYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "XP",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = KynaroTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.formattedXp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = KynaroYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Level XP Progress Bar matching unnamed__2__99fbef1b.webp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F212A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stats.levelProgressFraction)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(KynaroYellow)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${stats.xpToNextLevel} to Level ${stats.level + 1}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = KynaroTextSecondary
                    )
                }
            }
        }

        // 4. FIGHT CAMP Card (DAY 18 of 30, 57%, M T W T F S S + expandable 30-day mission selector)
        item {
            val campPercent = ((stats.currentCampDay / 30f) * 100).toInt().coerceIn(3, 100)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FIGHT CAMP",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroTextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KynaroCardElevated)
                            .border(1.dp, KynaroCardBorder, CircleShape)
                            .clickable { campDaysExpanded = !campDaysExpanded }
                            .testTag("toggle_camp_days_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (campDaysExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Fight Camp",
                            tint = KynaroTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${completedWeekdaysThisWeek.size} / 7 this week",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = KynaroTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "DAY ${stats.currentCampDay}",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 26.sp,
                            color = KynaroTextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "of 30",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KynaroTextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Text(
                        text = "$campPercent%",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KynaroRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF20222C))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(campPercent / 100f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(KynaroRed)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Weekly M T W T F S S Row based on actual completed sessions & current system day
                val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEachIndexed { idx, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (idx == todayWeekdayIndex) KynaroRed else KynaroTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            when {
                                idx in completedWeekdaysThisWeek -> {
                                    // Completed Championship Gold Check Box
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(KynaroRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = KynaroBlack,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                idx == todayWeekdayIndex -> {
                                    // Today Dashed Gold Circle Target
                                    Box(
                                        modifier = Modifier.size(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(
                                                color = KynaroRed,
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                                )
                                            )
                                            drawCircle(
                                                color = KynaroRed,
                                                radius = 3.dp.toPx()
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    // Upcoming Dark Box
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(KynaroCardElevated)
                                            .border(1.dp, KynaroCardBorder, RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable 30-Day Camp Mission Selector
                AnimatedVisibility(
                    visible = campDaysExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SELECT ANY CAMP MISSION (DAYS 1–30)",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KynaroYellow
                        )
                        allMissions.forEach { mission ->
                            val isDone = mission.day in completedDaysSet
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(KynaroCardElevated)
                                    .border(
                                        1.dp,
                                        if (mission.day == stats.currentCampDay) KynaroRed else KynaroCardBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onStartMission(mission) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAY ${mission.day}",
                                    fontFamily = OswaldFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDone) KynaroGreen else KynaroRed,
                                    modifier = Modifier.width(56.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = KynaroTextWhite
                                    )
                                    Text(
                                        text = mission.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = KynaroTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Mission",
                                    tint = KynaroRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. GALLERY / TRANSFORMATION & QUICK DRILLS Card matching bottom of unnamed__2__99fbef1b.webp
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                    .clickable { showTransformationModal = !showTransformationModal }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GALLERY & TRANSFORMATION",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroTextSecondary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Expand",
                        tint = KynaroTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CoachTransformationDualView(isSilhouetteMode = false)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goal: ${answers.ringGoal} • ${answers.strongHand}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KynaroTextSecondary
                    )
                    Text(
                        text = "Open Drills ›",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = KynaroRed,
                        modifier = Modifier.clickable { onOpenDrillsTab() }
                    )
                }
            }
        }
    }
}

/**
 * DRILLS Tab: Recreates unnamed__3__3dd5267c.webp ("PRACTICE DRILLS" with +5% XP badge & 3D coach thumbnails)
 */
@Composable
private fun DrillsTabContent(
    drills: List<BoxingDrill>,
    onStartDrill: (BoxingDrill) -> Unit,
    onOpenTechniqueVault: () -> Unit,
    onOpenHeavyBagTimer: () -> Unit
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
                    text = "KYNARO AI • BOXING MASTERCLASS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp,
                    color = KynaroRed
                )
                Text(
                    text = "SHARPEN EVERY PUNCH",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 36.sp,
                    color = KynaroTextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Guided 3D drills that sharpen form, speed, and kinetic chain torque.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary
                )
            }
        }

        // HERO CARD 1: 3D BIOMECHANICS VAULT (PUNCHES 1-6)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF181407), Color(0xFF100E06))
                        )
                    )
                    .border(1.dp, KynaroRed.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .clickable { onOpenTechniqueVault() }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(KynaroRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsMma,
                        contentDescription = null,
                        tint = KynaroBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "3D BIOMECHANICS VAULT",
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = "Canonical punches 1-6, slips, rolls & 360° form breakdowns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Masterclass",
                    tint = KynaroRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // HERO CARD 2: HEAVY BAG ROUND TIMER & LIVE COMBO CALLER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E170A), Color(0xFF141007))
                        )
                    )
                    .border(1.dp, KynaroYellow.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .clickable { onOpenHeavyBagTimer() }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF332306)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = KynaroYellow,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HEAVY BAG ROUND TIMER",
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = "Championship bells, 10s warnings & live combo callouts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Timer",
                    tint = KynaroYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Section header + filter chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRACTICE DRILLS",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.4.sp,
                        color = KynaroTextSecondary
                    )
                    KynaroPillBadge(text = "⚡ +5% XP")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "PUNCHES", "COMBOS", "DEFENSE", "FOOTWORK").forEach { chip ->
                        val isSel = chip == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) KynaroRed else KynaroCardBg)
                                .border(
                                    1.dp,
                                    if (isSel) KynaroRed else KynaroCardBorder,
                                    CircleShape
                                )
                                .clickable { selectedFilter = chip }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSel) KynaroBlack else KynaroTextSecondary
                            )
                        }
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
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoachDrillThumbnail(
                    pose = drill.pose,
                    size = 104.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drill.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KynaroTextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${drill.durationMinutes} min • ${drill.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KynaroTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(CircleShape)
                            .background(KynaroRed)
                            .clickable { onStartDrill(drill) }
                            .testTag("practice_drill_${drill.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = KynaroBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRACTICE",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.1.sp,
                                color = KynaroBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * LEAGUE Tab: Global Fighter Rankings & Live XP Leaderboard
 */
@Composable
private fun LeagueTabContent(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    workoutSessions: List<WorkoutSessionEntity>,
    onStartQuickRound: () -> Unit
) {
    data class LeagueEntry(val name: String, val xp: Int, val streak: Int, val isUser: Boolean = false)

    val leaderboard = remember(answers.fighterName, stats.totalXp, stats.streakDays) {
        listOf(
            LeagueEntry("Dante 'The Viper' Cruz", 15400, 24),
            LeagueEntry("Elena Volkov", 13900, 21),
            LeagueEntry(answers.fighterName.ifBlank { "You" }, stats.totalXp, stats.streakDays, isUser = true),
            LeagueEntry("Tariq Vance", 11850, 15),
            LeagueEntry("Kai Nakamura", 10900, 12),
            LeagueEntry("Liam O'Connor", 9400, 9)
        ).sortedByDescending { it.xp }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CHAMPIONSHIP DIVISION",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.6.sp,
                    color = KynaroYellow
                )
                Text(
                    text = "FIGHTER LEAGUE",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 36.sp,
                    color = KynaroTextWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Complete daily missions and training rounds to climb the global KYNARO division.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KynaroTextSecondary
                )
            }
        }

        items(leaderboard.size) { index ->
            val entry = leaderboard[index]
            val rank = index + 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (entry.isUser) Color(0xFF1A1608) else KynaroCardBg)
                    .border(
                        width = if (entry.isUser) 1.8.dp else 1.dp,
                        color = if (entry.isUser) KynaroRed else KynaroCardBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$rank",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = if (rank <= 3) KynaroYellow else KynaroTextMuted,
                    modifier = Modifier.width(40.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (entry.isUser) "${entry.name} (YOU)" else entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = "${entry.streak} day streak",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = KynaroTextSecondary
                    )
                }
                Text(
                    text = "${String.format("%.1fk", entry.xp / 1000f)} XP",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KynaroYellow
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            KynaroPrimaryButton(
                text = "EARN +450 XP IN LIVE ROUND",
                onClick = onStartQuickRound,
                testTag = "league_quick_round_button"
            )
        }
    }
}

/**
 * PROFILE Tab: Displays saved onboarding personalization answers, workout history, and option to edit/retake onboarding
 */
@Composable
private fun ProfileTabContent(
    answers: OnboardingAnswers,
    stats: KynaroRepository.FighterStats,
    workoutSessions: List<WorkoutSessionEntity>,
    onRetakeOnboarding: () -> Unit,
    onUpdateAnswers: (OnboardingAnswers) -> Unit
) {
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
                        text = "FIGHTER PROFILE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.6.sp,
                        color = KynaroRed
                    )
                    Text(
                        text = answers.fighterName.ifBlank { "Champion" }.uppercase(),
                        fontFamily = OswaldFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        color = KynaroTextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KynaroCardBg)
                        .border(1.dp, KynaroCardBorder, CircleShape)
                        .clickable { onRetakeOnboarding() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("retake_onboarding_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Edit Camp Setup",
                            tint = KynaroYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Answers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KynaroYellow
                        )
                    }
                }
            }
        }

        // Personalized Onboarding Summary Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(KynaroCardBg)
                    .border(1.dp, KynaroCardBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PERSONALIZED CAMP SETTINGS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.3.sp,
                    color = KynaroYellow
                )
                ProfileDetailRow("Primary Goal", answers.ringGoal)
                ProfileDetailRow("Boxing Experience", answers.boxedBefore)
                ProfileDetailRow("Weekly Schedule", "${answers.daysPerWeek} • ${answers.sessionDuration}")
                ProfileDetailRow("Strong Hand / Stance", answers.strongHand)
                ProfileDetailRow("Coach Persona", answers.coachStyle)
                ProfileDetailRow("Stats", "${answers.ageRange} • ${answers.heightValue} ${answers.heightUnit} • ${answers.weightValue} ${answers.weightUnit}")
                ProfileDetailRow("Fitness Level", "${answers.fitnessLevel} / 10")
                ProfileDetailRow("Available Gear", answers.gear.joinToString(", "))
                ProfileDetailRow("Work Arounds", answers.limitations.joinToString(", "))
                ProfileDetailRow("Training Target", "${answers.trainingFor} (${answers.meaning})")
            }
        }

        // Completed Workout History from Room DB
        item {
            Text(
                text = "RECENT COMPLETED ROUNDS (${workoutSessions.size})",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.3.sp,
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
                        text = "No completed sessions logged yet. Tap the red boxing glove button below to start your first round!",
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
                        .padding(16.dp),
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
                            text = "${session.punchesLanded} punches • ${session.kcalBurned} kcal • ${session.roundsCompleted} rounds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KynaroTextSecondary
                        )
                    }
                    Text(
                        text = "+${session.xpEarned} XP",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = KynaroYellow
                    )
                }
            }
        }
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
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KynaroTextWhite
        )
    }
}

/**
 * Floating Bottom Bar with Central Red Boxing Glove Action Button (matching unnamed__2__99fbef1b.webp & unnamed__3__3dd5267c.webp)
 */
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
        // Dark Rounded Pill Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(CircleShape)
                .background(Color(0xFF15161E).copy(alpha = 0.96f))
                .border(1.dp, KynaroCardBorder, CircleShape)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                tab = MainNavTab.HOME,
                icon = Icons.Default.Home,
                selected = selectedTab == MainNavTab.HOME,
                onClick = { onSelectTab(MainNavTab.HOME) }
            )
            BottomNavItem(
                tab = MainNavTab.LEAGUE,
                icon = Icons.Default.EmojiEvents,
                selected = selectedTab == MainNavTab.LEAGUE,
                onClick = { onSelectTab(MainNavTab.LEAGUE) }
            )

            // Center spacer for floating red glove button
            Spacer(modifier = Modifier.width(64.dp))

            BottomNavItem(
                tab = MainNavTab.DRILLS,
                icon = Icons.Default.FitnessCenter,
                selected = selectedTab == MainNavTab.DRILLS,
                onClick = { onSelectTab(MainNavTab.DRILLS) }
            )
            BottomNavItem(
                tab = MainNavTab.PROFILE,
                icon = Icons.Default.PersonOutline,
                selected = selectedTab == MainNavTab.PROFILE,
                onClick = { onSelectTab(MainNavTab.PROFILE) }
            )
        }

        // Elevated Central Championship Gold Circular Boxing Glove Button
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(KynaroRed, Color(0xFFD9A300))
                    )
                )
                .border(3.dp, KynaroBlack, CircleShape)
                .clickable { onCenterGloveClick() }
                .testTag("bottom_nav_quick_workout_glove"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsMma,
                contentDescription = "Start Boxing Round",
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("nav_tab_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.label,
            tint = if (selected) KynaroRed else KynaroTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (selected) KynaroRed else KynaroTextMuted
        )
    }
}
