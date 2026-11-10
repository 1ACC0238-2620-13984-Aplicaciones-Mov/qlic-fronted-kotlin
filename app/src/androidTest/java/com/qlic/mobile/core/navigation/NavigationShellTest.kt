package com.qlic.mobile.core.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.qlic.mobile.app.QlicApp
import org.junit.Rule
import org.junit.Test

class NavigationShellTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigationOpensBusinessWithDevicesAndTanks() {
        composeRule.setContent {
            QlicApp()
        }

        composeRule.onNodeWithTag("bottom-nav-business").performClick()

        composeRule.onNodeWithTag("screen-business").assertIsDisplayed()
        composeRule.onNodeWithTag("business-devices").assertIsDisplayed()
        composeRule.onNodeWithTag("business-tanks").assertIsDisplayed()
    }
}
