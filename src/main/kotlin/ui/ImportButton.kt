import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun ImportButton(onImportClick: () -> Unit, onImportCancelClick: ()-> Unit, onTaggingDisabled: (Boolean) -> Unit, importerViewModel: ImporterViewModel) {
    if(importerViewModel.appState!=AppState.IMPORTING) {
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp))
        {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Button(onClick = onImportClick, enabled = importerViewModel.isImportButtonEnabled()) {
                Text("Import & Tag")
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
                Text("Disable tagging?", textAlign = TextAlign.Center, color = MaterialTheme.colors.onBackground)
                Switch(checked = importerViewModel.isTaggingDisabled, onCheckedChange = onTaggingDisabled)

        }
        }
    }else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ){
            LinearProgressIndicator(progress = importerViewModel.percentageProcessed,modifier = Modifier.height(5.dp))
            Button(onClick = onImportCancelClick, enabled = true){
                Text("Cancel")
            }
        }
    }
}
