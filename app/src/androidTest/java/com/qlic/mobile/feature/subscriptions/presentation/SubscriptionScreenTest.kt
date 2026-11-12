package com.qlic.mobile.feature.subscriptions.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.runtime.Composable
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.PaymentStatus
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test

class SubscriptionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateIsVisible() {
        composeRule.setContent {
            QlicTheme {
                screen(SubscriptionUiState(isLoading = true))
            }
        }

        composeRule.onNodeWithTag("subscription-loading").assertIsDisplayed()
    }

    @Test
    fun emptyStateOffersTrialWithoutCardCollection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme { screen(SubscriptionUiState()) }
        }

        composeRule.onNodeWithText(context.getString(R.string.subscription_none_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.subscription_start_trial)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.subscription_payment_safety))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun currentPlanAndBackendPaymentAreVisible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            QlicTheme {
                screen(
                    SubscriptionUiState(
                        subscription = subscription(),
                        payments = listOf(payment()),
                    ),
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.subscription_current_plan)).assertIsDisplayed()
        composeRule.onNodeWithTag("current-subscription-plan")
            .assertTextEquals(context.getString(R.string.subscription_plan_home_pro))
        composeRule.onNodeWithText(context.getString(R.string.subscription_payment_amount, "49.90"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Composable
    private fun screen(state: SubscriptionUiState) = SubscriptionScreen(
        state = state,
        onRetry = {},
        onStartTrial = {},
        onPurchase = {},
        onChangePlan = {},
        onCancel = {},
    )

    private fun subscription() = Subscription(
        id = "subscription-id",
        accountId = "account-id",
        currentPlan = Plan.HomePro,
        nextBillingPlan = null,
        status = SubscriptionStatus.Active,
        trialUsed = true,
        paidFeaturesEnabled = true,
        maxWaterPointsAllowed = 6,
        startedAt = null,
        currentPeriodEndsAt = null,
        autoRenew = true,
    )

    private fun payment() = PaymentRecord(
        id = "payment-id",
        subscriptionId = "subscription-id",
        planAssociated = Plan.HomePro,
        amountSoles = BigDecimal("49.90"),
        status = PaymentStatus.Confirmed,
        paymentMethod = "YAPE_PLIN_OR_CARD",
        rejectionReason = null,
        paidAt = null,
    )
}
