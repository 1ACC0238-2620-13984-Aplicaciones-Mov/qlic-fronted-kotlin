package com.qlic.mobile.feature.alerts.presentation

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTextField
import com.qlic.mobile.feature.alerts.domain.AlertState

@Composable
fun AlertDetailScreen(
    state: AlertsUiState,
    onBack: () -> Unit,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionTaken by rememberSaveable(state.selectedAlert?.id) { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
            Text(stringResource(R.string.alert_detail_title), style = MaterialTheme.typography.headlineMedium)
        }

        when {
            state.isDetailLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.error != null -> Text(
                stringResource(state.error.messageResource()),
                color = MaterialTheme.colorScheme.error,
            )
            state.selectedAlert == null -> Text(stringResource(R.string.alert_detail_unavailable))
            else -> {
                val alert = state.selectedAlert
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(stringResource(R.string.alerts_possible_leak), style = MaterialTheme.typography.titleLarge)
                        DetailLine(R.string.alert_detail_water_point, alert.waterPointId)
                        DetailLine(R.string.alert_detail_state, stringResource(alert.state.labelResource()))
                        DetailLine(R.string.alert_detail_urgency, stringResource(alert.urgency.labelResource()))
                        DetailLine(
                            R.string.alert_detail_volume,
                            alert.estimatedVolumeLiters.stripTrailingZeros().toPlainString(),
                        )
                        DetailLine(
                            R.string.alert_detail_cost,
                            alert.estimatedCostSoles.stripTrailingZeros().toPlainString(),
                        )
                        DetailLine(R.string.alert_detail_recommendation, alert.recommendedAction)
                        alert.acknowledgedAction?.let {
                            DetailLine(R.string.alert_detail_action_taken, it)
                        }
                    }
                }

                if (alert.state == AlertState.Active) {
                    QlicTextField(
                        value = actionTaken,
                        onValueChange = { actionTaken = it },
                        label = stringResource(R.string.alert_detail_action_input),
                        isError = state.formError == AlertFormError.ActionRequired,
                        errorMessage = if (state.formError == AlertFormError.ActionRequired) {
                            stringResource(R.string.alert_error_action)
                        } else {
                            null
                        },
                    )
                    QlicButton(
                        text = if (state.isSubmitting) {
                            stringResource(R.string.common_saving)
                        } else {
                            stringResource(R.string.alert_detail_acknowledge)
                        },
                        onClick = { onAcknowledge(actionTaken) },
                        enabled = !state.isSubmitting,
                    )
                }

                if (state.operationSucceeded) {
                    Text(
                        stringResource(R.string.alert_operation_success),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(labelResource: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(stringResource(labelResource), style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

internal fun AlertsError.messageResource(): Int = when (this) {
    AlertsError.Network -> R.string.alerts_error_network
    AlertsError.Unauthorized -> R.string.alerts_error_session
    AlertsError.Server -> R.string.alerts_error_server
}
