package com.qlic.mobile.feature.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.qlic.mobile.core.designsystem.QlicTheme
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sessionIdentityIsReadOnlyAndSignOutIsExposed() {
        var signedOut = false
        composeRule.setContent {
            QlicTheme {
                AccountScreen(
                    state = AccountUiState(
                        fullName = "Ana Ruiz",
                        email = "ana@qlic.com",
                        accountId = "account-id",
                    ),
                    onSignOut = { signedOut = true },
                )
            }
        }

        composeRule.onNodeWithTag("account-name").assertTextEquals("Ana Ruiz")
        composeRule.onNodeWithTag("account-email").assertTextEquals("ana@qlic.com")
        composeRule.onNodeWithTag("account-id").assertTextEquals("account-id")
        composeRule.onNodeWithTag("account-sign-out")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertTrue(signedOut)
    }
}
