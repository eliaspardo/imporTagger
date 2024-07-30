package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult

/**
 * Represents a message to be displayed in a snackbar.
 */
sealed interface SnackbarMessage {
    /**
     * Represents a text message to be displayed in a snackbar.
     *
     * @property userMessage text to be shown in the Snackbar.
     * @property actionLabelMessage optional action label to show as button in the Snackbar.
     * @property withDismissAction a boolean to show a dismiss action in the Snackbar. This is
     * recommended to be set to true better accessibility when a Snackbar is set with a
     * [SnackbarDuration.Indefinite].
     * @property duration duration of the Snackbar.
     * @property onSnackbarResult A callback for when the snackbar is dismissed or the action is performed.
     */
    data class Text(
        val userMessage: UserMessage,
        val actionLabelMessage: UserMessage? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val onSnackbarResult: (SnackbarResult) -> Unit = {}
    ) : SnackbarMessage

    companion object {
        /**
         * Returns a new [SnackbarMessage.Text] instance.
         *
         * @param userMessage text to be shown in the Snackbar.
         * @param actionLabelMessage optional action label to show as button in the Snackbar.
         * @param withDismissAction a boolean to show a dismiss action in the Snackbar. This is
         * recommended to be set to true better accessibility when a Snackbar is set with a
         * [SnackbarDuration.Indefinite].
         * @param duration duration of the Snackbar.
         * @param onSnackbarResult A callback for when the snackbar is dismissed or the action is performed.
         * @return a [Text] instance of [SnackbarMessage].
         */
        fun from(
            userMessage: UserMessage,
            actionLabelMessage: UserMessage? = null,
            withDismissAction: Boolean = false,
            duration: SnackbarDuration = SnackbarDuration.Short,
            onSnackbarResult: (SnackbarResult) -> Unit = {}
        ) = Text(
            userMessage = userMessage,
            actionLabelMessage = actionLabelMessage,
            withDismissAction = withDismissAction,
            duration = duration,
            onSnackbarResult = onSnackbarResult
        )
    }
}