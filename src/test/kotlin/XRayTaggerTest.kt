import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
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
            Arguments.of("src/test/resources/fileTEST-2806WithTag.feature", "TEST-2806", true)
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

    @ParameterizedTest
    @MethodSource("featureFilesScenarioLine")
    fun testFindLineWhereScenario(expectedScenario:String, featureFile: String, expectedLine:Int){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expectedLine, xRayTagger.findLineWhereScenario(expectedScenario,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesAddTag")
    fun testAddTag(scenarioLine:Int, testID: String, featureFile: String, isPreviousLineTagged: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.addTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,testID))
    }
}

