package com.fanguskdev.bakiface.testing

import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.EffectRepository
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.picture.PictureEditor
import com.fanguskdev.bakiface.domain.picture.Region
import com.fanguskdev.bakiface.domain.render.FaceRender
import com.fanguskdev.bakiface.domain.settings.PreferencesRepository
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ExportError
import com.fanguskdev.bakiface.domain.shot.ExportedPhoto
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.ImportedPhoto
import com.fanguskdev.bakiface.domain.shot.PhotoExportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoImportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.shot.Shot
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.shot.ShotRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/** A camera whose frames the test sends by hand, remembering which lens was asked for and whether it is running. */
class FakeCamera : FaceCameraRepository {
    val frames = MutableSharedFlow<AppResult<CameraFrame, CameraError>>(replay = 16)
    val lensesAsked = mutableListOf<Lens>()

    /** How many sessions are collecting frames right now: 0 means the camera is off. */
    val running: Int get() = frames.subscriptionCount.value

    override fun frames(lens: Lens): Flow<AppResult<CameraFrame, CameraError>> {
        lensesAsked += lens
        return frames
    }
}

/** Crops nothing: returns a picture of the region's size and remembers what it was asked. */
class FakePictureEditor : PictureEditor {
    var lastRegion: Region? = null
    var lastMirror: Boolean? = null

    override suspend fun crop(picture: Picture, region: Region, mirror: Boolean): Picture {
        lastRegion = region
        lastMirror = mirror
        return FakePicture(region.width, region.height)
    }
}

class FakeShotRepository : ShotRepository {
    val shots = mutableMapOf<ShotId, Shot>()
    private var next = 0

    override suspend fun save(picture: Picture, landmarks: FaceLandmarks?): Shot {
        val shot = Shot(ShotId("shot-${next++}"), picture, landmarks)
        shots[shot.id] = shot
        return shot
    }

    override suspend fun get(id: ShotId): Shot? = shots[id]

    override suspend fun remove(id: ShotId) {
        shots.remove(id)
    }
}

class FakePhotoImport(var result: AppResult<ImportedPhoto, ImportError>) : PhotoImportRepository {
    val sources = mutableListOf<PhotoSource>()

    override suspend fun import(source: PhotoSource): AppResult<ImportedPhoto, ImportError> {
        sources += source
        return result
    }
}

class FakePhotoExport(var result: AppResult<ExportedPhoto, ExportError> = AppResult.Success(ExportedPhoto("done.jpg"))) :
    PhotoExportRepository {
    val exports = mutableListOf<Triple<Picture, FaceRender?, ExportDestination>>()

    override suspend fun export(
        picture: Picture,
        render: FaceRender?,
        destination: ExportDestination,
    ): AppResult<ExportedPhoto, ExportError> {
        exports += Triple(picture, render, destination)
        return result
    }
}

class FakePreferences(initial: EffectId? = null) : PreferencesRepository {
    private val selected = MutableStateFlow(initial)

    override val selectedEffectId: Flow<EffectId?> = selected

    override suspend fun setSelectedEffectId(id: EffectId?) {
        selected.value = id
    }
}

/** A catalogue with the effects the test gives, or the failure it sets. */
class FakeEffectRepository(effects: List<Effect> = emptyList()) : EffectRepository {
    var result: AppResult<ImmutableList<Effect>, CatalogError> = AppResult.Success(effects.toImmutableList())

    override suspend fun effects(): AppResult<ImmutableList<Effect>, CatalogError> = result
}
