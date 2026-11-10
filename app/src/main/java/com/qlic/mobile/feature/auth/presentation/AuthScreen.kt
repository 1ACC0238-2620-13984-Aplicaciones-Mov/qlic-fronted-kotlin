package com.qlic.mobile.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.auth.domain.AuthError
import com.qlic.mobile.core.localization.AppLanguage

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onTermsChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onModeChanged: (AuthMode) -> Unit,
    selectedLanguage: AppLanguage = AppLanguage.English,
    onLanguageSelected: (AppLanguage) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onLanguageSelected(AppLanguage.English) }) {
                Text(stringResource(R.string.language_english))
            }
            TextButton(onClick = { onLanguageSelected(AppLanguage.SpanishLatinAmerica) }) {
                Text(stringResource(R.string.language_spanish))
            }
        }
        Text(
            stringResource(if (state.mode == AuthMode.SignIn) R.string.auth_welcome else R.string.auth_create_title),
            style = MaterialTheme.typography.titleLarge,
        )
        if (state.mode == AuthMode.SignUp) {
            QlicTextField(
                value = state.fullName,
                onValueChange = onNameChanged,
                label = stringResource(R.string.auth_full_name),
                modifier = Modifier.testTag("auth-name"),
            )
        }
        QlicTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.auth_email),
            modifier = Modifier.testTag("auth-email"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        QlicTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = stringResource(R.string.auth_password),
            modifier = Modifier.testTag("auth-password"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        if (state.mode == AuthMode.SignUp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.acceptedTerms, onCheckedChange = onTermsChanged)
                Text(stringResource(R.string.auth_accept_terms))
            }
        }
        state.error?.let { error ->
            Text(
                text = stringResource(error.stringResource()),
                color = MaterialTheme.colorScheme.error,
            )
        }
        QlicButton(
            text = stringResource(if (state.mode == AuthMode.SignIn) R.string.auth_sign_in else R.string.auth_create_account),
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.testTag("auth-submit"),
        )
        TextButton(
            onClick = {
                onModeChanged(if (state.mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn)
            },
            modifier = Modifier.testTag("auth-mode-toggle"),
        ) {
            Text(stringResource(if (state.mode == AuthMode.SignIn) R.string.auth_go_to_sign_up else R.string.auth_go_to_sign_in))
        }
        Text(
            stringResource(
                if (selectedLanguage == AppLanguage.English) {
                    R.string.language_selected_english
                } else {
                    R.string.language_selected_spanish
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun AuthError.stringResource(): Int = when (this) {
    AuthError.InvalidName -> R.string.auth_error_name
    AuthError.InvalidEmail -> R.string.auth_error_email
    AuthError.InvalidPassword -> R.string.auth_error_password
    AuthError.TermsRequired -> R.string.auth_error_terms
    AuthError.Unauthorized -> R.string.auth_error_unauthorized
    AuthError.Network -> R.string.auth_error_network
    AuthError.Server -> R.string.auth_error_server
    AuthError.Unknown -> R.string.auth_error_unknown
}
