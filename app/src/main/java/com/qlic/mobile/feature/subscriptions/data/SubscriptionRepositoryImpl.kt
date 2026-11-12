package com.qlic.mobile.feature.subscriptions.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.PaymentStatus
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.PurchaseResult
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import java.time.LocalDateTime

class SubscriptionRepositoryImpl(
    private val api: SubscriptionApi,
    private val sessionSnapshot: SessionSnapshot,
) : SubscriptionRepository {
    override suspend fun getCurrentSubscription(): ApiResult<Subscription?> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return when (val result = apiCall { api.getCurrent(accountId) }) {
            is ApiResult.Success -> ApiResult.Success(result.value.toDomain())
            is ApiResult.Failure -> if (result.statusCode == 404) ApiResult.Success(null) else result
            ApiResult.Empty -> ApiResult.Success(null)
            ApiResult.NetworkError -> ApiResult.NetworkError
            ApiResult.Unauthorized -> ApiResult.Unauthorized
        }
    }

    override suspend fun getPaymentHistory(): ApiResult<List<PaymentRecord>> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getPayments(accountId) }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun startTrial(): ApiResult<Subscription> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.startTrial(StartTrialDto(accountId)) }.map { it.toDomain() }
    }

    override suspend fun purchase(plan: Plan): ApiResult<PurchaseResult> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        if (plan !in Plan.purchasable) return ApiResult.Failure()
        return apiCall {
            api.purchase(PurchaseSubscriptionDto(accountId = accountId, plan = plan.toApiValue()))
        }.map { response ->
            PurchaseResult(
                message = response.message,
                subscription = response.subscription?.toDomain(),
                payment = response.payment.toDomain(),
            )
        }
    }

    override suspend fun changePlan(subscriptionId: String, plan: Plan): ApiResult<Subscription> {
        if (accountId() == null) return ApiResult.Unauthorized
        if (plan !in Plan.purchasable) return ApiResult.Failure()
        return apiCall { api.changePlan(subscriptionId, ChangePlanDto(plan.toApiValue())) }
            .map { it.toDomain() }
    }

    override suspend fun cancel(subscriptionId: String): ApiResult<Subscription> {
        if (accountId() == null) return ApiResult.Unauthorized
        return apiCall { api.cancel(subscriptionId) }.map { it.toDomain() }
    }

    private fun accountId(): String? = sessionSnapshot.currentSession()?.accountId

    private fun SubscriptionDto.toDomain() = Subscription(
        id = id,
        accountId = accountId,
        currentPlan = currentPlan.toPlan(),
        nextBillingPlan = nextBillingPlan?.toPlan(),
        status = when (status) {
            "TRIAL_ACTIVE" -> SubscriptionStatus.TrialActive
            "TRIAL_EXPIRED" -> SubscriptionStatus.TrialExpired
            "ACTIVE" -> SubscriptionStatus.Active
            "CANCELLED_PENDING_EXPIRATION" -> SubscriptionStatus.CancelledPendingExpiration
            "PAYMENT_REJECTED" -> SubscriptionStatus.PaymentRejected
            else -> SubscriptionStatus.Unknown
        },
        trialUsed = trialUsed,
        paidFeaturesEnabled = paidFeaturesEnabled,
        maxWaterPointsAllowed = maxWaterPointsAllowed,
        startedAt = startedAt.toDateOrNull(),
        currentPeriodEndsAt = currentPeriodEndsAt.toDateOrNull(),
        autoRenew = autoRenew,
    )

    private fun PaymentRecordDto.toDomain() = PaymentRecord(
        id = id,
        subscriptionId = subscriptionId,
        planAssociated = planAssociated.toPlan(),
        amountSoles = amountSoles,
        status = when (status) {
            "CONFIRMED" -> PaymentStatus.Confirmed
            "REJECTED" -> PaymentStatus.Rejected
            else -> PaymentStatus.Unknown
        },
        paymentMethod = paymentMethod,
        rejectionReason = rejectionReason,
        paidAt = paidAt.toDateOrNull(),
    )

    private fun String.toPlan(): Plan = when (this) {
        "TRIAL" -> Plan.Trial
        "HOME_BASIC" -> Plan.HomeBasic
        "HOME_PRO" -> Plan.HomePro
        "PYME_BUSINESS" -> Plan.PymeBusiness
        "PYME_ENTERPRISE" -> Plan.PymeEnterprise
        else -> Plan.Unknown
    }

    private fun Plan.toApiValue(): String = when (this) {
        Plan.Trial -> "TRIAL"
        Plan.HomeBasic -> "HOME_BASIC"
        Plan.HomePro -> "HOME_PRO"
        Plan.PymeBusiness -> "PYME_BUSINESS"
        Plan.PymeEnterprise -> "PYME_ENTERPRISE"
        Plan.Unknown -> "UNKNOWN"
    }

    private fun String.toDateOrNull(): LocalDateTime? = runCatching { LocalDateTime.parse(this) }.getOrNull()

    private inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(value))
        ApiResult.Empty -> ApiResult.Empty
        ApiResult.NetworkError -> ApiResult.NetworkError
        ApiResult.Unauthorized -> ApiResult.Unauthorized
        is ApiResult.Failure -> this
    }
}
