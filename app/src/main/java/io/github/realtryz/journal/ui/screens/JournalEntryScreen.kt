package io.github.realtryz.journal.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.realtryz.journal.R
import io.github.realtryz.journal.ui.components.ConfirmationDialog
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryView(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel,
    journalId: String
) {
    // Ensure the correct journal is selected when entering the screen
    LaunchedEffect(journalId) {
        viewModel.selectJournal(journalId)
    }

    val date by viewModel.selectedDate.collectAsState()
    val entry by viewModel.currentEntry.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val textStyle = LocalTextStyle.current.copy(
        lineHeight = 32.sp,
        fontSize = 18.sp
    )
    val density = LocalDensity.current
    val lineHeightPx = with(density) { textStyle.lineHeight.toPx() }
    val paddingTopPx = with(density) { 16.dp.toPx() }

    LaunchedEffect(date, entry) {
        val selectedDateString = date?.format(DateTimeFormatter.ISO_DATE)
        if (entry?.date == selectedDateString) {
            val newText = entry?.content ?: ""
            if (textFieldValue.text != newText) {
                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
            }
        } else {
            textFieldValue = TextFieldValue("")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveEntry(textFieldValue.text)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                showDeleteConfirmation = true
            }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_entry))
            }

            if(showDeleteConfirmation) {
                ConfirmationDialog(
                    onDelete = { viewModel.deleteEntry()
                               showDeleteConfirmation = false },
                    onDismiss = { showDeleteConfirmation = false },
                    title = stringResource(R.string.delete_entry) + "?",
                    text = stringResource(R.string.permanently_delete_entry),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousDay(textFieldValue.text) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.previous_day)
                    )
                }

                Text(
                    text = date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { showDatePicker = true }
                )

                IconButton(onClick = { viewModel.nextDay(textFieldValue.text) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_day)
                    )
                }
            }

            IconButton(onClick = { viewModel.saveEntry(textFieldValue.text) }) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val color = Color.LightGray.copy(alpha = 0.5f)

                    var y = paddingTopPx + lineHeightPx
                    while (y < size.height) {
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                        y += lineHeightPx
                    }
                },
            placeholder = { Text(stringResource(R.string.on_mind_today)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setDate(selectedDate, textFieldValue.text)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.select_date),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            )
        }
    }
}
