package edu.uniquindio.co.tribooo.navigation

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

class SafePopTest {

    @Test
    fun onlyAResumedEntryMayPop() {
        val allowed = Lifecycle.State.entries.filter(::canPop)

        assertEquals(listOf(Lifecycle.State.RESUMED), allowed)
    }
}
