import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ImportButton(onImportClick: () -> Unit, onImportCancelClick: ()-> Unit, importerViewModel: ImporterViewModel) {
    if(importerViewModel.appState!=AppState.IMPORTING) {
        Button(onClick = onImportClick, enabled = importerViewModel.isImportButtonEnabled()) {
            Text("Import")
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
