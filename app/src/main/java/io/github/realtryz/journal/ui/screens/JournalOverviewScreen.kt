package io.github.realtryz.journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.domain.JournalEntry
import io.github.realtryz.journal.ui.theme.BeigeYellow
import io.github.realtryz.journal.ui.viewmodels.JournalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JournalOverviewScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel,
    journalId: String,
    onNavigateToDetail: (String) -> Unit
) {
    val entries by viewModel.getEntriesForJournal(journalId).collectAsState(initial = emptyList())
    val journals by viewModel.journals.collectAsState()
    val journal = journals.find { it.id == journalId }

    Column(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(entries) { entry ->
                JournalOverviewItem(
                    entry = entry,
                    journal = journal,
                    onClick = {
                        viewModel.selectJournalAndDate(journalId, LocalDate.parse(entry.date))
                        onNavigateToDetail(journalId)
                    }
                )
            }
        }
    }
}

@Composable
fun JournalOverviewItem(
    modifier: Modifier = Modifier,
    entry: JournalEntry,
    journal: Journal?,
    onClick: () -> Unit
) {
    val date = LocalDate.parse(entry.date)
    val formattedDate = date.format(DateTimeFormatter.ofPattern("d. MMM"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = BeigeYellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
