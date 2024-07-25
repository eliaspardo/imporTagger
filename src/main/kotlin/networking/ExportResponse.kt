import kotlinx.serialization.Serializable

@Serializable
data class ExportResponse(val exportedTestCase: ByteArray)