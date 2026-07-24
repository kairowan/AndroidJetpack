package com.kotlinmvvm.shared.ui.navigation

import com.kotlinmvvm.core.navigation.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTopLevelDestinationTest {
    @Test
    fun everyTopLevelRouteHasExactlyOneDestination() {
        assertEquals(
            AppRoute.TopLevel.entries.toSet(),
            AppTopLevelDestination.entries.map { destination -> destination.route }.toSet()
        )
    }

    @Test
    fun detailWithoutTopLevelFallsBackToHome() {
        assertEquals(
            AppTopLevelDestination.HOME,
            AppTopLevelDestination.from(route = null)
        )
    }
}
