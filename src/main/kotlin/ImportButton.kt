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
    }else {
        LinearProgressIndicator(progress = ImporterViewModel.percentageProcessed)
    }
}