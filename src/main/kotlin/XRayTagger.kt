import mu.KotlinLogging
class XRayTagger {
    val testTag = "@TEST_"
    val preconditionTag = "#PRECON_"
    val preconditionPrefix = "Background"
    private val logger = KotlinLogging.logger {}

    fun tagTest(scenario: String, testID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        logger.info("Tagging Test: "+testID+" "+scenario);
        // Parse Feature File looking for Scenario. Get line number.
        var scenarioLine = findLineWhereScenario(scenario,featureFileLines);

        // Check if previous line is tagged, so tag is appended, otherwise, new line created.
        var isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFileLines);

        // Add tag
        return addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
    }

    fun tagPrecondition(preconditionID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        logger.info("Tagging Precondition: "+preconditionID);
        // Parse Feature File looking for Scenario. Get line number.
        var preconditionLine = findLineWherePrecondition(featureFileLines);

        // Add tag
        return addPreconditionTag(preconditionLine, preconditionID, featureFileLines);
    }

    // Parse Feature File looking for Scenario. Get line number.
     fun findLineWhereScenario(scenario: String, featureFileLines: MutableList<String>):Int{
        logger.debug("Looking for Scenario/Precondition:"+scenario);

        // Trim tabs and whitespaces
        var trimmedScenario = scenario.replace(" ","").replace("\t", "");
        var lineNumber = 0;
        for(line in featureFileLines){
            lineNumber++;
            var trimmedLine = line.replace(" ", "").replace("\t", "");
            if (trimmedLine.contains(trimmedScenario)){
                logger.debug("Found Scenario/Precondition in line:"+lineNumber);
                logger.debug(line);
                return lineNumber;
            }
        }
        return 0;
    }

    // Parse Feature File looking for Precondition. Get line number.
    fun findLineWherePrecondition(featureFileLines: MutableList<String>):Int{
        logger.debug("Looking for Preconditions");
        var lineNumber = 0;
        for(line in featureFileLines){
            lineNumber++;
            if (line.contains(preconditionPrefix)){
                logger.debug("Found Precondition in line:"+lineNumber);
                logger.debug(line);
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
                logger.debug("Line previous to scenario: "+line);
                if(line.contains("@")){
                    return true;
                }
            }

        }
        return false;
    }


    fun addTestTag(scenarioLine: Int, testID: String, isPreviousLineTagged: Boolean, featureFileLines: MutableList<String>):MutableList<String>{
        logger.info("Adding test tag: "+testID);
        if(isPreviousLineTagged){
            // Append to scenarioLine-1
            logger.debug("Adding tag to existing tags");
            appendToLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }else{
            // Add newLine in scenario Line
            logger.debug("Adding new line with tag");
            addNewLine(featureFileLines,scenarioLine-1,formatTestTag(testID));
        }
        return featureFileLines
    }

    fun addPreconditionTag(preconditionLine: Int, preconditionID: String, featureFileLines: MutableList<String>):MutableList<String>{
        logger.info("Adding precondition tag: "+preconditionID);
        addNewLine(featureFileLines,preconditionLine,formatPreconditionTag(preconditionID));
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
        logger.info("Checking if file tagged: "+testOrPreconditionID)
        return featureFileLines.filter{line->line.contains(testOrPreconditionID)}.isNotEmpty();
    }

    fun getScenario(unzippedFileLines: List<String>):String{
        var scenario = "";
        for (line in unzippedFileLines){
            // Looking for Scenario instead of Scenario: so we also capture scenario outlines
            if(line.contains("Scenario")){
                logger.debug("Scenario found: "+line)
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
                logger.debug("Background found: "+line)
                precondition = line
                return precondition;
            }
        }
        // TODO This should return an exception
        return precondition;
    }
}