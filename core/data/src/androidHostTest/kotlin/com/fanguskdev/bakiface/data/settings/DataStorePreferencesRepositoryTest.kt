package com.fanguskdev.bakiface.data.settings

import app.cash.turbine.test
import com.fanguskdev.bakiface.domain.effect.EffectId
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStorePreferencesRepositoryTest {
    private val folder = Files.createTempDirectory("bakiface-preferences").toFile()

    @Test
    fun theChosenEffectIsRememberedAndForgotten() = runTest {
        val repository = DataStorePreferencesRepository(createPreferencesDataStore(folder.resolve(PREFERENCES_FILE_NAME).absolutePath))

        repository.selectedEffectId.test {
            assertNull(awaitItem())
            repository.setSelectedEffectId(EffectId("yujiro"))
            assertEquals(EffectId("yujiro"), awaitItem())
            repository.setSelectedEffectId(null)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
