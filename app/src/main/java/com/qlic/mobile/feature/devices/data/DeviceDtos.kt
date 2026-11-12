package com.qlic.mobile.feature.devices.data

import com.google.gson.annotations.SerializedName

data class RegisterDeviceDto(
    @SerializedName("qrCodePayload") val qrCodePayload: String,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("model") val model: String,
    @SerializedName("accountId") val accountId: String,
)

data class AssignWaterPointDto(
    @SerializedName("waterPointId") val waterPointId: String,
)

data class DeviceDto(
    @SerializedName("id") val id: String,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("model") val model: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("waterPointId") val waterPointId: String?,
    @SerializedName("status") val status: String,
    @SerializedName("batteryPercentage") val batteryPercentage: Int,
    @SerializedName("lastSyncAt") val lastSyncAt: String,
)
