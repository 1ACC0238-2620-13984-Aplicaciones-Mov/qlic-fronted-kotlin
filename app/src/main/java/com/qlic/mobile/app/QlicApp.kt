package com.qlic.mobile.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.core.localization.AppLanguage
import com.qlic.mobile.core.localization.LanguageRepository
import kotlinx.coroutines.launch

@Composable
fun QlicRoot(languageRepository: LanguageRepository) {
    val selectedLanguage by languageRepository.language.collectAsStateWithLifecycle(initialValue = null)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedLanguage) {
        selectedLanguage?.let { language ->
            val requestedLocales = LocaleListCompat.forLanguageTags(language.languageTag)
            if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != requestedLocales.toLanguageTags()) {
                AppCompatDelegate.setApplicationLocales(requestedLocales)
            }
        }
    }

    QlicApp(
        selectedLanguage = selectedLanguage ?: AppLanguage.English,
        onLanguageSelected = { language ->
            coroutineScope.launch {
                languageRepository.setLanguage(language)
            }
        },
    )
}

@Composable
fun QlicApp(
    selectedLanguage: AppLanguage = AppLanguage.English,
    onLanguageSelected: (AppLanguage) -> Unit = {},
) {
    QlicTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.language_preview_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
                QlicButton(
                    text = stringResource(R.string.language_english),
                    onClick = { onLanguageSelected(AppLanguage.English) },
                    modifier = Modifier.widthIn(max = 360.dp),
                )
                QlicButton(
                    text = stringResource(R.string.language_spanish),
                    onClick = { onLanguageSelected(AppLanguage.SpanishLatinAmerica) },
                    modifier = Modifier.widthIn(max = 360.dp),
                )
                Text(
                    text = stringResource(
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
    }
}
