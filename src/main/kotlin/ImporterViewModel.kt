import androidx.compose.runtime.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ImporterViewModel {
    var featureFiles = mutableStateListOf<File>()
        private set
    var testInfoFile = mutableStateOf<java.io.File?>(null)
        private set
    var xrayClientID by mutableStateOf("")
        private set
    var xrayClientSecret by mutableStateOf("")
        private set
    var percentageProcessed by mutableStateOf(0f)
        private set
    var isLoggingIn by mutableStateOf(false)
        private set
    var appState by mutableStateOf(AppState.DEFAULT)
        private set
    var loginState by mutableStateOf(LoginState.LOGGED_OUT)
        private set

    var loginResponseCode by mutableStateOf(404)
    var loginResponseMessage by mutableStateOf("")
    var loginToken by mutableStateOf("")

    var importResponseCode by mutableStateOf(404)
    var importResponseMessage by mutableStateOf("")
    //var initialResponseBody = ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList())
    var importResponseBody by mutableStateOf<ImportResponse>(ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList()))

    // Lambda callback functions for the UI
    val onImportClick: () -> Unit = {
        appState=AppState.IMPORTING
        importXRayTests()
    }

    var onLoginChanged: (username: String, password: String)-> Unit = { username, password ->
        this.xrayClientID = username
        this.xrayClientSecret = password
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

    val onTestInfoFileChooserClose: (result: java.io.File?) -> Unit ={ file->
        if(file!=null){
            this.testInfoFile.value = file
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
        return (xrayClientID.isNotEmpty() && xrayClientSecret.isNotEmpty() && appState==AppState.DEFAULT)
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

    fun calculatePercentageProcessed(){
        percentageProcessed = percentageProcessed+(1/getFilesToImport().toFloat())
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
            if (logInOnXRay(xrayClientID,xrayClientSecret) == HttpStatusCode(200,"OK")){
                isLoggingIn=false
                appState = AppState.DEFAULT
                loginState = LoginState.LOGGED_IN
                xrayClientSecret=""
            }else{
                isLoggingIn=false
                appState = AppState.DEFAULT
                // TODO This is not working. Works with LoginState.LOGGED_OUT.
                loginState = LoginState.ERROR
                xrayClientSecret=""
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

    fun importXRayTests() = GlobalScope.launch {
        launch {
            percentageProcessed = 0f
            featureFiles.map{ file-> if(file.isChecked){
                file.import();
                // TODO Tagging needs to happen here, so we have a reference to the file the test is coming from
                // TODO This process needs to be done per each test case/precondition in each file, so the loop can get quite complex
                // for test in importResponseBody.updatedOrCreatedTests
                //
                calculatePercentageProcessed();
                if(file.isImported){file.isChecked=false}else{
                    file.isError=true
                }
            }}
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