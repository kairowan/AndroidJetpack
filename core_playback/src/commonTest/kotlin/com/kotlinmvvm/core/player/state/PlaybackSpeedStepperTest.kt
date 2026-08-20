package com.kotlinmvvm.core.player.state

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaybackSpeedStepperTest {
    @Test
    fun nextSpeedAdvancesAndWrapsAround() {
        val candidates = listOf(1f, 1.25f, 1.5f, 2f)

        assertEquals(1.25f, PlaybackSpeedStepper.nextSpeed(1f, candidates))
        assertEquals(1f, PlaybackSpeedStepper.nextSpeed(2f, candidates))
        assertEquals(1.5f, PlaybackSpeedStepper.nextSpeed(1.4f, candidates))
    }

    @Test
    fun emptyOptionsKeepCurrentSpeed() {
        assertEquals(1.75f, PlaybackSpeedStepper.nextSpeed(1.75f, emptyList()))
    }
}
