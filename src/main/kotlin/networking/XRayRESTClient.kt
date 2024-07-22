import ImporterViewModel.importResponseBody
import ImporterViewModel.importResponseCode
import ImporterViewModel.importResponseMessage
import ImporterViewModel.loginResponseCode
import ImporterViewModel.loginResponseMessage
import ImporterViewModel.loginToken
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
import networking.createKtorHTTPClient
import util.XRayError
import java.io.File


private val logger = KotlinLogging.logger {}

suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String): HttpStatusCode {
    logger.info("Logging into XRay")
    val client = createKtorHTTPClient();
    val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v2/authenticate") {
        contentType(ContentType.Application.Json)
        setBody("{\n" +
                "    \"client_id\": \""+xrayClientID+"\",\n" +
                "    \"client_secret\": \""+xrayClientSecret+"\"\n" +
                "}")
    }
    loginResponseCode = response.status.value
    loginResponseMessage = response.status.description
    loginToken = response.body()
    // Remove double quotes from token
    loginToken = loginToken.replace("\"", "")
    client.close()
    return response.status
}

// Import Feature File To XRay
suspend fun importFileToXray(featureFilePath: String): Result<ImportResponse, NetworkError> {
    logger.info("Importing file to XRay")
    val client = createKtorHTTPClient(loginToken);
    try{
        val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey=TEST") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", File(featureFilePath).readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")
                            append(HttpHeaders.ContentDisposition,"filename="+ File(featureFilePath))
                        })
                        append("testInfo", File(ImporterViewModel.testInfoFile.value?.absolutePath).readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")
                            append(HttpHeaders.ContentDisposition,"filename="+ImporterViewModel.testInfoFile.value)
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
        importResponseMessage = response.status.description
        importResponseBody = response.body()

        client.close()
    // TODO Handle errors here - e.g. timeouts (mock with "http://www.google.com:81/")
    }catch(e: UnresolvedAddressException) {
        return Result.Error(NetworkError.NO_INTERNET)
    } catch(e: SerializationException) {
        return Result.Error(NetworkError.SERIALIZATION)
    }

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
        401 -> Result.Error(NetworkError.UNAUTHORIZED)
        404 -> Result.Error(NetworkError.NOT_FOUND)
        409 -> Result.Error(NetworkError.CONFLICT)
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)
    }
}

suspend fun downloadCucumberTestsFromXRay(testID: String): File {
    logger.info("Downloading Cucumber Test from Xray "+testID);
    val client = createKtorHTTPClient(loginToken);
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


// TODO Investigate potential race conditions if we do this too quickly after importing - tests might not yet be available on the API
suspend fun main(args: Array<String>) {
    val featureFileName = "TEST-4788_untagged.feature"
    val file = FeatureFile(featureFileName, featureFileName)
    val testInfoFilePath = "testInfo - xms.test.api.json"
    val testInfoFile = File(testInfoFilePath)
    ImporterViewModel.onTestInfoFileChooserClose(testInfoFile)
    logInOnXRay("","");
    //file.import();
}

