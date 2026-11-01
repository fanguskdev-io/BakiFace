package com.fanguskdev.bakiface.data.di

import com.fanguskdev.bakiface.data.catalog.CatalogEffectRepository
import com.fanguskdev.bakiface.data.picture.BitmapPictureEditor
import com.fanguskdev.bakiface.data.settings.DataStorePreferencesRepository
import com.fanguskdev.bakiface.data.shot.InMemoryShotRepository
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.effect.EffectRepository
import com.fanguskdev.bakiface.domain.picture.PictureEditor
import com.fanguskdev.bakiface.domain.settings.PreferencesRepository
import com.fanguskdev.bakiface.domain.shot.ShotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The data layer for the container (ADR 0004): the repositories the domain declares, bound to their implementations,
 * plus what only one platform can give ([platformDataModule]: the camera, the photo library, the preferences file).
 */
fun dataModule(): Module = module {
    includes(platformDataModule())
    single { AppDispatchers(default = Dispatchers.Default, io = Dispatchers.IO) }
    single<EffectRepository> { CatalogEffectRepository(get()) }
    single<PreferencesRepository> { DataStorePreferencesRepository(get()) }
    single<ShotRepository> { InMemoryShotRepository() }
    single<PictureEditor> { BitmapPictureEditor(get()) }
}

/**
 * What each platform provides: `FaceCameraRepository`, `PhotoImportRepository`, `PhotoExportRepository` and the
 * preferences `DataStore`.
 */
internal expect fun platformDataModule(): Module
