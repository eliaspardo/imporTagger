import java.io.File

class XRayTagger {
    fun tagTest(scenario: String, testID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        // Parse Feature File looking for Scenario. Get line number.
        var scenarioLine = findLineWhereScenario(scenario,featureFileLines);

        // Check if previous line is tagged, so tag is appended, otherwise, new line created.
        var isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFileLines);

        // Add tag
        return addTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
    }

    // Parse Feature File looking for Scenario. Get line number.
     fun findLineWhereScenario(scenario: String,featureFileLines: MutableList<String>):Int{
        println("Looking for scenario:"+scenario);

        // Trim tabs and whitespaces
        var trimmedScenario = scenario.replace(" ","").replace("\t", "");
        var lineNumber = 0;
        for(line in featureFileLines){
            lineNumber++;
            var trimmedLine = line.replace(" ", "").replace("\t", "");
            if (trimmedLine.contains(trimmedScenario)){
                println("Found Scenario in line :"+lineNumber);
                return lineNumber;
            }
        }
        return 0;
    }

    // Check if previous line is tagged.
    fun checkIfPreviousLineIsTagged(scenarioLine: Int,featureFileLines: MutableList<String>):Boolean{
        var lineNumber = 0;
        for(line in featureFileLines) {
            lineNumber++;
            if(lineNumber==scenarioLine-1){
                println("Line previous to scenario: "+line);
                if(line.contains("@")){
                    return true;
                }
            }

        }
        return false;
    }


    fun addTag(scenarioLine: Int, testID: String, isPreviousLineTagged: Boolean, featureFileLines: MutableList<String>):MutableList<String>{
        if(isPreviousLineTagged){
            // Append to scenarioLine-1
            println("Adding tag to existing tags");
            appendToLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }else{
            // Add newLine in scenario Line
            println("Adding new line with tag");
            addNewLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }
        return featureFileLines
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


    fun isFileTagged(featureFileLines: List<String>, testID:String):Boolean{
        println("Checking if file tagged: "+formatTestTag(testID))
        return featureFileLines.filter{line->line.contains(formatTestTag(testID))}.isNotEmpty();
    }

    fun getScenario(unzippedFileLines: List<String>):String{
        var scenario = "";
        for (line in unzippedFileLines){
            if(line.contains("Scenario:")){
                println("Scenario found:"+line)
                scenario = line
                return scenario;
            }
        }
        return "";
    }
}