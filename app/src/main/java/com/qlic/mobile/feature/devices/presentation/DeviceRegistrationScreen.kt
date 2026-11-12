package com.qlic.mobile.feature.devices.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField

@Composable
fun DeviceRegistrationScreen(
    state: DevicesUiState,
    onSubmit: (qrPayload: String, serialNumber: String, model: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var qrPayload by rememberSaveable { mutableStateOf("") }
    var serialNumber by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var scannerVisible by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scannerVisible = granted
        permissionDenied = !granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.device_registration_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.device_manual_entry), style = MaterialTheme.typography.bodyMedium)

        QlicTextField(
            value = qrPayload,
            onValueChange = { qrPayload = it },
            label = stringResource(R.string.device_qr_payload),
            isError = state.formError == DeviceFormError.QrPayloadRequired,
            errorMessage = if (state.formError == DeviceFormError.QrPayloadRequired) {
                stringResource(R.string.device_error_qr)
            } else {
                null
            },
        )

        QlicButton(
            text = stringResource(
                if (scannerVisible) R.string.device_stop_scanner else R.string.device_scan_qr,
            ),
            onClick = {
                if (scannerVisible) {
                    scannerVisible = false
                } else if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    scannerVisible = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
        )

        if (permissionDenied) {
            Text(
                text = stringResource(R.string.device_camera_denied),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (scannerVisible) {
            QrScanner(
                onQrCode = { value ->
                    qrPayload = value
                    scannerVisible = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )
        }

        QlicTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            label = stringResource(R.string.device_serial_number),
            isError = state.formError == DeviceFormError.SerialNumberRequired,
            errorMessage = if (state.formError == DeviceFormError.SerialNumberRequired) {
                stringResource(R.string.device_error_serial)
            } else {
                null
            },
        )
        QlicTextField(
            value = model,
            onValueChange = { model = it },
            label = stringResource(R.string.device_model),
            isError = state.formError == DeviceFormError.ModelRequired,
            errorMessage = if (state.formError == DeviceFormError.ModelRequired) {
                stringResource(R.string.device_error_model)
            } else {
                null
            },
        )

        if (state.operationSucceeded) {
            Text(
                text = stringResource(R.string.device_operation_success),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        QlicButton(
            text = stringResource(R.string.device_save),
            onClick = { onSubmit(qrPayload.trim(), serialNumber.trim(), model.trim()) },
            enabled = !state.isSubmitting,
        )
    }
}
