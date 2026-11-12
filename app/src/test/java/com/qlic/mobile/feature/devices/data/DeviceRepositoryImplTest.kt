package com.qlic.mobile.feature.devices.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.feature.auth.domain.AuthSession
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import com.qlic.mobile.feature.devices.domain.RegisterDeviceInput
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class DeviceRepositoryImplTest {
    @Test
    fun `list uses session account and maps devices`() = runBlocking {
        val api = FakeDeviceApi(devices = listOf(deviceDto()))
        val repository = DeviceRepositoryImpl(api, authenticatedSnapshot())

        val result = repository.getDevices()

        assertEquals("account-id", api.requestedAccountId)
        assertTrue(result is ApiResult.Success)
        val device = (result as ApiResult.Success).value.single()
        assertEquals("device-id", device.id)
        assertEquals(DeviceStatus.Online, device.status)
        assertEquals(87, device.batteryPercentage)
    }

    @Test
    fun `empty backend list remains an empty successful list`() = runBlocking {
        val result = DeviceRepositoryImpl(FakeDeviceApi(), authenticatedSnapshot()).getDevices()

        assertEquals(emptyList<Any>(), (result as ApiResult.Success).value)
    }

    @Test
    fun `register injects session account instead of accepting account input`() = runBlocking {
        val api = FakeDeviceApi()
        val repository = DeviceRepositoryImpl(api, authenticatedSnapshot())

        val result = repository.registerDevice(
            RegisterDeviceInput("qlic://sensor/123", "QL-123", "Qlic Flow Mini"),
        )

        assertTrue(result is ApiResult.Success)
        assertEquals("account-id", api.registerRequest?.accountId)
        assertEquals("qlic://sensor/123", api.registerRequest?.qrCodePayload)
    }

    @Test
    fun `assign sends only selected resource identifiers`() = runBlocking {
        val api = FakeDeviceApi()
        val repository = DeviceRepositoryImpl(api, authenticatedSnapshot())

        val result = repository.assignWaterPoint("device-id", "water-point-id")

        assertTrue(result is ApiResult.Success)
        assertEquals("device-id", api.assignedDeviceId)
        assertEquals("water-point-id", api.assignRequest?.waterPointId)
    }

    @Test
    fun `missing session returns unauthorized without calling backend`() = runBlocking {
        val api = FakeDeviceApi()
        val repository = DeviceRepositoryImpl(api, SessionSnapshot())

        val result = repository.getDevices()

        assertEquals(ApiResult.Unauthorized, result)
        assertFalse(api.listCalled)
    }

    @Test
    fun `unknown backend status maps safely`() = runBlocking {
        val api = FakeDeviceApi(devices = listOf(deviceDto(status = "FUTURE_STATUS")))

        val result = DeviceRepositoryImpl(api, authenticatedSnapshot()).getDevices()

        val device = (result as ApiResult.Success).value.single()
        assertEquals(DeviceStatus.Unknown, device.status)
    }

    @Test
    fun `server failure is preserved for the presentation layer`() = runBlocking {
        val api = FakeDeviceApi(listResponseCode = 500)

        val result = DeviceRepositoryImpl(api, authenticatedSnapshot()).getDevices()

        assertTrue(result is ApiResult.Failure)
        assertEquals(500, (result as ApiResult.Failure).statusCode)
    }

    private fun authenticatedSnapshot() = SessionSnapshot().apply {
        updateSession(AuthSession("jwt", "account-id"))
    }

    private fun deviceDto(status: String = "ONLINE") = DeviceDto(
        id = "device-id",
        serialNumber = "QL-123",
        model = "Qlic Flow Mini",
        accountId = "account-id",
        waterPointId = null,
        status = status,
        batteryPercentage = 87,
        lastSyncAt = "2026-11-11T20:15:30",
    )

    private inner class FakeDeviceApi(
        private val devices: List<DeviceDto> = emptyList(),
        private val listResponseCode: Int = 200,
    ) : DeviceApi {
        var requestedAccountId: String? = null
        var registerRequest: RegisterDeviceDto? = null
        var assignedDeviceId: String? = null
        var assignRequest: AssignWaterPointDto? = null
        var listCalled = false

        override suspend fun getDevices(accountId: String): Response<List<DeviceDto>> {
            listCalled = true
            requestedAccountId = accountId
            return if (listResponseCode == 200) {
                Response.success(devices)
            } else {
                Response.error(
                    listResponseCode,
                    "{}".toResponseBody("application/problem+json".toMediaType()),
                )
            }
        }

        override suspend fun registerDevice(request: RegisterDeviceDto): Response<DeviceDto> {
            registerRequest = request
            return Response.success(201, deviceDto())
        }

        override suspend fun assignWaterPoint(
            deviceId: String,
            request: AssignWaterPointDto,
        ): Response<DeviceDto> {
            assignedDeviceId = deviceId
            assignRequest = request
            return Response.success(deviceDto())
        }
    }
}
