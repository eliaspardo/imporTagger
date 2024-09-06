package networking

import ImporterViewModel
import XRayRESTClient
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import util.NetworkError
import util.Result
import java.io.File
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class XRayRESTClientTest {
    lateinit var xRayRESTClient:XRayRESTClient
    lateinit var importerViewModel:ImporterViewModel
    val keyValueStorageImpl = KeyValueStorageImpl()
    val snackbarMessageHandler = SnackbarMessageHandler()

    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+ File.separator+"resources"+ File.separator+"common")
        xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)
    }

    @AfterTest
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun logInOnXRay_wrongCredentials() = runTest{
        val result = xRayRESTClient.logInOnXRay("test", "test", importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun importFileToXray_nonExistingFile() = runTest{
        val result = xRayRESTClient.importFileToXray("inexistentFile.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.ERROR_READING_FEATURE_FILE))
    }

    @Test
    fun importFileToXray_nonExistingTestInfoFile() = runTest{
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.ERROR_READING_TEST_INFO_FILE))
    }

    @Test
    fun importFileToXray_notLoggedIn() = runTest{
        // Setting a dummy TestInfoFile, clearing token
        importerViewModel.onTestInfoFileChooserClose(File("src/test/resources/TEST-3470_withPreconditions_untagged.feature"))
        keyValueStorageImpl.cleanStorage()
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.NO_TOKEN))
    }

    @Test
    fun downloadCucumberTestsFromXRay() {
    }
}