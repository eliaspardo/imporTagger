import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun logInOnXRay(username:String, password:String): HttpStatusCode {
    if(!(username.equals("test")&&password.equals("test"))){
        return HttpStatusCode(404, "Not Found")
    }
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("https://ktor.io/")
    println(response.status)
    client.close()
    return response.status
}


