package com.qlic.mobile.feature.devices.domain

data class Device(
    val id: String,
    val serialNumber: String,
    val model: String,
    val accountId: String,
    val waterPointId: String?,
    val status: DeviceStatus,
    val batteryPercentage: Int,
    val lastSyncAt: String,
)

enum class DeviceStatus {
    Online,
    Offline,
    LowBattery,
    Calibrating,
    Unknown,
}

data class RegisterDeviceInput(
    val qrCodePayload: String,
    val serialNumber: String,
    val model: String,
)
