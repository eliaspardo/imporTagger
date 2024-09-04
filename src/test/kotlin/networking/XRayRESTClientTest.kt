package networking

import ImporterViewModel
import XRayRESTClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

import org.junit.jupiter.api.Assertions.*
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import util.NetworkError
import util.Result
import util.onError
import java.io.File
import java.nio.file.Paths

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

    @Test
    fun logInOnXRay_wrongCredentials() = runTest{
        val result = xRayRESTClient.logInOnXRay("test", "test", importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun importFileToXray() {
    }

    @Test
    fun downloadCucumberTestsFromXRay() {
    }
}