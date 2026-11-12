package com.qlic.mobile.feature.devices.presentation

import com.qlic.mobile.feature.devices.domain.Device

data class DevicesUiState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val error: DevicesError? = null,
    val isSubmitting: Boolean = false,
    val formError: DeviceFormError? = null,
    val operationSucceeded: Boolean = false,
)

enum class DevicesError {
    Network,
    Unauthorized,
    Server,
}

enum class DeviceFormError {
    QrPayloadRequired,
    SerialNumberRequired,
    ModelRequired,
    InvalidWaterPointId,
}
