import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

@Composable
fun ImportButton(onImportClick: (coroutineScope: CoroutineScope) -> Unit, importerViewModel: ImporterViewModel) {
    val coroutineScope = rememberCoroutineScope()
    if(importerViewModel.appState!=AppState.IMPORTING) {
        Button(onClick = { onImportClick(coroutineScope) }, enabled = importerViewModel.isImportButtonEnabled()) {
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