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
fun SingleFileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: File?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a TestInfo.json file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (files.size!=0) {onCloseRequest(files.get(0))}
                    else{onCloseRequest(null)}
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

@Composable
fun TestInfoFileChooserUI(onTestInfoFileChooserClick: () -> Unit, onTestInfoFileChooserClose: (result: File?) -> Unit,importerViewModel: ImporterViewModel) {
    Button(
        onClick = onTestInfoFileChooserClick,
        enabled = importerViewModel.isFileChooserButtonEnabled(),
        modifier = Modifier.padding(5.dp)) {
        Text("Select Test Info File")
    }
    if(importerViewModel.appState==AppState.TEST_INFO_FILE_DIALOG_OPEN) {
        SingleFileDialog(
            onCloseRequest = { file ->
                onTestInfoFileChooserClose(file)
            }
        )
    }
}
