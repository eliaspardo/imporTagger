import exceptions.FeatureFileTaggingException
import exceptions.NoPreconditionFoundException
import exceptions.NoScenarioFoundException
import mu.KotlinLogging
import networking.IXRayRESTClient
import snackbar.UserMessageHandler
import util.FileManager
import util.onError
import util.onSuccess
import java.io.File

class XRayTagger(private val iUserMessageHandler: UserMessageHandler) {
    private val testTag = Constants.TEST_TAG
    private val preconditionTag = Constants.PRECONDITION_TAG
    private val preconditionPrefix = Constants.PRECONDITION_PREFIX
    private val logger = KotlinLogging.logger {}

    fun tagTest(scenario: String, testID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        logger.info("Tagging Test: "+testID+" "+scenario);
        // Parse Feature File looking for Scenario. Get line number.
        val scenarioLine = findLineWhereScenario(scenario,featureFileLines);

        // Check if previous line is tagged, so tag is appended, otherwise, new line created.
        val isPreviousLineTagged = checkIfPreviousLineIsTagged(scenarioLine, featureFileLines);

        // Add tag
        return addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
    }

    fun tagPrecondition(preconditionID: String, featureFileLines: MutableList<String>) : MutableList<String> {
        logger.info("Tagging Precondition: "+preconditionID);
        // Parse Feature File looking for Scenario. Get line number.
        val preconditionLine = findLineWherePrecondition(featureFileLines);

        // Add tag
        return addPreconditionTag(preconditionLine, preconditionID, featureFileLines);
    }

    // Parse Feature File looking for Scenario. Get line number.
     fun findLineWhereScenario(scenario: String, featureFileLines: MutableList<String>):Int{
        logger.debug("Looking for Scenario/Precondition:"+scenario);

        // Trim tabs and whitespaces
        val trimmedScenario = scenario.replace(" ","").replace("\t", "");
        var lineNumber = 0;
        for(line in featureFileLines){
            lineNumber++;
            val trimmedLine = line.replace(" ", "").replace("\t", "");
            if (trimmedLine.contains(trimmedScenario)){
                logger.debug("Found Scenario/Precondition in line:"+lineNumber);
                logger.debug(line);
                return lineNumber;
            }
        }
        throw NoScenarioFoundException("Scenario "+scenario+" not found in file!");
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
        throw NoPreconditionFoundException("Precondition not found in file!");
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
        logger.debug("Adding test tag: "+testID);
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
        logger.debug("Adding precondition tag: "+preconditionID);
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
        return featureFileLines.filter{line->(line.contains(formatTestTag(testOrPreconditionID))||line.contains(formatPreconditionTag(testOrPreconditionID)))}.isNotEmpty();
    }

    fun getScenario(unzippedFileLines: List<String>):String{
        for (line in unzippedFileLines){
            // Looking for Scenario instead of Scenario: so we also capture scenario outlines
            if(line.contains("Scenario")){
                logger.debug("Scenario found: "+line)
                return line;
            }
        }
        throw NoScenarioFoundException("No Scenario found in file!")
    }

    fun getPrecondition(unzippedFileLines: List<String>):String{
        for (line in unzippedFileLines){
            if(line.contains("Background:")){
                logger.debug("Background found: "+line)
                return line;
            }
        }
        throw NoPreconditionFoundException("Precondition not found in file!");
    }

    suspend fun processUpdatedOrCreatedTests(
        featureFilePath:String,
        updatedOrCreatedTests: List<Test>,
        fileManager: FileManager,
        ixRayRESTClient: IXRayRESTClient,
        importerViewModel: ImporterViewModel
    ) {
        logger.info("Processing Tests for "+featureFilePath)
        var featureFileLines:MutableList<String>
        try{
            featureFileLines = fileManager.readFile(featureFilePath)
        }catch(exception: Exception){
            logger.error("Error reading file "+featureFilePath);
            iUserMessageHandler.showUserMessage("Error reading file "+featureFilePath)
            return
        }
        for (test in updatedOrCreatedTests){
            val testID = test.key
            // Check if feature file is already tagged, if not, start tagging process
            if(!isFileTagged(featureFileLines,testID)) {
                logger.info("File is not tagged")
                // Download zip file to know which scenario needs tagging
                ixRayRESTClient.downloadCucumberTestsFromXRay(testID,importerViewModel)
                    .onSuccess {
                        val zipFile = File.createTempFile("imporTagger", ".zip")
                        zipFile.writeBytes(it.exportedTestCase)
                        val unzippedTestFile = fileManager.unzipFile(zipFile)
                        fileManager.deleteFile(zipFile)

                        // Get Scenario from extracted file
                        val unzippedFileLines = fileManager.readFile(unzippedTestFile)
                        try {
                            val scenario = getScenario(unzippedFileLines)
                            // Find Scenario in featureFile and tag it
                            val featureFileLinesTagged = tagTest(scenario, testID, featureFileLines)
                            fileManager.writeFile(featureFilePath, featureFileLinesTagged)
                        }catch(nsfe: NoScenarioFoundException){
                            logger.error("No scenario found in unzipped file: "+unzippedTestFile);
                            iUserMessageHandler.showUserMessage("No scenario found in unzipped file: "+unzippedTestFile)
                        }finally{
                            // Always delete file
                            fileManager.deleteFile(File(unzippedTestFile))
                        }
                    }.onError {
                        logger.error("Error tagging tests in "+featureFilePath);
                        iUserMessageHandler.showUserMessage("Error tagging tests in "+featureFilePath)
                        throw FeatureFileTaggingException("Error tagging tests in "+featureFilePath)
                    }
            }
        }
    }

    suspend fun processUpdatedOrCreatedPreconditions(
        featureFilePath:String,
        updatedOrCreatedPreconditions: List<Precondition>,
        fileManager: FileManager,
    ) {
        logger.info("Processing Preconditions for "+featureFilePath)
        // This looks as duplicated code, but we need to re-read the file in case the processUpdatedOrCreatedTests has written to file
        val featureFileLines = fileManager.readFile(featureFilePath)
        for (precondition in updatedOrCreatedPreconditions){
            val preconditionID = precondition.key
            if(!isFileTagged(featureFileLines,preconditionID)) {
                logger.info("File is not tagged")
                try{
                    // Find Precondition in featureFile and tag it. Cannot export from XRay so have to go with hardcoded prefix.
                    val featureFileLinesTagged = tagPrecondition(preconditionID, featureFileLines)
                    fileManager.writeFile(featureFilePath, featureFileLinesTagged)
                }catch(npfe: NoPreconditionFoundException){
                    logger.error("Precondition not found in "+featureFilePath);
                    iUserMessageHandler.showUserMessage("Precondition not found in "+featureFilePath)
                }
            }else{
                logger.info("file is tagged")
            }

        }

    }
}