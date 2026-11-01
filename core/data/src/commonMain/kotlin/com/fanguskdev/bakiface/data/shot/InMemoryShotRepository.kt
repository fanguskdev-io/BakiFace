package com.fanguskdev.bakiface.data.shot

import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.shot.Shot
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.shot.ShotRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_CAPACITY = 3

/**
 * Keeps the last [capacity] shots in memory, never on disk (ADR 0007): a photo only leaves the app when the person
 * saves or shares it. The oldest one goes when a new one does not fit, so a forgotten shot cannot pile up.
 */
class InMemoryShotRepository(private val capacity: Int = DEFAULT_CAPACITY) : ShotRepository {
    private val mutex = Mutex()
    private val shots = LinkedHashMap<ShotId, Shot>()
    private var counter = 0L

    override suspend fun save(picture: Picture, landmarks: FaceLandmarks?): Shot = mutex.withLock {
        counter += 1
        val shot = Shot(ShotId("shot-$counter"), picture, landmarks)
        shots[shot.id] = shot
        while (shots.size > capacity) shots.remove(shots.keys.first())
        shot
    }

    override suspend fun get(id: ShotId): Shot? = mutex.withLock { shots[id] }

    override suspend fun remove(id: ShotId) {
        mutex.withLock { shots.remove(id) }
    }
}
