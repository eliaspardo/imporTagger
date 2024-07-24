import androidx.compose.runtime.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import networking.IXRayRESTClient
import util.KeyValueStorage
import util.onError
import util.onSuccess

class ImporterViewModel(private var ixRayRESTClient: IXRayRESTClient) {

    private val logger = KotlinLogging.logger {}
    var featureFiles = mutableStateListOf<FeatureFile>()
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

    val onRemoveFile: (featureFile: FeatureFile) -> Unit = { file ->
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
        return (appState==AppState.DEFAULT&&loginState==LoginState.LOGGED_IN&&getFilesToImport()>0&&testInfoFile!=null)
    }

    fun isLoginError(): Boolean{
        return (loginState==LoginState.ERROR)
    }


    fun maxFilesCheckedReached(): Boolean{
        return featureFiles.filter{ file->file.isChecked==true}.size>=10
    }

    fun addFileToList(featureFile: FeatureFile){
        featureFiles.add(featureFile)
    }

    fun calculatePercentageProcessed(){
        percentageProcessed = percentageProcessed+(1/getFilesToImport().toFloat())
    }

    fun addFilesToList(files: Array<java.io.File>){
        files.forEach{ file->
            // Only add file if not found in list
            if((this.featureFiles.filter{ existingFile->existingFile.name.equals(file.name)&&existingFile.path.equals(file.absolutePath)}.size)==0){
                addFileToList(FeatureFile(file.name, file.absolutePath))
            }
        }
    }

    fun logIn() = GlobalScope.launch {
        launch {
            delay(1000L)
            ixRayRESTClient.logInOnXRay(xrayClientID,xrayClientSecret,this@ImporterViewModel).onSuccess {
                isLoggingIn=false
                appState = AppState.DEFAULT
                loginState = LoginState.LOGGED_IN
                xrayClientSecret=""
            }.onError {
                isLoggingIn=false
                appState = AppState.DEFAULT
                // TODO This is not working. Works with LoginState.LOGGED_OUT.
                loginState = LoginState.ERROR
                xrayClientSecret=""
            }
        }
    }

    // TODO Is this really needed? Can we logout? Does it revoke the token?
    // In any case it would be nice to have, if we want to switch users.
    // https://github.com/eliaspardo/xray-importer/issues/15
    fun logOut() = GlobalScope.launch {
        launch {
            KeyValueStorage.getInstance().cleanStorage()
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

                logger.info("Importing file: "+file.path);

                ixRayRESTClient.importFileToXray(file.path,this@ImporterViewModel).onSuccess {
                    logger.info("Import and tagging OK. Starting Tagging.");

                    // On Success start tagging tests and preconditions
                    val fileManager = FileManager()
                    val xRayTagger = XRayTagger()
                    if(!importResponseBody.updatedOrCreatedTests.isEmpty()) xRayTagger.processUpdatedOrCreatedTests(file.path, importResponseBody.updatedOrCreatedTests, fileManager, ixRayRESTClient, this@ImporterViewModel)
                    if(!importResponseBody.updatedOrCreatedPreconditions.isEmpty()) xRayTagger.processUpdatedOrCreatedPreconditions(file.path, importResponseBody.updatedOrCreatedPreconditions, fileManager)

                    file.isImported = true
                }.onError {
                    logger.info("Error importing file: "+it);
                    file.isError = true
                }
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