import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import util.FileManager
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


class FeatureFileChooser() {
    var dialogVisible by mutableStateOf(false)
}
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
fun FeatureFileChooserUI(onFeatureFileChooserClose: (result: Array<File>?) -> Unit, importerViewModel: ImporterViewModel, featureFileChooser: FeatureFileChooser) {
    Button(
        onClick = {featureFileChooser.dialogVisible=true},
        enabled = importerViewModel.isFileChooserButtonEnabled(),
        modifier = Modifier.padding(5.dp)) {
        Text("Select Feature Files")
    }
    if(featureFileChooser.dialogVisible==true) {
        importerViewModel.dialogOpened()
        MultipleFileDialog(
            onCloseRequest = { file ->
                onFeatureFileChooserClose(file)
                //importerViewModel.dialogClosed()
                featureFileChooser.dialogVisible=false
            }
        )
    }
}
@Composable
fun FeatureFileChooserUIStateful(onFeatureFileChooserClose: (result: Array<File>?) -> Unit, importerViewModel: ImporterViewModel){
    val featureFileChooser = FeatureFileChooser()
    FeatureFileChooserUI(onFeatureFileChooserClose, importerViewModel, featureFileChooser)
}
