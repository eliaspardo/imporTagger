import util.Config
import java.io.File

class Constants {
    companion object {
        val PROPERTIES_FILE_PATH = System.getProperty("compose.application.resources.dir")+File.separator+"default.properties"
        private val config = Config(PROPERTIES_FILE_PATH)
        val TEST_TAG = config.getProperty("testTag")
        val PRECONDITION_TAG = config.getProperty("preconditionTag")
        val PRECONDITION_PREFIX = config.getProperty("preconditionPrefix")
        val PROJECT_KEY = config.getProperty("projectKey")
    }
}