package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable

class SnackbarMessageHandler():UserMessageHandler{

    lateinit var snackbarController: SnackbarController
    override fun showUserMessage(message: String) {
        if (!this::snackbarController.isInitialized) return
        snackbarController.showUserMessage(
            message = message,
            actionLabel = null,
            withDismissAction = false,
            duration = SnackbarDuration.Short,
            onSnackbarResult = {}
        )
    }

    fun showUserMessage(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onSnackbarResult: (SnackbarResult) -> Unit = { }
    ) {
        if (!this::snackbarController.isInitialized) return
        snackbarController.showUserMessage(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
            onSnackbarResult = onSnackbarResult
        )
    }

    /*
    * Sample onSnackbarResult callback
    */
    fun handleSnackbarResult(snackbarResult: SnackbarResult) {
        when (snackbarResult) {
            SnackbarResult.Dismissed -> snackbarController.showUserMessage("Dismissed")
            SnackbarResult.ActionPerformed -> snackbarController.showUserMessage("Action Performed")
        }
    }

    @Composable
    fun setSnackbarController(snackbarController: SnackbarController = LocalSnackbarController.current) {
        this.snackbarController = snackbarController
    }
}