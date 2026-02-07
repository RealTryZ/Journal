package io.github.realtryz.journal.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.realtryz.journal.R

val journalColorResources = listOf(
    R.color.black,
    R.color.teal_700,
    R.color.purple_700
)

/**
 * Shows the Journal Icon with Text.
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
