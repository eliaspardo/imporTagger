import java.io.BufferedReader
import java.io.File

class XRayTagger {
    fun tagTest(scenario: String, testID: String, featureFile: File) : Boolean {
        // TODO Parse Feature File looking for Scenario. Get line number.
        // TODO Read line above Scenario: if line exist, append, if blank, newline.
        return true
    }

    fun checkIfFileIsTagged(featureFile: File, testID:String):Boolean{
        val bufferedReader: BufferedReader = featureFile.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        return inputString.contains(testID)
    }


    fun getScenario(unzippedFile:File):String{
        var scenario = "";
        unzippedFile.forEachLine {
            if(it.contains("Scenario:")){
                println(it)
                scenario = it
            }
        }
        return scenario;
    }
}