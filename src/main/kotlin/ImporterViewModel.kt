import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import networking.IXRayRESTClient
import util.IKeyValueStorage
import util.onError
import util.onSuccess
import java.io.File

class ImporterViewModel(private var iXRayRESTClient: IXRayRESTClient, private var iKeyValueStorage: IKeyValueStorage) {

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
    var loginState by mutableStateOf(LoginState.DEFAULT)
        private set

    var loginResponseCode by mutableStateOf(404)
    var loginResponseMessage by mutableStateOf("")

    var importResponseCode by mutableStateOf(404)
    var importResponseMessage by mutableStateOf("")
    //var initialResponseBody = ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList())
    var importResponseBody by mutableStateOf<ImportResponse>(ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList()))

    // Lambda callback functions for the UI
    val onImportClick: (coroutineScope:CoroutineScope) -> Unit = {
        it.launch {
            importXRayTests()
        }
    }

    var onLoginChanged: (username: String, password: String)-> Unit = { username, password ->
        this.xrayClientID = username
        this.xrayClientSecret = password
    }

    val onLoginClick: (coroutineScope:CoroutineScope) -> Unit = {
        it.launch {
            logIn()
        }
    }

    val onLogoutClick: () -> Unit = {
        logOut()
    }

    val onLoginCancelClick: () -> Unit = {
        // TODO https://github.com/eliaspardo/xray-importer/issues/17
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

    suspend fun logIn() {
        appState=AppState.LOGGING_IN
        isLoggingIn=true
        delay(1000L)
        iXRayRESTClient.logInOnXRay(xrayClientID,xrayClientSecret,this@ImporterViewModel).onSuccess {
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

    /*
     * Logout cleans the storage (token) and clears feature and test info files
     */
    fun logOut() {
        appState=AppState.LOGGING_OUT
        isLoggingIn=true
        iKeyValueStorage.cleanStorage()
        isLoggingIn=false
        featureFiles.clear()
        testInfoFile.value= null
        appState = AppState.DEFAULT
        loginState = LoginState.LOGGED_OUT
    }

    suspend fun importXRayTests(){
        appState=AppState.IMPORTING
        percentageProcessed = 0f
        featureFiles.map{ file-> if(file.isChecked){

            logger.info("Importing file: "+file.path);

            iXRayRESTClient.importFileToXray(file.path,this@ImporterViewModel).onSuccess {
                logger.info("Import and tagging OK. Starting Tagging.");

                // On Success start tagging tests and preconditions
                val fileManager = FileManager()
                val xRayTagger = XRayTagger()
                if(!importResponseBody.updatedOrCreatedTests.isEmpty()) xRayTagger.processUpdatedOrCreatedTests(file.path, importResponseBody.updatedOrCreatedTests, fileManager, iXRayRESTClient, this@ImporterViewModel)
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

enum class AppState {
    DEFAULT, IMPORTING, FEATURE_FILE_DIALOG_OPEN, TEST_INFO_FILE_DIALOG_OPEN, LOGGING_IN, LOGGING_OUT
}

enum class LoginState {
    DEFAULT, LOGGED_OUT, LOGGED_IN, ERROR
}