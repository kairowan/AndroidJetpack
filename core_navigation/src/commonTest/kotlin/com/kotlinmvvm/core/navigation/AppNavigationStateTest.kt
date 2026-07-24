package com.kotlinmvvm.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppNavigationStateTest {
    @Test
    fun emptyBackStackRestoresHome() {
        val state = AppNavigationState.fromBackStack(emptyList())

        assertEquals(listOf(AppRoute.Home), state.backStack)
        assertEquals(AppRoute.Home, state.currentRoute)
    }

    @Test
    fun rootPopNeverProducesAnEmptyBackStack() {
        val state = AppNavigationState(backStack = emptyList()).pop()

        assertEquals(listOf(AppRoute.Home), state.backStack)
    }

    @Test
    fun leavingFullscreenShortsResetsChromeAndSignalsDeactivation() {
        val state = AppNavigationState(
            backStack = listOf(AppRoute.Shorts),
            shellState = AppShellState(
                isShortsFullscreen = true,
                shortsDeactivateSignal = 3
            )
        ).navigateToTopLevel(AppRoute.TopLevel.HOME)

        assertEquals(listOf(AppRoute.Home), state.backStack)
        assertEquals(4, state.shortsDeactivateSignal)
        assertFalse(state.shellState.isShortsFullscreen)
    }
}
