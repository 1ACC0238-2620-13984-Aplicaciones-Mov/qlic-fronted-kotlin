package com.qlic.mobile.feature.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.qlic.mobile.core.designsystem.QlicTheme
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signInDisplaysRequiredControls() {
        composeRule.setContent {
            QlicTheme {
                AuthScreen(
                    state = AuthUiState(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onNameChanged = {},
                    onTermsChanged = {},
                    onSubmit = {},
                    onModeChanged = {},
                )
            }
        }

        composeRule.onNodeWithTag("auth-email").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-password").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-submit").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-mode-toggle").assertIsDisplayed()
    }
}
