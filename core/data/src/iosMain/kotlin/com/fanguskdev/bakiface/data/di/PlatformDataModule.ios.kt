package com.fanguskdev.bakiface.data.di

import com.fanguskdev.bakiface.data.camera.IosFaceCameraRepository
import com.fanguskdev.bakiface.data.settings.PREFERENCES_FILE_NAME
import com.fanguskdev.bakiface.data.settings.createPreferencesDataStore
import com.fanguskdev.bakiface.data.shot.IosPhotoExportRepository
import com.fanguskdev.bakiface.data.shot.IosPhotoImportRepository
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.shot.PhotoExportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoImportRepository
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

internal actual fun platformDataModule(): Module = module {
    single<FaceCameraRepository> { IosFaceCameraRepository() }
    single<PhotoImportRepository> { IosPhotoImportRepository(get()) }
    single<PhotoExportRepository> { IosPhotoExportRepository(get()) }
    single { createPreferencesDataStore("${documentsDirectory()}/$PREFERENCES_FILE_NAME") }
}

/** The app's private documents folder, where the preferences live (ADR 0007). */
@OptIn(ExperimentalForeignApi::class)
private fun documentsDirectory(): String =
    NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)?.path
        ?: NSTemporaryDirectory().removeSuffix("/")
