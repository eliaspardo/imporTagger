import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun MultipleFileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: Array<File>?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose your Feature files", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setMultipleMode(true)
                super.setVisible(value)
                if (value) {
                    onCloseRequest(files)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

@Composable
fun FeatureFileChooserUI(onFeatureFileChooserClick: () -> Unit, onFeatureFileChooserClose: (result: Array<File>) -> Unit) {
    Button(
        onClick = onFeatureFileChooserClick,
        enabled = ImporterViewModel.isFileChooserButtonEnabled(),
        modifier = Modifier.padding(5.dp)) {
        Text("Select Feature Files")
    }
    if(ImporterViewModel.appState==AppState.FEATURE_FILE_DIALOG_OPEN) {
        MultipleFileDialog(
            onCloseRequest = { file ->
                onFeatureFileChooserClose(file!!)
            }
        )
    }
}
