import ImporterViewModel.importResponseBody
import ImporterViewModel.importResponseCode
import ImporterViewModel.importResponseMessage
import ImporterViewModel.loginResponseCode
import ImporterViewModel.loginResponseMessage
import ImporterViewModel.loginToken
import KtorHTTPClient.Companion.getHTTPClientWithJSONParsing
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File

suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String): HttpStatusCode {
    val client = HttpClient(CIO)
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
    val client = getHTTPClientWithJSONParsing(loginToken);
    // TODO Handle errors here - e.g. timeouts
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
            println("Sent $bytesSentTotal bytes from $contentLength")
        }
    }

    importResponseCode = response.status.value
    importResponseMessage = response.status.description
    importResponseBody = response.body()

    if(!importResponseBody.errors.isEmpty()){
        println("There are errors")
        println(importResponseBody.errors.toString())
    }
    else{
        println("There are no errors")
        val fileManager = FileManager()
        val xRayTagger = XRayTagger()
        if(!importResponseBody.updatedOrCreatedTests.isEmpty()) processUpdatedOrCreatedTests(featureFilePath, importResponseBody.updatedOrCreatedTests, fileManager, xRayTagger)
        if(!importResponseBody.updatedOrCreatedPreconditions.isEmpty()) processUpdatedOrCreatedPreconditions(featureFilePath, importResponseBody.updatedOrCreatedPreconditions, fileManager, xRayTagger)
    }

    client.close()
    return response.status
}

suspend fun processUpdatedOrCreatedTests(
    featureFilePath:String,
    updatedOrCreatedTests: List<Test>,
    fileManager: FileManager,
    xRayTagger: XRayTagger
) {
    println("Processing Tests")
    val featureFileLines = fileManager.readFile(featureFilePath)
    for (test in updatedOrCreatedTests){
        val testID = test.key
        // Check if feature file is already tagged, if not, start tagging process
        if(!xRayTagger.isFileTagged(featureFileLines,testID)) {
            println("File is not tagged")
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
    println("Processing Preconditions")
    // This looks as duplicated code, but we need to re-read the file in case the processUpdatedOrCreatedTests has written to file
    val featureFileLines = fileManager.readFile(featureFilePath)
    for (precondition in updatedOrCreatedPreconditions){
        val preconditionID = precondition.key
        if(!xRayTagger.isFileTagged(featureFileLines,preconditionID)) {
            println("File is not tagged")
            // Find Precondition in featureFile and tag it. Cannot export from XRay so have to go with hardcoded prefix.
            val featureFileLinesTagged = xRayTagger.tagPrecondition(preconditionID, featureFileLines)
            fileManager.writeFile(featureFilePath, featureFileLinesTagged)
        }

    }

}

suspend fun downloadCucumberTestsFromXRay(testID: String): File {
    val client = getHTTPClientWithJSONParsing(loginToken);
    val file = File.createTempFile("xrayImporter", ".zip")
    runBlocking {
        val httpResponse: HttpResponse = client.get("https://xray.cloud.getxray.app/api/v2/export/cucumber?keys="+testID) {
            onDownload { bytesSentTotal, contentLength ->
                println("Received $bytesSentTotal bytes from $contentLength")
            }
        }
        // TODO Handle error cases - Eg. Key doesn't exist.
        val responseBody: ByteArray = httpResponse.body()
        file.writeBytes(responseBody)
        println("XRay exported ZIP file saved to ${file.path}")
        println(httpResponse.status.value.toString()+httpResponse.status.description)
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
    file.import();
}

