package com.qlic.mobile.feature.alerts.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertApi {
    @GET("api/v1/alerts")
    suspend fun getAlerts(@Query("accountId") accountId: String): Response<List<LeakAlertDto>>

    @GET("api/v1/alerts/{alertId}")
    suspend fun getAlert(
        @Path("alertId") alertId: String,
        @Query("accountId") accountId: String,
    ): Response<LeakAlertDto>

    @PATCH("api/v1/alerts/{alertId}/acknowledge")
    suspend fun acknowledgeAlert(
        @Path("alertId") alertId: String,
        @Body request: AcknowledgeAlertDto,
    ): Response<LeakAlertDto>

    @GET("api/v1/alerts/thresholds")
    suspend fun getThresholds(@Query("accountId") accountId: String): Response<List<ConsumptionThresholdDto>>

    @PUT("api/v1/alerts/thresholds")
    suspend fun configureThreshold(@Body request: ConfigureThresholdDto): Response<ConsumptionThresholdDto>

    @GET("api/v1/alerts/preferences")
    suspend fun getPreferences(@Query("accountId") accountId: String): Response<NotificationPreferenceDto>

    @PUT("api/v1/alerts/preferences")
    suspend fun configurePreferences(
        @Body request: ConfigureNotificationPreferenceDto,
    ): Response<NotificationPreferenceDto>

    @GET("api/v1/alerts/recipients")
    suspend fun getRecipients(@Query("accountId") accountId: String): Response<List<AuthorizedRecipientDto>>

    @POST("api/v1/alerts/recipients")
    suspend fun addRecipient(@Body request: AddRecipientDto): Response<AuthorizedRecipientDto>

    @PATCH("api/v1/alerts/recipients/{recipientId}/toggle")
    suspend fun toggleRecipient(
        @Path("recipientId") recipientId: String,
        @Body request: ToggleRecipientDto,
    ): Response<AuthorizedRecipientDto>
}
