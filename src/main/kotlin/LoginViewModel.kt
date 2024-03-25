import ImporterViewModel.loginResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun logInOnXRay(username:String, password:String): HttpStatusCode {
    if(!(username.equals("test")&&password.equals("test"))){
        loginResponse = 404
        return HttpStatusCode(404, "Not Found")
    }
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("https://ktor.io/")
    loginResponse = response.status.value
    println(response.status)
    client.close()
    return response.status
}


