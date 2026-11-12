package com.qlic.mobile.feature.support.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.FeatureError
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.support.domain.Incident
import com.qlic.mobile.feature.support.domain.IncidentState

@Composable
fun SupportScreen(
    state: SupportUiState,
    devices: List<Device>,
    onRetry: () -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDeviceSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.support_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.support_title), style = MaterialTheme.typography.headlineMedium)

        IncidentForm(state, devices, onDescriptionChanged, onDeviceSelected, onSubmit)

        state.error?.let { error ->
            FeatureError(
                message = stringResource(
                    when (error) {
                        SupportError.Network -> R.string.support_error_network
                        SupportError.Unauthorized -> R.string.support_error_session
                        SupportError.Server -> R.string.support_error_server
                    },
                ),
                onRetry = onRetry,
                tag = "support-error",
            )
        }

        Text(stringResource(R.string.support_history_heading), style = MaterialTheme.typography.titleMedium)
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("support-loading"),
            )
            state.incidents.isEmpty() -> Text(
                stringResource(R.string.support_history_empty),
                modifier = Modifier.testTag("support-empty"),
            )
            else -> state.incidents.forEach { IncidentCard(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentForm(
    state: SupportUiState,
    devices: List<Device>,
    onDescriptionChanged: (String) -> Unit,
    onDeviceSelected: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("support-form")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.support_form_heading), style = MaterialTheme.typography.titleMedium)
            QlicTextField(
                value = state.description,
                onValueChange = onDescriptionChanged,
                label = stringResource(R.string.support_description),
                modifier = Modifier.testTag("support-description"),
            )
            Text(stringResource(R.string.support_device), style = MaterialTheme.typography.labelLarge)
            if (devices.isEmpty()) {
                Text(stringResource(R.string.support_no_devices), modifier = Modifier.testTag("support-no-devices"))
            }
            devices.forEach { device ->
                FilterChip(
                    selected = state.selectedDeviceId == device.id,
                    onClick = { onDeviceSelected(device.id) },
                    label = { Text("${device.model} · ${device.serialNumber}") },
                    modifier = Modifier.testTag("support-device-${device.id}"),
                )
            }
            state.formError?.let { error ->
                Text(
                    stringResource(
                        when (error) {
                            SupportFormError.EmptyDescription -> R.string.support_error_empty
                            SupportFormError.TooLong -> R.string.support_error_too_long
                            SupportFormError.NoDevice -> R.string.support_error_no_device
                        },
                    ),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("support-form-error"),
                )
            }
            if (state.registered) {
                Text(stringResource(R.string.support_registered), modifier = Modifier.testTag("support-registered"))
            }
            QlicButton(
                text = stringResource(if (state.isSubmitting) R.string.common_saving else R.string.support_submit),
                onClick = onSubmit,
                modifier = Modifier.testTag("support-submit"),
            )
        }
    }
}

@Composable
private fun IncidentCard(incident: Incident) {
    Card(modifier = Modifier.fillMaxWidth().testTag("incident-${incident.id}")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(
                    when (incident.state) {
                        IncidentState.Pending -> R.string.support_state_pending
                        IncidentState.VisitRequested -> R.string.support_state_visit
                        IncidentState.InProgress -> R.string.support_state_progress
                        IncidentState.Resolved -> R.string.support_state_resolved
                        IncidentState.Unknown -> R.string.support_state_unknown
                    },
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(incident.description)
            Text(incident.registeredAt.take(10), style = MaterialTheme.typography.bodySmall)
            incident.resolution?.let {
                Text(stringResource(R.string.support_resolution, it), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
