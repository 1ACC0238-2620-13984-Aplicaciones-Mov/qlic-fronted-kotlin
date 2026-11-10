package com.qlic.mobile.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class QlicAppSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appDisplaysQlicBrand() {
        composeRule.setContent {
            QlicApp()
        }

        composeRule.onNodeWithText("Qlic").assertIsDisplayed()
    }
}
