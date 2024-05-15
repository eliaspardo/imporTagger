import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*

data class File(val name: String, val path: String, ) {
    var isChecked by mutableStateOf(false)
    var isImported by mutableStateOf(false)
    var isError by mutableStateOf(false)
    fun isEnabled(): Boolean {
        if(isImported){
            return false
        }
        // TODO not sure if this is needed
        if (!isFeatureFile()) {
            return false
        }
        if(ImporterViewModel.maxFilesCheckedReached()&&!isChecked){
            // TODO Show dialog

            return false
        }
        return true
    }

    fun isFeatureFile(): Boolean {
        return name.substringAfterLast('.', "").equals("feature")
    }

    // TODO Review
    suspend fun import() {
        val response = importFileToXray(path)
        if(response!= HttpStatusCode.OK){
            isError = true
        }else{
            isImported = true
        }
    }

    val onCheckedChange: (checked: Boolean) -> Unit = { checked ->
        isChecked = checked
    }

}