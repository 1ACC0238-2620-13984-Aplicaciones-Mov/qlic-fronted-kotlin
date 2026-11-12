package com.qlic.mobile.feature.sharing.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.FeatureError
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.sharing.domain.AuthorizedUser
import com.qlic.mobile.feature.sharing.domain.Invitation
import com.qlic.mobile.feature.sharing.domain.SharePermission

@Composable
fun SharingScreen(
    state: SharingUiState,
    onRetry: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPermissionChanged: (SharePermission) -> Unit,
    onInvite: () -> Unit,
    onAccept: (String) -> Unit,
    onRevoke: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.sharing_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.sharing_title), style = MaterialTheme.typography.headlineMedium)

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("sharing-loading"),
            )
            return@Column
        }

        state.error?.let { error ->
            FeatureError(
                message = stringResource(
                    when (error) {
                        SharingError.Network -> R.string.sharing_error_network
                        SharingError.Unauthorized -> R.string.sharing_error_session
                        SharingError.Forbidden -> R.string.sharing_error_forbidden
                        SharingError.Conflict -> R.string.sharing_error_conflict
                        SharingError.Server -> R.string.sharing_error_server
                    },
                ),
                onRetry = onRetry,
                tag = "sharing-error",
            )
        }
        state.notice?.let {
            Text(
                stringResource(
                    when (it) {
                        SharingNotice.Invited -> R.string.sharing_notice_invited
                        SharingNotice.Accepted -> R.string.sharing_notice_accepted
                        SharingNotice.Revoked -> R.string.sharing_notice_revoked
                    },
                ),
                modifier = Modifier.testTag("sharing-notice"),
            )
        }

        InviteCard(state, onEmailChanged, onPermissionChanged, onInvite)

        if (state.received.isNotEmpty()) {
            Text(stringResource(R.string.sharing_received_heading), style = MaterialTheme.typography.titleMedium)
            state.received.forEach { ReceivedCard(it, state.isSubmitting, onAccept) }
        }

        Text(stringResource(R.string.sharing_users_heading), style = MaterialTheme.typography.titleMedium)
        if (state.authorizedUsers.isEmpty()) {
            Text(stringResource(R.string.sharing_users_empty), modifier = Modifier.testTag("sharing-users-empty"))
        }
        state.authorizedUsers.forEach { UserCard(it, state.isSubmitting, onRevoke) }

        if (state.issued.isNotEmpty()) {
            Text(stringResource(R.string.sharing_issued_heading), style = MaterialTheme.typography.titleMedium)
            state.issued.forEach { IssuedCard(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteCard(
    state: SharingUiState,
    onEmailChanged: (String) -> Unit,
    onPermissionChanged: (SharePermission) -> Unit,
    onInvite: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("sharing-invite")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.sharing_invite_heading), style = MaterialTheme.typography.titleMedium)
            QlicTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.sharing_email),
                isError = state.invalidEmail,
                errorMessage = stringResource(R.string.sharing_email_invalid),
                modifier = Modifier.testTag("sharing-email"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.permission == SharePermission.Viewer,
                    onClick = { onPermissionChanged(SharePermission.Viewer) },
                    label = { Text(stringResource(R.string.sharing_permission_viewer)) },
                    modifier = Modifier.testTag("sharing-permission-viewer"),
                )
                FilterChip(
                    selected = state.permission == SharePermission.Operator,
                    onClick = { onPermissionChanged(SharePermission.Operator) },
                    label = { Text(stringResource(R.string.sharing_permission_operator)) },
                    modifier = Modifier.testTag("sharing-permission-operator"),
                )
            }
            QlicButton(
                text = stringResource(if (state.isSubmitting) R.string.common_saving else R.string.sharing_invite_action),
                onClick = onInvite,
                modifier = Modifier.testTag("sharing-invite-action"),
            )
        }
    }
}

@Composable
private fun ReceivedCard(invitation: Invitation, busy: Boolean, onAccept: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().testTag("received-${invitation.id}")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(invitation.accountName ?: invitation.invitedEmail, style = MaterialTheme.typography.titleSmall)
            Text(invitation.permission, style = MaterialTheme.typography.bodySmall)
            QlicButton(
                text = stringResource(R.string.sharing_accept),
                onClick = { if (!busy) onAccept(invitation.id) },
            )
        }
    }
}

@Composable
private fun UserCard(user: AuthorizedUser, busy: Boolean, onRevoke: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().testTag("user-${user.id}")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(user.fullName ?: user.email.orEmpty(), style = MaterialTheme.typography.titleSmall)
            user.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text(user.permission, style = MaterialTheme.typography.bodySmall)
            QlicButton(
                text = stringResource(R.string.sharing_revoke),
                onClick = { if (!busy) onRevoke(user.id) },
            )
        }
    }
}

@Composable
private fun IssuedCard(invitation: Invitation) {
    Card(modifier = Modifier.fillMaxWidth().testTag("issued-${invitation.id}")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(invitation.invitedEmail, style = MaterialTheme.typography.titleSmall)
            Text("${invitation.permission} · ${invitation.state}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
