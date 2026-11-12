package com.qlic.mobile.feature.dashboard.presentation

import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus

data class DashboardUiState(
    val devices: DashboardSection<DeviceMetrics> = DashboardSection(),
    val alerts: DashboardSection<AlertMetrics> = DashboardSection(),
    val subscription: DashboardSection<SubscriptionMetrics> = DashboardSection(),
) {
    val hasFailures: Boolean
        get() = devices.error != null || alerts.error != null || subscription.error != null
}

data class DashboardSection<T>(
    val isLoading: Boolean = true,
    val data: T? = null,
    val error: DashboardError? = null,
)

data class DeviceMetrics(
    val total: Int,
    val online: Int,
    val attentionRequired: Int,
)

data class AlertMetrics(
    val open: Int,
    val critical: Int,
    val acknowledged: Int,
)

data class SubscriptionMetrics(
    val currentPlan: Plan?,
    val status: SubscriptionStatus?,
    val paidFeaturesEnabled: Boolean,
    val maxWaterPointsAllowed: Int,
)

enum class DashboardError { Network, Unauthorized, Server }
