package com.qlic.mobile.feature.alerts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.alerts.domain.AuthorizedRecipient
import com.qlic.mobile.feature.alerts.domain.Urgency

@Composable
fun AlertSettingsScreen(
    state: AlertsUiState,
    onBack: () -> Unit,
    onSaveThreshold: (waterPointId: String, period: String, customLimit: String, baseline: String) -> Unit,
    onSavePreference: (groupingWindow: String, minimumUrgency: Urgency) -> Unit,
    onAddRecipient: (fullName: String, email: String, pushToken: String, enabled: Boolean) -> Unit,
    onToggleRecipient: (recipientId: String, enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
            Text(stringResource(R.string.alert_settings_title), style = MaterialTheme.typography.headlineMedium)
        }

        when {
            state.isSettingsLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.error != null -> Text(
                stringResource(state.error.messageResource()),
                color = MaterialTheme.colorScheme.error,
            )
            else -> {
                ThresholdSection(state, onSaveThreshold)
                NotificationSection(state, onSavePreference)
                RecipientSection(state, onAddRecipient, onToggleRecipient)
            }
        }

        if (state.operationSucceeded) {
            Text(
                stringResource(R.string.alert_settings_saved),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ThresholdSection(
    state: AlertsUiState,
    onSave: (String, String, String, String) -> Unit,
) {
    var waterPointId by rememberSaveable { mutableStateOf("") }
    val period = "HOURLY"
    var customLimit by rememberSaveable { mutableStateOf("") }
    var baseline by rememberSaveable { mutableStateOf("") }

    SettingsCard(title = stringResource(R.string.alert_settings_thresholds)) {
        if (state.thresholds.isEmpty()) {
            Text(stringResource(R.string.alert_settings_no_thresholds))
        } else {
            state.thresholds.forEach { threshold ->
                Text(
                    stringResource(
                        R.string.alert_settings_threshold_summary,
                        threshold.waterPointId,
                        threshold.effectiveLimitLitersPerHour.stripTrailingZeros().toPlainString(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            HorizontalDivider()
        }

        QlicTextField(
            value = waterPointId,
            onValueChange = { waterPointId = it },
            label = stringResource(R.string.alert_settings_water_point),
            isError = state.formError == AlertFormError.WaterPointRequired,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.WaterPointRequired),
        )
        Text(stringResource(R.string.alert_settings_period_hourly))
        QlicTextField(
            value = customLimit,
            onValueChange = { customLimit = it },
            label = stringResource(R.string.alert_settings_custom_limit),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.formError == AlertFormError.InvalidCustomLimit,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.InvalidCustomLimit),
        )
        QlicTextField(
            value = baseline,
            onValueChange = { baseline = it },
            label = stringResource(R.string.alert_settings_baseline),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.formError == AlertFormError.InvalidBaseline,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.InvalidBaseline),
        )
        QlicButton(
            text = stringResource(R.string.alert_settings_save_threshold),
            onClick = { onSave(waterPointId, period, customLimit, baseline) },
            enabled = !state.isSubmitting,
        )
    }
}

@Composable
private fun NotificationSection(
    state: AlertsUiState,
    onSave: (String, Urgency) -> Unit,
) {
    var groupingWindow by rememberSaveable(state.notificationPreference?.id) {
        mutableStateOf(state.notificationPreference?.groupingWindowMinutes?.toString().orEmpty())
    }
    var urgency by rememberSaveable(state.notificationPreference?.id) {
        mutableStateOf(state.notificationPreference?.minimumUrgency ?: Urgency.Informational)
    }

    SettingsCard(title = stringResource(R.string.alert_settings_notifications)) {
        QlicTextField(
            value = groupingWindow,
            onValueChange = { groupingWindow = it },
            label = stringResource(R.string.alert_settings_grouping_window),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.formError == AlertFormError.InvalidGroupingWindow,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.InvalidGroupingWindow),
        )
        Text(stringResource(R.string.alert_settings_minimum_urgency))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Urgency.Informational, Urgency.Moderate, Urgency.Critical).forEach { option ->
                FilterChip(
                    selected = urgency == option,
                    onClick = { urgency = option },
                    label = { Text(stringResource(option.labelResource())) },
                )
            }
        }
        QlicButton(
            text = stringResource(R.string.alert_settings_save_notifications),
            onClick = { onSave(groupingWindow, urgency) },
            enabled = !state.isSubmitting,
        )
    }
}

@Composable
private fun RecipientSection(
    state: AlertsUiState,
    onAdd: (String, String, String, Boolean) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pushToken by rememberSaveable { mutableStateOf("") }

    SettingsCard(title = stringResource(R.string.alert_settings_recipients)) {
        state.recipients.forEach { recipient ->
            RecipientRow(recipient, !state.isSubmitting, onToggle)
            HorizontalDivider()
        }
        if (state.recipients.isEmpty()) {
            Text(stringResource(R.string.alert_settings_no_recipients))
        }

        QlicTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = stringResource(R.string.alert_settings_recipient_name),
            isError = state.formError == AlertFormError.InvalidRecipientName,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.InvalidRecipientName),
        )
        QlicTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.alert_settings_recipient_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = state.formError == AlertFormError.InvalidRecipientEmail,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.InvalidRecipientEmail),
        )
        QlicTextField(
            value = pushToken,
            onValueChange = { pushToken = it },
            label = stringResource(R.string.alert_settings_push_token),
            isError = state.formError == AlertFormError.PushTokenRequired,
            errorMessage = state.formError.errorMessageOrNull(AlertFormError.PushTokenRequired),
        )
        QlicButton(
            text = stringResource(R.string.alert_settings_add_recipient),
            onClick = { onAdd(fullName, email, pushToken, true) },
            enabled = !state.isSubmitting,
        )
    }
}

@Composable
private fun RecipientRow(
    recipient: AuthorizedRecipient,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(recipient.fullName, style = MaterialTheme.typography.titleMedium)
            Text(recipient.email, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = recipient.enabled,
            onCheckedChange = { onToggle(recipient.id, it) },
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = recipient.fullName
            },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun AlertFormError?.errorMessageOrNull(expected: AlertFormError): String? {
    if (this != expected) return null
    val resource = when (expected) {
        AlertFormError.ActionRequired -> R.string.alert_error_action
        AlertFormError.WaterPointRequired -> R.string.alert_error_water_point
        AlertFormError.PeriodRequired -> R.string.alert_error_period
        AlertFormError.InvalidCustomLimit -> R.string.alert_error_custom_limit
        AlertFormError.InvalidBaseline -> R.string.alert_error_baseline
        AlertFormError.InvalidGroupingWindow -> R.string.alert_error_grouping_window
        AlertFormError.InvalidRecipientName -> R.string.alert_error_recipient_name
        AlertFormError.InvalidRecipientEmail -> R.string.alert_error_recipient_email
        AlertFormError.PushTokenRequired -> R.string.alert_error_push_token
    }
    return stringResource(resource)
}
