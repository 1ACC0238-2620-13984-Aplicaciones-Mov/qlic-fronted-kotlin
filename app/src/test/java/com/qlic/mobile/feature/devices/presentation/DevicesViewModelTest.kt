package com.qlic.mobile.feature.devices.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import com.qlic.mobile.feature.devices.domain.RegisterDeviceInput
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModelTest {
    @Test
    fun `load moves from loading to device content`() = runTest {
        val repository = FakeDeviceRepository().apply {
            devicesResult = ApiResult.Success(listOf(device()))
        }
        val viewModel = DevicesViewModel(repository, StandardTestDispatcher(testScheduler))

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("QL-123", viewModel.state.value.devices.single().serialNumber)
    }

    @Test
    fun `empty device response produces empty content without an error`() = runTest {
        val viewModel = DevicesViewModel(FakeDeviceRepository(), StandardTestDispatcher(testScheduler))

        advanceUntilIdle()

        assertTrue(viewModel.state.value.devices.isEmpty())
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `retry replaces an earlier network failure`() = runTest {
        val repository = FakeDeviceRepository().apply {
            devicesResult = ApiResult.NetworkError
        }
        val viewModel = DevicesViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        assertEquals(DevicesError.Network, viewModel.state.value.error)

        repository.devicesResult = ApiResult.Success(listOf(device()))
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.devices.size)
    }

    @Test
    fun `blank registration fields are rejected before calling repository`() = runTest {
        val repository = FakeDeviceRepository()
        val viewModel = DevicesViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.registerDevice("", "", "")
        advanceUntilIdle()

        assertEquals(DeviceFormError.QrPayloadRequired, viewModel.state.value.formError)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `a second registration is ignored while the first is running`() = runTest {
        val repository = FakeDeviceRepository().apply {
            registerGate = CompletableDeferred()
        }
        val viewModel = DevicesViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.registerDevice("qlic://sensor/123", "QL-123", "Qlic Flow Mini")
        viewModel.registerDevice("qlic://sensor/456", "QL-456", "Qlic Flow Mini")
        testScheduler.runCurrent()

        assertEquals(1, repository.registerCalls)
        assertTrue(viewModel.state.value.isSubmitting)

        repository.registerGate?.complete(ApiResult.Success(device()))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `invalid water point identifier is rejected locally`() = runTest {
        val repository = FakeDeviceRepository()
        val viewModel = DevicesViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.assignWaterPoint("device-id", "not-a-uuid")
        advanceUntilIdle()

        assertEquals(DeviceFormError.InvalidWaterPointId, viewModel.state.value.formError)
        assertEquals(0, repository.assignCalls)
    }

    private fun device() = Device(
        id = "device-id",
        serialNumber = "QL-123",
        model = "Qlic Flow Mini",
        accountId = "account-id",
        waterPointId = null,
        status = DeviceStatus.Online,
        batteryPercentage = 87,
        lastSyncAt = "2026-11-11T20:15:30",
    )

    private inner class FakeDeviceRepository : DeviceRepository {
        var devicesResult: ApiResult<List<Device>> = ApiResult.Success(emptyList())
        var registerGate: CompletableDeferred<ApiResult<Device>>? = null
        var registerCalls = 0
        var assignCalls = 0

        override suspend fun getDevices(): ApiResult<List<Device>> = devicesResult

        override suspend fun registerDevice(input: RegisterDeviceInput): ApiResult<Device> {
            registerCalls++
            return registerGate?.await() ?: ApiResult.Success(device())
        }

        override suspend fun assignWaterPoint(
            deviceId: String,
            waterPointId: String,
        ): ApiResult<Device> {
            assignCalls++
            return ApiResult.Success(device())
        }
    }
}
