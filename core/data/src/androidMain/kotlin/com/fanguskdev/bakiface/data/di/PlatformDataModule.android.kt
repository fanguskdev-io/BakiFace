package com.fanguskdev.bakiface.data.di

import com.fanguskdev.bakiface.data.camera.AndroidFaceCameraRepository
import com.fanguskdev.bakiface.data.settings.PREFERENCES_FILE_NAME
import com.fanguskdev.bakiface.data.settings.createPreferencesDataStore
import com.fanguskdev.bakiface.data.shot.AndroidPhotoExportRepository
import com.fanguskdev.bakiface.data.shot.AndroidPhotoImportRepository
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.shot.PhotoExportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoImportRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun platformDataModule(): Module = module {
    single<FaceCameraRepository> { AndroidFaceCameraRepository(androidContext()) }
    single<PhotoImportRepository> { AndroidPhotoImportRepository(androidContext(), get()) }
    single<PhotoExportRepository> { AndroidPhotoExportRepository(androidContext(), get()) }
    single { createPreferencesDataStore(androidContext().filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath) }
}
