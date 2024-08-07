import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import mu.KotlinLogging
import networking.IXRayRESTClient
import snackbar.UserMessageHandler
import util.KeyValueStorage
import util.onError
import util.onSuccess

class ImporterViewModel(private var iXRayRESTClient: IXRayRESTClient, private var keyValueStorage: KeyValueStorage, private var iUserMessageHandler: UserMessageHandler) {

    private val logger = KotlinLogging.logger {}
    private var loginCoroutineScope = CoroutineScope(Dispatchers.Default)
    private var importCoroutineScope = CoroutineScope(Dispatchers.Default)

    var featureFileList = mutableStateListOf<FeatureFile>()
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


    /*
     * Lambda callback functions for the UI
     */

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

    var onUserNameChanged: (username: String)-> Unit = { username ->
        this.xrayClientID = username
    }

    var onPasswordChanged: (password: String)-> Unit = { password ->
        this.xrayClientSecret = password
    }

    val onLoginClick: () -> Unit = {
        loginCoroutineScope.launch {
            logIn()
        }
    }

    val onLoginCancelClick: () -> Unit = {
        logger.debug("Clicked Login Cancel button")
        loginCoroutineScope.cancel()
        loginCoroutineScope = CoroutineScope(Dispatchers.Default)
        // Login state remains untouched
        appState = AppState.DEFAULT
        iUserMessageHandler.showUserMessage("Cancelled login")
    }

    val onLogoutClick: () -> Unit = {
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
            addFeatureFilesToFeatureFileList(files)
        }
        appState = AppState.DEFAULT
    }

    val onTestInfoFileChooserClose: (result: java.io.File?) -> Unit ={ file->
        if(file!=null){
            this.testInfoFile.value = file
        }
        appState = AppState.DEFAULT
    }

    val onRemoveFeatureFile: (featureFile: FeatureFile) -> Unit = { file ->
        featureFileList.remove(file)
    }

    /*
    * Checks what to do when clicking on a file, whether it should get selected or not.
    */
    val onFeatureFileCheckedChange: (featureFile: FeatureFile, checked: Boolean) -> Unit = { featureFile, checked ->
        if (maxFilesCheckedReached()){
            logger.warn("Max. no. of feature files reached!");
            iUserMessageHandler.showUserMessage("Max. no. of feature files reached!")
            featureFile.isChecked = false
        }
        else featureFile.isChecked = checked
    }

    private fun getFilesToImport(): Int{
        return featureFileList.filter{file->file.isChecked}.size
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

    fun maxFilesCheckedReached(): Boolean{
        return featureFileList.filter{ file->file.isChecked==true}.size>=10
    }

    fun calculatePercentageProcessed(){
        percentageProcessed = percentageProcessed+(1/getFilesToImport().toFloat())
    }

    fun addFeatureFilesToFeatureFileList(files: Array<java.io.File>){
        files.forEach{ file->
            if(!isFeatureFileAlreadyInFeatureFileList(file)){
                featureFileList.add(FeatureFile(file.name, file.absolutePath))
            }
        }
    }

    fun isFeatureFileAlreadyInFeatureFileList(file: java.io.File): Boolean{
        return this.featureFileList.filter{ existingFile->existingFile.name.equals(file.name)&&existingFile.path.equals(file.absolutePath)}.size!=0
    }

    /*
    * This function logs in on XRay, sets correct state
     */
    suspend fun logIn() {
        logger.debug("Logging in")
        appState=AppState.LOGGING_IN
        isLoggingIn=true
        delay(1000L)
        iXRayRESTClient.logInOnXRay(xrayClientID,xrayClientSecret,this@ImporterViewModel).onSuccess {
            isLoggingIn=false
            appState = AppState.DEFAULT
            loginState = LoginState.LOGGED_IN
            xrayClientSecret=""
            iUserMessageHandler.showUserMessage("Successfully logged in")
            logger.debug("Successfully logged in")
        }.onError {
            isLoggingIn=false
            appState = AppState.DEFAULT
            loginState = LoginState.ERROR
            xrayClientSecret=""
            iUserMessageHandler.showUserMessage("Error logging in "+it)
            logger.error("Error logging in "+it)
        }
    }

    /*
     * Logout cleans the storage (token) and clears feature and test info files
     */
    fun logOut() {
        logger.debug("Logging out")
        appState=AppState.LOGGING_OUT
        isLoggingIn=true
        keyValueStorage.cleanStorage()
        isLoggingIn=false
        featureFileList.clear()
        testInfoFile.value= null
        appState = AppState.DEFAULT
        loginState = LoginState.LOGGED_OUT
        iUserMessageHandler.showUserMessage("Successfully logged out")
        logger.debug("Successfully logged out")
    }

    /*
    * Iterates through selected Feature Files in list, importing and tagging them. Handles state.
     */
    suspend fun importXRayTests(){
        iUserMessageHandler.showUserMessage("Importing test cases")
        logger.debug("Importing test cases")
        appState=AppState.IMPORTING
        percentageProcessed = 0f
        featureFileList.map{ file-> if(file.isChecked){

            logger.info("Importing file: "+file.path);
            iXRayRESTClient.importFileToXray(file.path,this@ImporterViewModel).onSuccess {
                logger.info("Import and tagging OK. Starting Tagging.");
                // On Success start tagging tests and preconditions
                val fileManager = FileManager()
                val xRayTagger = XRayTagger(iUserMessageHandler)
                if(!importResponseBody.updatedOrCreatedTests.isEmpty()) xRayTagger.processUpdatedOrCreatedTests(file.path, importResponseBody.updatedOrCreatedTests, fileManager, iXRayRESTClient, this@ImporterViewModel)
                if(!importResponseBody.updatedOrCreatedPreconditions.isEmpty()) xRayTagger.processUpdatedOrCreatedPreconditions(file.path, importResponseBody.updatedOrCreatedPreconditions, fileManager)

                file.isImported = true
                iUserMessageHandler.showUserMessage("Imported file successfully")
            }.onError {
                logger.error("Error importing file: "+it);
                iUserMessageHandler.showUserMessage("Error importing file: "+it)
                file.isError = true
            }
            calculatePercentageProcessed();
            if(file.isImported){file.isChecked=false}else{
                file.isError=true
            }
        }}
        appState = AppState.DEFAULT
        iUserMessageHandler.showUserMessage("Finished importing test cases")
        logger.debug("Finished importing test cases")
    }
}

enum class AppState {
    DEFAULT, IMPORTING, FEATURE_FILE_DIALOG_OPEN, TEST_INFO_FILE_DIALOG_OPEN, LOGGING_IN, LOGGING_OUT
}

enum class LoginState {
    DEFAULT, LOGGED_OUT, LOGGED_IN, ERROR
}