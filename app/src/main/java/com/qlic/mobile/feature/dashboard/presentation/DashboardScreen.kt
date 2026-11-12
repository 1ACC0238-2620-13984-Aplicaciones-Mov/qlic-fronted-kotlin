package com.qlic.mobile.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.UnavailableFeature
import com.qlic.mobile.feature.subscriptions.domain.Plan

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRetryDevices: () -> Unit,
    onRetryAlerts: () -> Unit,
    onRetrySubscription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.dashboard_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.dashboard_real_data_note), color = MaterialTheme.colorScheme.onSurfaceVariant)

        DashboardCard(stringResource(R.string.dashboard_devices_section)) {
            when {
                state.devices.isLoading -> SectionLoading("dashboard-devices-loading")
                state.devices.error != null -> SectionError(
                    message = stringResource(R.string.dashboard_devices_error),
                    onRetry = onRetryDevices,
                )
                else -> state.devices.data?.let { metrics ->
                    MetricsRow(
                        items = listOf(
                            MetricItem(R.string.dashboard_devices_total, metrics.total, "dashboard-device-total"),
                            MetricItem(R.string.dashboard_devices_online, metrics.online, "dashboard-device-online"),
                            MetricItem(
                                R.string.dashboard_devices_attention,
                                metrics.attentionRequired,
                                "dashboard-device-attention",
                            ),
                        ),
                    )
                }
            }
        }

        DashboardCard(stringResource(R.string.dashboard_alerts_section)) {
            when {
                state.alerts.isLoading -> SectionLoading("dashboard-alerts-loading")
                state.alerts.error != null -> SectionError(
                    message = stringResource(R.string.dashboard_alerts_error),
                    onRetry = onRetryAlerts,
                )
                else -> state.alerts.data?.let { metrics ->
                    MetricsRow(
                        items = listOf(
                            MetricItem(R.string.dashboard_alerts_open, metrics.open, "dashboard-alert-open"),
                            MetricItem(R.string.dashboard_alerts_critical, metrics.critical, "dashboard-alert-critical"),
                            MetricItem(
                                R.string.dashboard_alerts_acknowledged,
                                metrics.acknowledged,
                                "dashboard-alert-acknowledged",
                            ),
                        ),
                    )
                }
            }
        }

        DashboardCard(stringResource(R.string.dashboard_subscription_section)) {
            when {
                state.subscription.isLoading -> SectionLoading("dashboard-subscription-loading")
                state.subscription.error != null -> SectionError(
                    message = stringResource(R.string.dashboard_subscription_error),
                    onRetry = onRetrySubscription,
                )
                state.subscription.data?.currentPlan == null -> Text(stringResource(R.string.dashboard_no_subscription))
                else -> {
                    val metrics = checkNotNull(state.subscription.data)
                    Text(
                        stringResource(checkNotNull(metrics.currentPlan).dashboardLabelResource()),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("dashboard-plan"),
                    )
                    Text(stringResource(R.string.dashboard_plan_capacity, metrics.maxWaterPointsAllowed))
                }
            }
        }

        Text(stringResource(R.string.dashboard_unavailable_section), style = MaterialTheme.typography.titleLarge)
        UnavailableFeature(
            title = stringResource(R.string.telemetry_unavailable_title),
            description = stringResource(R.string.telemetry_unavailable_body),
        )
        UnavailableFeature(
            title = stringResource(R.string.reports_unavailable_title),
            description = stringResource(R.string.reports_unavailable_body),
        )
        UnavailableFeature(
            title = stringResource(R.string.tanks_unavailable_title),
            description = stringResource(R.string.tanks_unavailable_body),
        )
        UnavailableFeature(
            title = stringResource(R.string.support_unavailable_title),
            description = stringResource(R.string.support_unavailable_body),
        )
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun SectionLoading(tag: String) {
    CircularProgressIndicator(
        modifier = Modifier
            .testTag(tag)
            .padding(8.dp),
    )
}

@Composable
private fun SectionError(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        QlicButton(stringResource(R.string.dashboard_retry), onRetry)
    }
}

private data class MetricItem(val label: Int, val value: Int, val tag: String)

@Composable
private fun MetricsRow(items: List<MetricItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        items.forEach { item ->
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(item.label), style = MaterialTheme.typography.labelMedium)
                Text(
                    item.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag(item.tag),
                )
            }
        }
    }
}

private fun Plan.dashboardLabelResource(): Int = when (this) {
    Plan.Trial -> R.string.subscription_plan_trial
    Plan.HomeBasic -> R.string.subscription_plan_home_basic
    Plan.HomePro -> R.string.subscription_plan_home_pro
    Plan.PymeBusiness -> R.string.subscription_plan_pyme_business
    Plan.PymeEnterprise -> R.string.subscription_plan_pyme_enterprise
    Plan.Unknown -> R.string.subscription_plan_unknown
}
