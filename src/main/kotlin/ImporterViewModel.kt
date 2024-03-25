import androidx.compose.runtime.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ImporterViewModel {
    var featureFiles = mutableStateListOf<File>()
        private set
    var testInfoFiles = mutableStateListOf<java.io.File>()
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var percentageImported by mutableStateOf(0f)
        private set
    var isLoggingIn by mutableStateOf(false)
        private set
    var appState by mutableStateOf(AppState.DEFAULT)
        private set
    var loginState by mutableStateOf(LoginState.LOGGED_OUT)
        private set

    var loginResponse by mutableStateOf(404)

    // Lambda callback functions for the UI
    val onImportClick: () -> Unit = {
        appState=AppState.IMPORTING
        recalculatePercentageImported()
        startImport()
    }

    var onLoginChanged: (username: String, password: String)-> Unit = { username, password ->
        this.username = username
        this.password = password
    }

    val onLoginClick: () -> Unit = {
        appState=AppState.LOGGING_IN
        isLoggingIn=true
        logIn()
    }

    val onLogoutClick: () -> Unit = {
        appState=AppState.LOGGING_OUT
        isLoggingIn=true
        logOut()
    }

    val onFeatureFileChooserClick: () -> Unit = {
        appState=AppState.FEATURE_FILE_DIALOG_OPEN
    }

    val onTestInfoFileChooserClick: () -> Unit = {
        appState=AppState.TEST_INFO_FILE_DIALOG_OPEN
    }

    val onFeatureFileChooserClose: (result: Array<java.io.File>?) -> Unit ={ files->
        if(files!=null){
            addFilesToList(files)
        }
        appState = AppState.DEFAULT
    }

    val onTestInfoFileChooserClose: (result: Array<java.io.File>?) -> Unit ={ file->
        // Remove existing file
        if(file!=null){
            this.testInfoFiles.add(file.get(0))
        }
        appState = AppState.DEFAULT
    }

    val onRemoveFile: (file: File) -> Unit = { file ->
        featureFiles.remove(file)
    }

    private fun getFilesToImport(): Int{
        return featureFiles.filter{ file->file.isChecked}.size
    }

    private fun getFilesImported(): Int{
        return featureFiles.filter{ testCase->testCase.isImported}.size
    }

    fun isLoginButtonEnabled(): Boolean{
        return (username.isNotEmpty() && password.isNotEmpty() && appState==AppState.DEFAULT)
    }

    fun isLogoutButtonEnabled(): Boolean{
        return (appState==AppState.DEFAULT&&loginState==LoginState.LOGGED_IN)
    }

    fun isFileChooserButtonEnabled(): Boolean{
        return appState==AppState.DEFAULT
    }

    fun isImportButtonEnabled(): Boolean{
        return (appState==AppState.DEFAULT&&loginState==LoginState.LOGGED_IN&&getFilesToImport()>0)
    }

    fun isLoginError(): Boolean{
        return (loginState==LoginState.ERROR)
    }


    fun maxFilesCheckedReached(): Boolean{
        return featureFiles.filter{ file->file.isChecked==true}.size>=10
    }

    fun addFileToList(file: File){
        featureFiles.add(file)
    }

    fun recalculatePercentageImported(){
        percentageImported = getFilesImported()/getFilesToImport().toFloat()
    }

    fun addFilesToList(files: Array<java.io.File>){
        files.forEach{ file->
            // Only add file if not found in list
            if((this.featureFiles.filter{ existingFile->existingFile.name.equals(file.name)&&existingFile.path.equals(file.absolutePath)}.size)==0){
                addFileToList(File(file.name, file.absolutePath))
            }
        }
    }

    fun logIn() = GlobalScope.launch {
        launch {
            delay(1000L)
            if (logInOnXRay(username,password) == HttpStatusCode(200,"OK")){
                isLoggingIn=false
                appState = AppState.DEFAULT
                loginState = LoginState.LOGGED_IN
                password=""
            }else{
                isLoggingIn=false
                appState = AppState.DEFAULT
                // TODO This is not working. Works with LoginState.LOGGED_OUT.
                loginState = LoginState.ERROR
                password=""
            }
        }
    }

    fun logOut() = GlobalScope.launch {
        launch {
            delay(1000L)
            isLoggingIn=false
            appState = AppState.DEFAULT
            loginState = LoginState.LOGGED_OUT
        }
    }

    fun startImport() = GlobalScope.launch {
        launch {
            while(percentageImported<=1.0f){
                println(percentageImported)
                percentageImported+=0.1f
                delay(250L)
                // Modify file to become disabled after imported - Testing purposes
                featureFiles.map{ file-> if(file.isChecked){file.isImported=true}}
                // Uncheck imported files
                featureFiles.map{ file-> if(file.isImported){file.isChecked=false}}
            }
            appState = AppState.DEFAULT
        }
    }
}

enum class AppState {
    DEFAULT, IMPORTING, FEATURE_FILE_DIALOG_OPEN, TEST_INFO_FILE_DIALOG_OPEN, LOGGING_IN, LOGGING_OUT
}

enum class LoginState {
    LOGGED_OUT, LOGGED_IN, ERROR
}