package networking

import ImporterViewModel
import LoginState
import XRayRESTClient
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import snackbar.SnackbarMessageHandler
import util.NetworkError
import util.Result
import java.io.File
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class XRayRESTClientTest {
    lateinit var xRayRESTClient:XRayRESTClient
    lateinit var importerViewModel:ImporterViewModel
    val snackbarMessageHandler = SnackbarMessageHandler()
    val httpClient = createHTTPClient();
    val dummyToken = "eyJhbGciOiJIUzI1IsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnQiOiI1ZTg0MWY1Ny1mNTBkLTM3YzQtYjVkOC0wMTg5YmE5OTU2MzIiLCJhY2NvdW50SWQiOiI2Mjk1ZWM5YTliYzcxNTAwNjhjZDc5ZWUiLCJpc1hlYSI6ZmFsc2UsImlhdCI6MTcyNTYzNzM2NywiZXhwIjoxNzI1NzIzNzY3LCJhdWQiOiJDNDE5NDc4QTc0MEY0NjYyQjA4ODRGRjAyQUZEREE4MiIsImlzcyI6ImNvbS54cGFuZGl0LnBsdWdpbnMueHJheSIsInN1YiI6IkM0MTk0NzhBNzQwRjQ2NjJCMDg4NEZGMDJBRkREQTgyIn0.ilD_KAqCZq-nwDqoeM0dzMzxoAMv-kMEiAcEEuGVUXY".replace("\"", "")

    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+ File.separator+"resources"+ File.separator+"common")
        xRayRESTClient = XRayRESTClient(httpClient)
        importerViewModel = ImporterViewModel(xRayRESTClient,snackbarMessageHandler)
    }

    @AfterTest
    fun tearDown(){
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
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.NO_TOKEN))
    }

    @Test
    fun importFileToXray_invalidToken() = runTest{
        // Setting a dummy TestInfoFile, setting dummy token
        importerViewModel.onTestInfoFileChooserClose(File("src/test/resources/TEST-3470_withPreconditions_untagged.feature"))
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun downloadCucumberTestsFromXRay_notLoggedIn() = runTest{
        val result = xRayRESTClient.downloadCucumberTestsFromXRay("testID",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.NO_TOKEN))
    }

    @Test
    fun downloadCucumberTestsFromXRay_invalidToken() = runTest{
        val result = xRayRESTClient.downloadCucumberTestsFromXRay("testID",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun testNewKtorClient() = runTest{
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(dummyToken),
                //content = HttpResponseData(jsonToken),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val mockedHttpClient = HttpClient(mockEngine){
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Auth) {
                bearer {

                }
            }
        }
        var xRayRESTClient2 = XRayRESTClient(mockedHttpClient)
        var importerViewModel2 = ImporterViewModel(xRayRESTClient2,snackbarMessageHandler)
        xRayRESTClient2.logInOnXRay("test","test")
        importerViewModel2.onUserNameChanged("test")
        importerViewModel2.onPasswordChanged("test")

        importerViewModel2.logIn()
        assertEquals(importerViewModel2.loginState,LoginState.LOGGED_IN)
        //val result = xRayRESTClient.logInOnXRay(mockedHttpClient,"test", "test", importerViewModel)
        //assertEquals(result, Result.Success(LoginResponse("dummyToken")))
        //assertEquals(keyValueStorageImpl.token,"dummyToken")
    }
}