import ImporterViewModel.importResponseBody
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ImportButton(onImportClick: () -> () -> Unit) {
    if(ImporterViewModel.appState!=AppState.IMPORTING) {
        Button(onClick = onImportClick(), enabled = ImporterViewModel.isImportButtonEnabled()) {
            Text("Import")
        }
        // TODO Review this
        /*if(!importResponseBody.errors.isEmpty()){
            Text(importResponseBody.errors.toString())
        }*/
    }else {
        LinearProgressIndicator(progress = ImporterViewModel.percentageProcessed)
    }
}