package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.OnboardingAnswers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "kynaro_production_prefs_v2")

class KynaroRepository(
    private val context: Context,
    private val dao: KynaroDao
) {
    private object Keys {
        val RING_GOAL = stringPreferencesKey("ring_goal")
        val BOXED_BEFORE = stringPreferencesKey("boxed_before")
        val DAYS_PER_WEEK = stringPreferencesKey("days_per_week")
        val SESSION_DURATION = stringPreferencesKey("session_duration")
        val AGE_RANGE = stringPreferencesKey("age_range")
        val HEIGHT_VALUE = intPreferencesKey("height_value")
        val HEIGHT_UNIT = stringPreferencesKey("height_unit")
        val WEIGHT_VALUE = intPreferencesKey("weight_value")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val FITNESS_LEVEL = intPreferencesKey("fitness_level")
        val GEAR = stringSetPreferencesKey("gear")
        val LIMITATIONS = stringSetPreferencesKey("limitations")
        val STOPPED_BEFORE = stringPreferencesKey("stopped_before")
        val MEANING = stringPreferencesKey("meaning")
        val TRAINING_FOR = stringPreferencesKey("training_for")
        val STRONG_HAND = stringPreferencesKey("strong_hand")
        val COACH_STYLE = stringPreferencesKey("coach_style")
        val FIGHTER_NAME = stringPreferencesKey("fighter_name")
        val AVATAR_INDEX = intPreferencesKey("avatar_index")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CAMP_UNLOCKED = booleanPreferencesKey("camp_unlocked")

        val STREAK_DAYS = intPreferencesKey("streak_days")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val CURRENT_CAMP_DAY = intPreferencesKey("current_camp_day")
        val TOTAL_PUNCHES = intPreferencesKey("total_punches")
        val LAST_WORKOUT_EPOCH_DAY = longPreferencesKey("last_workout_epoch_day")
    }

    val onboardingAnswersFlow: Flow<OnboardingAnswers> = context.dataStore.data.map { prefs ->
        OnboardingAnswers(
            ringGoal = prefs[Keys.RING_GOAL] ?: "Learn to box",
            boxedBefore = prefs[Keys.BOXED_BEFORE] ?: "Brand new",
            daysPerWeek = prefs[Keys.DAYS_PER_WEEK] ?: "3 days",
            sessionDuration = prefs[Keys.SESSION_DURATION] ?: "20 minutes",
            ageRange = prefs[Keys.AGE_RANGE] ?: "25 to 34",
            heightValue = prefs[Keys.HEIGHT_VALUE] ?: 175,
            heightUnit = prefs[Keys.HEIGHT_UNIT] ?: "CM",
            weightValue = prefs[Keys.WEIGHT_VALUE] ?: 75,
            weightUnit = prefs[Keys.WEIGHT_UNIT] ?: "KG",
            fitnessLevel = prefs[Keys.FITNESS_LEVEL] ?: 7,
            gear = prefs[Keys.GEAR] ?: setOf("Gloves"),
            limitations = prefs[Keys.LIMITATIONS] ?: setOf("No limitations"),
            stoppedBefore = prefs[Keys.STOPPED_BEFORE] ?: "No time",
            meaning = prefs[Keys.MEANING] ?: "Confidence",
            trainingFor = prefs[Keys.TRAINING_FOR] ?: "Fight-ready",
            strongHand = prefs[Keys.STRONG_HAND] ?: "Right-handed",
            coachStyle = prefs[Keys.COACH_STYLE] ?: "The Drill Sergeant",
            fighterName = prefs[Keys.FIGHTER_NAME] ?: "",
            avatarIndex = prefs[Keys.AVATAR_INDEX] ?: 0,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            campUnlocked = prefs[Keys.CAMP_UNLOCKED] ?: false
        )
    }

    /**
     * Real production FighterStats — starts at Day 1, 0 Streak, 0 XP, 0 Punches (Zero fake/example data).
     */
    data class FighterStats(
        val streakDays: Int = 0,
        val totalXp: Int = 0,
        val currentCampDay: Int = 1,
        val totalPunches: Int = 0
    ) {
        val level: Int
            get() = (totalXp / 1000) + 1

        val xpToNextLevel: Int
            get() = 1000 - (totalXp % 1000)

        val levelProgressFraction: Float
            get() = ((totalXp % 1000).toFloat() / 1000f).coerceIn(0.04f, 1f)

        val formattedXp: String
            get() = if (totalXp >= 1000) String.format("%.1fk", totalXp / 1000f) else totalXp.toString()
    }

    val fighterStatsFlow: Flow<FighterStats> = context.dataStore.data.map { prefs ->
        FighterStats(
            streakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            totalXp = prefs[Keys.TOTAL_XP] ?: 0,
            currentCampDay = prefs[Keys.CURRENT_CAMP_DAY] ?: 1,
            totalPunches = prefs[Keys.TOTAL_PUNCHES] ?: 0
        )
    }

    val workoutSessionsFlow: Flow<List<WorkoutSessionEntity>> = dao.getAllWorkoutSessions()
    val completedCampDaysFlow: Flow<List<CompletedCampDayEntity>> = dao.getCompletedCampDays()

    suspend fun saveOnboardingAnswers(answers: OnboardingAnswers) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RING_GOAL] = answers.ringGoal
            prefs[Keys.BOXED_BEFORE] = answers.boxedBefore
            prefs[Keys.DAYS_PER_WEEK] = answers.daysPerWeek
            prefs[Keys.SESSION_DURATION] = answers.sessionDuration
            prefs[Keys.AGE_RANGE] = answers.ageRange
            prefs[Keys.HEIGHT_VALUE] = answers.heightValue
            prefs[Keys.HEIGHT_UNIT] = answers.heightUnit
            prefs[Keys.WEIGHT_VALUE] = answers.weightValue
            prefs[Keys.WEIGHT_UNIT] = answers.weightUnit
            prefs[Keys.FITNESS_LEVEL] = answers.fitnessLevel
            prefs[Keys.GEAR] = answers.gear
            prefs[Keys.LIMITATIONS] = answers.limitations
            prefs[Keys.STOPPED_BEFORE] = answers.stoppedBefore
            prefs[Keys.MEANING] = answers.meaning
            prefs[Keys.TRAINING_FOR] = answers.trainingFor
            prefs[Keys.STRONG_HAND] = answers.strongHand
            prefs[Keys.COACH_STYLE] = answers.coachStyle
            prefs[Keys.FIGHTER_NAME] = answers.fighterName.trim()
            prefs[Keys.AVATAR_INDEX] = answers.avatarIndex
            prefs[Keys.ONBOARDING_COMPLETED] = answers.onboardingCompleted
            prefs[Keys.CAMP_UNLOCKED] = answers.campUnlocked
        }
    }

    suspend fun completeOnboardingAndUnlockCamp(answers: OnboardingAnswers) {
        saveOnboardingAnswers(
            answers.copy(
                onboardingCompleted = true,
                campUnlocked = true
            )
        )
    }

    suspend fun recordCompletedWorkout(
        title: String,
        missionDay: Int,
        durationSeconds: Int,
        kcalBurned: Int,
        punchesLanded: Int,
        roundsCompleted: Int,
        xpEarned: Int
    ) {
        val nowMs = System.currentTimeMillis()
        dao.insertWorkoutSession(
            WorkoutSessionEntity(
                title = title,
                missionDay = missionDay,
                durationSeconds = durationSeconds,
                kcalBurned = kcalBurned,
                punchesLanded = punchesLanded,
                roundsCompleted = roundsCompleted,
                xpEarned = xpEarned,
                timestamp = nowMs
            )
        )
        if (missionDay in 1..30) {
            dao.markCampDayCompleted(
                CompletedCampDayEntity(
                    dayNumber = missionDay,
                    completedAt = nowMs,
                    punchesLanded = punchesLanded,
                    kcalBurned = kcalBurned
                )
            )
        }
        context.dataStore.edit { prefs ->
            val currentXp = prefs[Keys.TOTAL_XP] ?: 0
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 0
            val currentDay = prefs[Keys.CURRENT_CAMP_DAY] ?: 1
            val currentPunches = prefs[Keys.TOTAL_PUNCHES] ?: 0
            val lastEpochDay = prefs[Keys.LAST_WORKOUT_EPOCH_DAY] ?: -1L
            val todayEpochDay = TimeUnit.MILLISECONDS.toDays(nowMs)

            prefs[Keys.TOTAL_XP] = currentXp + xpEarned
            prefs[Keys.TOTAL_PUNCHES] = currentPunches + punchesLanded

            val updatedStreak = when {
                currentStreak == 0 -> 1
                todayEpochDay == lastEpochDay -> currentStreak
                todayEpochDay == lastEpochDay + 1L -> currentStreak + 1
                else -> 1
            }
            prefs[Keys.STREAK_DAYS] = updatedStreak
            prefs[Keys.LAST_WORKOUT_EPOCH_DAY] = todayEpochDay

            if (missionDay >= currentDay && currentDay < 30) {
                prefs[Keys.CURRENT_CAMP_DAY] = (missionDay + 1).coerceAtMost(30)
            }
        }
    }

    suspend fun addBonusXp(bonusXp: Int, punches: Int) {
        context.dataStore.edit { prefs ->
            val currentXp = prefs[Keys.TOTAL_XP] ?: 0
            val currentPunches = prefs[Keys.TOTAL_PUNCHES] ?: 0
            prefs[Keys.TOTAL_XP] = currentXp + bonusXp
            prefs[Keys.TOTAL_PUNCHES] = currentPunches + punches
        }
    }

    suspend fun resetOnboardingForReview() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = false
        }
    }
}
