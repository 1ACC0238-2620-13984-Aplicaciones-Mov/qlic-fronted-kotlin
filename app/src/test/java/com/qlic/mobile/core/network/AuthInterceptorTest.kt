package com.qlic.mobile.core.network

import com.qlic.mobile.feature.auth.domain.AuthSession
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {
    private lateinit var server: HttpServer
    private val authorization = AtomicReference<String?>()
    private val language = AtomicReference<String?>()
    private val statusCode = AtomicInteger(200)

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                authorization.set(exchange.requestHeaders.getFirst("Authorization"))
                language.set(exchange.requestHeaders.getFirst("Accept-Language"))
                exchange.sendResponseHeaders(statusCode.get(), -1)
                exchange.close()
            }
            start()
        }
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `protected request includes session authorization and selected language`() {
        val snapshot = SessionSnapshot().apply {
            updateSession(AuthSession("jwt-token", "account-id", tokenType = "Bearer"))
            updateLanguageTag("es-419")
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(snapshot) {})
            .build()

        client.newCall(Request.Builder().url(serverUrl()).build()).execute().close()

        assertEquals("Bearer jwt-token", authorization.get())
        assertEquals("es-419", language.get())
    }

    @Test
    fun `request without session omits authorization`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionSnapshot()) {})
            .build()

        client.newCall(Request.Builder().url(serverUrl()).build()).execute().close()

        assertNull(authorization.get())
        assertEquals("en", language.get())
    }

    @Test
    fun `unauthorized response emits session expired signal`() {
        statusCode.set(401)
        val signals = AtomicInteger(0)
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionSnapshot()) { signals.incrementAndGet() })
            .build()

        client.newCall(Request.Builder().url(serverUrl()).build()).execute().close()

        assertEquals(1, signals.get())
    }

    private fun serverUrl(): String = "http://127.0.0.1:${server.address.port}/"
}
