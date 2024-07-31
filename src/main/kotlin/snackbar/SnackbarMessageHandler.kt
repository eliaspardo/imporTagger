package snackbar

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult

class SnackbarMessageHandler(){

    companion object{
        lateinit var snackbarController: SnackbarController

        fun setSnackbarController(snackbarController: SnackbarController = LocalSnackbarController.current){
            this.snackbarController = snackbarController
        }

        fun showMessage(message: String){
            snackbarController.showMessage(
                message = message,
                actionLabel = "Action Label Message",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
                onSnackbarResult = ::handleSnackbarResult
            )
        }

        private fun handleSnackbarResult(snackbarResult: SnackbarResult) {
            when (snackbarResult) {
                SnackbarResult.Dismissed -> snackbarController.showMessage("Dismissed")
                SnackbarResult.ActionPerformed -> snackbarController.showMessage("Action Performed")
            }
        }
    }
}