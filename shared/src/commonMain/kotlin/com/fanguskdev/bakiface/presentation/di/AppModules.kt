package com.fanguskdev.bakiface.presentation.di

import com.fanguskdev.bakiface.data.di.dataModule
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.usecase.CaptureShotUseCase
import com.fanguskdev.bakiface.domain.usecase.DiscardShotUseCase
import com.fanguskdev.bakiface.domain.usecase.ExportShotUseCase
import com.fanguskdev.bakiface.domain.usecase.GetEffectsUseCase
import com.fanguskdev.bakiface.domain.usecase.GetShotUseCase
import com.fanguskdev.bakiface.domain.usecase.ImportPhotoUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveLivePreviewUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveSelectedEffectUseCase
import com.fanguskdev.bakiface.domain.usecase.RenderShotUseCase
import com.fanguskdev.bakiface.domain.usecase.SelectEffectUseCase
import com.fanguskdev.bakiface.presentation.feature.camera.CameraViewModel
import com.fanguskdev.bakiface.presentation.feature.editor.EditorViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The use cases (ADR 0004): stateless, made fresh for whoever asks; the renderer is pure and shared. */
val domainModule: Module = module {
    single { FaceRenderer() }
    factory { GetEffectsUseCase(get()) }
    factory { ObserveSelectedEffectUseCase(get()) }
    factory { SelectEffectUseCase(get()) }
    factory { ObserveLivePreviewUseCase(get(), get()) }
    factory { CaptureShotUseCase(get(), get()) }
    factory { GetShotUseCase(get()) }
    factory { DiscardShotUseCase(get()) }
    factory { ImportPhotoUseCase(get(), get()) }
    factory { RenderShotUseCase(get()) }
    factory { ExportShotUseCase(get(), get()) }
}

/** The view models of the screens; the editor's gets its shot and character from the route. */
val presentationModule: Module = module {
    viewModel { CameraViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (shotId: ShotId, effectId: EffectId?) ->
        EditorViewModel(shotId, effectId, get(), get(), get(), get(), get(), get())
    }
}

/** Everything the app's container needs, in the order the layers depend on each other. */
fun bakiFaceModules(): List<Module> = listOf(dataModule(), domainModule, presentationModule)
