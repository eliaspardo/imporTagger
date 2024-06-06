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
import kotlinx.serialization.json.Json

suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String): HttpStatusCode {
    if((xrayClientID.equals("test")&&xrayClientSecret.equals("test"))){
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v2/authenticate"){
            contentType(ContentType.Application.Json)
            setBody("{\n" +
                    "    \"client_id\": \"C419478A740F4662B0884FF02AFDDA82\",\n" +
                    "    \"client_secret\": \"73515d32e58eceb7e3a24adcd13c902950fb20359c505db19a9ffc4913736357\"\n" +
                    "}")
        }
        loginResponseCode= response.status.value
        loginResponseMessage= response.status.description
        loginToken = response.body()
        // Remove double quotes from token
        loginToken = loginToken.replace("\"", "")
        client.close()
        return response.status
    }
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
    //importResponseBody = response.body()
    val importResponse: ImportResponse = response.body()

    println(importResponseCode)
    println(importResponseMessage)
    println(importResponse.toString())
    // TODO Perform ContentNegotiation and Parse JSON
    // println(importResponseBody.)
    client.close()
    return response.status
}

