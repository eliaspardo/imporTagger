package networking

import ExportResponse
import ImportResponse
import ImporterViewModel
import LoginResponse
import io.ktor.http.*
import util.NetworkError
import util.Result
import java.io.File

interface IXRayRESTClient {
    suspend fun logInOnXRay(xrayClientID:String, xrayClientSecret:String,importerViewModel: ImporterViewModel): Result<LoginResponse, NetworkError>
    suspend fun importFileToXray(featureFilePath: String,importerViewModel: ImporterViewModel): Result<ImportResponse, NetworkError>
    suspend fun downloadCucumberTestsFromXRay(testID: String, importerViewModel: ImporterViewModel): Result<ExportResponse, NetworkError>
}