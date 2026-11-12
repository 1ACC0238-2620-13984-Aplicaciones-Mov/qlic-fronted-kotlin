package com.qlic.mobile.feature.sharing.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.sharing.domain.AuthorizedUser
import com.qlic.mobile.feature.sharing.domain.Invitation
import com.qlic.mobile.feature.sharing.domain.SharePermission
import com.qlic.mobile.feature.sharing.domain.SharingRepository
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class InviteUserDto(val email: String, val permission: String)

data class InvitationDto(
    val id: String,
    val accountName: String?,
    val invitedEmail: String,
    val permission: String,
    val state: String,
    val expiresAt: String,
)

data class AuthorizedUserDto(
    val id: String,
    val fullName: String?,
    val email: String?,
    val permission: String,
)

interface SharingApi {
    @POST("api/v1/invitations")
    suspend fun invite(@Body request: InviteUserDto): Response<InvitationDto>

    @GET("api/v1/invitations")
    suspend fun getIssued(): Response<List<InvitationDto>>

    @GET("api/v1/invitations/received")
    suspend fun getReceived(): Response<List<InvitationDto>>

    @POST("api/v1/invitations/{invitationId}/acceptance")
    suspend fun accept(@Path("invitationId") invitationId: String): Response<AuthorizedUserDto>

    @GET("api/v1/authorized-users")
    suspend fun getAuthorizedUsers(): Response<List<AuthorizedUserDto>>

    @DELETE("api/v1/authorized-users/{authorizedUserId}")
    suspend fun revoke(@Path("authorizedUserId") authorizedUserId: String): Response<Unit>
}

class SharingRepositoryImpl(private val api: SharingApi) : SharingRepository {
    override suspend fun invite(email: String, permission: SharePermission): ApiResult<Invitation> =
        apiCall {
            api.invite(InviteUserDto(email.trim(), if (permission == SharePermission.Viewer) "VIEWER" else "OPERATOR"))
        }.mapResult { it.toDomain() }

    override suspend fun getIssuedInvitations(): ApiResult<List<Invitation>> =
        apiCall { api.getIssued() }.mapResult { list -> list.map { it.toDomain() } }

    override suspend fun getReceivedInvitations(): ApiResult<List<Invitation>> =
        apiCall { api.getReceived() }.mapResult { list -> list.map { it.toDomain() } }

    override suspend fun acceptInvitation(invitationId: String): ApiResult<AuthorizedUser> =
        apiCall { api.accept(invitationId) }.mapResult { it.toDomain() }

    override suspend fun getAuthorizedUsers(): ApiResult<List<AuthorizedUser>> =
        apiCall { api.getAuthorizedUsers() }.mapResult { list -> list.map { it.toDomain() } }

    override suspend fun revoke(authorizedUserId: String): ApiResult<Unit> =
        apiCall { api.revoke(authorizedUserId) }.let { result ->
            // A 204 has no body, which apiCall reports as Empty; both mean the access was revoked.
            if (result == ApiResult.Empty) ApiResult.Success(Unit) else result.mapResult { }
        }

    private fun InvitationDto.toDomain() = Invitation(id, accountName, invitedEmail, permission, state, expiresAt)

    private fun AuthorizedUserDto.toDomain() = AuthorizedUser(id, fullName, email, permission)
}

private inline fun <T, R> ApiResult<T>.mapResult(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value))
    ApiResult.Empty -> ApiResult.Empty
    ApiResult.NetworkError -> ApiResult.NetworkError
    ApiResult.Unauthorized -> ApiResult.Unauthorized
    is ApiResult.Failure -> this
}
