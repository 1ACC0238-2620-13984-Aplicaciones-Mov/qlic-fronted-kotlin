package com.qlic.mobile.feature.dashboard.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun realMetricsAreDisplayed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                DashboardScreen(
                    state = completeState(),
                    onRetryDevices = {},
                    onRetryAlerts = {},
                    onRetrySubscription = {},
                )
            }
        }

        composeRule.onNodeWithTag("dashboard-device-total").assertTextEquals("2")
        composeRule.onNodeWithTag("dashboard-alert-open").assertTextEquals("1")
        composeRule.onNodeWithTag("dashboard-plan")
            .assertTextEquals(context.getString(R.string.subscription_plan_home_pro))
    }

    @Test
    fun partialFailureKeepsOtherSectionsVisible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                DashboardScreen(
                    state = completeState().copy(
                        devices = DashboardSection(isLoading = false, error = DashboardError.Network),
                    ),
                    onRetryDevices = {},
                    onRetryAlerts = {},
                    onRetrySubscription = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.dashboard_devices_error)).assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-alert-open").assertTextEquals("1")
    }

    @Test
    fun unsupportedFeaturesAreExplicitAndNoSampleTelemetryIsShown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                DashboardScreen(
                    state = completeState(),
                    onRetryDevices = {},
                    onRetryAlerts = {},
                    onRetrySubscription = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.telemetry_unavailable_title))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("248 L").assertDoesNotExist()
    }

    private fun completeState() = DashboardUiState(
        devices = DashboardSection(false, DeviceMetrics(2, 1, 1)),
        alerts = DashboardSection(false, AlertMetrics(1, 1, 1)),
        subscription = DashboardSection(
            false,
            SubscriptionMetrics(Plan.HomePro, SubscriptionStatus.Active, true, 6),
        ),
    )
}
