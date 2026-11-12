package com.example.model

enum class CoachPose(
    val title: String,
    val subtitle: String,
    val cueText: String,
    val cameraYaw: Float,
    val cameraPitch: Float
) {
    IDLE_GUARD(
        title = "STANCE & GUARD",
        subtitle = "CHIN TUCKED • HANDS HIGH",
        cueText = "Elbows in, hands protecting your cheekbones, light bounce on the balls of your feet.",
        cameraYaw = 0f,
        cameraPitch = 0f
    ),
    JAB(
        title = "JAB — OTHER HAND",
        subtitle = "LEAD STRAIGHT SNAP",
        cueText = "Snap your lead glove straight out from the chin and retract twice as fast.",
        cameraYaw = -10f,
        cameraPitch = 2f
    ),
    CROSS(
        title = "REAR HAND STRAIGHT",
        subtitle = "POWER CROSS ROTATION",
        cueText = "Pivot your rear foot, rotate your rear hip and shoulder, and drive straight through the target.",
        cameraYaw = 12f,
        cameraPitch = 3f
    ),
    ONE_TWO_COMBO(
        title = "1-2 COMBO",
        subtitle = "JAB • REAR STRAIGHT",
        cueText = "Measure distance with the lead jab, then immediately fire the rear straight down the pipe.",
        cameraYaw = -6f,
        cameraPitch = 2f
    ),
    JAB_CROSS_HOOK(
        title = "JAB • CROSS • HOOK",
        subtitle = "STANCE & GUARD",
        cueText = "1-2 down the center, weight shifts to rear leg, whip the lead hook at 90 degrees.",
        cameraYaw = 8f,
        cameraPitch = 1f
    ),
    SPEED_HANDS(
        title = "SPEED HANDS",
        subtitle = "NON-STOP STRAIGHT FLURRY",
        cueText = "Keep shoulders relaxed and cycle straight punches continuously at eye level.",
        cameraYaw = 0f,
        cameraPitch = 4f
    ),
    UPPERCUT(
        title = "REAR UPPERCUT",
        subtitle = "DIP & DRIVE UPWARD",
        cueText = "Bend the knees slightly, load the rear hip, and drive your yellow glove upward through the guard.",
        cameraYaw = 14f,
        cameraPitch = -3f
    ),
    SLIP_DEFENSE(
        title = "GUARD RESET & SLIP",
        subtitle = "HEAD OFF CENTER LINE",
        cueText = "Crunch the obliques to take your head off the center line while keeping both gloves glued high.",
        cameraYaw = -14f,
        cameraPitch = 0f
    ),
    FOOTWORK(
        title = "RING FOOTWORK",
        subtitle = "STEP • SLIDE • PIVOT",
        cueText = "Never cross your feet. Step with the lead foot firstgoing forward, rear foot first moving back.",
        cameraYaw = 5f,
        cameraPitch = 5f
    ),
    VICTORY_RAISE(
        title = "CAMP CHAMPION",
        subtitle = "ROUND COMPLETE",
        cueText = "Hands raised high. Breathe deep through the nose and recover.",
        cameraYaw = 0f,
        cameraPitch = -4f
    )
}

enum class WorkoutSpeed(val label: String, val speedMultiplier: Float, val kcalMultiplier: Float) {
    LIGHT("LIGHT", 0.78f, 0.8f),
    NORMAL("NORMAL", 1.0f, 1.0f),
    BEAST("BEAST", 1.45f, 1.4f),
    SLOW("SLOW", 0.55f, 0.65f)
}

data class OnboardingAnswers(
    val ringGoal: String = "Learn to box",
    val boxedBefore: String = "Brand new",
    val daysPerWeek: String = "3 days",
    val sessionDuration: String = "20 minutes",
    val ageRange: String = "25 to 34",
    val heightValue: Int = 175,
    val heightUnit: String = "CM",
    val weightValue: Int = 75,
    val weightUnit: String = "KG",
    val fitnessLevel: Int = 7,
    val gear: Set<String> = setOf("Gloves"),
    val limitations: Set<String> = setOf("No limitations"),
    val stoppedBefore: String = "No time",
    val meaning: String = "Confidence",
    val trainingFor: String = "Fight-ready",
    val strongHand: String = "Right-handed",
    val coachStyle: String = "The Drill Sergeant",
    val fighterName: String = "",
    val avatarIndex: Int = 0,
    val onboardingCompleted: Boolean = false,
    val campUnlocked: Boolean = false
) {
    val sessionsCount30Days: Int
        get() = when {
            daysPerWeek.startsWith("2") -> 8
            daysPerWeek.startsWith("3") -> 12
            daysPerWeek.startsWith("4") -> 16
            else -> 20
        }

    val sessionMinutesInt: Int
        get() = when {
            sessionDuration.startsWith("10") -> 10
            sessionDuration.startsWith("20") -> 20
            sessionDuration.startsWith("30") -> 30
            else -> 45
        }

    val daysPerWeekShort: String
        get() = when {
            daysPerWeek.startsWith("2") -> "2 days/week"
            daysPerWeek.startsWith("3") -> "3 days/week"
            daysPerWeek.startsWith("4") -> "4 days/week"
            else -> "5+ days/week"
        }

    val sessionShortLabel: String
        get() = "${sessionMinutesInt}-min missions"
}

data class WorkoutExerciseStep(
    val id: String,
    val title: String,
    val subtitle: String,
    val pose: CoachPose,
    val durationSeconds: Int,
    val targetPunches: Int,
    val isTeachStep: Boolean = false,
    val isPadRound: Boolean = false
)

data class BoxingDrill(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    val level: String,
    val pose: CoachPose,
    val description: String,
    val xpBonusPercent: Int = 5,
    val steps: List<WorkoutExerciseStep>
)

data class CampMission(
    val day: Int,
    val title: String,
    val subtitle: String,
    val durationMinutes: Int,
    val focusPose: CoachPose,
    val roundsCount: Int,
    val steps: List<WorkoutExerciseStep>
)

object KynaroCurriculum {
    fun getPracticeDrills(): List<BoxingDrill> = listOf(
        BoxingDrill(
            id = "jab_builder",
            title = "Jab Builder",
            durationMinutes = 1,
            level = "Beginner",
            pose = CoachPose.JAB,
            description = "Master lead hand extension, shoulder protection, and snap retraction.",
            steps = listOf(
                WorkoutExerciseStep("jb_1", "JAB — OTHER HAND", "JAB — OTHER HAND", CoachPose.JAB, 30, 12, isTeachStep = true),
                WorkoutExerciseStep("jb_2", "DOUBLE JAB SNAP", "STANCE & GUARD", CoachPose.JAB, 45, 20),
                WorkoutExerciseStep("jb_3", "COMBINATION BURNOUT", "SPEED & ACCURACY", CoachPose.JAB, 30, 10, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "cross_control",
            title = "Cross Control",
            durationMinutes = 1,
            level = "Beginner",
            pose = CoachPose.CROSS,
            description = "Generate knockout torque from the ground up with your rear straight.",
            steps = listOf(
                WorkoutExerciseStep("cc_1", "REAR HAND STRAIGHT", "HIP & SHOULDER ROTATION", CoachPose.CROSS, 30, 12, isTeachStep = true),
                WorkoutExerciseStep("cc_2", "POWER CROSS REPS", "STANCE & GUARD", CoachPose.CROSS, 45, 18),
                WorkoutExerciseStep("cc_3", "POWER STRIKE BURNOUT", "POWER & SNAP", CoachPose.CROSS, 30, 10, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "one_two_combo",
            title = "1-2 Combo",
            durationMinutes = 1,
            level = "Intermediate",
            pose = CoachPose.ONE_TWO_COMBO,
            description = "Chain your lead jab and rear straight into one seamless rhythm.",
            steps = listOf(
                WorkoutExerciseStep("ot_1", "1-2 COMBO", "JAB • REAR STRAIGHT", CoachPose.ONE_TWO_COMBO, 35, 16, isTeachStep = true),
                WorkoutExerciseStep("ot_2", "JAB • CROSS • HOOK", "STANCE & GUARD", CoachPose.JAB_CROSS_HOOK, 45, 24),
                WorkoutExerciseStep("ot_3", "1-2 COMBINATION FIRE", "RHYTHM & FLOW", CoachPose.ONE_TWO_COMBO, 30, 12, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "speed_hands",
            title = "Speed Hands",
            durationMinutes = 1,
            level = "Intermediate",
            pose = CoachPose.SPEED_HANDS,
            description = "High-output straight punch burnouts to skyrocket hand speed and endurance.",
            steps = listOf(
                WorkoutExerciseStep("sh_1", "SPEED HANDS", "NON-STOP STRAIGHTS", CoachPose.SPEED_HANDS, 30, 30),
                WorkoutExerciseStep("sh_2", "JAB • CROSS • HOOK", "FAST RETRACTION", CoachPose.JAB_CROSS_HOOK, 40, 28),
                WorkoutExerciseStep("sh_3", "SPEED STRIKE BLITZ", "HIGH CADENCE", CoachPose.SPEED_HANDS, 30, 15, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "guard_reset",
            title = "Guard Reset",
            durationMinutes = 1,
            level = "Beginner",
            pose = CoachPose.SLIP_DEFENSE,
            description = "Build unbreakable defensive habits: high guard, chin tucked, and head slips.",
            steps = listOf(
                WorkoutExerciseStep("gr_1", "STANCE & GUARD", "DEFENSIVE SHELL", CoachPose.IDLE_GUARD, 30, 10, isTeachStep = true),
                WorkoutExerciseStep("gr_2", "SLIP & COUNTER CROSS", "HEAD MOVEMENT", CoachPose.SLIP_DEFENSE, 45, 16),
                WorkoutExerciseStep("gr_3", "RING FOOTWORK PIVOT", "STEP & SLIDE", CoachPose.FOOTWORK, 35, 14)
            )
        ),
        BoxingDrill(
            id = "uppercut_power",
            title = "Inside Uppercuts",
            durationMinutes = 2,
            level = "Advanced",
            pose = CoachPose.UPPERCUT,
            description = "Dip the knees and rip explosive lead and rear uppercuts on the inside.",
            steps = listOf(
                WorkoutExerciseStep("up_1", "REAR UPPERCUT", "DIP & DRIVE", CoachPose.UPPERCUT, 35, 15, isTeachStep = true),
                WorkoutExerciseStep("up_2", "JAB • CROSS • UPPERCUT", "INSIDE COMBO", CoachPose.UPPERCUT, 45, 22),
                WorkoutExerciseStep("up_3", "INSIDE COMBINATION DRILL", "CLOSE RANGE", CoachPose.UPPERCUT, 30, 12, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "pull_counter",
            title = "Pull Counter Master",
            durationMinutes = 2,
            level = "Advanced",
            pose = CoachPose.CROSS,
            description = "Rock weight back 2 inches to evade opponent jab and fire laser rear cross.",
            steps = listOf(
                WorkoutExerciseStep("pc_1", "PULL BACK & LOAD", "HEAD MOVEMENT", CoachPose.SLIP_DEFENSE, 30, 12, isTeachStep = true),
                WorkoutExerciseStep("pc_2", "LASER REAR CROSS", "PIVOT & SNAP", CoachPose.CROSS, 45, 20),
                WorkoutExerciseStep("pc_3", "PULL COUNTER BURNOUT", "TIMING & ACCURACY", CoachPose.CROSS, 30, 14, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "bob_weave",
            title = "Bob & Weave Roll",
            durationMinutes = 2,
            level = "Intermediate",
            pose = CoachPose.SLIP_DEFENSE,
            description = "Dip under incoming power hooks and explode back on the blind side.",
            steps = listOf(
                WorkoutExerciseStep("bw_1", "U-SHAPED ROLL", "LEG SQUAT", CoachPose.SLIP_DEFENSE, 35, 14, isTeachStep = true),
                WorkoutExerciseStep("bw_2", "ROLL & LEAD HOOK", "COUNTER TORQUE", CoachPose.JAB_CROSS_HOOK, 45, 22),
                WorkoutExerciseStep("bw_3", "DEFENSIVE ROLL FLURRY", "EVASION FLOW", CoachPose.SLIP_DEFENSE, 30, 15, isPadRound = false)
            )
        ),
        BoxingDrill(
            id = "check_hook_pivot",
            title = "Check Hook 90° Pivot",
            durationMinutes = 2,
            level = "Advanced",
            pose = CoachPose.FOOTWORK,
            description = "Plant lead foot, pivot 90° clockwise, and whip lead check hook to exit danger.",
            steps = listOf(
                WorkoutExerciseStep("ch_1", "90° LEAD PIVOT", "RING FOOTWORK", CoachPose.FOOTWORK, 30, 12, isTeachStep = true),
                WorkoutExerciseStep("ch_2", "CHECK HOOK PIVOT", "COUNTER WHIP", CoachPose.JAB_CROSS_HOOK, 45, 18),
                WorkoutExerciseStep("ch_3", "ANGLED COMBINATION", "RING GENERALSHIP", CoachPose.FOOTWORK, 35, 15, isPadRound = false)
            )
        )
    )

    fun get30DayCampMissions(answers: OnboardingAnswers): List<CampMission> {
        val baseMinutes = answers.sessionMinutesInt
        val missionTitles = listOf(
            "Stance & Jab Foundations" to CoachPose.JAB,
            "Rear Hand Straight Power" to CoachPose.CROSS,
            "The Classic 1-2 Combo" to CoachPose.ONE_TWO_COMBO,
            "Guard Reset & Chin Discipline" to CoachPose.IDLE_GUARD,
            "Lead Hook Mechanics" to CoachPose.JAB_CROSS_HOOK,
            "Ring Footwork & Balance" to CoachPose.FOOTWORK,
            "Slip & Counter Precision" to CoachPose.SLIP_DEFENSE,
            "Speed Hands Burnout" to CoachPose.SPEED_HANDS,
            "Inside Uppercut Drive" to CoachPose.UPPERCUT,
            "3-Punch Combination Flow" to CoachPose.JAB_CROSS_HOOK,
            "Double Jab Ring Control" to CoachPose.JAB,
            "Cross-Hook-Cross Torque" to CoachPose.CROSS,
            "Defensive Head Movement" to CoachPose.SLIP_DEFENSE,
            "High-Tempo Combo Rounds" to CoachPose.SPEED_HANDS,
            "Mid-Camp Fighter Assessment" to CoachPose.ONE_TWO_COMBO,
            "Counter-Punching Reflexes" to CoachPose.SLIP_DEFENSE,
            "Power Shots & Body Work" to CoachPose.UPPERCUT,
            "FLOW & FINISH" to CoachPose.JAB_CROSS_HOOK,
            "Angles & Pivot Attacks" to CoachPose.FOOTWORK,
            "Championship Stamina" to CoachPose.SPEED_HANDS,
            "Sharp Jab Distance Control" to CoachPose.JAB,
            "Knockout Rear Straight" to CoachPose.CROSS,
            "Seamless 4-Punch Combos" to CoachPose.ONE_TWO_COMBO,
            "Slip-Roll-Hook Counters" to CoachPose.SLIP_DEFENSE,
            "Explosive Combo Blitz" to CoachPose.SPEED_HANDS,
            "Inside Fighting Uppercuts" to CoachPose.UPPERCUT,
            "Ring Generalship & Pace" to CoachPose.FOOTWORK,
            "Peak Fight Conditioning" to CoachPose.JAB_CROSS_HOOK,
            "Final Sparring Prep" to CoachPose.ONE_TWO_COMBO,
            "Day 30 Fighter Graduation" to CoachPose.VICTORY_RAISE
        )

        return missionTitles.mapIndexed { index, (title, pose) ->
            val dayNum = index + 1
            val handHint = if (answers.strongHand == "Left-handed") "SOUTHPAW STANCE" else "ORTHODOX STANCE"
            CampMission(
                day = dayNum,
                title = title,
                subtitle = "$baseMinutes min • coached rounds ($handHint)",
                durationMinutes = baseMinutes,
                focusPose = pose,
                roundsCount = 5,
                steps = listOf(
                    WorkoutExerciseStep(
                        id = "m_${dayNum}_1",
                        title = "JAB — OTHER HAND",
                        subtitle = "JAB — OTHER HAND",
                        pose = CoachPose.JAB,
                        durationSeconds = 30,
                        targetPunches = 12,
                        isTeachStep = true
                    ),
                    WorkoutExerciseStep(
                        id = "m_${dayNum}_2",
                        title = "REAR HAND STRAIGHT",
                        subtitle = handHint,
                        pose = CoachPose.CROSS,
                        durationSeconds = 35,
                        targetPunches = 15,
                        isTeachStep = false
                    ),
                    WorkoutExerciseStep(
                        id = "m_${dayNum}_3",
                        title = "JAB • CROSS • HOOK",
                        subtitle = "STANCE & GUARD",
                        pose = CoachPose.JAB_CROSS_HOOK,
                        durationSeconds = 40,
                        targetPunches = 20,
                        isTeachStep = false
                    ),
                    WorkoutExerciseStep(
                        id = "m_${dayNum}_4",
                        title = pose.title,
                        subtitle = pose.subtitle,
                        pose = pose,
                        durationSeconds = 45,
                        targetPunches = 24,
                        isTeachStep = false
                    ),
                    WorkoutExerciseStep(
                        id = "m_${dayNum}_5",
                        title = "LAND 3 POWER COMBOS",
                        subtitle = "CHAMPIONSHIP ROUND",
                        pose = CoachPose.SPEED_HANDS,
                        durationSeconds = 30,
                        targetPunches = 15,
                        isPadRound = false
                    )
                )
            )
        }
    }
}
