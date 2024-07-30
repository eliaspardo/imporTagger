package snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Handles a [SnackbarMessage] by showing a Snackbar message or visuals using the [snackbarController].
 * The [snackbarMessage] can be either a [SnackbarMessage.Text] or a [SnackbarMessage.Visuals].
 * The [onDismissSnackbar] callback is invoked when the Snackbar is dismissed. If [snackbarMessage]
 * is null, this function returns early and does nothing.
 */
@Composable
fun SnackbarMessageHandler(
    snackbarMessage: SnackbarMessage?,
    onDismissSnackbar: () -> Unit,
    snackbarController: SnackbarController = LocalSnackbarController.current
) {
    if (snackbarMessage == null) return

    when (snackbarMessage) {
        is SnackbarMessage.Text -> {
            val message = snackbarMessage.userMessage.asString()
            val actionLabel = snackbarMessage.actionLabelMessage?.asString()

            LaunchedEffect(snackbarMessage, onDismissSnackbar) {
                snackbarController.showMessage(
                    message = message,
                    actionLabel = actionLabel,
                    withDismissAction = snackbarMessage.withDismissAction,
                    duration = snackbarMessage.duration,
                    onSnackbarResult = snackbarMessage.onSnackbarResult
                )

                onDismissSnackbar()
            }
        }
    }
}