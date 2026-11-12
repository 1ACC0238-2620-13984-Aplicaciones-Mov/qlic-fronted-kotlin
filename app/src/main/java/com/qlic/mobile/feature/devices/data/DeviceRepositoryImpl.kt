package com.qlic.mobile.feature.devices.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import com.qlic.mobile.feature.devices.domain.RegisterDeviceInput

class DeviceRepositoryImpl(
    private val api: DeviceApi,
    private val sessionSnapshot: SessionSnapshot,
) : DeviceRepository {
    override suspend fun getDevices(): ApiResult<List<Device>> {
        val accountId = sessionSnapshot.currentSession()?.accountId
            ?: return ApiResult.Unauthorized
        return apiCall { api.getDevices(accountId) }.map { devices ->
            devices.map { it.toDomain() }
        }
    }

    override suspend fun registerDevice(input: RegisterDeviceInput): ApiResult<Device> {
        val accountId = sessionSnapshot.currentSession()?.accountId
            ?: return ApiResult.Unauthorized
        return apiCall {
            api.registerDevice(
                RegisterDeviceDto(
                    qrCodePayload = input.qrCodePayload,
                    serialNumber = input.serialNumber,
                    model = input.model,
                    accountId = accountId,
                ),
            )
        }.map { it.toDomain() }
    }

    override suspend fun assignWaterPoint(
        deviceId: String,
        waterPointId: String,
    ): ApiResult<Device> = if (sessionSnapshot.currentSession() == null) {
        ApiResult.Unauthorized
    } else {
        apiCall {
            api.assignWaterPoint(deviceId, AssignWaterPointDto(waterPointId))
        }.map { it.toDomain() }
    }

    private fun DeviceDto.toDomain() = Device(
        id = id,
        serialNumber = serialNumber,
        model = model,
        accountId = accountId,
        waterPointId = waterPointId,
        status = when (status) {
            "ONLINE" -> DeviceStatus.Online
            "OFFLINE" -> DeviceStatus.Offline
            "LOW_BATTERY" -> DeviceStatus.LowBattery
            "CALIBRATING" -> DeviceStatus.Calibrating
            else -> DeviceStatus.Unknown
        },
        batteryPercentage = batteryPercentage,
        lastSyncAt = lastSyncAt,
    )

    private inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(value))
        ApiResult.Empty -> ApiResult.Empty
        ApiResult.NetworkError -> ApiResult.NetworkError
        ApiResult.Unauthorized -> ApiResult.Unauthorized
        is ApiResult.Failure -> this
    }
}
