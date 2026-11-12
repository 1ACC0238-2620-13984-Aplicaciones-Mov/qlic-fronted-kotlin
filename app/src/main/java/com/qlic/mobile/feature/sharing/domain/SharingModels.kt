package com.qlic.mobile.feature.sharing.domain

import com.qlic.mobile.core.network.ApiResult

enum class SharePermission { Viewer, Operator }

data class Invitation(
    val id: String,
    val accountName: String?,
    val invitedEmail: String,
    val permission: String,
    val state: String,
    val expiresAt: String,
)

data class AuthorizedUser(
    val id: String,
    val fullName: String?,
    val email: String?,
    val permission: String,
)

interface SharingRepository {
    suspend fun invite(email: String, permission: SharePermission): ApiResult<Invitation>
    suspend fun getIssuedInvitations(): ApiResult<List<Invitation>>
    suspend fun getReceivedInvitations(): ApiResult<List<Invitation>>
    suspend fun acceptInvitation(invitationId: String): ApiResult<AuthorizedUser>
    suspend fun getAuthorizedUsers(): ApiResult<List<AuthorizedUser>>
    suspend fun revoke(authorizedUserId: String): ApiResult<Unit>
}

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun isValidEmail(value: String): Boolean = EMAIL_PATTERN.matches(value.trim())
