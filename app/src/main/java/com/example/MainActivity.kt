package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.KynaroCurriculum
import com.example.ui.AppScreenDestination
import com.example.ui.KynaroViewModel
import com.example.ui.screens.MainTrainingScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.PreparationFlowScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MyApplicationTheme {
                KynaroAppRoot()
            }
        }
    }
}

@Composable
fun KynaroAppRoot(viewModel: KynaroViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val answers by viewModel.onboardingAnswers.collectAsStateWithLifecycle()
    val stats by viewModel.fighterStats.collectAsStateWithLifecycle()
    val workoutSessions by viewModel.workoutSessions.collectAsStateWithLifecycle()
    val completedDays by viewModel.completedCampDays.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(tween(260)).togetherWith(fadeOut(tween(260)))
        },
        label = "root_screen_navigation"
    ) { screen ->
        when (screen) {
            AppScreenDestination.WELCOME -> {
                WelcomeScreen(
                    onStartOnboarding = {
                        viewModel.navigateTo(AppScreenDestination.ONBOARDING)
                    },
                    onQuickSignIn = {
                        viewModel.quickSignInToCamp()
                    }
                )
            }
            AppScreenDestination.ONBOARDING -> {
                OnboardingScreen(
                    answers = answers,
                    onUpdateAnswers = { viewModel.updateOnboardingAnswers(it) },
                    onBackToWelcome = { viewModel.navigateTo(AppScreenDestination.WELCOME) },
                    onFinishOnboardingQuestionnaire = {
                        viewModel.navigateTo(AppScreenDestination.PREPARATION)
                    }
                )
            }
            AppScreenDestination.PREPARATION -> {
                PreparationFlowScreen(
                    answers = answers,
                    onUpdateAnswers = { viewModel.updateOnboardingAnswers(it) },
                    onBackToOnboarding = { viewModel.navigateTo(AppScreenDestination.ONBOARDING) },
                    onCompleteCampSetup = { finalAnswers ->
                        viewModel.completePreparationToPaywall(finalAnswers)
                    }
                )
            }
            AppScreenDestination.PAYWALL -> {
                PaywallScreen(
                    answers = answers,
                    onBack = { viewModel.navigateTo(AppScreenDestination.PREPARATION) },
                    onStartSubscription = { chosenPlan ->
                        viewModel.selectSubscriptionPlan(chosenPlan)
                        viewModel.completePreparationAndUnlockCamp(answers.copy(onboardingCompleted = true, campUnlocked = true))
                    },
                    onRestorePurchases = {
                        viewModel.completePreparationAndUnlockCamp(answers.copy(onboardingCompleted = true, campUnlocked = true))
                    }
                )
            }
            AppScreenDestination.MAIN_TRAINING -> {
                MainTrainingScreen(
                    answers = answers,
                    stats = stats,
                    workoutSessions = workoutSessions,
                    completedCampDays = completedDays,
                    onStartMission = { mission -> viewModel.startMissionWorkout(mission) },
                    onStartDrill = { drill -> viewModel.startDrillWorkout(drill) },
                    activePlan = activePlan,
                    onOpenPaywall = { viewModel.navigateTo(AppScreenDestination.PAYWALL) },
                    onRetakeOnboarding = { viewModel.navigateTo(AppScreenDestination.ONBOARDING) },
                    onUpdateAnswers = { viewModel.updateOnboardingAnswers(it) }
                )
            }
            AppScreenDestination.WORKOUT -> {
                val defaultMission = KynaroCurriculum.get30DayCampMissions(answers).first()
                val spec = activeWorkout
                WorkoutScreen(
                    workoutTitle = spec?.title ?: defaultMission.title,
                    missionDay = spec?.missionDay ?: stats.currentCampDay,
                    steps = spec?.steps ?: defaultMission.steps,
                    answers = answers,
                    onCompleteWorkout = { duration, kcal, punches, rounds, xp ->
                        viewModel.completeActiveWorkout(duration, kcal, punches, rounds, xp)
                    },
                    onExitWorkout = {
                        viewModel.navigateTo(AppScreenDestination.MAIN_TRAINING)
                    }
                )
            }
        }
    }
}
