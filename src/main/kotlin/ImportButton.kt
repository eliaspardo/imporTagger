import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ImportButton(onImportClick: () -> () -> Unit, importerViewModel: ImporterViewModel) {
    if(importerViewModel.appState!=AppState.IMPORTING) {
        Button(onClick = onImportClick(), enabled = importerViewModel.isImportButtonEnabled()) {
            Text("Import")
        }
        // TODO Review this
        /*if(!importResponseBody.errors.isEmpty()){
            Text(importResponseBody.errors.toString())
        }*/
    }else {
        LinearProgressIndicator(progress = importerViewModel.percentageProcessed)
    }
}