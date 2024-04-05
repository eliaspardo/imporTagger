import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ImportButton(onImportClick: () -> () -> Unit) {
    // TODO - Button becomes enabled even if no Test Info file is selected
    if(ImporterViewModel.appState!=AppState.IMPORTING) {
        Button(onClick = onImportClick(), enabled = ImporterViewModel.isImportButtonEnabled()) {
            Text("Import")
        }
    }else {
        LinearProgressIndicator(progress = ImporterViewModel.percentageProcessed)
    }
}