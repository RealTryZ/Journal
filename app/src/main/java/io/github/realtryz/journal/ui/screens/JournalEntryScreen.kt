package io.github.realtryz.journal.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.realtryz.journal.R
import io.github.realtryz.journal.ui.components.ConfirmationDialog
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryView(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel,
    journalId: String,
    onSaved: () -> Unit
) {
    LaunchedEffect(journalId) {
        if (viewModel.selectedJournalId.value != journalId) {
            viewModel.selectJournal(journalId)
        }
    }

    val date by viewModel.selectedDate.collectAsState()
    val entry by viewModel.currentEntry.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val imageUris = remember { mutableStateListOf<String>() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uris.forEach { uri ->
                if (!imageUris.contains(uri.toString())) {
                    imageUris.add(uri.toString())
                }
            }
        }
    )

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
            imageUris.clear()
            imageUris.addAll(entry?.imageUris ?: emptyList())
        } else {
            textFieldValue = TextFieldValue("")
            imageUris.clear()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveEntry(textFieldValue.text, imageUris.toList())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        JournalEntryTopBar(
            date = date,
            onDeleteClick = { showDeleteConfirmation = true },
            onPreviousDayClick = { viewModel.previousDay(textFieldValue.text, imageUris.toList()) },
            onNextDayClick = { viewModel.nextDay(textFieldValue.text, imageUris.toList()) },
            onDateClick = { showDatePicker = true },
            onSaveClick = {
                viewModel.saveEntry(textFieldValue.text, imageUris.toList())
                onSaved()
            },
            onAddImageClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        if (imageUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(imageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imageUris.remove(uri) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        JournalEntryTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier.weight(1f)
        )
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            onDelete = {
                viewModel.deleteEntry()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false },
            title = stringResource(R.string.delete_entry) + "?",
            text = stringResource(R.string.permanently_delete_entry),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel)
        )
    }

    if (showDatePicker) {
        JournalEntryDatePicker(
            initialDate = date,
            onDateSelected = { selectedDate ->
                viewModel.setDate(selectedDate, textFieldValue.text, imageUris.toList())
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun JournalEntryTopBar(
    date: LocalDate?,
    onDeleteClick: () -> Unit,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    onDateClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAddImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_entry))
            }
            IconButton(onClick = onAddImageClick) {
                Icon(Icons.Default.Image, contentDescription = "Bild hinzufügen")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDayClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }

            Text(
                text = date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDateClick() }
            )

            IconButton(onClick = onNextDayClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        IconButton(onClick = onSaveClick) {
            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
        }
    }
}

@Composable
fun JournalEntryTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = LocalTextStyle.current.copy(
        lineHeight = 32.sp,
        fontSize = 18.sp
    )
    val density = LocalDensity.current
    val lineHeightPx = with(density) { textStyle.lineHeight.toPx() }
    val paddingTopPx = with(density) { 16.dp.toPx() }

    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryDatePicker(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
            ?.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(selectedDate)
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            title = null,
            headline = {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        )
    }
}
