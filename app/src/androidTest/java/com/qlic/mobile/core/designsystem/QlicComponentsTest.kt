package com.qlic.mobile.core.designsystem

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class QlicComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primaryButtonKeepsMinimumTouchHeightAndDisabledSemantics() {
        composeRule.setContent {
            QlicTheme {
                QlicButton(
                    text = "Continue",
                    onClick = {},
                    enabled = false,
                )
            }
        }

        composeRule.onNodeWithText("Continue")
            .assertIsNotEnabled()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun textFieldDisplaysAnExplicitErrorMessage() {
        composeRule.setContent {
            QlicTheme {
                QlicTextField(
                    value = "invalid",
                    onValueChange = {},
                    label = "Email address",
                    isError = true,
                    errorMessage = "Enter a valid email",
                )
            }
        }

        composeRule.onNodeWithText("Enter a valid email").assertIsDisplayed()
    }
}
