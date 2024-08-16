package ui

import ImporterViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import util.FileManager

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
    PropertiesDialogButton(propertiesDialog)
    PropertiesDialogDialog(propertiesDialog,importerViewModel)
}

@Composable
fun PropertiesDialogDialog(propertiesDialog: PropertiesDialog, importerViewModel: ImporterViewModel){
    if(propertiesDialog.dialogVisible)importerViewModel.propertiesDialogOpened()
    Dialog(visible = propertiesDialog.dialogVisible, state = DialogState(width=700.dp),onCloseRequest = {propertiesDialog.dialogVisible=false;importerViewModel.propertiesDialogClosed()}, title = propertiesDialog.title, resizable = true ) {
        Column(Modifier.fillMaxWidth()) {
            Text("File Location: "+propertiesDialog.propertiesFileLocation)
            for (line in propertiesDialog.propertiesFileLines){
                Text(line)
            }
        }
    }
}

@Composable
fun PropertiesDialogButton(propertiesDialog: PropertiesDialog){
    Button(onClick = {propertiesDialog.dialogVisible=true},modifier = Modifier.padding(5.dp)){
        Text("See default.properties")
    }
}



