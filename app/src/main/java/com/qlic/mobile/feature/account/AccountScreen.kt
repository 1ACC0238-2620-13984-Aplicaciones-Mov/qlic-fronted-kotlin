package com.qlic.mobile.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton

data class AccountUiState(
    val fullName: String,
    val email: String,
    val accountId: String,
)

@Composable
fun AccountScreen(
    state: AccountUiState,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen-account")
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.account_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.account_read_only_note), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AccountField(R.string.account_full_name, state.fullName, "account-name")
                AccountField(R.string.account_email, state.email, "account-email")
                AccountField(R.string.account_identifier, state.accountId, "account-id")
            }
        }
        QlicButton(
            text = stringResource(R.string.account_sign_out),
            onClick = onSignOut,
            modifier = Modifier.testTag("account-sign-out"),
        )
    }
}

@Composable
private fun AccountField(label: Int, value: String, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
        Text(
            value.ifBlank { stringResource(R.string.account_value_unavailable) },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(tag),
        )
    }
}
