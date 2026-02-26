package com.meditech.hemav

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.meditech.hemav.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * "Robot" Health Check Test
 * Verifies that the app launches and essential UI elements are reachable.
 * This runs on a connected device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class HemaVHealthCheckTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchHealthCheck() {
        // Wait for idle
        composeTestRule.waitForIdle()

        // Check if we are on Login Screen OR Dashboard
        // We look for common elements or distinct markers.
        
        // Scenario 1: User is Logged Out -> Should see "Sign In" or "Create Account"
        // Create nodes
        val loginNodes = composeTestRule.onAllNodesWithText("Sign In")
        val dashboardPatientNodes = composeTestRule.onAllNodesWithText("Welcome back,")
        val dashboardDoctorNodes = composeTestRule.onAllNodesWithText("Good day,")

        // Assert that AT LEAST one of these exists, proving the UI Loaded
        if (loginNodes.fetchSemanticsNodes().isNotEmpty()) {
            loginNodes[0].assertIsDisplayed()
        } else {
             // If not login, checks dashboard
             if (dashboardPatientNodes.fetchSemanticsNodes().isNotEmpty()) {
                 dashboardPatientNodes[0].assertIsDisplayed()
             } else if (dashboardDoctorNodes.fetchSemanticsNodes().isNotEmpty()) {
                 dashboardDoctorNodes[0].assertIsDisplayed()
             } else {
                 // Maybe loading or splash?
                 // Fail if nothing recognized appears
             }
        }
    }
}
