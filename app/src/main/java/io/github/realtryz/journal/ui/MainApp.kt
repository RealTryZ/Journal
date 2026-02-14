package io.github.realtryz.journal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import io.github.realtryz.journal.R
import io.github.realtryz.journal.navigation.Screens
import io.github.realtryz.journal.ui.screens.ContributionsScreen
import io.github.realtryz.journal.ui.screens.HomeScreen
import io.github.realtryz.journal.ui.screens.JournalEntryView
import io.github.realtryz.journal.ui.screens.JournalOverviewScreen
import io.github.realtryz.journal.ui.screens.SettingsScreen
import io.github.realtryz.journal.ui.theme.BeigeYellow
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel

/**
 * Entry point for the Compose UI of the app. Initializes the back stack and starts
 * the main scaffold.
 */
@Composable
fun MainApp() {
    val backStack = remember { mutableStateListOf<Any>(Screens.Home) }
    MainScaffold(backStack)
}

/**
 * Main scaffold containing bottom navigation and the NavDisplay host.
 *
 * @param backStack MutableList that holds the navigation back stack (top-level/detail/overview).
 */
@Composable
fun MainScaffold(
    backStack: MutableList<Any>,
) {
    val viewModel: JournalViewModel = viewModel()

    val navigateToTopLevel: (Any) -> Unit = { screen ->
        MainNavigation.navigateToTopLevel(backStack, screen)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val currentScreen = backStack.lastOrNull()
            if (MainNavigation.shouldShowBottomBar(currentScreen)) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen is Screens.Home,
                        onClick = { navigateToTopLevel(Screens.Home) },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = stringResource(R.string.home)
                            )
                        },
                        label = { Text(stringResource(R.string.journals)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screens.Settings,
                        onClick = { navigateToTopLevel(Screens.Settings) },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        },
                        label = { Text(stringResource(R.string.options)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { MainNavigation.popBackStack(backStack) },
            entryProvider = { key ->
                when (key) {
                    is Screens.Home -> NavEntry(key) {
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onNavigateToDetail = { id ->
                                MainNavigation.pushDetail(backStack, id)
                            },
                            onNavigateToOverview = { id ->
                                MainNavigation.pushOverview(backStack, id)
                            }
                        )
                    }

                    is Screens.Detail -> NavEntry(key) {
                        JournalEntryView(
                            modifier = Modifier.padding(innerPadding).background(BeigeYellow),
                            viewModel = viewModel,
                            journalId = key.id,
                            onSaved = { MainNavigation.popBackStack(backStack) }
                        )
                    }

                    is Screens.Overview -> NavEntry(key) {
                        JournalOverviewScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            journalId = key.id,
                            onNavigateToDetail = { id ->
                                MainNavigation.pushDetail(backStack, id)
                            }
                        )
                    }

                    is Screens.Settings -> NavEntry(key) {
                        SettingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            onContributorsClicked = { backStack.add(Screens.Contributions) }
                        )
                    }

                    is Screens.Contributions -> NavEntry(key) {
                        ContributionsScreen(onBackClicked = { MainNavigation.popBackStack(backStack) })
                    }

                    else -> NavEntry(Unit) { Text(stringResource(R.string.unknown_route)) }
                }
            }
        )
    }
}

internal object MainNavigation {
    internal fun navigateToTopLevel(backStack: MutableList<Any>, screen: Any) {
        backStack.clear()
        backStack.add(screen)
    }

    internal fun pushDetail(backStack: MutableList<Any>, id: String) {
        backStack.add(Screens.Detail(id))
    }

    internal fun pushOverview(backStack: MutableList<Any>, id: String) {
        backStack.add(Screens.Overview(id))
    }

    internal fun popBackStack(backStack: MutableList<Any>) {
        backStack.removeLastOrNull()
    }

    internal fun shouldShowBottomBar(currentScreen: Any?): Boolean {
        return currentScreen is Screens.Home ||
            currentScreen is Screens.Settings ||
            currentScreen is Screens.Contributions
    }
}
