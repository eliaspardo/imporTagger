package networking

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
/*
* Get HTTP Client.
*/
fun createHTTPClient(): HttpClient {

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

            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
}
