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


class TestInfoFileChooser() {
    var dialogVisible by mutableStateOf(false)
}

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
fun TestInfoFileChooserUI(onTestInfoFileChooserClose: (result: File?) -> Unit,importerViewModel: ImporterViewModel, testInfoFileChooser: TestInfoFileChooser) {
    Button(
        onClick = {testInfoFileChooser.dialogVisible=true},
        enabled = importerViewModel.isFileChooserButtonEnabled(),
        modifier = Modifier.padding(5.dp)) {
        Text("Select Test Info File")
    }
    if(testInfoFileChooser.dialogVisible==true) {
        importerViewModel.dialogOpened()
        SingleFileDialog(
            onCloseRequest = { file ->
                onTestInfoFileChooserClose(file)
                //importerViewModel.dialogClosed()
                testInfoFileChooser.dialogVisible=false
            }
        )
    }
}

@Composable
fun TestInfoFileChooserUIStateful(onTestInfoFileChooserClose: (result: File?) -> Unit,importerViewModel: ImporterViewModel){
    val testInfoFileChooser = TestInfoFileChooser()
    TestInfoFileChooserUI(onTestInfoFileChooserClose,importerViewModel,testInfoFileChooser)
}
