import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class ImportResponse(val errors: List <String>, val updatedOrCreatedTests: List<Test>, val updatedOrCreatedPreconditions: List<Precondition>)

@Serializable
data class Test (
    val id: String,
    val key: String,
    val self: URL
)

@Serializable
data class Precondition (
    val id: String,
    val key: String,
    val self: URL
)