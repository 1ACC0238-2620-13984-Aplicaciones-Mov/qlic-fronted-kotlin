package com.qlic.mobile.core.localization

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qlic.mobile.MainActivity
import com.qlic.mobile.R
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class LanguageSwitchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun switchingToSpanishTranslatesContentAndSurvivesRecreation() {
        composeRule.onNodeWithText("English").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("Welcome to Qlic"))
        composeRule.onNodeWithText("Welcome to Qlic").assertIsDisplayed()

        composeRule.onNodeWithText("Español").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("Bienvenido a Qlic"))
        composeRule.onNodeWithText("Bienvenido a Qlic").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.waitUntilAtLeastOneExists(hasText("Bienvenido a Qlic"))
        composeRule.onNodeWithText("Bienvenido a Qlic").assertIsDisplayed()
    }

    @Test
    fun everyIntegratedScreenHasDistinctEnglishAndSpanishContent() {
        val context = composeRule.activity
        val englishConfiguration = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags("en"))
        }
        val spanishConfiguration = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags("es-419"))
        }
        val english = context.createConfigurationContext(englishConfiguration)
        val spanish = context.createConfigurationContext(spanishConfiguration)
        val representativeContent = listOf(
            R.string.dashboard_real_data_note,
            R.string.alerts_empty_title,
            R.string.alert_detail_title,
            R.string.alert_settings_title,
            R.string.device_registration_title,
            R.string.subscription_none_title,
            R.string.account_read_only_note,
            R.string.reports_unavailable_title,
            R.string.tanks_unavailable_title,
            R.string.support_unavailable_title,
        )

        representativeContent.forEach { resource ->
            assertNotEquals(english.getString(resource), spanish.getString(resource))
        }
    }
}
