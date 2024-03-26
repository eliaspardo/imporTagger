import ImporterViewModel.importResponseCode
import ImporterViewModel.importResponseMessage
import ImporterViewModel.loginResponseCode
import ImporterViewModel.loginResponseMessage
import ImporterViewModel.loginToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

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
    println(response.status)
    client.close()
    return response.status
}

suspend fun importFileToXray(): HttpStatusCode {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.post("https://xray.cloud.getxray.app/api/v1/import/feature?projectKey=TEST") {
        header("Authorization", "Bearer "+loginToken)
        contentType(ContentType.MultiPart.FormData)
        setBody("")
    }
    importResponseCode = response.status.value
    importResponseMessage = response.status.description
    println(response.status)
    client.close()
    return response.status
}

