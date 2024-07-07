import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging

class KtorHTTPClient {
    companion object {
        val httpClient: HttpClient? = null
        private val kotlinLogger = KotlinLogging.logger {}
        private val timeout:Long = 15000
        fun getHTTPClientWithJSONParsing(loginToken: String): HttpClient {
            if (httpClient!=null) return this.httpClient
            else return HttpClient(CIO) {
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
    }
}