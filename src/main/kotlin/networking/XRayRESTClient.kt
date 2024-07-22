import util.NetworkError
import util.Result
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import networking.IXRayRESTClient
import networking.createKtorHTTPClient
import util.XRayError
import java.io.File

class XRayRESTClient: IXRayRESTClient{
    private val logger = KotlinLogging.logger {}

    override suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String, importerViewModel: ImporterViewModel): HttpStatusCode {
        logger.info("Logging into XRay")
        val client = createKtorHTTPClient();
        val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v2/authenticate") {
            contentType(ContentType.Application.Json)
            setBody("{\n" +
                    "    \"client_id\": \""+xrayClientID+"\",\n" +
                    "    \"client_secret\": \""+xrayClientSecret+"\"\n" +
                    "}")
        }
        importerViewModel.loginResponseCode = response.status.value
        importerViewModel.loginResponseMessage = response.status.description
        importerViewModel.loginToken = response.body()
        // Remove double quotes from token
        importerViewModel.loginToken = importerViewModel.loginToken.replace("\"", "")
        client.close()
        return response.status
    }

    // Import Feature File To XRay
    override suspend fun importFileToXray(featureFilePath: String, importerViewModel: ImporterViewModel): Result<ImportResponse, NetworkError> {
        logger.info("Importing file to XRay")
        val client = createKtorHTTPClient(importerViewModel.loginToken);
        try{
            val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey=TEST") {
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
            importerViewModel.importResponseCode = response.status.value
            importerViewModel.importResponseMessage = response.status.description
            importerViewModel.importResponseBody = response.body()

            client.close()
        // TODO Handle errors here - e.g. timeouts (mock with "http://www.google.com:81/")
        }catch(e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch(e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

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
    }

    override suspend fun downloadCucumberTestsFromXRay(testID: String, importerViewModel: ImporterViewModel): File {
        logger.info("Downloading Cucumber Test from Xray "+testID);
        val client = createKtorHTTPClient(importerViewModel.loginToken);
        val file = File.createTempFile("xrayImporter", ".zip")
        runBlocking {
            val httpResponse: HttpResponse = client.get("https://xray.cloud.getxray.app/api/v2/export/cucumber?keys="+testID) {
                onDownload { bytesSentTotal, contentLength ->
                    logger.debug("Received $bytesSentTotal bytes from $contentLength")
                }
            }
            // TODO Handle error cases - Eg. Key doesn't exist.
            val responseBody: ByteArray = httpResponse.body()
            file.writeBytes(responseBody)
            logger.info("XRay exported ZIP file saved to ${file.path}")
            logger.debug(httpResponse.status.value.toString()+" "+httpResponse.status.description)
        }
        client.close()
        return file.absoluteFile
    }
}