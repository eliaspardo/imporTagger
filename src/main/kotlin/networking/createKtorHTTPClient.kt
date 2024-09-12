package networking

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import util.KeyValueStorage
import util.NetworkError
import util.Result


/*
* Get HTTP Client. Setup with token.
*/
fun createKtorHTTPClient(loginToken: String): HttpClient {
    val kotlinLogger = KotlinLogging.logger {}
    val timeout:Long = 15000

    return HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            if (kotlinLogger.isDebugEnabled) level = LogLevel.ALL
            else level = LogLevel.INFO
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
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
        }
    }
}

/*
* Get HTTP Client. If no token is passed, this must be used for unauthenticated calls or logins.
*/
fun createKtorHTTPClient(): HttpClient {

    val kotlinLogger = KotlinLogging.logger {}
    val timeout:Long = 15000

    return HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            if (kotlinLogger.isDebugEnabled) level = LogLevel.ALL
            else level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
        }
        /*install(Auth){
            bearer{
                refreshTokens {
                    client.post("https://xray.cloud.getxray.app/api/v2/authenticate") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            "{\n" +
                                    "    \"client_id\": \"" + xrayClientID + "\",\n" +
                                    "    \"client_secret\": \"" + xrayClientSecret + "\"\n" +
                                    "}"
                        )
                    }
                    BearerTokens(response.bodyAsText().replace("\"", ""),"")
                }
            }
        }*/
    }
}

fun createHttpClient(keyValueStorage: KeyValueStorage): HttpClient{
    keyValueStorage.token?.let { println("We have a token"); return createKtorHTTPClient(it)
    }?: run {println("We dont have a token"); return createKtorHTTPClient()}
}
