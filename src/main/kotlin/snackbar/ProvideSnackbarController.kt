package snackbar

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

/**
 * Provides a [SnackbarController] to its content.
 *
 * @param snackbarHostState The [SnackbarHostState] to use.
 * @param coroutineScope The [CoroutineScope] to use.
 * @param content The content that will have access to the [SnackbarController].
 */
@Composable
fun ProvideSnackbarController(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSnackbarController provides SnackbarController(
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope
        ),
        content = content
    )
}

/**
 * Static CompositionLocal that provides access to a [SnackbarController]. The value of the
 * [LocalSnackbarController] is set using the [CompositionLocalProvider] composable. If no
 * [SnackbarController] is provided, an error is thrown.
 */
val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("No SnackbarController provided.")
}