import ImporterViewModel.importResponseBody
import ImporterViewModel.importResponseCode
import ImporterViewModel.importResponseMessage
import ImporterViewModel.loginResponseCode
import ImporterViewModel.loginResponseMessage
import ImporterViewModel.loginToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile

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

suspend fun importFileToXray(path: String): HttpStatusCode {
    val client = HttpClient (CIO){
        install(Logging){
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(Auth){
            bearer{
               loadTokens {
                   BearerTokens(loginToken,"")
               }
            }
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey=TEST") {

        setBody(
            MultiPartFormDataContent(
                formData {
                    append("file", java.io.File(path).readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition,"filename="+java.io.File(path))
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

    // TODO Continue parsing response body
    if(!importResponseBody.errors.isEmpty()){
        println("There are errors")
        println(importResponseBody.errors.toString())
    }
    else{
        println("There are no errors")
    }

    client.close()
    return response.status
}

suspend fun downloadCucumberTestsFromXRay(): File {
    val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(loginToken, "")
                }
            }
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    val file = File.createTempFile("xrayImporter", ".zip")
    runBlocking {
        val httpResponse: HttpResponse = client.get("https://xray.cloud.getxray.app/api/v2/export/cucumber?keys=TEST-2806") {
            onDownload { bytesSentTotal, contentLength ->
                println("Received $bytesSentTotal bytes from $contentLength")
            }
        }
        val responseBody: ByteArray = httpResponse.body()
        file.writeBytes(responseBody)
        println("A file saved to ${file.path}")
        println(httpResponse.status.value)
        println(httpResponse.status.description)
    }
    client.close()
    return file.absoluteFile
}

fun unzipFile(file:File){
    ZipFile(file).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                File(file.absolutePath+entry.name).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
// TODO for test purposes only
suspend fun main(args: Array<String>) {
    logInOnXRay();
    var file = downloadCucumberTestsFromXRay()
    unzipFile(file)
    // TODO Remove ZIP files and extracted files
}

