package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.MockData
import edu.uniquindio.co.tribooo.domain.model.Location
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCurrentLocationUseCaseTest {

    private val repository = LocationRepositoryImpl()
    private val update = UpdateCurrentLocationUseCase(repository)
    private val start = MockData.currentLocation

    @Test
    fun aLargeMoveUpdatesTheLocation() = runTest {
        val moved = Location(start.latitude + 0.01, start.longitude) // ~1,1 km

        assertTrue(update(moved))
        assertEquals(moved, repository.currentLocation.value)
    }

    @Test
    fun aMoveUnderFiftyMetersIsIgnored() = runTest {
        val nudged = Location(start.latitude + 0.0002, start.longitude) // ~22 m

        assertFalse(update(nudged))
        assertEquals(start, repository.currentLocation.value)
    }

    @Test
    fun theSamePointIsIgnored() = runTest {
        assertFalse(update(start))
    }
}
