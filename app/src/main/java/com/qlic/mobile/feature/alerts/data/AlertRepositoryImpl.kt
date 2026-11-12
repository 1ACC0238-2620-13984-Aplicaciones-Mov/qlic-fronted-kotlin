package com.qlic.mobile.feature.alerts.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.alerts.domain.AlertRepository
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.AuthorizedRecipient
import com.qlic.mobile.feature.alerts.domain.ConfigureThresholdInput
import com.qlic.mobile.feature.alerts.domain.ConsumptionThreshold
import com.qlic.mobile.feature.alerts.domain.LeakAlert
import com.qlic.mobile.feature.alerts.domain.NotificationPreference
import com.qlic.mobile.feature.alerts.domain.NotificationPreferenceInput
import com.qlic.mobile.feature.alerts.domain.RecipientInput
import com.qlic.mobile.feature.alerts.domain.ThresholdSource
import com.qlic.mobile.feature.alerts.domain.Urgency
import java.time.LocalDateTime

class AlertRepositoryImpl(
    private val api: AlertApi,
    private val sessionSnapshot: SessionSnapshot,
) : AlertRepository {
    override suspend fun getAlerts(): ApiResult<List<LeakAlert>> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getAlerts(accountId) }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAlert(alertId: String): ApiResult<LeakAlert> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getAlert(alertId, accountId) }.map { it.toDomain() }
    }

    override suspend fun acknowledgeAlert(
        alertId: String,
        actionTaken: String,
    ): ApiResult<LeakAlert> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall {
            api.acknowledgeAlert(alertId, AcknowledgeAlertDto(accountId, actionTaken))
        }.map { it.toDomain() }
    }

    override suspend fun getThresholds(): ApiResult<List<ConsumptionThreshold>> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getThresholds(accountId) }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun configureThreshold(
        input: ConfigureThresholdInput,
    ): ApiResult<ConsumptionThreshold> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall {
            api.configureThreshold(
                ConfigureThresholdDto(
                    accountId = accountId,
                    waterPointId = input.waterPointId,
                    period = input.period,
                    customLimitLitersPerHour = input.customLimitLitersPerHour,
                    baselineLitersPerHour = input.baselineLitersPerHour,
                ),
            )
        }.map { it.toDomain() }
    }

    override suspend fun getNotificationPreference(): ApiResult<NotificationPreference> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getPreferences(accountId) }.map { it.toDomain() }
    }

    override suspend fun configureNotificationPreference(
        input: NotificationPreferenceInput,
    ): ApiResult<NotificationPreference> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall {
            api.configurePreferences(
                ConfigureNotificationPreferenceDto(
                    accountId = accountId,
                    groupingWindowMinutes = input.groupingWindowMinutes,
                    minimumUrgency = input.minimumUrgency.toApiValue(),
                ),
            )
        }.map { it.toDomain() }
    }

    override suspend fun getRecipients(): ApiResult<List<AuthorizedRecipient>> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall { api.getRecipients(accountId) }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addRecipient(input: RecipientInput): ApiResult<AuthorizedRecipient> {
        val accountId = accountId() ?: return ApiResult.Unauthorized
        return apiCall {
            api.addRecipient(
                AddRecipientDto(
                    accountId = accountId,
                    fullName = input.fullName,
                    email = input.email,
                    devicePushToken = input.devicePushToken,
                    enabled = input.enabled,
                ),
            )
        }.map { it.toDomain() }
    }

    override suspend fun toggleRecipient(
        recipientId: String,
        enabled: Boolean,
    ): ApiResult<AuthorizedRecipient> {
        if (accountId() == null) return ApiResult.Unauthorized
        return apiCall {
            api.toggleRecipient(recipientId, ToggleRecipientDto(enabled))
        }.map { it.toDomain() }
    }

    private fun accountId(): String? = sessionSnapshot.currentSession()?.accountId

    private fun LeakAlertDto.toDomain() = LeakAlert(
        id = id,
        accountId = accountId,
        waterPointId = waterPointId,
        state = when (state) {
            "ACTIVE" -> AlertState.Active
            "ACKNOWLEDGED" -> AlertState.Acknowledged
            "RESOLVED" -> AlertState.Resolved
            else -> AlertState.Unknown
        },
        urgency = urgency.toUrgency(),
        detectedAt = detectedAt.toLocalDateTimeOrNull(),
        estimatedVolumeLiters = estimatedVolumeLiters,
        estimatedCostSoles = estimatedCostSoles,
        recommendedAction = recommendedAction,
        acknowledgedAction = acknowledgedAction,
        previousAlertId = previousAlertId,
        pushDispatched = pushDispatched,
        notifiedRecipientsCount = notifiedRecipientsCount,
    )

    private fun ConsumptionThresholdDto.toDomain() = ConsumptionThreshold(
        id = id,
        waterPointId = waterPointId,
        period = period,
        customLimitLitersPerHour = customLimitLitersPerHour,
        baselineLitersPerHour = baselineLitersPerHour,
        effectiveLimitLitersPerHour = effectiveLimitLitersPerHour,
        source = when (source) {
            "USER_DEFINED" -> ThresholdSource.UserDefined
            "SYSTEM_CALCULATED" -> ThresholdSource.SystemCalculated
            else -> ThresholdSource.Unknown
        },
        updatedAt = updatedAt.toLocalDateTimeOrNull(),
    )

    private fun NotificationPreferenceDto.toDomain() = NotificationPreference(
        id = id,
        groupingWindowMinutes = groupingWindowMinutes,
        minimumUrgency = minimumUrgency.toUrgency(),
        updatedAt = updatedAt.toLocalDateTimeOrNull(),
    )

    private fun AuthorizedRecipientDto.toDomain() = AuthorizedRecipient(
        id = id,
        fullName = fullName,
        email = email,
        devicePushToken = devicePushToken,
        enabled = enabled,
        lastNotifiedAt = lastNotifiedAt?.toLocalDateTimeOrNull(),
    )

    private fun String.toUrgency(): Urgency = when (this) {
        "INFORMATIONAL" -> Urgency.Informational
        "MODERATE" -> Urgency.Moderate
        "CRITICAL" -> Urgency.Critical
        else -> Urgency.Unknown
    }

    private fun Urgency.toApiValue(): String = when (this) {
        Urgency.Informational -> "INFORMATIONAL"
        Urgency.Moderate -> "MODERATE"
        Urgency.Critical -> "CRITICAL"
        Urgency.Unknown -> "INFORMATIONAL"
    }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
        runCatching { LocalDateTime.parse(this) }.getOrNull()

    private inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(value))
        ApiResult.Empty -> ApiResult.Empty
        ApiResult.NetworkError -> ApiResult.NetworkError
        ApiResult.Unauthorized -> ApiResult.Unauthorized
        is ApiResult.Failure -> this
    }
}
