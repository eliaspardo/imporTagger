import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class File(val name: String, val path: String, ) {
    var isChecked by mutableStateOf(false)
    var isImported by mutableStateOf(false)
    fun isEnabled(): Boolean {
        if(isImported){
            return false
        }
        if (!name.substringAfterLast('.', "").equals("feature")) {
            return false
        }
        if(ImporterViewModel.maxFilesCheckedReached()&&!isChecked){
            // TODO Show dialog

            return false
        }
        return true
    }
    val onCheckedChange: (checked: Boolean) -> Unit = { checked ->
        isChecked = checked
    }

}