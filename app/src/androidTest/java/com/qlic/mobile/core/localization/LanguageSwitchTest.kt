package com.qlic.mobile.core.localization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qlic.mobile.MainActivity
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class LanguageSwitchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun switchingToSpanishTranslatesContentAndSurvivesRecreation() {
        composeRule.onNodeWithText("English").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("Water management dashboard"))
        composeRule.onNodeWithText("Water management dashboard").assertIsDisplayed()

        composeRule.onNodeWithText("Español").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("Panel de gestión del agua"))
        composeRule.onNodeWithText("Panel de gestión del agua").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.waitUntilAtLeastOneExists(hasText("Panel de gestión del agua"))
        composeRule.onNodeWithText("Panel de gestión del agua").assertIsDisplayed()
    }
}
