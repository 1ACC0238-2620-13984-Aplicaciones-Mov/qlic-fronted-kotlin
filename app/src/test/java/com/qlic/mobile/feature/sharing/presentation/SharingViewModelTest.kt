package com.qlic.mobile.feature.sharing.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.sharing.domain.AuthorizedUser
import com.qlic.mobile.feature.sharing.domain.Invitation
import com.qlic.mobile.feature.sharing.domain.SharePermission
import com.qlic.mobile.feature.sharing.domain.SharingRepository
import com.qlic.mobile.feature.sharing.domain.isValidEmail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharingViewModelTest {
    private fun TestScope.viewModel(repository: FakeSharingRepository) =
        SharingViewModel(repository, StandardTestDispatcher(testScheduler))

    @Test
    fun `email validation accepts addresses and rejects free text`() {
        assertTrue(isValidEmail("ana@qlic.pe"))
        assertFalse(isValidEmail("ana"))
        assertFalse(isValidEmail("ana@qlic"))
        assertFalse(isValidEmail("ana @qlic.pe"))
    }

    @Test
    fun `load shows authorized users and invitations`() = runTest {
        val repository = FakeSharingRepository(users = listOf(AuthorizedUser("u1", "Ana", "ana@qlic.pe", "VIEWER")))
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.authorizedUsers.size)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `an invalid email is not sent`() = runTest {
        val repository = FakeSharingRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onEmailChanged("not-an-email")

        viewModel.invite()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.invalidEmail)
        assertNull(repository.invited)
    }

    @Test
    fun `inviting sends email and permission then clears the form`() = runTest {
        val repository = FakeSharingRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onEmailChanged("ana@qlic.pe")
        viewModel.onPermissionChanged(SharePermission.Operator)

        viewModel.invite()
        advanceUntilIdle()

        assertEquals("ana@qlic.pe" to SharePermission.Operator, repository.invited)
        assertEquals(SharingNotice.Invited, viewModel.state.value.notice)
        assertEquals("", viewModel.state.value.email)
    }

    @Test
    fun `a plan limit conflict is reported`() = runTest {
        val repository = FakeSharingRepository(inviteResult = ApiResult.Failure(statusCode = 409))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onEmailChanged("ana@qlic.pe")

        viewModel.invite()
        advanceUntilIdle()

        assertEquals(SharingError.Conflict, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `a non owner gets a forbidden error`() = runTest {
        val repository = FakeSharingRepository(inviteResult = ApiResult.Failure(statusCode = 403))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onEmailChanged("ana@qlic.pe")

        viewModel.invite()
        advanceUntilIdle()

        assertEquals(SharingError.Forbidden, viewModel.state.value.error)
    }

    @Test
    fun `revoking an access reloads the list`() = runTest {
        val repository = FakeSharingRepository(users = listOf(AuthorizedUser("u1", "Ana", "ana@qlic.pe", "VIEWER")))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.revoke("u1")
        advanceUntilIdle()

        assertEquals("u1", repository.revoked)
        assertEquals(SharingNotice.Revoked, viewModel.state.value.notice)
    }

    private class FakeSharingRepository(
        private val users: List<AuthorizedUser> = emptyList(),
        private val inviteResult: ApiResult<Invitation> = ApiResult.Success(
            Invitation("i1", null, "ana@qlic.pe", "VIEWER", "PENDING", "2026-11-20T00:00:00Z"),
        ),
    ) : SharingRepository {
        var invited: Pair<String, SharePermission>? = null
        var revoked: String? = null

        override suspend fun invite(email: String, permission: SharePermission): ApiResult<Invitation> {
            invited = email to permission
            return inviteResult
        }

        override suspend fun getIssuedInvitations(): ApiResult<List<Invitation>> = ApiResult.Success(emptyList())

        override suspend fun getReceivedInvitations(): ApiResult<List<Invitation>> = ApiResult.Success(emptyList())

        override suspend fun acceptInvitation(invitationId: String): ApiResult<AuthorizedUser> =
            ApiResult.Success(AuthorizedUser(invitationId, null, null, "VIEWER"))

        override suspend fun getAuthorizedUsers(): ApiResult<List<AuthorizedUser>> = ApiResult.Success(users)

        override suspend fun revoke(authorizedUserId: String): ApiResult<Unit> {
            revoked = authorizedUserId
            return ApiResult.Success(Unit)
        }
    }
}
