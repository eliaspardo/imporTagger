class XRayTagger {
    val testTag = "@TEST_"
    val preconditionTag = "#PRECON_"
    fun tagTest(scenario: String, testID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        // Parse Feature File looking for Scenario. Get line number.
        var scenarioLine = findLineWhereScenarioOrPrecondition(scenario,featureFileLines);

        // Check if previous line is tagged, so tag is appended, otherwise, new line created.
        var isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFileLines);

        // Add tag
        return addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
    }

    // TODO
    fun tagPrecondition(precondition: String, preconditionID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        // Parse Feature File looking for Scenario. Get line number.
        var preconditionLine = findLineWhereScenarioOrPrecondition(precondition,featureFileLines);

        // Add tag
        return addPreconditionTag(preconditionLine, preconditionID, featureFileLines);
    }

    // Parse Feature File looking for Scenario. Get line number.
     fun findLineWhereScenarioOrPrecondition(scenarioOrPrecondition: String, featureFileLines: MutableList<String>):Int{
        println("Looking for Scenario/Precondition:"+scenarioOrPrecondition);

        // Trim tabs and whitespaces
        var trimmedScenario = scenarioOrPrecondition.replace(" ","").replace("\t", "");
        var lineNumber = 0;
        for(line in featureFileLines){
            lineNumber++;
            var trimmedLine = line.replace(" ", "").replace("\t", "");
            if (trimmedLine.contains(trimmedScenario)){
                println("Found Scenario/Precondition in line :"+lineNumber);
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


    fun addTestTag(scenarioLine: Int, testID: String, isPreviousLineTagged: Boolean, featureFileLines: MutableList<String>):MutableList<String>{
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

    fun addPreconditionTag(preconditionLine: Int, preconditionID: String, featureFileLines: MutableList<String>):MutableList<String>{
        println("Adding new line with precondition tag");
        addNewLine(featureFileLines,preconditionLine+1,formatPreconditionTag(preconditionID));
        return featureFileLines
    }

    fun addNewLine(lines: MutableList<String>, lineIndex: Int, tag: String) {
        if (lineIndex in lines.indices) {
            lines[lineIndex] = "    "+tag+"\n"+lines[lineIndex]
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

    fun formatTestTag(testID: String):String{
        return testTag+testID;
    }

    fun formatPreconditionTag(preconditionID: String):String{
        return preconditionTag+preconditionID;
    }

    fun isFileTagged(featureFileLines: List<String>, testOrPreconditionID:String):Boolean{
        println("Checking if file tagged: "+testOrPreconditionID)
        return featureFileLines.filter{line->line.contains(testOrPreconditionID)}.isNotEmpty();
    }

    fun getScenario(unzippedFileLines: List<String>):String{
        var scenario = "";
        for (line in unzippedFileLines){
            // Looking for Scenario instead of Scenario: so we also capture scenario outlines
            if(line.contains("Scenario")){
                println("Scenario found:"+line)
                scenario = line
                return scenario;
            }
        }
        // TODO This should return an exception
        return scenario;
    }

    fun getPrecondition(unzippedFileLines: List<String>):String{
        var precondition = "";
        for (line in unzippedFileLines){
            if(line.contains("Background:")){
                println("Background found:"+line)
                precondition = line
                return precondition;
            }
        }
        // TODO This should return an exception
        return precondition;
    }
}