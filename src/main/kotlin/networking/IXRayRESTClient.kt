package networking

import ImportResponse
import ImporterViewModel
import io.ktor.http.*
import util.NetworkError
import util.Result
import java.io.File

interface IXRayRESTClient {
    suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String,importerViewModel: ImporterViewModel): HttpStatusCode
    suspend fun importFileToXray(featureFilePath: String,importerViewModel: ImporterViewModel): Result<ImportResponse, NetworkError>
    suspend fun downloadCucumberTestsFromXRay(testID: String, importerViewModel: ImporterViewModel): File
}