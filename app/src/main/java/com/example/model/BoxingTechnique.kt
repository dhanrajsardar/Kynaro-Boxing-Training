package com.example.model

/**
 * Canonical Boxing Techniques & Biomechanics Masterclass.
 * Numbered according to universal professional boxing standard (1 to 6).
 */
data class BoxingTechnique(
    val id: String,
    val canonicalNumber: String,
    val name: String,
    val subtitle: String,
    val category: BoxingCategory,
    val level: String,
    val pose: CoachPose,
    val hallOfFameMaster: String,
    val kineticChainSummary: String,
    val checkpoints: List<KineticCheckpoint>,
    val amateurFlaw: String,
    val proFix: String,
    val tacticalUsage: String,
    val targetArea: String
)

enum class BoxingCategory(val label: String) {
    PUNCH("Punches (1-6)"),
    DEFENSE("Defense & Slips"),
    FOOTWORK("Footwork & Ring IQ")
}

data class KineticCheckpoint(
    val title: String,
    val description: String,
    val iconName: String
)

object BoxingTechniqueRepository {
    val allTechniques: List<BoxingTechnique> = listOf(
        BoxingTechnique(
            id = "punch_1_jab",
            canonicalNumber = "1",
            name = "Lead Jab",
            subtitle = "The most important punch in boxing — distance, defense, and damage.",
            category = BoxingCategory.PUNCH,
            level = "Fundamentals",
            pose = CoachPose.JAB,
            hallOfFameMaster = "Muhammad Ali & Larry Holmes",
            kineticChainSummary = "Push off rear ball of foot -> Extend lead arm linearly -> Rotate wrist 90° on impact -> Shrug lead shoulder to guard chin -> Immediate snap return along same path.",
            checkpoints = listOf(
                KineticCheckpoint("Weight 50/50", "Maintain equal weight distribution so you can instantly advance or retreat without telegraphing.", "balance"),
                KineticCheckpoint("Shoulder Chin Shield", "Elevate your lead shoulder as the fist extends to guard your chin against overhand counters.", "shield"),
                KineticCheckpoint("Rear Hand Glued", "Keep your rear glove glued to your cheekbone protecting your temple and jaw.", "guard"),
                KineticCheckpoint("Snap Retraction", "Never push the jab; whip it out like a wet towel and snap it back twice as fast as it left.", "snap")
            ),
            amateurFlaw = "Flaring the elbow out wide (chicken wing) or dropping the rear hand to the chest.",
            proFix = "Keep the elbow tracking directly along your rib cage and imagine throwing the punch through a narrow pipe.",
            tacticalUsage = "Use to measure distance, disrupt opponent rhythm, blind vision, and set up the rear power cross.",
            targetArea = "Nose, chin, and sternum"
        ),
        BoxingTechnique(
            id = "punch_2_cross",
            canonicalNumber = "2",
            name = "Rear Cross (Straight)",
            subtitle = "Primary power punch delivered from the rear hand directly down the pipe.",
            category = BoxingCategory.PUNCH,
            level = "Power Striking",
            pose = CoachPose.CROSS,
            hallOfFameMaster = "Deontay Wilder & Wladimir Klitschko",
            kineticChainSummary = "Drive ball of rear foot into floor -> Pivot rear heel 45° outward -> Hip and torso rotate through centerline -> Lat fires fist forward -> Full lockout with chin tucked behind lead hand.",
            checkpoints = listOf(
                KineticCheckpoint("Rear Foot Pivot", "Pivot on the ball of the back foot like extinguishing a cigarette; heel points out.", "footwork"),
                KineticCheckpoint("Hip Engagement", "Rotate the rear hip 45 degrees forward to transfer body weight directly into the strike.", "hip"),
                KineticCheckpoint("Lead Hand Wall", "The lead glove stays locked high at your temple to absorb any return fire.", "shield"),
                KineticCheckpoint("Straight Vector", "Travels on a laser line from cheek directly to opponent jaw — no looping.", "line")
            ),
            amateurFlaw = "Leaning the chest forward past the lead knee, resulting in loss of balance and counter vulnerabilities.",
            proFix = "Keep your spine vertical like a rotating pillar; power comes from rotation, not falling forward.",
            tacticalUsage = "Fire immediately behind the jab (1-2) or as a pull-counter when the opponent misses.",
            targetArea = "Jawline and solar plexus"
        ),
        BoxingTechnique(
            id = "punch_3_lead_hook",
            canonicalNumber = "3",
            name = "Lead Hook",
            subtitle = "Devastating rotational knockout punch thrown on the blind side.",
            category = BoxingCategory.PUNCH,
            level = "Knockout Mechanics",
            pose = CoachPose.JAB_CROSS_HOOK,
            hallOfFameMaster = "Mike Tyson & Joe Frazier",
            kineticChainSummary = "Shift weight 60% onto lead foot -> Squash the bug by pivoting lead heel 90° -> Lock elbow at rigid 90° angle -> Torso turns as a single solid unit -> Fist impacts target with thumb up or palm down.",
            checkpoints = listOf(
                KineticCheckpoint("90° Rigid Arm Angle", "Keep the elbow locked at a strict right angle; never extend the arm into a slap.", "elbow"),
                KineticCheckpoint("Squash the Bug", "Violently pivot the lead foot onto its ball, turning knee and hip into the punch.", "pivot"),
                KineticCheckpoint("Level Shoulder Plane", "Keep your punching shoulder and elbow on the exact same horizontal plane.", "level"),
                KineticCheckpoint("Rear Guard Anchor", "Glue rear glove firmly against the right jaw to absorb hook counters.", "guard")
            ),
            amateurFlaw = "Winding up the arm backwards before throwing, completely telegraphing the strike.",
            proFix = "Turn the hips first; the arm stays bent and simply whips along with the hip torque without any windup.",
            tacticalUsage = "Throw around the outside of opponent's guard after a straight cross or as a check hook while circling.",
            targetArea = "Temple, jaw, and liver (lead hook to body)"
        ),
        BoxingTechnique(
            id = "punch_4_rear_hook",
            canonicalNumber = "4",
            name = "Rear Hook",
            subtitle = "Heavy rotational rear hook targeting the temple or ear from the outside.",
            category = BoxingCategory.PUNCH,
            level = "Power Striking",
            pose = CoachPose.CROSS,
            hallOfFameMaster = "Gennadiy Golovkin & Canelo Alvarez",
            kineticChainSummary = "Drive off rear foot -> Rotate rear hip completely across centerline -> Keep compact arch behind elbow -> Lead hand shields opposite side -> Snap fist through target.",
            checkpoints = listOf(
                KineticCheckpoint("Compact Arc", "Keep the trajectory tight; looping too wide allows straight counters to beat it.", "arc"),
                KineticCheckpoint("Full Hip Drive", "Bring the rear hip completely square to front upon impact.", "hip"),
                KineticCheckpoint("Lead Hand High", "Keep the lead hand shielding your ear and cheek.", "guard"),
                KineticCheckpoint("Core Brace", "Contract the abdominal wall upon impact to absorb the shock of contact.", "core")
            ),
            amateurFlaw = "Over-rotating and spinning out of stance when the punch misses.",
            proFix = "Set a hard brake with your lead leg and core so you instantly return to balanced guard if they slip.",
            tacticalUsage = "Follow up a lead hook to bracket both sides of opponent's guard.",
            targetArea = "Temple, jaw, and floating ribs"
        ),
        BoxingTechnique(
            id = "punch_5_lead_uppercut",
            canonicalNumber = "5",
            name = "Lead Uppercut",
            subtitle = "Inside strike fired vertically between the opponent's gloves.",
            category = BoxingCategory.PUNCH,
            level = "Inside Fighting",
            pose = CoachPose.UPPERCUT,
            hallOfFameMaster = "Roy Jones Jr. & Roberto Duran",
            kineticChainSummary = "Slight dip to lead side -> Drive upward through lead quad and glute -> Palm faces inward toward chest -> Elbow tucked against ribs -> Snap straight up through centerline.",
            checkpoints = listOf(
                KineticCheckpoint("Dip With Knees", "Never bend at the waist; lower your elevation by bending your knees.", "knees"),
                KineticCheckpoint("Elbow Tucked", "Keep elbow pinned close to your hip for structural support and power generation.", "elbow"),
                KineticCheckpoint("Palm Faces You", "Rotate the fist so your palm is facing back toward your chest upon release.", "fist"),
                KineticCheckpoint("Short Vector", "Throw compact 6-10 inch punches; long uppercuts get intercepted by straights.", "vector")
            ),
            amateurFlaw = "Dropping the hand down to the waist before swinging upward.",
            proFix = "The hand stays at chin level; your whole body dips down via the knees and springs upward.",
            tacticalUsage = "Lift the opponent's head when they are crouching or tucking in a high guard, exposing them to hooks.",
            targetArea = "Chin and solar plexus"
        ),
        BoxingTechnique(
            id = "punch_6_rear_uppercut",
            canonicalNumber = "6",
            name = "Rear Uppercut",
            subtitle = "Explosive upward knockout punch powered by the rear leg drive.",
            category = BoxingCategory.PUNCH,
            level = "Knockout Mechanics",
            pose = CoachPose.UPPERCUT,
            hallOfFameMaster = "Lennox Lewis & George Foreman",
            kineticChainSummary = "Dip rear knee -> Drive explosively upward off rear foot -> Thrust rear hip up and forward -> Fist travels vertically up the centerline -> Instant guard return.",
            checkpoints = listOf(
                KineticCheckpoint("Ground Drive", "Power is pushed directly from the mat through your rear calf and thigh.", "power"),
                KineticCheckpoint("Tight Arc", "Fist stays within 8 inches of your torso until the final vertical snap.", "tight"),
                KineticCheckpoint("Lead Hand Shield", "Opposite hand remains cemented to cheek to guard against check hooks.", "guard"),
                KineticCheckpoint("Breathe On Release", "Sharp exhalation on release locks the core muscles.", "breath")
            ),
            amateurFlaw = "Leaning back while throwing, robbing the punch of all bodyweight.",
            proFix = "Drive straight UP into the sky, keeping center of gravity squarely between your feet.",
            tacticalUsage = "Punish an opponent who ducks their head forward or rushes in with their chin low.",
            targetArea = "Chin, jaw, and liver"
        ),
        BoxingTechnique(
            id = "def_slip",
            canonicalNumber = "DEF-1",
            name = "Slip Defense (Lead & Rear)",
            subtitle = "Subtle head evasion that makes punches miss by inches while loading counters.",
            category = BoxingCategory.DEFENSE,
            level = "Evasion Master",
            pose = CoachPose.SLIP_DEFENSE,
            hallOfFameMaster = "Floyd Mayweather & Pernell Whitaker",
            kineticChainSummary = "Slight knee bend -> Crunch oblique on slipping side -> Rotate shoulders 15° -> Head shifts 3-4 inches off centerline -> Weight loads onto loaded foot ready to counter.",
            checkpoints = listOf(
                KineticCheckpoint("Minimal Movement", "Only move your head 3 inches — just enough for the glove to slide past your ear.", "precision"),
                KineticCheckpoint("Keep Eyes on Opponent", "Never look at the floor; look directly through the punch at opponent chest.", "eyes"),
                KineticCheckpoint("Loaded Spring", "Slipping loads the hip on that side like a cocked pistol for an instant return punch.", "spring"),
                KineticCheckpoint("Hands Stay High", "Maintain full guard even while slipping to catch unexpected looping punches.", "guard")
            ),
            amateurFlaw = "Bending deeply at the waist and taking eyes off the opponent.",
            proFix = "Bend at the knees and crunch your side obliques. Your eyes never leave your opponent.",
            tacticalUsage = "Slip inside a jab to land a rear cross, or slip outside a cross to land a lead hook.",
            targetArea = "Head movement evasion"
        ),
        BoxingTechnique(
            id = "def_roll",
            canonicalNumber = "DEF-2",
            name = "Bob & Weave (Roll)",
            subtitle = "Roll underneath incoming hooks to enter inside angles completely unscathed.",
            category = BoxingCategory.DEFENSE,
            level = "Inside Evasion",
            pose = CoachPose.SLIP_DEFENSE,
            hallOfFameMaster = "Mike Tyson & Joe Frazier",
            kineticChainSummary = "Bend both knees smoothly -> Trace a tight 'U' shape with your head underneath the punch -> Shift weight to opposite foot -> Rise up on opponent's blind side.",
            checkpoints = listOf(
                KineticCheckpoint("U-Shaped Dip", "Move your head down, across, and back up in a smooth continuous semicircle.", "u_shape"),
                KineticCheckpoint("Legs Do The Work", "Use your quads to drop elevation; back remains upright and engaged.", "legs"),
                KineticCheckpoint("Never Cross Center", "Don't pause at the bottom of the roll; immediately rise into a counter-punch.", "fluid"),
                KineticCheckpoint("Gloves On Temples", "Keep hands glued to temples in case the hook dips lower than anticipated.", "guard")
            ),
            amateurFlaw = "Bending forward at the waist like picking something off the ground, leaving chin open to uppercuts.",
            proFix = "Squat smoothly using your hips and knees while keeping your chest and eyes proud.",
            tacticalUsage = "Roll underneath opponent's #3 lead hook and emerge ready to deliver a bone-crushing #2 or #6.",
            targetArea = "Hook evasion"
        ),
        BoxingTechnique(
            id = "def_philly_shell",
            canonicalNumber = "DEF-3",
            name = "High Guard & Shell",
            subtitle = "The impenetrable fortress defense that absorbs heavy firepower safely on gloves.",
            category = BoxingCategory.DEFENSE,
            level = "Guard Discipline",
            pose = CoachPose.IDLE_GUARD,
            hallOfFameMaster = "Arthur Abraham & Winky Wright",
            kineticChainSummary = "Elbows pinned to ribs -> Gloves placed firmly on forehead/temples -> Peep through the narrow slit between wrists -> Absorb impact on gloves and forearms.",
            checkpoints = listOf(
                KineticCheckpoint("Elbows Glued", "Protect your floating ribs and liver by keeping elbows clamped to your sides.", "ribs"),
                KineticCheckpoint("Glove Window", "Look between your gloves; never turn your head away or close your eyes.", "window"),
                KineticCheckpoint("Firm Base", "Brace your legs to absorb the kinetic shock of incoming power punches.", "base"),
                KineticCheckpoint("Immediate Return", "Never stay in the shell for more than 2 punches without firing back.", "fire")
            ),
            amateurFlaw = "Holding hands out in front like a zombie, allowing punches to knock hands into your own face.",
            proFix = "Hands must make physical contact with your own forehead or temple so impact is safely dissipated.",
            tacticalUsage = "Survive heavy flurries against the ropes and wait for the exact moment the opponent exhales to counter.",
            targetArea = "Total defensive shield"
        ),
        BoxingTechnique(
            id = "def_pull_counter",
            canonicalNumber = "DEF-4",
            name = "The Pull Counter",
            subtitle = "Master counter-punching maneuver made famous by Floyd Mayweather.",
            category = BoxingCategory.DEFENSE,
            level = "Elite Counter",
            pose = CoachPose.CROSS,
            hallOfFameMaster = "Floyd Mayweather Jr.",
            kineticChainSummary = "Bait the jab -> Rock weight 70% onto back leg pulling head 2 inches back -> Opponent misses by millimeters -> Spring forward instantly with explosive #2 Rear Cross.",
            checkpoints = listOf(
                KineticCheckpoint("Bait & Distance", "Stand right on the edge of the opponent's striking range to invite their jab.", "bait"),
                KineticCheckpoint("Subtle Lean", "Shift weight to back leg just enough for punch to pass in front of your nose.", "lean"),
                KineticCheckpoint("Instant Spring", "The rear calf acts as a loaded springboard to immediately fire the cross.", "spring"),
                KineticCheckpoint("Laser Accuracy", "Aim directly at the exposed chin while their lead hand is still extended.", "laser")
            ),
            amateurFlaw = "Pulling too far back and losing balance, unable to fire the return cross.",
            proFix = "Keep your rear foot firmly planted on the canvas; only pull your torso back 2 to 3 inches.",
            tacticalUsage = "The ultimate weapon against lazy or predictable jabbing opponents.",
            targetArea = "Chin counter"
        ),
        BoxingTechnique(
            id = "foot_step_slide",
            canonicalNumber = "FOOT-1",
            name = "Step-Slide Footwork",
            subtitle = "The sacred golden rule of boxing footwork: never cross your feet, stay 50/50.",
            category = BoxingCategory.FOOTWORK,
            level = "Fundamentals",
            pose = CoachPose.FOOTWORK,
            hallOfFameMaster = "Vasiliy Lomachenko & Sugar Ray Leonard",
            kineticChainSummary = "Step with the foot closest to direction -> Push off opposite foot -> Trailing foot slides into stance -> Boxing base remains strictly shoulder-width apart.",
            checkpoints = listOf(
                KineticCheckpoint("Lead Foot First Moving Forward", "Never take a step forward with your back foot first.", "forward"),
                KineticCheckpoint("Rear Foot First Moving Back", "Push off your lead ball to retreat smoothly.", "retreat"),
                KineticCheckpoint("Never Cross Feet", "Crossing feet destroys your balance and leaves you helpless if hit.", "balance"),
                KineticCheckpoint("Light on Balls", "Keep heels slightly elevated off canvas for instantaneous directional change.", "agile")
            ),
            amateurFlaw = "Flat-footed clomping or taking massive strides that leave legs stretched out.",
            proFix = "Take short 4-6 inch micro-steps so you are always 100% ready to strike or defend at any fraction of a second.",
            tacticalUsage = "Cut off the ring, control fighting distance, and stay out of opponent power zones.",
            targetArea = "Ring movement"
        ),
        BoxingTechnique(
            id = "foot_90_pivot",
            canonicalNumber = "FOOT-2",
            name = "90° Check Hook Pivot",
            subtitle = "Spin out of danger while landing a check hook, leaving your opponent swinging at air.",
            category = BoxingCategory.FOOTWORK,
            level = "Ring Generalship",
            pose = CoachPose.FOOTWORK,
            hallOfFameMaster = "Floyd Mayweather (vs Ricky Hatton) & Dmitry Bivol",
            kineticChainSummary = "As opponent charges -> Plant lead ball of foot firmly -> Swing rear leg 90° clockwise -> Throw crisp #3 lead check hook -> Opponent crashes into the ropes.",
            checkpoints = listOf(
                KineticCheckpoint("Lead Foot Anchor", "Plant the ball of the lead foot like a compass needle.", "compass"),
                KineticCheckpoint("90° Arc Swing", "Whip your rear leg in a clean quarter-circle arc behind you.", "arc"),
                KineticCheckpoint("Check Hook Timing", "Throw the hook as you pivot to rotate opponent's momentum away.", "hook"),
                KineticCheckpoint("Instant New Angle", "You are now facing opponent flank while their back is exposed to you.", "angle")
            ),
            amateurFlaw = "Jumping with both feet off the floor, losing all traction.",
            proFix = "Keep the lead foot glued to the canvas as the pivot axle while the back foot swings around.",
            tacticalUsage = "Escape corners and ropes when aggressive brawlers try to pin you down.",
            targetArea = "Angled counter & ring escape"
        )
    )
}
