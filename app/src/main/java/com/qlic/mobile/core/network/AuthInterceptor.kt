package com.qlic.mobile.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionSnapshot: SessionSnapshot,
    private val onUnauthorized: () -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = sessionSnapshot.currentSession()
        val request = chain.request().newBuilder()
            .header("Accept-Language", sessionSnapshot.currentLanguageTag())
            .apply {
                if (session != null) {
                    header("Authorization", "${session.tokenType} ${session.accessToken}")
                }
            }
            .build()

        return chain.proceed(request).also { response ->
            if (response.code == 401) onUnauthorized()
        }
    }
}
