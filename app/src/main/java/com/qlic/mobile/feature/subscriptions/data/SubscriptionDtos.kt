package com.qlic.mobile.feature.subscriptions.data

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class StartTrialDto(@SerializedName("accountId") val accountId: String)

data class PurchaseSubscriptionDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("plan") val plan: String,
    @SerializedName("paymentMethod") val paymentMethod: String = "YAPE_PLIN_OR_CARD",
    @SerializedName("simulatePaymentRejected") val simulatePaymentRejected: Boolean = false,
)

data class ChangePlanDto(@SerializedName("newPlan") val newPlan: String)

data class SubscriptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("currentPlan") val currentPlan: String,
    @SerializedName("nextBillingPlan") val nextBillingPlan: String?,
    @SerializedName("status") val status: String,
    @SerializedName("trialUsed") val trialUsed: Boolean,
    @SerializedName("paidFeaturesEnabled") val paidFeaturesEnabled: Boolean,
    @SerializedName("maxWaterPointsAllowed") val maxWaterPointsAllowed: Int,
    @SerializedName("startedAt") val startedAt: String,
    @SerializedName("currentPeriodEndsAt") val currentPeriodEndsAt: String,
    @SerializedName("autoRenew") val autoRenew: Boolean,
)

data class PaymentRecordDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("subscriptionId") val subscriptionId: String?,
    @SerializedName("planAssociated") val planAssociated: String,
    @SerializedName("amountSoles") val amountSoles: BigDecimal,
    @SerializedName("status") val status: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("rejectionReason") val rejectionReason: String?,
    @SerializedName("paidAt") val paidAt: String,
)

data class PurchaseSubscriptionResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("subscription") val subscription: SubscriptionDto?,
    @SerializedName("payment") val payment: PaymentRecordDto,
)
