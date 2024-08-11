import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import networking.IXRayRESTClient
import networking.createKtorHTTPClient
import util.*
import java.io.File

class MockXRayRESTClient(private var keyValueStorage: KeyValueStorage): IXRayRESTClient{
    private val logger = KotlinLogging.logger {}
    override suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String, importerViewModel: ImporterViewModel): Result<LoginResponse, NetworkError> {
        return Result.Error(NetworkError.UNKNOWN)
    }

    // Import Feature File To XRay
    override suspend fun importFileToXray(featureFilePath: String, importerViewModel: ImporterViewModel): Result<ImportResponse, NetworkError> {
        return Result.Error(NetworkError.UNKNOWN)
    }

    override suspend fun downloadCucumberTestsFromXRay(testID: String, importerViewModel: ImporterViewModel): Result<ExportResponse, NetworkError> {
        return Result.Error(NetworkError.UNKNOWN)
    }
}