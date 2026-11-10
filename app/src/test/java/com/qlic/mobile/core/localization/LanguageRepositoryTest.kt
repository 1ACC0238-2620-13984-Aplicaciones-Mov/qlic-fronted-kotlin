package com.qlic.mobile.core.localization

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LanguageRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var preferencesFile: File

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        preferencesFile = File(temporaryFolder.newFolder("preferences"), "language.preferences_pb")
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `defaults to English when no language was selected`() = runBlocking {
        val repository = repository()

        assertEquals(AppLanguage.English, repository.language.first())
    }

    @Test
    fun `stores Latin American Spanish as the selected language`() = runBlocking {
        val repository = repository()

        repository.setLanguage(AppLanguage.SpanishLatinAmerica)

        assertEquals(AppLanguage.SpanishLatinAmerica, repository.language.first())
    }

    private fun repository(): LanguageRepository = DataStoreLanguageRepository(
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { preferencesFile },
        ),
    )
}
