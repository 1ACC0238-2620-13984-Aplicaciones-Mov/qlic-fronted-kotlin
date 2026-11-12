package com.qlic.mobile.feature.alerts.domain

import java.math.BigDecimal
import java.time.LocalDateTime

enum class AlertState { Active, Acknowledged, Resolved, Unknown }

enum class Urgency { Informational, Moderate, Critical, Unknown }

enum class ThresholdSource { UserDefined, SystemCalculated, Unknown }

data class LeakAlert(
    val id: String,
    val accountId: String,
    val waterPointId: String,
    val state: AlertState,
    val urgency: Urgency,
    val detectedAt: LocalDateTime?,
    val estimatedVolumeLiters: BigDecimal,
    val estimatedCostSoles: BigDecimal,
    val recommendedAction: String,
    val acknowledgedAction: String?,
    val previousAlertId: String?,
    val pushDispatched: Boolean,
    val notifiedRecipientsCount: Int,
)

data class ConfigureThresholdInput(
    val waterPointId: String,
    val period: String,
    val customLimitLitersPerHour: BigDecimal?,
    val baselineLitersPerHour: BigDecimal,
)

data class ConsumptionThreshold(
    val id: String,
    val waterPointId: String,
    val period: String,
    val customLimitLitersPerHour: BigDecimal?,
    val baselineLitersPerHour: BigDecimal,
    val effectiveLimitLitersPerHour: BigDecimal,
    val source: ThresholdSource,
    val updatedAt: LocalDateTime?,
)

data class NotificationPreferenceInput(
    val groupingWindowMinutes: Int,
    val minimumUrgency: Urgency,
)

data class NotificationPreference(
    val id: String,
    val groupingWindowMinutes: Int,
    val minimumUrgency: Urgency,
    val updatedAt: LocalDateTime?,
)

data class RecipientInput(
    val fullName: String,
    val email: String,
    val devicePushToken: String,
    val enabled: Boolean,
)

data class AuthorizedRecipient(
    val id: String,
    val fullName: String,
    val email: String,
    val devicePushToken: String,
    val enabled: Boolean,
    val lastNotifiedAt: LocalDateTime?,
)
