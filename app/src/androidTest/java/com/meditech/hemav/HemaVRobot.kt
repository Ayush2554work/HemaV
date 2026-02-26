package com.meditech.hemav

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule

/**
 * HemaV Robot for Automated End-to-End Testing.
 * Use this to verify core flows in the app.
 *
 * Usage:
 * val robot = HemaVRobot(composeTestRule)
 * robot.loginAsPatient()
 *      .verifyDashboardLoaded()
 *      .startAnemiaScan()
 */
class HemaVRobot(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    fun loginAsPatient(email: String = "test@patient.com", password: String = "password123") {
        composeTestRule.onNodeWithText("Login").performClick()
        
        composeTestRule.onNodeWithText("Email").performTextInput(email)
        composeTestRule.onNodeWithText("Password").performTextInput(password)
        
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Wait for dashboard (handling potential network delay)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Good morning,").fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun verifyDashboardLoaded() = apply {
        composeTestRule.onNodeWithText("AI Scan").assertExists()
        composeTestRule.onNodeWithText("Find Doctor").assertExists()
    }

    fun startAnemiaScan() = apply {
        composeTestRule.onNodeWithText("Start Scan").performClick() // Hero card button
        composeTestRule.onNodeWithText("AI Anemia Screening").assertExists()
        composeTestRule.onNodeWithText("Begin Analysis").performClick()
    }

    fun searchDoctorAndBook() = apply {
        composeTestRule.onNodeWithText("Find Doctor").performClick()
        
        // Select logic
        composeTestRule.onAllNodesWithText("Book").onFirst().performClick()
        
        // Booking screen
        composeTestRule.onNodeWithText("Video Call").performClick()
        composeTestRule.onNodeWithText("Select Date").performScrollTo().assertExists()
    }
}
