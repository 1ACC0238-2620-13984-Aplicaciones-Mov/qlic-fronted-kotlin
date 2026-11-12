package com.qlic.mobile.feature.alerts.presentation

import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.AuthorizedRecipient
import com.qlic.mobile.feature.alerts.domain.ConsumptionThreshold
import com.qlic.mobile.feature.alerts.domain.LeakAlert
import com.qlic.mobile.feature.alerts.domain.NotificationPreference
import com.qlic.mobile.feature.alerts.domain.Urgency

data class AlertsUiState(
    val isLoading: Boolean = false,
    val alerts: List<LeakAlert> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.All,
    val error: AlertsError? = null,
    val selectedAlert: LeakAlert? = null,
    val isDetailLoading: Boolean = false,
    val isSettingsLoading: Boolean = false,
    val thresholds: List<ConsumptionThreshold> = emptyList(),
    val notificationPreference: NotificationPreference? = null,
    val recipients: List<AuthorizedRecipient> = emptyList(),
    val isSubmitting: Boolean = false,
    val formError: AlertFormError? = null,
    val operationSucceeded: Boolean = false,
) {
    val filteredAlerts: List<LeakAlert>
        get() = alerts.filter { alert ->
            when (selectedFilter) {
                AlertFilter.All -> true
                AlertFilter.Open -> alert.state == AlertState.Active
                AlertFilter.Acknowledged -> alert.state == AlertState.Acknowledged
                AlertFilter.Critical -> alert.urgency == Urgency.Critical
                AlertFilter.Moderate -> alert.urgency == Urgency.Moderate
                AlertFilter.Informational -> alert.urgency == Urgency.Informational
            }
        }
}

enum class AlertFilter {
    All,
    Open,
    Acknowledged,
    Critical,
    Moderate,
    Informational,
}

enum class AlertsError {
    Network,
    Unauthorized,
    Server,
}

enum class AlertFormError {
    ActionRequired,
    WaterPointRequired,
    PeriodRequired,
    InvalidCustomLimit,
    InvalidBaseline,
    InvalidGroupingWindow,
    InvalidRecipientName,
    InvalidRecipientEmail,
    PushTokenRequired,
}
