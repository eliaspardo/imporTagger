import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import mu.KotlinLogging

data class FeatureFile(val name: String, val path: String) {
    var isChecked by mutableStateOf(false)
    var isImported by mutableStateOf(false)
    var isError by mutableStateOf(false)
    private val logger = KotlinLogging.logger {}

    fun isEnabled(): Boolean {
        if(isImported){
            return false
        }
        // TODO not sure if this is needed
        if (!isFeatureFile()) {
            return false
        }
        // TODO Fix this to check for max files, probably shouldnt be done here
        //if(importerViewModel.maxFilesCheckedReached()&&!isChecked){
            // TODO Show dialog

        //    return false
        //}
        return true
    }

    fun isFeatureFile(): Boolean {
        return name.substringAfterLast('.', "").equals("feature")
    }
}