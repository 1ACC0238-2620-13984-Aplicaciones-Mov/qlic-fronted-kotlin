package com.qlic.mobile.feature.devices.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceApi {
    @GET("api/v1/devices")
    suspend fun getDevices(@Query("accountId") accountId: String): Response<List<DeviceDto>>

    @POST("api/v1/devices")
    suspend fun registerDevice(@Body request: RegisterDeviceDto): Response<DeviceDto>

    @PATCH("api/v1/devices/{id}/water-point")
    suspend fun assignWaterPoint(
        @Path("id") deviceId: String,
        @Body request: AssignWaterPointDto,
    ): Response<DeviceDto>
}
