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

        @JvmStatic
        fun featureFilesTestTag() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", "TEST-2806", false),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", "TEST-2806", false),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "TEST-2807", false),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "TEST-2806", true),
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_tagged.feature", "TEST-3436", true),
            Arguments.of("src/test/resources/TEST-3470_withPreconditions_untagged.feature", "TEST-3436", false)
        )

        @JvmStatic
        fun featureFilesPreviousLineTagged() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", 2, false),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", 3, true),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", 3, true)
        )

        @JvmStatic
        fun featureFilesScenarios() = listOf(
            Arguments.of("src/test/resources/fileTEST-2806WithoutTag.feature", expectedTrimmedScenario, true),
            Arguments.of("src/test/resources/fileTEST-2806WithOtherTag.feature", expectedTrimmedScenario, true),
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", expectedTrimmedScenario,true),
            Arguments.of("src/test/resources/fileTEST-2806WithoutScenario.feature", expectedTrimmedScenario,false)
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
        fun featureFilesAddTag() = listOf(
            Arguments.of(2,"TEST-2806","src/test/resources/fileTEST-2806WithoutTag.feature",false),
            Arguments.of(3,"TEST-2806","src/test/resources/fileTEST-2806WithOtherTag.feature",true)
        )
    }
    @ParameterizedTest
    @MethodSource("featureFilesTestTag")
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

    // TODO Make this prettier so file and precon is not hardcoded
    @Test
    fun testGetPrecondition(){
        val featureFileLines = fileManager.readFile("src/test/resources/TEST-3470_withPreconditions_tagged.feature")
        var actualTrimmedPrecondition = xRayTagger.getPrecondition(featureFileLines).replace(" ","").replace("\t", "");
        assertEquals("Background:Precondition-BaseURL",actualTrimmedPrecondition)
    }

    @ParameterizedTest
    @MethodSource("featureFilesScenarioLine")
    fun testFindLineWhereScenario(expectedScenario:String, featureFile: String, expectedLine:Int){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expectedLine, xRayTagger.findLineWhereScenarioOrPrecondition(expectedScenario,featureFileLines));
    }

    // TODO Make this prettier so file and precon is not hardcoded
    @Test
    fun testFindLineWherePrecondition(){
        val featureFileLines = fileManager.readFile("src/test/resources/TEST-3470_withPreconditions_tagged.feature")
        assertEquals(4, xRayTagger.findLineWhereScenarioOrPrecondition("Background:Precondition-BaseURL",featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesAddTag")
    fun testAddTag(scenarioLine:Int, testID: String, featureFile: String, isPreviousLineTagged: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,testID))
    }
}

