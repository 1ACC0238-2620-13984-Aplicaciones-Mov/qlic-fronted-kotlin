package com.qlic.mobile.feature.alerts.data

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class LeakAlertDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("waterPointId") val waterPointId: String,
    @SerializedName("state") val state: String,
    @SerializedName("urgency") val urgency: String,
    @SerializedName("detectedAt") val detectedAt: String,
    @SerializedName("estimatedVolumeLiters") val estimatedVolumeLiters: BigDecimal,
    @SerializedName("estimatedCostSoles") val estimatedCostSoles: BigDecimal,
    @SerializedName("recommendedAction") val recommendedAction: String,
    @SerializedName("acknowledgedAction") val acknowledgedAction: String?,
    @SerializedName("previousAlertId") val previousAlertId: String?,
    @SerializedName("pushDispatched") val pushDispatched: Boolean = true,
    @SerializedName("notifiedRecipientsCount") val notifiedRecipientsCount: Int = 0,
)

data class AcknowledgeAlertDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("actionTaken") val actionTaken: String,
)

data class ConfigureThresholdDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("waterPointId") val waterPointId: String,
    @SerializedName("period") val period: String,
    @SerializedName("customLimitLitersPerHour") val customLimitLitersPerHour: BigDecimal?,
    @SerializedName("baselineLitersPerHour") val baselineLitersPerHour: BigDecimal,
)

data class ConsumptionThresholdDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("waterPointId") val waterPointId: String,
    @SerializedName("period") val period: String,
    @SerializedName("customLimitLitersPerHour") val customLimitLitersPerHour: BigDecimal?,
    @SerializedName("baselineLitersPerHour") val baselineLitersPerHour: BigDecimal,
    @SerializedName("effectiveLimitLitersPerHour") val effectiveLimitLitersPerHour: BigDecimal,
    @SerializedName("source") val source: String,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class ConfigureNotificationPreferenceDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("groupingWindowMinutes") val groupingWindowMinutes: Int,
    @SerializedName("minimumUrgency") val minimumUrgency: String,
)

data class NotificationPreferenceDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("groupingWindowMinutes") val groupingWindowMinutes: Int,
    @SerializedName("minimumUrgency") val minimumUrgency: String,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class AddRecipientDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("devicePushToken") val devicePushToken: String,
    @SerializedName("enabled") val enabled: Boolean,
)

data class ToggleRecipientDto(@SerializedName("enabled") val enabled: Boolean)

data class AuthorizedRecipientDto(
    @SerializedName("id") val id: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("devicePushToken") val devicePushToken: String,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("lastNotifiedAt") val lastNotifiedAt: String?,
)
