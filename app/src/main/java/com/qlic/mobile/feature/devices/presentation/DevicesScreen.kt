package com.qlic.mobile.feature.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceStatus

@Composable
fun DevicesScreen(
    state: DevicesUiState,
    onRetry: () -> Unit,
    onRegisterClick: () -> Unit,
    onAssignClick: (deviceId: String, waterPointId: String) -> Unit,
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
            Text(stringResource(R.string.devices_title), style = MaterialTheme.typography.headlineMedium)
        }

        QlicButton(
            text = stringResource(R.string.devices_add),
            onClick = onRegisterClick,
            enabled = !state.isSubmitting,
            modifier = Modifier.testTag("devices-add"),
        )

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("devices-loading"),
            )
            state.error != null -> DevicesErrorContent(state.error, onRetry)
            state.devices.isEmpty() -> DevicesEmptyContent()
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.devices, key = Device::id) { device ->
                    DeviceCard(
                        device = device,
                        isSubmitting = state.isSubmitting,
                        formError = state.formError,
                        onAssignClick = onAssignClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun DevicesEmptyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.devices_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.devices_empty_body), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DevicesErrorContent(error: DevicesError, onRetry: () -> Unit) {
    val message = when (error) {
        DevicesError.Network -> R.string.devices_error_network
        DevicesError.Unauthorized -> R.string.devices_error_session
        DevicesError.Server -> R.string.devices_error_server
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(message), color = MaterialTheme.colorScheme.error)
        QlicButton(stringResource(R.string.devices_retry), onRetry)
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    isSubmitting: Boolean,
    formError: DeviceFormError?,
    onAssignClick: (String, String) -> Unit,
) {
    var waterPointId by rememberSaveable(device.id) { mutableStateOf(device.waterPointId.orEmpty()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(device.serialNumber, style = MaterialTheme.typography.titleMedium)
            Text(device.model, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(device.status.labelResource()), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.devices_battery, device.batteryPercentage))
            QlicTextField(
                value = waterPointId,
                onValueChange = { waterPointId = it },
                label = stringResource(R.string.devices_water_point_id),
                isError = formError == DeviceFormError.InvalidWaterPointId,
                errorMessage = if (formError == DeviceFormError.InvalidWaterPointId) {
                    stringResource(R.string.device_error_water_point)
                } else {
                    null
                },
            )
            QlicButton(
                text = stringResource(R.string.devices_assign),
                onClick = { onAssignClick(device.id, waterPointId.trim()) },
                enabled = !isSubmitting,
            )
        }
    }
}

private fun DeviceStatus.labelResource(): Int = when (this) {
    DeviceStatus.Online -> R.string.devices_status_online
    DeviceStatus.Offline -> R.string.devices_status_offline
    DeviceStatus.LowBattery -> R.string.devices_status_low_battery
    DeviceStatus.Calibrating -> R.string.devices_status_calibrating
    DeviceStatus.Unknown -> R.string.devices_status_unknown
}
