import java.io.BufferedReader
import java.io.File

class XRayTagger {
    fun tagTest(scenario: String, testID: String, featureFile: File, featureFileLines: MutableList<String>) : Boolean {
        // TODO Parse Feature File looking for Scenario. Get line number.
        var scenarioLine = findLineWhereScenario(scenario,featureFile);
        var isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFile);
        println(scenarioLine);
        println(isPreviousLineTagged);
        // TODO Read line above Scenario: if line exist, append, if blank, newline.
        val outputPath = addTag(scenarioLine,testID,featureFile,isPreviousLineTagged,featureFileLines);
        println(outputPath);
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

    fun addTag(scenarioLine:Int, testID: String, featureFile: File, isPreviousLineTagged:Boolean,featureFileLines: MutableList<String>):String{
        val outputPath = featureFile.absolutePath+"test"

        if(isPreviousLineTagged){
            // Append to scenarioLine-1
            println("Adding tag to existing tags");
            appendToLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }else{
            // Add newLine in scenario Line
            println("Adding new line with tag");
            addNewLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }
        writeFile(featureFile.absolutePath+"test", featureFileLines)
        return outputPath
    }

    fun addNewLine(lines: MutableList<String>, lineIndex: Int, tag: String) {
        if (lineIndex in lines.indices) {
            lines[lineIndex] = "\n    "+tag+"\n"+lines[lineIndex]
        } else {
            throw IndexOutOfBoundsException("Line index $lineIndex is out of bounds.")
        }
    }

    fun appendToLine(lines: MutableList<String>, lineIndex: Int, tag: String) {
        if (lineIndex in lines.indices) {
            lines[lineIndex-1] = lines[lineIndex-1]+" "+tag
        } else {
            throw IndexOutOfBoundsException("Line index $lineIndex is out of bounds.")
        }
    }

    fun formatTestTag(tag: String):String{
        return "@TEST_"+tag;
    }

    fun writeFile(filePath: String, lines: List<String>) {
        File(filePath).printWriter().use { out ->
            lines.forEach { line ->
                out.println(line)
            }
        }
    }


    fun isFileTagged(featureFileLines: List<String>, testID:String):Boolean{
        println("Checking if file tagged: "+formatTestTag(testID))
        return featureFileLines.filter{line->line.contains(formatTestTag(testID))}.isNotEmpty();
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