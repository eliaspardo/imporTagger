import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeatureFileListUI(onRemoveFile: (featureFile: FeatureFile) -> Unit, importerViewModel: ImporterViewModel) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colors.primary)) {
        Row(Modifier.fillMaxWidth(),  verticalAlignment = Alignment.CenterVertically) {
            Text("Feature Files", Modifier.fillMaxWidth().height(30.dp), textAlign = TextAlign.Center, color = MaterialTheme.colors.onPrimary, fontSize= 20.sp)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Selected", Modifier.width(80.dp), textAlign = TextAlign.Center, color = MaterialTheme.colors.onPrimary)
            Text("Filename", Modifier.fillMaxWidth(0.40f), textAlign = TextAlign.Left, color = MaterialTheme.colors.onPrimary)
            Text("Path", Modifier.fillMaxWidth(0.40f), textAlign = TextAlign.Left, color = MaterialTheme.colors.onPrimary)
            Text("Remove", Modifier.width(60.dp), textAlign = TextAlign.Center, color = MaterialTheme.colors.onPrimary)
        }
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colors.primary).verticalScroll(rememberScrollState())) {
            importerViewModel.featureFileList.forEach { file ->
                Surface(
                    Modifier.fillMaxWidth(),
                    color = if (file.isImported) Color.Green else if (file.isError) MaterialTheme.colors.error else MaterialTheme.colors.background
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

                        if(file.isEnabled()){
                            Checkbox(
                                checked = file.isChecked,
                                onCheckedChange = {importerViewModel.onFeatureFileCheckedChange(file,it)},
                                Modifier.width(80.dp),
                                enabled = file.isEnabled()
                            )
                        }else if(file.isFeatureFile()==false){
                            Icon(Icons.Default.Close, "Not a feature file!", modifier = Modifier.width(60.dp))
                        }else{
                            Icon(Icons.Default.Check, "File imported successfully!", modifier = Modifier.width(60.dp))
                        }
                        Text(file.name, Modifier.fillMaxWidth(0.40f), textAlign = TextAlign.Left)
                        Text(file.path, Modifier.fillMaxWidth(0.40f), textAlign = TextAlign.Left)
                        IconButton(
                            onClick = { onRemoveFile(file) },
                            Modifier.width(60.dp)
                        ) {
                            // TODO Think if we need this
                            if (!file.isImported) Icon(Icons.Default.Delete, "Remove file")
                        }
                    }
                }
            }
        }
    }
}



