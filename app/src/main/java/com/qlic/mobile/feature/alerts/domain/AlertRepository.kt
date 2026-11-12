package com.qlic.mobile.feature.alerts.domain

import com.qlic.mobile.core.network.ApiResult

interface AlertRepository {
    suspend fun getAlerts(): ApiResult<List<LeakAlert>>
    suspend fun getAlert(alertId: String): ApiResult<LeakAlert>
    suspend fun acknowledgeAlert(alertId: String, actionTaken: String): ApiResult<LeakAlert>
    suspend fun getThresholds(): ApiResult<List<ConsumptionThreshold>>
    suspend fun configureThreshold(input: ConfigureThresholdInput): ApiResult<ConsumptionThreshold>
    suspend fun getNotificationPreference(): ApiResult<NotificationPreference>
    suspend fun configureNotificationPreference(input: NotificationPreferenceInput): ApiResult<NotificationPreference>
    suspend fun getRecipients(): ApiResult<List<AuthorizedRecipient>>
    suspend fun addRecipient(input: RecipientInput): ApiResult<AuthorizedRecipient>
    suspend fun toggleRecipient(recipientId: String, enabled: Boolean): ApiResult<AuthorizedRecipient>
}
