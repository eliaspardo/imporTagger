import java.io.BufferedReader
import java.io.File

class XRayTagger {
    fun tagTest(scenario: String, testID: String, featureFile: File) : Boolean {
        // TODO Parse Feature File looking for Scenario. Get line number.
        var scenarioLine = findLineWhereScenario(scenario,featureFile);
        var isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFile);
        println(scenarioLine);
        println(isPreviousLineTagged);
        // TODO Read line above Scenario: if line exist, append, if blank, newline.
        addTag(scenarioLine,testID,featureFile,isPreviousLineTagged);
        return true
    }

    fun findLineWhereScenario(scenario: String,featureFile: File):Int{
        println("Looking for scenario:"+scenario);
        // Trim tabs and whitespaces
        var trimmedScenario = scenario.replace(" ","").replace("\t", "");
        var lineNumber = 0;
        var br = featureFile.bufferedReader();
        br.lines().use { lines ->
            for (it in lines) {
                lineNumber++;
                var trimmedLine = it.replace(" ", "").replace("\t", "");
                if (trimmedLine.contains(trimmedScenario)){
                    println("Found Scenario in line :"+lineNumber);
                    return lineNumber;
                }
            }
        }
        return 0;
    }
    fun checkIfPreviousLineIsTagged(scenarioLine: Int,featureFile: File):Boolean{
        var lineNumber = 0;
        var br = featureFile.bufferedReader();
        br.lines().use { lines ->
            for (it in lines) {
                lineNumber++;
                if(lineNumber==scenarioLine-1){
                    println("Line previous to scenario: "+it);
                    if(it.contains("@")){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    fun addTag(scenarioLine:Int, testID: String, featureFile: File, isPreviousLineTagged:Boolean){
        if(isPreviousLineTagged){
            // Append to scenarioLine-1
            println("Adding tag to existing tags");
        }else{
            // Add newLine in scenario Line
            println("Adding new line with tag");
        }
    }


    fun checkIfFileIsTagged(featureFile: File, testID:String):Boolean{
        val bufferedReader: BufferedReader = featureFile.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        return inputString.contains(testID)
    }


    fun getScenario(unzippedFile:File):String{
        var scenario = "";
        var br = unzippedFile.bufferedReader();
        br.lines().use { lines ->
            for (it in lines) {
                if(it.contains("Scenario:")){
                    println("Scenario found:"+it)
                    scenario = it
                    return scenario;
                }
            }
        }
        return "";
    }
}