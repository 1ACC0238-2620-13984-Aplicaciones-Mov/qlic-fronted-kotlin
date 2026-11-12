package com.qlic.mobile.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qlic.mobile.R

/** Error message with a retry button, shared by the screens that load data from the API. */
@Composable
fun FeatureError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "feature-error",
) {
    Column(
        modifier = modifier.testTag(tag),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        QlicButton(text = stringResource(R.string.common_retry), onClick = onRetry)
    }
}
