package com.qlic.mobile.feature.subscriptions.domain

import com.qlic.mobile.core.network.ApiResult

interface SubscriptionRepository {
    suspend fun getCurrentSubscription(): ApiResult<Subscription?>
    suspend fun getPaymentHistory(): ApiResult<List<PaymentRecord>>
    suspend fun startTrial(): ApiResult<Subscription>
    suspend fun purchase(plan: Plan): ApiResult<PurchaseResult>
    suspend fun changePlan(subscriptionId: String, plan: Plan): ApiResult<Subscription>
    suspend fun cancel(subscriptionId: String): ApiResult<Subscription>
}
