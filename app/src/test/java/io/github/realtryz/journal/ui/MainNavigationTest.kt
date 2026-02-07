package io.github.realtryz.journal.ui

import io.github.realtryz.journal.navigation.Screens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainNavigationTest {
    @Test
    fun navigateToTopLevel_clearsAndAddsScreen() {
        val backStack = mutableListOf<Any>(Screens.Home, Screens.Detail("1"))

        MainNavigation.navigateToTopLevel(backStack, Screens.Settings)

        assertEquals(1, backStack.size)
        assertEquals(Screens.Settings, backStack.single())
    }

    @Test
    fun pushDetail_addsDetailScreenWithId() {
        val backStack = mutableListOf<Any>(Screens.Home)

        MainNavigation.pushDetail(backStack, "abc")

        assertEquals(2, backStack.size)
        assertEquals(Screens.Detail("abc"), backStack.last())
    }

    @Test
    fun pushOverview_addsOverviewScreenWithId() {
        val backStack = mutableListOf<Any>(Screens.Home)

        MainNavigation.pushOverview(backStack, "xyz")

        assertEquals(2, backStack.size)
        assertEquals(Screens.Overview("xyz"), backStack.last())
    }

    @Test
    fun popBackStack_removesLastWhenPresent() {
        val backStack = mutableListOf<Any>(Screens.Home, Screens.Settings)

        MainNavigation.popBackStack(backStack)

        assertEquals(1, backStack.size)
        assertEquals(Screens.Home, backStack.single())
    }

    @Test
    fun popBackStack_noopWhenEmpty() {
        val backStack = mutableListOf<Any>()

        MainNavigation.popBackStack(backStack)

        assertTrue(backStack.isEmpty())
    }

    @Test
    fun shouldShowBottomBar_onlyForTopLevelScreens() {
        assertTrue(MainNavigation.shouldShowBottomBar(Screens.Home))
        assertTrue(MainNavigation.shouldShowBottomBar(Screens.Settings))
        assertTrue(MainNavigation.shouldShowBottomBar(Screens.Contributions))
        assertFalse(MainNavigation.shouldShowBottomBar(Screens.Detail("1")))
        assertFalse(MainNavigation.shouldShowBottomBar(Screens.Overview("2")))
        assertFalse(MainNavigation.shouldShowBottomBar(null))
    }
}
