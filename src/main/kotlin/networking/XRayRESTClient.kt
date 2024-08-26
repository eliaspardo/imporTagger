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
            // TODO Review this, probably shouldn't go here. Not needed?
            importerViewModel.loginResponseCode = response.status.value
            importerViewModel.loginResponseMessage = response.status.description

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
        val client = keyValueStorage.token?.let { createKtorHTTPClient(it)
        }?: run {return Result.Error(NetworkError.NO_TOKEN)}
        try{
            val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey="+projectKey) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", File(featureFilePath).readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition,"filename="+ File(featureFilePath))
                            })
                            append("testInfo", File(importerViewModel.testInfoFile.value?.absolutePath).readBytes(), Headers.build {
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
            // TODO Review this, probably shouldn't go here. Not needed?
            importerViewModel.importResponseCode = response.status.value
            importerViewModel.importResponseMessage = response.status.description
            importerViewModel.importResponseBody = response.body()

            client.close()
            return when (importerViewModel.importResponseCode){
                in 200..299 -> {
                    if(!importerViewModel.importResponseBody.errors.isEmpty()){
                        logger.warn("There are errors")
                        logger.warn(importerViewModel.importResponseBody.errors.toString())
                        Result.Error(XRayError.UNKNOWN)
                    }else{
                        logger.debug("There are no errors importing to XRay")
                    }
                    Result.Success(importerViewModel.importResponseBody)
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