package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Immutable

/**
 * An interface that provides methods to display snackbars.
 */
@Immutable
interface SnackbarController {
    /**
     * Displays a text message with an optional action label, and an optional dismiss action.
     *
     * @param message text to be shown in the Snackbar.
     * @param actionLabel optional action label to show as button in the Snackbar.
     * @param withDismissAction a boolean to show a dismiss action in the Snackbar. This is
     * recommended to be set to true better accessibility when a Snackbar is set with a
     * [SnackbarDuration.Indefinite].
     * @param duration duration of the Snackbar.
     * @param onSnackbarResult A callback for when the snackbar is dismissed or the action is performed.
     */
    fun showMessage(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onSnackbarResult: (SnackbarResult) -> Unit = {}
    )
}