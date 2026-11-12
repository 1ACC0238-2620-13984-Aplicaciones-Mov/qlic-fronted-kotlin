package com.qlic.mobile.feature.auth.data

import com.qlic.mobile.feature.auth.domain.AuthError
import com.qlic.mobile.feature.auth.domain.AuthRepository
import com.qlic.mobile.feature.auth.domain.AuthResult
import com.qlic.mobile.feature.auth.domain.AuthSession
import com.qlic.mobile.feature.auth.domain.SignInRequest
import com.qlic.mobile.feature.auth.domain.SignUpRequest
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override suspend fun signIn(request: SignInRequest): AuthResult<AuthSession> = try {
        val response = api.signIn(SignInDto(request.email, request.password))
        response.toAuthResult()?.also { sessionStore.save(it) }
            ?.let { AuthResult.Success(it) }
            ?: AuthResult.Failure(response.toAuthError())
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: IOException) {
        AuthResult.Failure(AuthError.Network)
    } catch (_: Exception) {
        AuthResult.Failure(AuthError.Unknown)
    }

    override suspend fun signUp(request: SignUpRequest): AuthResult<AuthSession> = try {
        val response = api.signUp(
            SignUpDto(
                fullName = request.fullName,
                email = request.email,
                password = request.password,
                acceptTerms = request.acceptedTerms,
            ),
        )
        if (response.isSuccessful && response.body() != null) {
            signIn(SignInRequest(request.email, request.password))
        } else {
            AuthResult.Failure(response.toAuthError())
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: IOException) {
        AuthResult.Failure(AuthError.Network)
    } catch (_: Exception) {
        AuthResult.Failure(AuthError.Unknown)
    }

    private fun Response<AuthenticationDto>.toAuthResult(): AuthSession? = body()
        ?.takeIf { isSuccessful }
        ?.let { body ->
            AuthSession(
                accessToken = body.token,
                accountId = body.accountId,
                email = body.email,
                fullName = body.fullName,
                tokenType = body.tokenType,
                expiresAt = body.expiresAt,
            )
        }

    private fun Response<*>.toAuthError(): AuthError = when (code()) {
        401 -> AuthError.Unauthorized
        in 500..599 -> AuthError.Server
        else -> AuthError.Unknown
    }
}
