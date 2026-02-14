package io.github.realtryz.journal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.realtryz.journal.R
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.ui.components.AddJournalButton
import io.github.realtryz.journal.ui.components.ConfirmationDialog
import io.github.realtryz.journal.ui.components.JournalItem
import io.github.realtryz.journal.ui.components.journalColorResources
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel

/**
 * Start / overview screen showing the list of journals and available actions (add, edit).
 *
 * @param modifier Optional Modifier.
 * @param viewModel ViewModel that manages the journals.
 * @param onNavigateToDetail Callback to open the detail/edit view.
 * @param onNavigateToOverview Callback to open the grid overview for a journal.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToOverview: (String) -> Unit
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
            },
            onOverview = {
                onNavigateToOverview(journal.id)
                journalToEdit = null
            }
        )
    }
}

/**
 * Dialog to create or edit a journal.
 *
 * Shows a text field for the title, color selection and optional actions (delete / open overview).
 *
 * @param initialTitle Pre-filled title (for edit mode).
 * @param initialColorArgb Pre-selected color as ARGB (optional).
 * @param titleText Dialog title (e.g. "New Journal" / "Edit").
 * @param confirmText Text for the confirm button.
 * @param onDismiss Called when the dialog is dismissed.
 * @param onConfirm Callback with (title, ARGB color) when confirmed.
 * @param onDelete Optional delete callback.
 * @param onOverview Optional callback to open the journal overview.
 */
@Composable
fun JournalDialog(
    initialTitle: String = "",
    initialColorArgb: Int? = null,
    titleText: String = stringResource(R.string.new_journal),
    confirmText: String = stringResource(R.string.create),
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    onOverview: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }

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

                if (onOverview != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onOverview,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.overview))
                    }
                }

                if (onDelete != null) {
                    if (onOverview == null) {
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
                        Text(stringResource(R.string.delete))
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
