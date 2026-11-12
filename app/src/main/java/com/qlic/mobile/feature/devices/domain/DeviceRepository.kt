package com.qlic.mobile.feature.devices.domain

import com.qlic.mobile.core.network.ApiResult

interface DeviceRepository {
    suspend fun getDevices(): ApiResult<List<Device>>

    suspend fun registerDevice(input: RegisterDeviceInput): ApiResult<Device>

    suspend fun assignWaterPoint(deviceId: String, waterPointId: String): ApiResult<Device>
}
