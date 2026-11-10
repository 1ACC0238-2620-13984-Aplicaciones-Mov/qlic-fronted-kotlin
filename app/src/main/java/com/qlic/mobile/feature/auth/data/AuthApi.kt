package com.qlic.mobile.feature.auth.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body request: SignInDto): Response<AuthenticationDto>

    @POST("api/v1/authentication/sign-up")
    suspend fun signUp(@Body request: SignUpDto): Response<AccountDto>
}

data class SignInDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class SignUpDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("acceptTerms") val acceptTerms: Boolean,
)

data class AccountDto(
    @SerializedName("subscriberId") val subscriberId: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("createdAt") val createdAt: String,
)

data class AuthenticationDto(
    @SerializedName("subscriberId") val subscriberId: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("token") val token: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresAt") val expiresAt: String,
)
