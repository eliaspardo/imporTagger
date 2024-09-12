package networking

import ImporterViewModel
import LoginResponse
import XRayRESTClient
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
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
    val httpClient = createHttpClient(keyValueStorageImpl);
    val dummyToken = "eyJhbGciOiJIUzI1IsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnQiOiI1ZTg0MWY1Ny1mNTBkLTM3YzQtYjVkOC0wMTg5YmE5OTU2MzIiLCJhY2NvdW50SWQiOiI2Mjk1ZWM5YTliYzcxNTAwNjhjZDc5ZWUiLCJpc1hlYSI6ZmFsc2UsImlhdCI6MTcyNTYzNzM2NywiZXhwIjoxNzI1NzIzNzY3LCJhdWQiOiJDNDE5NDc4QTc0MEY0NjYyQjA4ODRGRjAyQUZEREE4MiIsImlzcyI6ImNvbS54cGFuZGl0LnBsdWdpbnMueHJheSIsInN1YiI6IkM0MTk0NzhBNzQwRjQ2NjJCMDg4NEZGMDJBRkREQTgyIn0.ilD_KAqCZq-nwDqoeM0dzMzxoAMv-kMEiAcEEuGVUXY".replace("\"", "")

    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+ File.separator+"resources"+ File.separator+"common")
        xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)
    }

    @AfterTest
    fun tearDown(){
        keyValueStorageImpl.cleanStorage()
        unmockkAll()
    }

    @Test
    fun logInOnXRay_wrongCredentials() = runTest{
        val result = xRayRESTClient.logInOnXRay("test", "test")
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
    fun importFileToXray_invalidToken() = runTest{
        // Setting a dummy TestInfoFile, setting dummy token
        importerViewModel.onTestInfoFileChooserClose(File("src/test/resources/TEST-3470_withPreconditions_untagged.feature"))
        keyValueStorageImpl.token = dummyToken
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun downloadCucumberTestsFromXRay_notLoggedIn() = runTest{
        keyValueStorageImpl.cleanStorage()
        val result = xRayRESTClient.downloadCucumberTestsFromXRay("testID",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.NO_TOKEN))
    }

    @Test
    fun downloadCucumberTestsFromXRay_invalidToken() = runTest{
        keyValueStorageImpl.token = dummyToken
        val result = xRayRESTClient.downloadCucumberTestsFromXRay("testID",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun testNewKtorClient() = runTest{
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("dummyToken"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val mockedHttpClient = HttpClient(mockEngine){}
        //val result = xRayRESTClient.logInOnXRay(mockedHttpClient,"test", "test", importerViewModel)
        //assertEquals(result, Result.Success(LoginResponse("dummyToken")))
        //assertEquals(keyValueStorageImpl.token,"dummyToken")
    }
}