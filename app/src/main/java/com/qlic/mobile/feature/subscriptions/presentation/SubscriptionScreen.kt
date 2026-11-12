package com.qlic.mobile.feature.subscriptions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.PaymentStatus
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus

@Composable
fun SubscriptionScreen(
    state: SubscriptionUiState,
    onRetry: () -> Unit,
    onStartTrial: () -> Unit,
    onPurchase: (Plan) -> Unit,
    onChangePlan: (Plan) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.subscription_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.subscription_title), style = MaterialTheme.typography.headlineMedium)

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("subscription-loading"),
            )
            state.error != null -> SubscriptionErrorContent(state.error, onRetry)
            else -> {
                if (state.subscription == null) {
                    NoSubscriptionCard(state.isSubmitting, onStartTrial)
                } else {
                    CurrentSubscriptionCard(state.subscription, state.isSubmitting, onCancel)
                }

                Text(stringResource(R.string.subscription_plans_title), style = MaterialTheme.typography.titleLarge)
                state.purchasablePlans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        currentSubscription = state.subscription,
                        isSubmitting = state.isSubmitting,
                        onPurchase = onPurchase,
                        onChangePlan = onChangePlan,
                    )
                }

                Text(
                    stringResource(R.string.subscription_payment_safety),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                PaymentHistory(state.payments)
            }
        }

        state.operation?.let { operation ->
            Text(
                stringResource(operation.messageResource()),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NoSubscriptionCard(isSubmitting: Boolean, onStartTrial: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.subscription_none_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.subscription_none_body))
            QlicButton(
                text = stringResource(R.string.subscription_start_trial),
                onClick = onStartTrial,
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun CurrentSubscriptionCard(
    subscription: Subscription,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.subscription_current_plan), style = MaterialTheme.typography.labelMedium)
            Text(
                stringResource(subscription.currentPlan.labelResource()),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag("current-subscription-plan"),
            )
            Text(stringResource(subscription.status.labelResource()))
            Text(stringResource(R.string.subscription_water_points, subscription.maxWaterPointsAllowed))
            subscription.nextBillingPlan?.let { nextPlan ->
                Text(stringResource(R.string.subscription_next_plan, stringResource(nextPlan.labelResource())))
            }
            if (subscription.status == SubscriptionStatus.Active ||
                subscription.status == SubscriptionStatus.TrialActive
            ) {
                QlicButton(
                    text = stringResource(R.string.subscription_cancel),
                    onClick = onCancel,
                    enabled = !isSubmitting,
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: Plan,
    currentSubscription: Subscription?,
    isSubmitting: Boolean,
    onPurchase: (Plan) -> Unit,
    onChangePlan: (Plan) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(plan.labelResource()), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        R.string.subscription_price,
                        plan.monthlyPriceSoles.toPlainString(),
                    ),
                )
            }
            Text(stringResource(R.string.subscription_water_points, plan.maxWaterPoints))
            val alreadyCurrent = currentSubscription?.currentPlan == plan
            QlicButton(
                text = when {
                    alreadyCurrent -> stringResource(R.string.subscription_current_button)
                    currentSubscription == null -> stringResource(R.string.subscription_purchase)
                    else -> stringResource(R.string.subscription_change_plan)
                },
                onClick = {
                    if (currentSubscription == null) onPurchase(plan) else onChangePlan(plan)
                },
                enabled = !isSubmitting && !alreadyCurrent,
            )
        }
    }
}

@Composable
private fun PaymentHistory(payments: List<PaymentRecord>) {
    Text(stringResource(R.string.subscription_history_title), style = MaterialTheme.typography.titleLarge)
    if (payments.isEmpty()) {
        Text(stringResource(R.string.subscription_history_empty))
        return
    }

    payments.forEach { payment ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(stringResource(payment.planAssociated.labelResource()))
                Text(
                    stringResource(
                        R.string.subscription_payment_amount,
                        payment.amountSoles.toPlainString(),
                    ),
                )
                Text(stringResource(payment.status.labelResource()))
                Text(stringResource(R.string.subscription_payment_method))
                payment.rejectionReason?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun SubscriptionErrorContent(error: SubscriptionError, onRetry: () -> Unit) {
    val message = when (error) {
        SubscriptionError.Network -> R.string.subscription_error_network
        SubscriptionError.Unauthorized -> R.string.subscription_error_session
        SubscriptionError.Server -> R.string.subscription_error_server
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(message), color = MaterialTheme.colorScheme.error)
        QlicButton(stringResource(R.string.subscription_retry), onRetry)
    }
}

internal fun Plan.labelResource(): Int = when (this) {
    Plan.Trial -> R.string.subscription_plan_trial
    Plan.HomeBasic -> R.string.subscription_plan_home_basic
    Plan.HomePro -> R.string.subscription_plan_home_pro
    Plan.PymeBusiness -> R.string.subscription_plan_pyme_business
    Plan.PymeEnterprise -> R.string.subscription_plan_pyme_enterprise
    Plan.Unknown -> R.string.subscription_plan_unknown
}

private fun SubscriptionStatus.labelResource(): Int = when (this) {
    SubscriptionStatus.TrialActive -> R.string.subscription_status_trial_active
    SubscriptionStatus.TrialExpired -> R.string.subscription_status_trial_expired
    SubscriptionStatus.Active -> R.string.subscription_status_active
    SubscriptionStatus.CancelledPendingExpiration -> R.string.subscription_status_cancelled
    SubscriptionStatus.PaymentRejected -> R.string.subscription_status_payment_rejected
    SubscriptionStatus.Unknown -> R.string.subscription_status_unknown
}

private fun PaymentStatus.labelResource(): Int = when (this) {
    PaymentStatus.Confirmed -> R.string.subscription_payment_confirmed
    PaymentStatus.Rejected -> R.string.subscription_payment_rejected
    PaymentStatus.Unknown -> R.string.subscription_payment_unknown
}

private fun SubscriptionOperation.messageResource(): Int = when (this) {
    SubscriptionOperation.TrialStarted -> R.string.subscription_success_trial
    SubscriptionOperation.Purchased -> R.string.subscription_success_purchase
    SubscriptionOperation.PlanChanged -> R.string.subscription_success_change
    SubscriptionOperation.Cancelled -> R.string.subscription_success_cancel
}
