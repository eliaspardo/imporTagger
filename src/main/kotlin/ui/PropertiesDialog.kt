package ui

import ImporterViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import util.FileManager

const val PROPERTIES_DIALOG_BUTTON = "properties_dialog_button"
const val PROPERTIES_FILE_LOCATION_FIELD = "properties_file_location"
class PropertiesDialog() {
    var dialogVisible by mutableStateOf(false)
    var title by mutableStateOf("Current default.properties")
    var propertiesFileLocation by mutableStateOf(Constants.PROPERTIES_FILE_PATH)
    val fileManager = FileManager()
    val propertiesFileLines = fileManager.readFile(propertiesFileLocation)
}

@Composable
fun PropertiesDialogUI(importerViewModel: ImporterViewModel){
    val propertiesDialog = PropertiesDialog()
    PropertiesDialogButton(propertiesDialog,importerViewModel)
    PropertiesDialogDialog(propertiesDialog,importerViewModel)
}

@Composable
fun PropertiesDialogDialog(propertiesDialog: PropertiesDialog, importerViewModel: ImporterViewModel){
    if(propertiesDialog.dialogVisible)importerViewModel.dialogOpened()
    Dialog(visible = propertiesDialog.dialogVisible, state = DialogState(width=700.dp),onCloseRequest = {propertiesDialog.dialogVisible=false;importerViewModel.dialogClosed()}, title = propertiesDialog.title, resizable = true ) {
        Column(Modifier.fillMaxWidth()) {
            Text("File Location: "+propertiesDialog.propertiesFileLocation, modifier = Modifier.testTag(PROPERTIES_FILE_LOCATION_FIELD))
            for (line in propertiesDialog.propertiesFileLines){
                Text(line)
            }
        }
    }
}

@Composable
fun PropertiesDialogButton(propertiesDialog: PropertiesDialog,importerViewModel: ImporterViewModel){
    Button(onClick = {propertiesDialog.dialogVisible=true},enabled = importerViewModel.isPropertiesDialogButtonEnabled(), modifier = Modifier.padding(5.dp).testTag(PROPERTIES_DIALOG_BUTTON)){
        Text("See default.properties")
    }
}



