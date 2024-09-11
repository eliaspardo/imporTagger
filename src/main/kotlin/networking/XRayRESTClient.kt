import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import networking.IXRayRESTClient
import networking.createKtorHTTPClient
import util.*
import java.io.File

class XRayRESTClient(private var keyValueStorage: KeyValueStorage): IXRayRESTClient{
    private val projectKey = Constants.PROJECT_KEY
    private val logger = KotlinLogging.logger {}
    override suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String, importerViewModel: ImporterViewModel): Result<LoginResponse, NetworkError> {
        logger.info("Logging into XRay")
        val client = createKtorHTTPClient();
        try {
            val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v2/authenticate") {
                contentType(ContentType.Application.Json)
                setBody(
                    "{\n" +
                            "    \"client_id\": \"" + xrayClientID + "\",\n" +
                            "    \"client_secret\": \"" + xrayClientSecret + "\"\n" +
                            "}"
                )
            }

            client.close()
            return when (response.status.value){
                in 200..299 -> {
                    // Remove double quotes from token. Save in storage
                    keyValueStorage.token = response.bodyAsText().replace("\"", "")
                    Result.Success(LoginResponse(response.bodyAsText()))
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                404 -> Result.Error(NetworkError.NOT_FOUND)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> Result.Error(NetworkError.UNKNOWN)
            }
        }catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        } catch(e: HttpRequestTimeoutException) {
            return Result.Error(NetworkError.REQUEST_TIMEOUT)
        }
    }

    // Import Feature File To XRay
    override suspend fun importFileToXray(featureFilePath: String, importerViewModel: ImporterViewModel): Result<ImportResponse, NetworkError> {
        logger.info("Importing file to XRay")

        val featureFileByteArray:ByteArray
        val testInfoFileByteArray:ByteArray
        val importResponseCode:Int
        var importResponseBody = ImportResponse(errors = emptyList(), updatedOrCreatedTests = emptyList(), updatedOrCreatedPreconditions = emptyList())

        try {
            featureFileByteArray = File(featureFilePath).readBytes()
        }catch(exception:Exception){
            return Result.Error(NetworkError.ERROR_READING_FEATURE_FILE)
        }
        try {
            testInfoFileByteArray = File(importerViewModel.testInfoFile.value?.absolutePath).readBytes()
        }catch(exception:Exception){
            return Result.Error(NetworkError.ERROR_READING_TEST_INFO_FILE)
        }

        val client = keyValueStorage.token?.let { createKtorHTTPClient(it)
        }?: run {return Result.Error(NetworkError.NO_TOKEN)}
        try{
            val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey="+projectKey) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", featureFileByteArray, Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition,"filename="+ File(featureFilePath))
                            })
                            append("testInfo", testInfoFileByteArray, Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition,"filename="+importerViewModel.testInfoFile.value)
                            })
                        },
                        boundary="boundary"
                    )
                )
                onUpload { bytesSentTotal, contentLength ->
                    logger.debug("Sent $bytesSentTotal bytes from $contentLength")
                }
            }

            importResponseCode = response.status.value

            try{
                importResponseBody = response.body()
            }catch(se: SerializationException){
                logger.error("Error serializing response: "+response.bodyAsText())
                Result.Error(NetworkError.SERIALIZATION)
            }
            client.close()

            return when (importResponseCode){
                in 200..299 -> {
                    if(!importResponseBody.errors.isEmpty()){
                        logger.warn("There are errors")
                        logger.warn(importResponseBody.errors.toString())
                        Result.Error(XRayError.UNKNOWN)
                    }else{
                        logger.debug("There are no errors importing to XRay")
                    }
                    Result.Success(importResponseBody)
                }
                400 -> Result.Error(NetworkError.BAD_REQUEST)
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                404 -> Result.Error(NetworkError.NOT_FOUND)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> Result.Error(NetworkError.UNKNOWN)
            }
        // TODO Handle errors here - e.g. timeouts (mock with "http://www.google.com:81/")
        }catch(e: UnresolvedAddressException) {
            logger.error(importResponseBody.toString())
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            logger.error(importResponseBody.toString())
            return Result.Error(NetworkError.SERIALIZATION)
        } catch(e: HttpRequestTimeoutException) {
            logger.error(importResponseBody.toString())
            return Result.Error(NetworkError.REQUEST_TIMEOUT)
        }
    }

    override suspend fun downloadCucumberTestsFromXRay(testID: String, importerViewModel: ImporterViewModel): Result<ExportResponse, NetworkError> {
        logger.info("Downloading Cucumber Test from Xray "+testID);
        // TODO Fix this
        val client = keyValueStorage.token?.let { createKtorHTTPClient(it)
        }?: run {return Result.Error(NetworkError.NO_TOKEN)}
        try {
            val httpResponse: HttpResponse =
                client.get("https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=" + testID) {
                    onDownload { bytesSentTotal, contentLength ->
                        logger.debug("Received $bytesSentTotal bytes from $contentLength")
                    }
                }
            client.close()
            return when (httpResponse.status.value){
                in 200..299 -> {
                    logger.debug("There are no errors importing to XRay")
                    Result.Success(ExportResponse(httpResponse.body()))
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                404 -> Result.Error(NetworkError.NOT_FOUND)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> Result.Error(NetworkError.UNKNOWN)
            }
        // TODO Handle errors here - e.g. timeouts (mock with "http://www.google.com:81/")
        }catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        } catch(e: HttpRequestTimeoutException) {
            return Result.Error(NetworkError.REQUEST_TIMEOUT)
        }
    }
}