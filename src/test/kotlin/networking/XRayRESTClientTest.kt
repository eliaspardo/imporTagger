package networking

import ImporterViewModel
import LoginResponse
import XRayRESTClient
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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


    companion object {
        @JvmStatic
        fun responseCodes() = listOf(
            Arguments.of(HttpStatusCode.Unauthorized,NetworkError.UNAUTHORIZED),
            Arguments.of(HttpStatusCode.NotFound,NetworkError.NOT_FOUND),
            Arguments.of(HttpStatusCode.Conflict,NetworkError.CONFLICT),
            Arguments.of(HttpStatusCode.RequestTimeout,NetworkError.REQUEST_TIMEOUT),
            Arguments.of(HttpStatusCode.PayloadTooLarge,NetworkError.PAYLOAD_TOO_LARGE),
            Arguments.of(HttpStatusCode.TooManyRequests,NetworkError.TOO_MANY_REQUESTS),
            Arguments.of(HttpStatusCode.InternalServerError,NetworkError.SERVER_ERROR)
        )
    }
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
    fun importFileToXray_testInfoFileNotSet() = runTest{
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.ERROR_READING_TEST_INFO_FILE))
    }

    @Test
    fun importFileToXray_nonExistingTestInfoFile() = runTest{
        importerViewModel.onTestInfoFileChooserClose(File("inexistentFile.feature"))
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.ERROR_READING_TEST_INFO_FILE))
    }

    @Test
    fun importFileToXray_notLoggedIn() = runTest{
        // Setting dummy Test Info file and trying to import test case without logging in
        importerViewModel.onTestInfoFileChooserClose(File("src/test/resources/TEST-3470_withPreconditions_untagged.feature"))
        val result = xRayRESTClient.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun downloadCucumberTestsFromXRay_notLoggedIn() = runTest{
        val result = xRayRESTClient.downloadCucumberTestsFromXRay("testID",importerViewModel)
        assertEquals(result, Result.Error(NetworkError.UNAUTHORIZED))
    }

    @Test
    fun logInOnXRay_success_mocked() = runTest{
        var xRayRESTClientMocked = XRayRESTClient(getMockedHTTPClient(HttpStatusCode.OK))
        val result = xRayRESTClientMocked.logInOnXRay("test","test")
        assertEquals(result, Result.Success(LoginResponse(dummyToken)))
    }

    @ParameterizedTest
    @MethodSource("responseCodes")
    fun logInOnXRay_responseCodes_mocked(httpStatusCode: HttpStatusCode, networkError: NetworkError) = runTest{
        var xRayRESTClientMocked = XRayRESTClient(getMockedHTTPClient(httpStatusCode))
        val result = xRayRESTClientMocked.logInOnXRay("test","test")
        assertEquals(result, Result.Error(networkError))
    }

    @ParameterizedTest
    @MethodSource("responseCodes")
    fun importFileToXray_responseCodes_mocked(httpStatusCode: HttpStatusCode, networkError: NetworkError) = runTest{
        // Setting dummy Test Info file and trying to import test case without logging in
        importerViewModel.onTestInfoFileChooserClose(File("src/test/resources/TEST-3470_withPreconditions_untagged.feature"))
        var xRayRESTClientMocked = XRayRESTClient(getMockedHTTPClient(httpStatusCode))
        val result = xRayRESTClientMocked.importFileToXray("src/test/resources/TEST-3470_withPreconditions_untagged.feature",importerViewModel)
        assertEquals(result, Result.Error(networkError))
    }

    @ParameterizedTest
    @MethodSource("responseCodes")
    fun downloadCucumberTestsFromXRay_responseCodes_mocked(httpStatusCode: HttpStatusCode, networkError: NetworkError) = runTest{
        var xRayRESTClientMocked = XRayRESTClient(getMockedHTTPClient(httpStatusCode))
        val result = xRayRESTClientMocked.downloadCucumberTestsFromXRay("test",importerViewModel)
        assertEquals(result, Result.Error(networkError))
    }

    /*
     * Returns a mocked HTTP client which responds with the status code passed in as param
     */
    fun getMockedHTTPClient(httpStatusCode: HttpStatusCode): HttpClient{
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(dummyToken),
                status = httpStatusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
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
    }
}