package io.github.realtryz.journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.realtryz.journal.R

/**
 * Screen displaying credits and links to third-party resources.
 *
 * @param onBackClicked Callback invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(
    onBackClicked: () -> Unit
) {
    val credits = listOf(
        stringResource(R.string.credit_journal) to "https://thenounproject.com/icon/journal-7530841/",
    )

    Scaffold(
        topBar = { ContributionsTopAppBar(onBackClicked) }
    ) { innerPadding ->
        ContributionsContent(credits = credits, innerPadding = innerPadding)
    }
}

/**
 * Top app bar for the Contributions screen with a back navigation icon.
 *
 * @param onBackClicked Callback invoked when the navigation/back icon is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContributionsTopAppBar(onBackClicked: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.contributions_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )
}

/**
 * Content area for the Contributions screen that lists credit items as clickable links.
 *
 * @param credits List of pairs where the first element is the visible text and the second is the URL.
 * @param innerPadding Padding values provided by the parent Scaffold.
 */
@Composable
private fun ContributionsContent(credits: List<Pair<String, String>>, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.credits_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        credits.forEach { (text, url) ->
            CreditLink(text = text, url = url)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Clickable link text that opens the given URL using the platform URI handler.
 *
 * @param text The visible link text.
 * @param url The URL to open when clicked.
 */
@Composable
fun CreditLink(text: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.clickable {
            uriHandler.openUri(url)
        }
    )
}
