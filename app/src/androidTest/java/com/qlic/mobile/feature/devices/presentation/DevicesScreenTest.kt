package com.qlic.mobile.feature.devices.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import org.junit.Rule
import org.junit.Test

class DevicesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateIsVisible() {
        composeRule.setContent {
            QlicTheme {
                DevicesScreen(
                    state = DevicesUiState(isLoading = true),
                    onRetry = {},
                    onRegisterClick = {},
                    onAssignClick = { _, _ -> },
                )
            }
        }

        composeRule.onNode(hasTestTag("devices-loading")).assertIsDisplayed()
    }

    @Test
    fun emptyStateDoesNotInventDevices() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                DevicesScreen(
                    state = DevicesUiState(),
                    onRetry = {},
                    onRegisterClick = {},
                    onAssignClick = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.devices_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.devices_add)).assertIsDisplayed()
    }

    @Test
    fun contentShowsBackendDeviceData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                DevicesScreen(
                    state = DevicesUiState(
                        devices = listOf(
                            Device(
                                id = "device-id",
                                serialNumber = "QL-123",
                                model = "Qlic Flow Mini",
                                accountId = "account-id",
                                waterPointId = null,
                                status = DeviceStatus.Online,
                                batteryPercentage = 87,
                                lastSyncAt = "2026-11-11T20:15:30",
                            ),
                        ),
                    ),
                    onRetry = {},
                    onRegisterClick = {},
                    onAssignClick = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("QL-123").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.devices_battery, 87)).assertIsDisplayed()
    }
}
