package util

import com.natpryce.konfig.*
import java.io.File

class Config(val filePath:String) {

    val configProp = ConfigurationProperties.fromFile(File(filePath))

    fun getProperty(property: String):String{
        return configProp.get(Key(property, stringType))
    }
}