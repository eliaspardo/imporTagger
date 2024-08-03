package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable

class SnackbarMessageHandler() {

    companion object {
        lateinit var snackbarController: SnackbarController

        @Composable
        fun setSnackbarController(snackbarController: SnackbarController = LocalSnackbarController.current) {
            this.snackbarController = snackbarController
        }

        fun showMessage(
            message: String,
            actionLabel: String? = null,
            withDismissAction: Boolean = false,
            duration: SnackbarDuration = SnackbarDuration.Short,
            onSnackbarResult: (SnackbarResult) -> Unit = { }
        ) {
            if (!this::snackbarController.isInitialized) return
            snackbarController.showMessage(
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
                SnackbarResult.Dismissed -> snackbarController.showMessage("Dismissed")
                SnackbarResult.ActionPerformed -> snackbarController.showMessage("Action Performed")
            }
        }
    }
}