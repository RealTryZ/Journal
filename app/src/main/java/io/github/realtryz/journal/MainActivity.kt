package io.github.realtryz.journal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.navigation.Screens
import io.github.realtryz.journal.ui.components.ConfirmationDialog
import io.github.realtryz.journal.ui.screens.ContributionsScreen
import io.github.realtryz.journal.ui.screens.JournalEntryView
import io.github.realtryz.journal.ui.screens.SettingsScreen
import io.github.realtryz.journal.ui.theme.JournalTheme
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel

val journalColorResources = listOf(
    R.color.black,
    R.color.teal_700,
    R.color.purple_700
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JournalTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun NavigationBar(
    backStack: MutableList<Any>,
) {
    val viewModel: JournalViewModel = viewModel()

    val navigateToTopLevel: (Any) -> Unit = { screen ->
        backStack.clear()
        backStack.add(screen)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val currentScreen = backStack.lastOrNull()
            // Die Leiste wird nur auf den Hauptseiten angezeigt
            if (currentScreen is Screens.Home || currentScreen is Screens.Settings || currentScreen is Screens.Contributions) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen is Screens.Home,
                        onClick = { navigateToTopLevel(Screens.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
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
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is Screens.Home -> NavEntry(key) {
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            onNavigateToDetail = { id ->
                                backStack.add(Screens.Detail(id))
                            }
                        )
                    }

                    is Screens.Detail -> NavEntry(key) {
                        JournalEntryView(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            journalId = key.id
                        )
                    }

                    is Screens.Settings -> NavEntry(key) {
                        SettingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            onContributorsClicked = { backStack.add(Screens.Contributions) }
                        )
                    }

                    is Screens.Contributions -> NavEntry(key) {
                        ContributionsScreen(onBackClicked = { backStack.removeLastOrNull() })
                    }

                    else -> NavEntry(Unit) { Text(stringResource(R.string.unknown_route)) }
                }
            }
        )
    }

}

@Composable
fun MainApp() {
    val backStack = remember { mutableStateListOf<Any>(Screens.Home) }

    NavigationBar(backStack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel,
    onNavigateToDetail: (String) -> Unit // Neuer Callback
) {
    val journals by viewModel.journals.collectAsState()
    var journalToEdit by remember { mutableStateOf<Journal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            journals.forEach { journal ->
                JournalItem(
                    name = journal.title,
                    color = Color(journal.color),
                    onClick = {
                        viewModel.selectJournal(journal.id)
                        onNavigateToDetail(journal.id)
                    },
                    onLongClick = { journalToEdit = journal }
                )
            }
            AddJournalButton(onClick = { showAddDialog = true })
        }
    }

    if (showAddDialog) {
        JournalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, argbColor ->
                viewModel.addJournal(title, argbColor)
                showAddDialog = false
            }
        )
    }

    journalToEdit?.let { journal ->
        JournalDialog(
            initialTitle = journal.title,
            initialColorArgb = journal.color,
            titleText = stringResource(R.string.edit_journal),
            confirmText = stringResource(R.string.save),
            onDismiss = { journalToEdit = null },
            onConfirm = { title, argbColor ->
                viewModel.updateJournal(journal.copy(title = title, color = argbColor))
                journalToEdit = null
            },
            onDelete = {
                viewModel.deleteJournal(journal)
                journalToEdit = null
            }
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalDialog(
    initialTitle: String = "",
    initialColorArgb: Int? = null,
    titleText: String = stringResource(R.string.new_journal),
    confirmText: String = stringResource(R.string.create),
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    
    // Wir holen uns die ARGB Werte aus den Ressourcen
    val colorOptions = journalColorResources.map { colorResource(id = it).toArgb() }
    
    var selectedColor by remember { 
        mutableIntStateOf(initialColorArgb ?: colorOptions.first()) 
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ConfirmationDialog(
            onDelete = onDelete,
            onDismiss = { showDeleteConfirm = false },
            title = stringResource(R.string.delete_journal),
            text = stringResource(R.string.delete_journal_and_entries),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column {
                Text(stringResource(R.string.enter_name_and_color))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.color), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { argb ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(argb))
                                .border(
                                    width = if (selectedColor == argb) 3.dp else 0.dp,
                                    color = if (selectedColor == argb) MaterialTheme.colorScheme.outline else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = argb }
                        )
                    }
                }

                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.title))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, selectedColor) },
                enabled = title.isNotBlank()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
Shows the Journal Icon with Text.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalItem(
    modifier: Modifier = Modifier,
    name: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(150.dp, 250.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = R.drawable.journal),
                contentDescription = name,
                tint = color,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.5f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 30.dp, bottom = 90.dp,
                        start = 42.dp, end = 30.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AddJournalButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(150.dp, 250.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_journal),
            modifier = Modifier.size(48.dp)
        )
    }
}
