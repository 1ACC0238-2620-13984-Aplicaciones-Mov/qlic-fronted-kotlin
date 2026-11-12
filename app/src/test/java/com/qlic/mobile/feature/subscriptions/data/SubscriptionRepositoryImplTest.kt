package com.qlic.mobile.feature.subscriptions.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.feature.auth.domain.AuthSession
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SubscriptionRepositoryImplTest {
    @Test
    fun `current subscription uses session account and maps response`() = runBlocking {
        val api = FakeSubscriptionApi(currentResponse = Response.success(subscriptionDto()))
        val repository = SubscriptionRepositoryImpl(api, authenticatedSnapshot())

        val result = repository.getCurrentSubscription()

        assertEquals("account-id", api.currentAccountId)
        assertEquals(Plan.HomePro, (result as ApiResult.Success).value?.currentPlan)
        assertEquals(SubscriptionStatus.Active, result.value?.status)
    }

    @Test
    fun `current subscription 404 becomes successful empty state`() = runBlocking {
        val api = FakeSubscriptionApi(currentResponse = errorResponse(404))

        val result = SubscriptionRepositoryImpl(api, authenticatedSnapshot()).getCurrentSubscription()

        assertTrue(result is ApiResult.Success)
        assertNull((result as ApiResult.Success).value)
    }

    @Test
    fun `trial injects account from session`() = runBlocking {
        val api = FakeSubscriptionApi()

        val result = SubscriptionRepositoryImpl(api, authenticatedSnapshot()).startTrial()

        assertTrue(result is ApiResult.Success)
        assertEquals("account-id", api.trialRequest?.accountId)
    }

    @Test
    fun `purchase uses supported simulated payment method and never accepts card data`() = runBlocking {
        val api = FakeSubscriptionApi()

        val result = SubscriptionRepositoryImpl(api, authenticatedSnapshot()).purchase(Plan.HomeBasic)

        assertTrue(result is ApiResult.Success)
        assertEquals("account-id", api.purchaseRequest?.accountId)
        assertEquals("HOME_BASIC", api.purchaseRequest?.plan)
        assertEquals("YAPE_PLIN_OR_CARD", api.purchaseRequest?.paymentMethod)
        assertFalse(api.purchaseRequest?.simulatePaymentRejected ?: true)
    }

    @Test
    fun `plan change and cancellation use current subscription identifier`() = runBlocking {
        val api = FakeSubscriptionApi()
        val repository = SubscriptionRepositoryImpl(api, authenticatedSnapshot())

        repository.changePlan("subscription-id", Plan.PymeBusiness)
        repository.cancel("subscription-id")

        assertEquals("subscription-id", api.changedSubscriptionId)
        assertEquals("PYME_BUSINESS", api.changeRequest?.newPlan)
        assertEquals("subscription-id", api.cancelledSubscriptionId)
    }

    @Test
    fun `payment history uses session account and maps records`() = runBlocking {
        val api = FakeSubscriptionApi()

        val result = SubscriptionRepositoryImpl(api, authenticatedSnapshot()).getPaymentHistory()

        assertEquals("account-id", api.paymentAccountId)
        assertEquals("29.90", (result as ApiResult.Success).value.single().amountSoles.toPlainString())
    }

    @Test
    fun `missing session is unauthorized without calling backend`() = runBlocking {
        val api = FakeSubscriptionApi()

        val result = SubscriptionRepositoryImpl(api, SessionSnapshot()).getCurrentSubscription()

        assertEquals(ApiResult.Unauthorized, result)
        assertFalse(api.currentCalled)
    }

    private fun authenticatedSnapshot() = SessionSnapshot().apply {
        updateSession(AuthSession("jwt", "account-id"))
    }

    private fun subscriptionDto() = SubscriptionDto(
        id = "subscription-id",
        accountId = "account-id",
        currentPlan = "HOME_PRO",
        nextBillingPlan = null,
        status = "ACTIVE",
        trialUsed = true,
        paidFeaturesEnabled = true,
        maxWaterPointsAllowed = 6,
        startedAt = "2026-11-01T10:00:00",
        currentPeriodEndsAt = "2026-12-01T10:00:00",
        autoRenew = true,
    )

    private fun paymentDto() = PaymentRecordDto(
        id = "payment-id",
        accountId = "account-id",
        subscriptionId = "subscription-id",
        planAssociated = "HOME_BASIC",
        amountSoles = java.math.BigDecimal("29.90"),
        status = "CONFIRMED",
        paymentMethod = "YAPE_PLIN_OR_CARD",
        rejectionReason = null,
        paidAt = "2026-11-01T10:00:00",
    )

    private fun <T> errorResponse(code: Int): Response<T> = Response.error(
        code,
        "{}".toResponseBody("application/problem+json".toMediaType()),
    )

    private inner class FakeSubscriptionApi(
        private val currentResponse: Response<SubscriptionDto> = Response.success(subscriptionDto()),
    ) : SubscriptionApi {
        var currentCalled = false
        var currentAccountId: String? = null
        var trialRequest: StartTrialDto? = null
        var purchaseRequest: PurchaseSubscriptionDto? = null
        var changedSubscriptionId: String? = null
        var changeRequest: ChangePlanDto? = null
        var cancelledSubscriptionId: String? = null
        var paymentAccountId: String? = null

        override suspend fun getCurrent(accountId: String): Response<SubscriptionDto> {
            currentCalled = true
            currentAccountId = accountId
            return currentResponse
        }

        override suspend fun startTrial(request: StartTrialDto): Response<SubscriptionDto> {
            trialRequest = request
            return Response.success(201, subscriptionDto().copy(currentPlan = "TRIAL", status = "TRIAL_ACTIVE"))
        }

        override suspend fun purchase(request: PurchaseSubscriptionDto): Response<PurchaseSubscriptionResponseDto> {
            purchaseRequest = request
            return Response.success(
                201,
                PurchaseSubscriptionResponseDto("Activated", subscriptionDto(), paymentDto()),
            )
        }

        override suspend fun changePlan(
            subscriptionId: String,
            request: ChangePlanDto,
        ): Response<SubscriptionDto> {
            changedSubscriptionId = subscriptionId
            changeRequest = request
            return Response.success(subscriptionDto().copy(nextBillingPlan = request.newPlan))
        }

        override suspend fun cancel(subscriptionId: String): Response<SubscriptionDto> {
            cancelledSubscriptionId = subscriptionId
            return Response.success(
                subscriptionDto().copy(status = "CANCELLED_PENDING_EXPIRATION", autoRenew = false),
            )
        }

        override suspend fun getPayments(accountId: String): Response<List<PaymentRecordDto>> {
            paymentAccountId = accountId
            return Response.success(listOf(paymentDto()))
        }
    }
}
