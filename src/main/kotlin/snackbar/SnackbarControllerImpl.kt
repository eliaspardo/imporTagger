package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Implementation of the [SnackbarController] interface that uses a [SnackbarHostState] to show
 * Snackbar messages. The [coroutineScope] is used to launch coroutines for showing Snackbar messages.
 *
 * @param snackbarHostState The [SnackbarHostState] used to show Snackbar messages.
 * @param coroutineScope The [CoroutineScope] used to launch coroutines for showing Snackbar messages.
 */
@Immutable
private class SnackbarControllerImpl(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) : SnackbarController {
    /**
     * Shows a Snackbar message with the given parameters and invokes the [onSnackbarResult] callback
     * on the snackbar result.
     */
    override fun showUserMessage(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration,
        onSnackbarResult: (SnackbarResult) -> Unit
    ) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                //withDismissAction = withDismissAction,
                duration = duration
            ).let(onSnackbarResult)
        }
    }
}

/**
 * Returns a [SnackbarController] that uses the given [snackbarHostState] and [coroutineScope] to display snackbars.
 *
 * @param snackbarHostState The [SnackbarHostState] to use.
 * @param coroutineScope The [CoroutineScope] to use.
 * @return A [SnackbarController] that uses the given [snackbarHostState] and [coroutineScope] to display snackbars.
 */
@Stable
fun SnackbarController(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
): SnackbarController = SnackbarControllerImpl(
    snackbarHostState = snackbarHostState,
    coroutineScope = coroutineScope
)