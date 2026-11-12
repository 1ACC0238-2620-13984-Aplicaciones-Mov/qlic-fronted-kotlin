package com.qlic.mobile.core.network

import com.qlic.mobile.BuildConfig
import com.qlic.mobile.feature.auth.data.AuthApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientFactory {
    fun createAuthApi(): AuthApi = createRetrofit(createHttpClient())
        .create(AuthApi::class.java)

    fun createAuthenticatedRetrofit(
        sessionSnapshot: SessionSnapshot,
        onUnauthorized: () -> Unit,
    ): Retrofit = createRetrofit(
        createHttpClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(sessionSnapshot, onUnauthorized))
            .build(),
    )

    private fun createHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private fun createRetrofit(httpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
