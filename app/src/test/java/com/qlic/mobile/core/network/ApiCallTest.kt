package com.qlic.mobile.core.network

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

class ApiCallTest {
    @Test
    fun `successful response returns its body`() = runBlocking {
        val result = apiCall { Response.success("content") }

        assertEquals(ApiResult.Success("content"), result)
    }

    @Test
    fun `successful response without body returns empty`() = runBlocking {
        val result = apiCall<String> { Response.success(204, null as String?) }

        assertEquals(ApiResult.Empty, result)
    }

    @Test
    fun `unauthorized response has a dedicated result`() = runBlocking {
        val result = apiCall<String> {
            Response.error(401, "".toResponseBody("application/json".toMediaType()))
        }

        assertEquals(ApiResult.Unauthorized, result)
    }

    @Test
    fun `problem detail is parsed from an error response`() = runBlocking {
        val body = """
            {
              "title": "Bad Request",
              "detail": "The water point identifier is invalid.",
              "status": 400,
              "code": "monitoring.water-point.invalid"
            }
        """.trimIndent()

        val result = apiCall<String> {
            Response.error(400, body.toResponseBody("application/problem+json".toMediaType()))
        }

        assertTrue(result is ApiResult.Failure)
        val failure = result as ApiResult.Failure
        assertEquals(400, failure.statusCode)
        assertEquals("monitoring.water-point.invalid", failure.problem?.code)
        assertEquals("The water point identifier is invalid.", failure.problem?.detail)
    }

    @Test
    fun `network failure has a dedicated result`() = runBlocking {
        val result = apiCall<String> { throw IOException("offline") }

        assertEquals(ApiResult.NetworkError, result)
    }

    @Test
    fun `coroutine cancellation is never converted into an API error`() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                apiCall<String> { throw CancellationException("cancelled") }
            }
        }
    }
}
