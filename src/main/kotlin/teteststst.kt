import com.natpryce.konfig.*
import util.Config


fun main(args: Array<String>){
    val config = Config("defaults.properties")
    val testTag = Key("testTag", stringType)
    val preconditionTag = Key("preconditionTag", stringType)
    val preconditionPrefix = Key("preconditionPrefix", stringType)
    println(config.configProp.get(testTag))
    println(config.configProp[preconditionTag])
    println(config.configProp[preconditionPrefix])
}