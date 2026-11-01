package com.fanguskdev.bakiface.data.shot

import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.shot.ShotId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

private data class SizedPicture(override val width: Int, override val height: Int) : Picture

class InMemoryShotRepositoryTest {
    @Test
    fun aSavedShotCanBeFoundUntilItIsRemoved() = runTest {
        val repository = InMemoryShotRepository()

        val shot = repository.save(SizedPicture(10, 20), null)

        assertEquals(shot, repository.get(shot.id))
        repository.remove(shot.id)
        assertNull(repository.get(shot.id))
        assertNull(repository.get(ShotId("never")))
    }

    @Test
    fun everyShotHasItsOwnName() = runTest {
        val repository = InMemoryShotRepository()

        assertNotEquals(repository.save(SizedPicture(1, 1), null).id, repository.save(SizedPicture(1, 1), null).id)
    }

    @Test
    fun theOldestShotGoesWhenTheyDoNotFit() = runTest {
        val repository = InMemoryShotRepository(capacity = 2)
        val first = repository.save(SizedPicture(1, 1), null)
        val second = repository.save(SizedPicture(2, 2), null)
        val third = repository.save(SizedPicture(3, 3), null)

        assertNull(repository.get(first.id))
        assertEquals(second, repository.get(second.id))
        assertEquals(third, repository.get(third.id))
    }
}
