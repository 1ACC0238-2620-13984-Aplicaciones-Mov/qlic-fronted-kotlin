package com.qlic.mobile.feature.consumption.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.qlic.mobile.feature.consumption.domain.ActivityReport
import com.qlic.mobile.feature.consumption.domain.WEEK_DAYS
import java.time.DayOfWeek
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
fun ConsumptionScreen(
    state: ConsumptionUiState,
    onRetry: () -> Unit,
    onOpeningChanged: (String) -> Unit,
    onClosingChanged: (String) -> Unit,
    onDayToggled: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.consumption_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.consumption_title), style = MaterialTheme.typography.headlineMedium)

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("consumption-loading"),
            )
            return@Column
        }

        state.error?.let { error ->
            FeatureError(
                message = stringResource(
                    when (error) {
                        ConsumptionError.Network -> R.string.consumption_error_network
                        ConsumptionError.Unauthorized -> R.string.consumption_error_session
                        ConsumptionError.Server -> R.string.consumption_error_server
                    },
                ),
                onRetry = onRetry,
                tag = "consumption-error",
            )
        }

        ScheduleCard(state, onOpeningChanged, onClosingChanged, onDayToggled, onSave)
        ReportCard(state.report)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleCard(
    state: ConsumptionUiState,
    onOpeningChanged: (String) -> Unit,
    onClosingChanged: (String) -> Unit,
    onDayToggled: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("consumption-schedule")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.consumption_schedule_heading), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.consumption_schedule_hint), style = MaterialTheme.typography.bodyMedium)
            QlicTextField(
                value = state.openingTime,
                onValueChange = onOpeningChanged,
                label = stringResource(R.string.consumption_opening),
                modifier = Modifier.testTag("consumption-opening"),
            )
            QlicTextField(
                value = state.closingTime,
                onValueChange = onClosingChanged,
                label = stringResource(R.string.consumption_closing),
                modifier = Modifier.testTag("consumption-closing"),
            )
            Text(stringResource(R.string.consumption_days), style = MaterialTheme.typography.labelLarge)
            val locale: Locale = LocalConfiguration.current.locales[0]
            // Seven chips do not fit in a row on a phone, so they are laid out in two rows.
            WEEK_DAYS.chunked(4).forEach { row ->
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { day ->
                        FilterChip(
                            selected = day in state.activeDays,
                            onClick = { onDayToggled(day) },
                            label = {
                                Text(DayOfWeek.valueOf(day).getDisplayName(TextStyle.SHORT, locale))
                            },
                            modifier = Modifier.testTag("consumption-day-$day"),
                        )
                    }
                }
            }
            if (state.invalidInput) {
                Text(
                    stringResource(R.string.consumption_invalid),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("consumption-invalid"),
                )
            }
            if (state.scheduleSaved) {
                Text(stringResource(R.string.consumption_saved), modifier = Modifier.testTag("consumption-saved"))
            }
            QlicButton(
                text = stringResource(if (state.isSaving) R.string.common_saving else R.string.consumption_save),
                onClick = onSave,
                modifier = Modifier.testTag("consumption-save"),
            )
        }
    }
}

@Composable
private fun ReportCard(report: ActivityReport?) {
    Card(modifier = Modifier.fillMaxWidth().testTag("consumption-report")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.consumption_report_heading), style = MaterialTheme.typography.titleMedium)
            if (report == null) {
                Text(stringResource(R.string.consumption_report_empty))
                return@Column
            }
            Text(stringResource(R.string.consumption_period, report.from, report.to))
            Text(stringResource(R.string.consumption_total, report.totalLiters))
            if (!report.scheduleDefined) {
                Text(stringResource(R.string.consumption_no_schedule), color = MaterialTheme.colorScheme.error)
                return@Column
            }
            Text(stringResource(R.string.consumption_within, report.withinActivityLiters ?: 0.0))
            Text(
                stringResource(R.string.consumption_outside, report.outsideActivityLiters ?: 0.0),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.testTag("consumption-outside"),
            )
            report.outsideActivityPercentage?.let {
                Text(stringResource(R.string.consumption_outside_pct, it))
            }
            report.byWaterPoint.forEach { point ->
                Text(
                    stringResource(
                        R.string.consumption_water_point_line,
                        point.waterPointId.take(8),
                        point.outsideActivityLiters ?: 0.0,
                        point.totalLiters,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
