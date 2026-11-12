package com.qlic.mobile.core.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.qlic.mobile.app.QlicApp
import org.junit.Assert.assertEquals
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

    @Test
    fun devicesOpenNativeRegistrationRoute() {
        composeRule.setContent { QlicApp() }

        composeRule.onNodeWithTag("bottom-nav-business").performClick()
        composeRule.onNodeWithTag("business-devices").performClick()
        composeRule.onNodeWithTag("devices-add").performClick()

        composeRule.onNodeWithTag("screen-device-registration").assertIsDisplayed()
    }

    @Test
    fun alertSettingsRouteIsReachable() {
        composeRule.setContent { QlicApp() }

        composeRule.onNodeWithTag("bottom-nav-alerts").performClick()
        composeRule.onNodeWithTag("screen-alerts").assertIsDisplayed()
        composeRule.onNodeWithTag("alerts-settings").performClick()

        composeRule.onNodeWithTag("screen-alert-settings").assertIsDisplayed()
    }

    @Test
    fun businessOpensSubscriptionRoute() {
        composeRule.setContent { QlicApp() }

        composeRule.onNodeWithTag("bottom-nav-business").performClick()
        composeRule.onNodeWithTag("business-subscription").performClick()

        composeRule.onNodeWithTag("screen-subscription").assertIsDisplayed()
    }

    @Test
    fun moreOpensAccountRoute() {
        composeRule.setContent { QlicApp() }

        composeRule.onNodeWithTag("bottom-nav-more").performClick()
        composeRule.onNodeWithTag("more-account").performClick()

        composeRule.onNodeWithTag("screen-account").assertIsDisplayed()
    }

    @Test
    fun alertDetailRouteCarriesSelectedIdentifier() {
        assertEquals("alert-detail/alert-123", QlicDestination.AlertDetail.createRoute("alert-123"))
    }
}
