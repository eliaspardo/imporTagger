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
        // TODO Continue parsing response body
        if(!importResponseBody.updatedOrCreatedTests.isEmpty()) processUpdatedOrCreatedTests(featureFilePath, importResponseBody.updatedOrCreatedTests, fileManager, xRayTagger)
        // TODO Uncomment once implemented.
        //if(!importResponseBody.updatedOrCreatedPreconditions.isEmpty()) processUpdatedOrCreatedPreconditions(path, importResponseBody.updatedOrCreatedPreconditions)
    }

    client.close()
    return response.status
}

suspend fun processUpdatedOrCreatedTests(
    featureFilePath: String,
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
            // TODO This needs to write to the same file not +"test"
            fileManager.writeFile(featureFilePath+"test", featureFileLinesTagged)
        }
    }
}

// TODO This is just the scaffolding. To be implemented
fun processUpdatedOrCreatedPreconditions(
    featureFilePath: String,
    updatedOrCreatedPreconditions: List<Precondition>,
    fileManager: FileManager,
    xRayTagger: XRayTagger) {
    for (precondition in updatedOrCreatedPreconditions){
        precondition.key
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
        val responseBody: ByteArray = httpResponse.body()
        file.writeBytes(responseBody)
        println("XRay exported ZIP file saved to ${file.path}")
        println(httpResponse.status.value.toString()+httpResponse.status.description)
    }
    client.close()
    return file.absoluteFile
}


// This is what we need to call to tag tests into featureFiles.
// This process needs to be done per each test case/precondition in each file, so the loop can get quite complex.
// TODO Investigate potential race conditions if we do this too quickly after importing - tests might not yet be available on the API
suspend fun main(args: Array<String>) {
    // TODO Parse response from import calls to Xray to get list of testIDs
    // TODO This should be a list of tests and preconditions.
    //  Find a way to identify: using Backgrounds/Scenarios and TEST_ / PRECON_ tags
    val testID = "TEST-4788"
    val fileManager = FileManager()
    val xRayTagger = XRayTagger()
    // This file matches the TEST-2806 scenario, but not tagged
    //val featureFile = "src/test/resources/fileTEST-2806WithoutTag.feature"
    // This file matches TEST-4788 but is not tagged
    val featureFile = "TEST-4788_untagged.feature"
    logInOnXRay("","");

    // Check if feature file is already tagged, if not, start tagging process
    val featureFileLines = fileManager.readFile(featureFile)
    if(!xRayTagger.isFileTagged(featureFileLines,testID)){
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
        fileManager.writeFile(featureFile+"test", featureFileLinesTagged)
    }
}

