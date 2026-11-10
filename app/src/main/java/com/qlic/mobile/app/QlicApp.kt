package com.qlic.mobile.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.core.localization.AppLanguage
import com.qlic.mobile.core.localization.LanguageRepository
import com.qlic.mobile.core.navigation.QlicNavHost
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
            QlicNavHost(
                isAuthenticated = true,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
            )
        }
    }
}
