import org.junit.jupiter.params.ParameterizedTest
import kotlin.test.Test
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class XRayTaggerTest{
    lateinit var xRayTagger:XRayTagger;
    lateinit var fileManager:FileManager;


    @BeforeTest
    fun setup(){
        xRayTagger = XRayTagger()
        fileManager = FileManager();
    }

    companion object {
        var expectedScenario = "Scenario: XMS Automation - Xtadium - Config - Configuration creation successfull"
        var expectedTrimmedScenario = "Scenario: XMS Automation - Xtadium - Config - Configuration creation successfull".replace(" ","").replace("\t", "")
        var expectedTrimmedScenarioOutline = "Scenario Outline: AppLayout URL Parameters Status Codes".replace(" ","").replace("\t", "")
        var expectedPrecondition1 = "Background: Precondition - BaseURL"
        var expectedPrecondition2 = "Background: Create app - Add config"
        var expectedTrimmedPrecondition1 = "Background: Precondition - BaseURL".replace(" ","").replace("\t", "")
        var expectedTrimmedPrecondition2 = "Background: Create app - Add config".replace(" ","").replace("\t", "")
        @JvmStatic
        fun featureFilesTags() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", "TEST-2806", false),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", "TEST-2806", false),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "TEST-2807", false),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "TEST-2806", true),
            // Testing for Preconditions
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_tagged.feature", "TEST-3436", true),
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", "TEST-3436", false),
            Arguments.of("src/test/resources/TEST-4701_withPreconditionsAndOtherTests_tagged.feature", "TEST-4705", true),
            Arguments.of("src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature", "TEST-4705", false)
        )

        @JvmStatic
        fun featureFilesPreviousLineTagged() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", 2, false),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", 3, true),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", 3, true),
            // With Preconditions and other tests
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_tagged.feature", 9, true),
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", 7, false),
            Arguments.of("src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature", 14, false)
        )

        @JvmStatic
        fun featureFilesScenarios() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", expectedTrimmedScenario, true),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", expectedTrimmedScenario, true),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", expectedTrimmedScenario,true),
            Arguments.of("src/test/resources/fileTEST-2806WithoutScenario.feature", expectedTrimmedScenario,false),
            // Testing file with Preconditions and Scenario Outline
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", expectedTrimmedScenarioOutline,true)
        )

        @JvmStatic
        fun featureFilesPreconditions() = listOf(
            // Testing file without precondition
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "",true),
            // Testing file with Preconditions and Scenario Outline
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", expectedTrimmedPrecondition1,true),
            Arguments.of("src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature", expectedTrimmedPrecondition2,true),
            // Negative test, shouldn't match
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", "Made up Precondition",false),
        )

        @JvmStatic
        fun featureFilesScenarioLine() = listOf(
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithoutTag.feature", 2),
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithOtherTag.feature", 3),
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithTag.feature", 3),
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithTag.feature", 3),
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithOtherScenario.feature", 0),
            Arguments.of(expectedScenario, "src/test/resources/fileTEST-2806WithoutScenario.feature", 0)
        )
        @JvmStatic
        fun featureFilesPreconditionsLine() = listOf(
            Arguments.of(expectedPrecondition1, "src/test/resources/TEST-3470_withPreconditions_untagged.feature", 4),
            Arguments.of(expectedPrecondition2, "src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature", 4),
            // File without preconditions
            Arguments.of("There's no precondition on this file", "src/test/resources/fileTEST-2806WithOtherScenario.feature", 0),
        )


        @JvmStatic
        fun featureFilesAddTag() = listOf(
            Arguments.of(2,"TEST-2806","src/test/resources/fileTEST-2806WithoutTag.feature",false),
            Arguments.of(3,"TEST-2806","src/test/resources/fileTEST-2806WithOtherTag.feature",true)
        )
    }
    @ParameterizedTest
    @MethodSource("featureFilesTags")
    fun testIsFileTagged(featureFile: String, testID:String, isTagged: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(isTagged,xRayTagger.isFileTagged(featureFileLines,testID));
    }

    @ParameterizedTest
    @MethodSource("featureFilesPreviousLineTagged")
    fun testCheckIfPreviousLineIsTagged(featureFile: String, expectedLine:Int,expected: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expected, xRayTagger.checkIfPreviousLineIsTagged(expectedLine,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesScenarios")
    fun testGetScenario(featureFile: String, expectedTrimmedScenario:String,expected: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        var actualTrimmedScenario = xRayTagger.getScenario(featureFileLines).replace(" ","").replace("\t", "");
        assertEquals(expected,expectedTrimmedScenario==actualTrimmedScenario)
    }

    @ParameterizedTest
    @MethodSource("featureFilesPreconditions")
    fun testGetPrecondition(featureFile: String, expectedTrimmedPrecondition:String,expected: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        var actualTrimmedPrecondition = xRayTagger.getPrecondition(featureFileLines).replace(" ","").replace("\t", "");
        assertEquals(expected,expectedTrimmedPrecondition==actualTrimmedPrecondition,"Precondition did not match: "+expectedTrimmedPrecondition+" not equal to "+actualTrimmedPrecondition)
    }

    @ParameterizedTest
    @MethodSource("featureFilesScenarioLine")
    fun testFindLineWhereScenario(expectedScenario:String, featureFile: String, expectedLine:Int){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expectedLine, xRayTagger.findLineWhereScenarioOrPrecondition(expectedScenario,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesPreconditionsLine")
    fun testFindLineWherePrecondition(expectedPrecondition:String, featureFile: String, expectedLine:Int){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expectedLine, xRayTagger.findLineWhereScenarioOrPrecondition(expectedPrecondition,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesAddTag")
    fun testAddTag(scenarioLine:Int, testID: String, featureFile: String, isPreviousLineTagged: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,testID))
    }
}

