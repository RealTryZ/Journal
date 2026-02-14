package io.github.realtryz.journal.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
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
 * Displays a journal icon with the name below as a clickable card.
 *
 * Used in the `HomeScreen` overview to represent individual journals. Supports short tap
 * and long press via `combinedClickable`.
 *
 * @param modifier Optional Modifier for layout adjustments.
 * @param name Name of the journal to display.
 * @param color Icon tint color (use `Color.Unspecified` for default behavior).
 * @param onClick Callback for short click.
 * @param onLongClick Callback for long press.
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

/**
 * Large icon button used to create a new journal.
 *
 * Presented in the Home overview as a prominent call-to-action.
 *
 * @param modifier Optional Modifier to adjust size/position.
 * @param onClick Callback executed when the button is tapped.
 */
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
