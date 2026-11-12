package com.qlic.mobile.feature.subscriptions.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SubscriptionApi {
    @GET("api/v1/subscriptions/current")
    suspend fun getCurrent(@Query("accountId") accountId: String): Response<SubscriptionDto>

    @POST("api/v1/subscriptions/trial")
    suspend fun startTrial(@Body request: StartTrialDto): Response<SubscriptionDto>

    @POST("api/v1/subscriptions")
    suspend fun purchase(
        @Body request: PurchaseSubscriptionDto,
    ): Response<PurchaseSubscriptionResponseDto>

    @PATCH("api/v1/subscriptions/{subscriptionId}/plan")
    suspend fun changePlan(
        @Path("subscriptionId") subscriptionId: String,
        @Body request: ChangePlanDto,
    ): Response<SubscriptionDto>

    @POST("api/v1/subscriptions/{subscriptionId}/cancel")
    suspend fun cancel(@Path("subscriptionId") subscriptionId: String): Response<SubscriptionDto>

    @GET("api/v1/subscriptions/payments")
    suspend fun getPayments(@Query("accountId") accountId: String): Response<List<PaymentRecordDto>>
}
