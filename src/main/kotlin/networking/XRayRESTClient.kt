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

suspend fun importFileToXray(featureFilePath: String): HttpStatusCode {
    logger.info("Importing file to XRay")
    val client = createKtorHTTPClient(loginToken);
    try{
        val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey=TEST") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", java.io.File(featureFilePath).readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")
                            append(HttpHeaders.ContentDisposition,"filename="+java.io.File(featureFilePath))
                        })
                        append("testInfo", java.io.File(ImporterViewModel.testInfoFile.value?.absolutePath).readBytes(), Headers.build {
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

        if(!importResponseBody.errors.isEmpty()){
            logger.warn("There are errors")
            logger.warn(importResponseBody.errors.toString())
        }
        else{
            // TODO This probably shouldn't go here
            logger.debug("There are no errors")
            val fileManager = FileManager()
            val xRayTagger = XRayTagger()
            if(!importResponseBody.updatedOrCreatedTests.isEmpty()) processUpdatedOrCreatedTests(featureFilePath, importResponseBody.updatedOrCreatedTests, fileManager, xRayTagger)
            if(!importResponseBody.updatedOrCreatedPreconditions.isEmpty()) processUpdatedOrCreatedPreconditions(featureFilePath, importResponseBody.updatedOrCreatedPreconditions, fileManager, xRayTagger)
        }

        client.close()
        return response.status
    }catch(e: Throwable){
        // TODO Handle errors here - e.g. timeouts (mock with "http://www.google.com:81/")
        logger.error("Caught "+e)
        return HttpStatusCode.NotFound
    }
}
// TODO Second version of importFileToXray returning correct types, delegating handling to ImproterViewModel.
suspend fun importFileToXray2(featureFilePath: String): Result<ImportResponse, NetworkError> {
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

suspend fun processUpdatedOrCreatedTests(
    featureFilePath:String,
    updatedOrCreatedTests: List<Test>,
    fileManager: FileManager,
    xRayTagger: XRayTagger
) {
    logger.info("Processing Tests for "+featureFilePath)
    val featureFileLines = fileManager.readFile(featureFilePath)
    for (test in updatedOrCreatedTests){
        val testID = test.key
        // Check if feature file is already tagged, if not, start tagging process
        if(!xRayTagger.isFileTagged(featureFileLines,testID)) {
            logger.info("File is not tagged")
            // Download zip file to know which scenario needs tagging
            var zipFile = downloadCucumberTestsFromXRay(testID)
            val unzippedTestFile = fileManager.unzipFile(zipFile)
            fileManager.deleteFile(zipFile)

            // Get Scenario from extracted file
            val unzippedFileLines = fileManager.readFile(unzippedTestFile)
            val scenario = xRayTagger.getScenario(unzippedFileLines)
            fileManager.deleteFile(File(unzippedTestFile))

            // Find Scenario in featureFile and tag it
            val featureFileLinesTagged = xRayTagger.tagTest(scenario, testID, featureFileLines)
            fileManager.writeFile(featureFilePath, featureFileLinesTagged)
        }
    }
}

suspend fun processUpdatedOrCreatedPreconditions(
    featureFilePath:String,
    updatedOrCreatedPreconditions: List<Precondition>,
    fileManager: FileManager,
    xRayTagger: XRayTagger) {
    logger.info("Processing Preconditions for "+featureFilePath)
    // This looks as duplicated code, but we need to re-read the file in case the processUpdatedOrCreatedTests has written to file
    val featureFileLines = fileManager.readFile(featureFilePath)
    for (precondition in updatedOrCreatedPreconditions){
        val preconditionID = precondition.key
        if(!xRayTagger.isFileTagged(featureFileLines,preconditionID)) {
            logger.info("File is not tagged")
            // Find Precondition in featureFile and tag it. Cannot export from XRay so have to go with hardcoded prefix.
            val featureFileLinesTagged = xRayTagger.tagPrecondition(preconditionID, featureFileLines)
            fileManager.writeFile(featureFilePath, featureFileLinesTagged)
        }

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

