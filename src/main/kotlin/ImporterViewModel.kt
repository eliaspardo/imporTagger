import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import mu.KotlinLogging
import networking.IXRayRESTClient
import snackbar.SnackbarController
import snackbar.SnackbarMessageHandler
import util.IKeyValueStorage
import util.onError
import util.onSuccess

class ImporterViewModel(private var iXRayRESTClient: IXRayRESTClient, private var iKeyValueStorage: IKeyValueStorage) {

    private val logger = KotlinLogging.logger {}
    private var loginCoroutineScope = CoroutineScope(Dispatchers.Default)
    private var importCoroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var snackbarController: SnackbarController
    fun setSnackbarController(snackbarController: SnackbarController){
        this.snackbarController = snackbarController
    }

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
    var importResponseBody by mutableStateOf<ImportResponse>(ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList()))

    // Lambda callback functions for the UI
    val onImportClick: () -> Unit = {
        importCoroutineScope.launch {
            importXRayTests()
        }
    }

    val onImportCancelClick: ()-> Unit={
        logger.debug("Clicked Import Cancel button")
        importCoroutineScope.cancel()
        importCoroutineScope = CoroutineScope(Dispatchers.Default)
        appState = AppState.DEFAULT
    }

    var onLoginChanged: (username: String, password: String)-> Unit = { username, password ->
        this.xrayClientID = username
        this.xrayClientSecret = password
    }

    val onLoginClick: () -> Unit = {
        loginCoroutineScope.launch {
            logIn()
        }
    }

    val onLogoutClick: () -> Unit = {
        logOut()
    }

    val onLoginCancelClick: () -> Unit = {
        logger.debug("Clicked Login Cancel button")
        loginCoroutineScope.cancel()
        loginCoroutineScope = CoroutineScope(Dispatchers.Default)
        // Login state remains untouched
        appState = AppState.DEFAULT
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
        println("max files checked reached "+featureFiles.filter{ file->file.isChecked==true}.size)
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
            // TODO This logic could go in another function
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

    val onCheckedChange: (featureFile: FeatureFile, checked: Boolean) -> Unit = { featureFile, checked ->
        if (maxFilesCheckedReached()){
            SnackbarMessageHandler.showMessage("Max. no. of feature files reached!")
            featureFile.isChecked = false
        }
        else featureFile.isChecked = checked
    }
}

enum class AppState {
    DEFAULT, IMPORTING, FEATURE_FILE_DIALOG_OPEN, TEST_INFO_FILE_DIALOG_OPEN, LOGGING_IN, LOGGING_OUT
}

enum class LoginState {
    DEFAULT, LOGGED_OUT, LOGGED_IN, ERROR
}