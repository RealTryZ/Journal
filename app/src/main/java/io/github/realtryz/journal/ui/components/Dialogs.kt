package io.github.realtryz.journal.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Simple confirmation dialog composable with configurable title, text and buttons.
 *
 * Reusable for delete/confirm flows across the UI.
 *
 * @param onDelete Optional callback executed when the confirm button is pressed.
 * @param onDismiss Optional callback executed on dismiss/cancel.
 * @param title Dialog title.
 * @param text Dialog body text.
 * @param confirmText Text for the confirm button.
 * @param dismissText Text for the dismiss button.
 */
@Composable
fun ConfirmationDialog(
    onDelete: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    title: String,
    text: String,
    confirmText: String,
    dismissText: String
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = {
                    onDelete?.invoke()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss?.invoke() }) {
                Text(dismissText)
            }
        }
    )
}
