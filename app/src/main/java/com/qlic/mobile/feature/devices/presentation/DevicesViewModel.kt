package com.qlic.mobile.feature.devices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.devices.domain.RegisterDeviceInput
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DevicesViewModel(
    private val repository: DeviceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(DevicesUiState(isLoading = true))
    val state: StateFlow<DevicesUiState> = _state.asStateFlow()

    init {
        loadDevices()
    }

    fun retry() = loadDevices()

    fun registerDevice(qrPayload: String, serialNumber: String, model: String) {
        if (_state.value.isSubmitting) return
        val error = when {
            qrPayload.isBlank() -> DeviceFormError.QrPayloadRequired
            serialNumber.isBlank() -> DeviceFormError.SerialNumberRequired
            model.isBlank() -> DeviceFormError.ModelRequired
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(formError = error, operationSucceeded = false) }
            return
        }

        _state.update { it.copy(isSubmitting = true, formError = null, operationSucceeded = false) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.registerDevice(RegisterDeviceInput(qrPayload, serialNumber, model))) {
                is ApiResult.Success -> _state.update { current ->
                    current.copy(
                        isSubmitting = false,
                        devices = current.devices.upsert(result.value),
                        error = null,
                        operationSucceeded = true,
                    )
                }
                else -> _state.update {
                    it.copy(isSubmitting = false, error = result.toDevicesError())
                }
            }
        }
    }

    fun assignWaterPoint(deviceId: String, waterPointId: String) {
        if (_state.value.isSubmitting) return
        if (runCatching { UUID.fromString(waterPointId) }.isFailure) {
            _state.update { it.copy(formError = DeviceFormError.InvalidWaterPointId) }
            return
        }

        _state.update { it.copy(isSubmitting = true, formError = null, operationSucceeded = false) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.assignWaterPoint(deviceId, waterPointId)) {
                is ApiResult.Success -> _state.update { current ->
                    current.copy(
                        isSubmitting = false,
                        devices = current.devices.upsert(result.value),
                        error = null,
                        operationSucceeded = true,
                    )
                }
                else -> _state.update {
                    it.copy(isSubmitting = false, error = result.toDevicesError())
                }
            }
        }
    }

    private fun loadDevices() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.getDevices()) {
                is ApiResult.Success -> _state.update {
                    it.copy(isLoading = false, devices = result.value, error = null)
                }
                else -> _state.update {
                    it.copy(isLoading = false, error = result.toDevicesError())
                }
            }
        }
    }

    private fun ApiResult<*>.toDevicesError(): DevicesError = when (this) {
        ApiResult.NetworkError -> DevicesError.Network
        ApiResult.Unauthorized -> DevicesError.Unauthorized
        else -> DevicesError.Server
    }

    private fun List<Device>.upsert(device: Device): List<Device> =
        filterNot { it.id == device.id } + device
}
