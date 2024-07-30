package snackbar

import androidx.compose.runtime.Composable

/**
 * A sealed interface for defining user messages that can be displayed in the UI.
 */
sealed interface UserMessage {
    /**
     * A data class that represents a user message as a simple text string.
     *
     * @property value The text string value of the user message.
     */
    data class Text(val value: String) : UserMessage

    companion object {
        /**
         * Returns a [UserMessage.Text] object with the given text [value].
         *
         * @param value The text string value of the user message.
         * @return A new instance of [UserMessage.Text] with the given text [value].
         */
        fun from(value: String) = Text(value = value)
    }
}

/**
 * Returns a [String] representation of this [UserMessage] object.
 */
@Composable
fun UserMessage.asString() = when (this) {
    is UserMessage.Text -> value
}