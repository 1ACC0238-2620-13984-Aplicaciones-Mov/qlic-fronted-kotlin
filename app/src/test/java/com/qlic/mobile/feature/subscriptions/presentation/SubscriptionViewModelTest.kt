package com.qlic.mobile.feature.subscriptions.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.PaymentStatus
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.PurchaseResult
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    @Test
    fun `load exposes empty current subscription and payment history`() = runTest {
        val repository = FakeSubscriptionRepository()
        val viewModel = SubscriptionViewModel(repository, StandardTestDispatcher(testScheduler))

        advanceUntilIdle()

        assertNull(viewModel.state.value.subscription)
        assertEquals(1, viewModel.state.value.payments.size)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `only paid plans are purchasable`() {
        assertFalse(SubscriptionUiState().purchasablePlans.contains(Plan.Trial))
        assertFalse(SubscriptionUiState().purchasablePlans.contains(Plan.Unknown))
        assertEquals(4, SubscriptionUiState().purchasablePlans.size)
    }

    @Test
    fun `starting trial updates current subscription`() = runTest {
        val repository = FakeSubscriptionRepository()
        val viewModel = SubscriptionViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.startTrial()
        advanceUntilIdle()

        assertEquals(Plan.Trial, viewModel.state.value.subscription?.currentPlan)
        assertEquals(SubscriptionOperation.TrialStarted, viewModel.state.value.operation)
    }

    @Test
    fun `purchase updates subscription and prepends payment`() = runTest {
        val repository = FakeSubscriptionRepository()
        val viewModel = SubscriptionViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.purchase(Plan.HomePro)
        advanceUntilIdle()

        assertEquals(Plan.HomePro, viewModel.state.value.subscription?.currentPlan)
        assertEquals("new-payment", viewModel.state.value.payments.first().id)
    }

    @Test
    fun `plan change and cancellation update only subscription`() = runTest {
        val repository = FakeSubscriptionRepository().apply {
            current = ApiResult.Success(subscription())
        }
        val viewModel = SubscriptionViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        val paymentsBefore = viewModel.state.value.payments

        viewModel.changePlan(Plan.PymeBusiness)
        advanceUntilIdle()
        assertEquals(Plan.PymeBusiness, viewModel.state.value.subscription?.nextBillingPlan)

        viewModel.cancel()
        advanceUntilIdle()
        assertEquals(SubscriptionStatus.CancelledPendingExpiration, viewModel.state.value.subscription?.status)
        assertEquals(paymentsBefore, viewModel.state.value.payments)
    }

    @Test
    fun `repeated purchase is ignored while first request is running`() = runTest {
        val repository = FakeSubscriptionRepository().apply { purchaseGate = CompletableDeferred() }
        val viewModel = SubscriptionViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.purchase(Plan.HomeBasic)
        viewModel.purchase(Plan.HomePro)
        testScheduler.runCurrent()

        assertEquals(1, repository.purchaseCalls)
        assertTrue(viewModel.state.value.isSubmitting)

        repository.purchaseGate?.complete(ApiResult.Success(purchaseResult(Plan.HomeBasic)))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSubmitting)
    }

    private fun subscription(
        plan: Plan = Plan.HomeBasic,
        status: SubscriptionStatus = SubscriptionStatus.Active,
    ) = Subscription(
        id = "subscription-id",
        accountId = "account-id",
        currentPlan = plan,
        nextBillingPlan = null,
        status = status,
        trialUsed = true,
        paidFeaturesEnabled = true,
        maxWaterPointsAllowed = 3,
        startedAt = null,
        currentPeriodEndsAt = null,
        autoRenew = true,
    )

    private fun payment(id: String = "payment-id") = PaymentRecord(
        id = id,
        subscriptionId = "subscription-id",
        planAssociated = Plan.HomeBasic,
        amountSoles = BigDecimal("29.90"),
        status = PaymentStatus.Confirmed,
        paymentMethod = "YAPE_PLIN_OR_CARD",
        rejectionReason = null,
        paidAt = null,
    )

    private fun purchaseResult(plan: Plan) = PurchaseResult(
        message = "Activated",
        subscription = subscription(plan),
        payment = payment("new-payment"),
    )

    private inner class FakeSubscriptionRepository : SubscriptionRepository {
        var current: ApiResult<Subscription?> = ApiResult.Success(null)
        var purchaseGate: CompletableDeferred<ApiResult<PurchaseResult>>? = null
        var purchaseCalls = 0

        override suspend fun getCurrentSubscription(): ApiResult<Subscription?> = current
        override suspend fun getPaymentHistory(): ApiResult<List<PaymentRecord>> =
            ApiResult.Success(listOf(payment()))

        override suspend fun startTrial(): ApiResult<Subscription> =
            ApiResult.Success(subscription(Plan.Trial, SubscriptionStatus.TrialActive))

        override suspend fun purchase(plan: Plan): ApiResult<PurchaseResult> {
            purchaseCalls++
            return purchaseGate?.await() ?: ApiResult.Success(purchaseResult(plan))
        }

        override suspend fun changePlan(subscriptionId: String, plan: Plan): ApiResult<Subscription> =
            ApiResult.Success(subscription().copy(nextBillingPlan = plan))

        override suspend fun cancel(subscriptionId: String): ApiResult<Subscription> =
            ApiResult.Success(
                subscription(status = SubscriptionStatus.CancelledPendingExpiration).copy(autoRenew = false),
            )
    }
}
