package com.qlic.mobile.feature.alerts.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicError
import com.qlic.mobile.core.designsystem.QlicWarning
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.LeakAlert
import com.qlic.mobile.feature.alerts.domain.Urgency

@Composable
fun AlertsScreen(
    state: AlertsUiState,
    onRetry: () -> Unit,
    onFilterSelected: (AlertFilter) -> Unit,
    onAlertClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.alerts_eyebrow), color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.alerts_title), style = MaterialTheme.typography.headlineMedium)
            }
            TextButton(onClick = onSettingsClick) {
                Text(stringResource(R.string.alerts_settings))
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AlertFilter.entries, key = AlertFilter::name) { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(stringResource(filter.labelResource())) },
                )
            }
        }

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("alerts-loading"),
            )
            state.error != null -> AlertsErrorContent(state.error, onRetry)
            state.filteredAlerts.isEmpty() -> AlertsEmptyContent(hasAlerts = state.alerts.isNotEmpty())
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.filteredAlerts, key = LeakAlert::id) { alert ->
                    AlertCard(alert, onAlertClick)
                }
            }
        }
    }
}

@Composable
private fun AlertsEmptyContent(hasAlerts: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(
                if (hasAlerts) R.string.alerts_empty_filter_title else R.string.alerts_empty_title,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(
                if (hasAlerts) R.string.alerts_empty_filter_body else R.string.alerts_empty_body,
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AlertsErrorContent(error: AlertsError, onRetry: () -> Unit) {
    val message = when (error) {
        AlertsError.Network -> R.string.alerts_error_network
        AlertsError.Unauthorized -> R.string.alerts_error_session
        AlertsError.Server -> R.string.alerts_error_server
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(message), color = MaterialTheme.colorScheme.error)
        QlicButton(stringResource(R.string.alerts_retry), onRetry)
    }
}

@Composable
private fun AlertCard(alert: LeakAlert, onAlertClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAlertClick(alert.id) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.alerts_possible_leak), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(alert.urgency.labelResource()),
                    color = alert.urgency.labelColor(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(alert.waterPointId, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(alert.state.labelResource()), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(
                    R.string.alerts_estimated_volume,
                    alert.estimatedVolumeLiters.stripTrailingZeros().toPlainString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

internal fun AlertFilter.labelResource(): Int = when (this) {
    AlertFilter.All -> R.string.alerts_filter_all
    AlertFilter.Open -> R.string.alerts_filter_open
    AlertFilter.Acknowledged -> R.string.alerts_filter_acknowledged
    AlertFilter.Critical -> R.string.alerts_filter_critical
    AlertFilter.Moderate -> R.string.alerts_filter_moderate
    AlertFilter.Informational -> R.string.alerts_filter_informational
}

internal fun Urgency.labelResource(): Int = when (this) {
    Urgency.Critical -> R.string.alerts_urgency_critical
    Urgency.Moderate -> R.string.alerts_urgency_moderate
    Urgency.Informational -> R.string.alerts_urgency_informational
    Urgency.Unknown -> R.string.alerts_urgency_unknown
}

private fun Urgency.labelColor(): Color = when (this) {
    Urgency.Critical -> QlicError
    Urgency.Moderate -> QlicWarning
    Urgency.Informational, Urgency.Unknown -> Color.Unspecified
}

internal fun AlertState.labelResource(): Int = when (this) {
    AlertState.Active -> R.string.alerts_state_active
    AlertState.Acknowledged -> R.string.alerts_state_acknowledged
    AlertState.Resolved -> R.string.alerts_state_resolved
    AlertState.Unknown -> R.string.alerts_state_unknown
}
