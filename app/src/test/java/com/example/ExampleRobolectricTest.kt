package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.KynaroCurriculum
import com.example.model.OnboardingAnswers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and verify 30 day boxing curriculum`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KYNARO: Boxing Training", appName)

        val customAnswers = OnboardingAnswers(
            ringGoal = "Get in fighting shape",
            daysPerWeek = "4 days",
            sessionDuration = "30 minutes",
            strongHand = "Left-handed",
            coachStyle = "The Technician",
            fighterName = "Iron Mike"
        )
        assertEquals(16, customAnswers.sessionsCount30Days)
        assertEquals(30, customAnswers.sessionMinutesInt)

        val missions = KynaroCurriculum.get30DayCampMissions(customAnswers)
        assertEquals(30, missions.size)
        assertTrue(missions.first().subtitle.contains("SOUTHPAW"))

        val drills = KynaroCurriculum.getPracticeDrills()
        assertTrue(drills.isNotEmpty())
    }
}
