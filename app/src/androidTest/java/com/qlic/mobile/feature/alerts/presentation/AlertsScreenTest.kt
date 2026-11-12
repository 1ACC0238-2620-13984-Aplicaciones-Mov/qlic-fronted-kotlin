package com.qlic.mobile.feature.alerts.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.LeakAlert
import com.qlic.mobile.feature.alerts.domain.Urgency
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test

class AlertsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateIsVisible() {
        composeRule.setContent {
            QlicTheme {
                AlertsScreen(
                    state = AlertsUiState(isLoading = true),
                    onRetry = {},
                    onFilterSelected = {},
                    onAlertClick = {},
                    onSettingsClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("alerts-loading").assertIsDisplayed()
    }

    @Test
    fun emptyFilterResultIsHonest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                AlertsScreen(
                    state = AlertsUiState(
                        alerts = listOf(alert()),
                        selectedFilter = AlertFilter.Acknowledged,
                    ),
                    onRetry = {},
                    onFilterSelected = {},
                    onAlertClick = {},
                    onSettingsClick = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.alerts_empty_filter_title)).assertIsDisplayed()
    }

    @Test
    fun contentShowsBackendAlertData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                AlertsScreen(
                    state = AlertsUiState(alerts = listOf(alert())),
                    onRetry = {},
                    onFilterSelected = {},
                    onAlertClick = {},
                    onSettingsClick = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.alerts_possible_leak)).assertIsDisplayed()
        composeRule.onNodeWithText("Kitchen valve").assertIsDisplayed()
        composeRule.onNodeWithTag("alert-urgency-alert-id", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun alert() = LeakAlert(
        id = "alert-id",
        accountId = "account-id",
        waterPointId = "Kitchen valve",
        state = AlertState.Active,
        urgency = Urgency.Critical,
        detectedAt = null,
        estimatedVolumeLiters = BigDecimal("4.2"),
        estimatedCostSoles = BigDecimal("8.4"),
        recommendedAction = "Close the valve",
        acknowledgedAction = null,
        previousAlertId = null,
        pushDispatched = true,
        notifiedRecipientsCount = 1,
    )
}
