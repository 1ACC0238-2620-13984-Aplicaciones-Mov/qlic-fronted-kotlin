package com.qlic.mobile.feature.subscriptions.domain

import java.math.BigDecimal
import java.time.LocalDateTime

enum class Plan(
    val monthlyPriceSoles: BigDecimal,
    val maxWaterPoints: Int,
) {
    Trial(BigDecimal("0.00"), 2),
    HomeBasic(BigDecimal("29.90"), 3),
    HomePro(BigDecimal("49.90"), 6),
    PymeBusiness(BigDecimal("99.90"), 15),
    PymeEnterprise(BigDecimal("199.90"), 50),
    Unknown(BigDecimal.ZERO, 0),
    ;

    companion object {
        val purchasable: List<Plan> = listOf(HomeBasic, HomePro, PymeBusiness, PymeEnterprise)
    }
}

enum class SubscriptionStatus {
    TrialActive,
    TrialExpired,
    Active,
    CancelledPendingExpiration,
    PaymentRejected,
    Unknown,
}

enum class PaymentStatus { Confirmed, Rejected, Unknown }

data class Subscription(
    val id: String,
    val accountId: String,
    val currentPlan: Plan,
    val nextBillingPlan: Plan?,
    val status: SubscriptionStatus,
    val trialUsed: Boolean,
    val paidFeaturesEnabled: Boolean,
    val maxWaterPointsAllowed: Int,
    val startedAt: LocalDateTime?,
    val currentPeriodEndsAt: LocalDateTime?,
    val autoRenew: Boolean,
)

data class PaymentRecord(
    val id: String,
    val subscriptionId: String?,
    val planAssociated: Plan,
    val amountSoles: BigDecimal,
    val status: PaymentStatus,
    val paymentMethod: String,
    val rejectionReason: String?,
    val paidAt: LocalDateTime?,
)

data class PurchaseResult(
    val message: String,
    val subscription: Subscription?,
    val payment: PaymentRecord,
)
