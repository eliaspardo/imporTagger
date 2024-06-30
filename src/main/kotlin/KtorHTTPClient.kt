import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KtorHTTPClient {
    companion object {
        fun getHTTPClientWithJSONParsing(loginToken: String): HttpClient {
            return HttpClient(CIO) {
                install(Logging) {
                    // TODO This logging should be controlled from somewhere else
                    logger = Logger.DEFAULT
                    //level = LogLevel.ALL
                    level = LogLevel.INFO
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
        }
    }
}