package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CompletedCampDayEntity
import com.example.data.KynaroDatabase
import com.example.data.KynaroRepository
import com.example.data.WorkoutSessionEntity
import com.example.model.BoxingDrill
import com.example.model.CampMission
import com.example.model.OnboardingAnswers
import com.example.model.WorkoutExerciseStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreenDestination {
    WELCOME,
    ONBOARDING,
    PREPARATION,
    MAIN_TRAINING,
    WORKOUT
}

data class ActiveWorkoutSpec(
    val title: String,
    val missionDay: Int,
    val steps: List<WorkoutExerciseStep>
)

class KynaroViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KynaroDatabase.getInstance(application)
    private val repository = KynaroRepository(application, database.kynaroDao())

    val onboardingAnswers: StateFlow<OnboardingAnswers> = repository.onboardingAnswersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OnboardingAnswers()
        )

    val fighterStats: StateFlow<KynaroRepository.FighterStats> = repository.fighterStatsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = KynaroRepository.FighterStats()
        )

    val workoutSessions: StateFlow<List<WorkoutSessionEntity>> = repository.workoutSessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedCampDays: StateFlow<List<CompletedCampDayEntity>> = repository.completedCampDaysFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(AppScreenDestination.WELCOME)
    val currentScreen: StateFlow<AppScreenDestination> = _currentScreen.asStateFlow()

    private val _activeWorkout = MutableStateFlow<ActiveWorkoutSpec?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutSpec?> = _activeWorkout.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = repository.onboardingAnswersFlow.first()
            if (saved.onboardingCompleted) {
                _currentScreen.value = AppScreenDestination.MAIN_TRAINING
            }
        }
    }

    fun navigateTo(destination: AppScreenDestination) {
        _currentScreen.value = destination
    }

    fun updateOnboardingAnswers(updated: OnboardingAnswers) {
        viewModelScope.launch {
            repository.saveOnboardingAnswers(updated)
        }
    }

    fun completePreparationAndUnlockCamp(finalAnswers: OnboardingAnswers) {
        viewModelScope.launch {
            repository.completeOnboardingAndUnlockCamp(finalAnswers)
            _currentScreen.value = AppScreenDestination.MAIN_TRAINING
        }
    }

    fun quickSignInToCamp() {
        viewModelScope.launch {
            val current = onboardingAnswers.value
            repository.completeOnboardingAndUnlockCamp(current)
            _currentScreen.value = AppScreenDestination.MAIN_TRAINING
        }
    }

    fun startMissionWorkout(mission: CampMission) {
        _activeWorkout.value = ActiveWorkoutSpec(
            title = mission.title,
            missionDay = mission.day,
            steps = mission.steps
        )
        _currentScreen.value = AppScreenDestination.WORKOUT
    }

    fun startDrillWorkout(drill: BoxingDrill) {
        _activeWorkout.value = ActiveWorkoutSpec(
            title = drill.title,
            missionDay = fighterStats.value.currentCampDay,
            steps = drill.steps
        )
        _currentScreen.value = AppScreenDestination.WORKOUT
    }

    fun completeActiveWorkout(
        durationSeconds: Int,
        kcalBurned: Int,
        punchesLanded: Int,
        roundsCompleted: Int,
        xpEarned: Int
    ) {
        val spec = _activeWorkout.value
        viewModelScope.launch {
            repository.recordCompletedWorkout(
                title = spec?.title ?: "Coached Boxing Round",
                missionDay = spec?.missionDay ?: fighterStats.value.currentCampDay,
                durationSeconds = durationSeconds,
                kcalBurned = kcalBurned,
                punchesLanded = punchesLanded,
                roundsCompleted = roundsCompleted,
                xpEarned = xpEarned
            )
            _currentScreen.value = AppScreenDestination.MAIN_TRAINING
        }
    }
}
