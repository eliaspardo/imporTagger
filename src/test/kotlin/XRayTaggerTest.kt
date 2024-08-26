import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import snackbar.SnackbarMessageHandler
import util.Config
import util.FileManager
import java.io.File
import java.nio.file.Paths
import kotlin.test.*
import kotlin.test.Test

internal class XRayTaggerTest{
    lateinit var xRayTagger:XRayTagger;
    lateinit var fileManager: FileManager;
    var snackbarMessageHandler = SnackbarMessageHandler()


    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+File.separator+"resources"+File.separator+"common")
        xRayTagger = XRayTagger(snackbarMessageHandler)
        fileManager = FileManager();
    }

    companion object {
        var expectedScenario = "Scenario: TEST-2806"
        var expectedTrimmedScenario = "Scenario: TEST-2806".replace(" ","").replace("\t", "")
        var expectedTrimmedScenarioOutline = "Scenario Outline: TEST-3470 Scenario Outline".replace(" ","").replace("\t", "")
        var expectedPrecondition1 = "Background: TEST-3436"
        var expectedPrecondition2 = "Background: TEST-4705"
        var expectedTrimmedPrecondition1 = expectedPrecondition1.replace(" ","").replace("\t", "")
        var expectedTrimmedPrecondition2 = expectedPrecondition2.replace(" ","").replace("\t", "")
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
        fun featureFilesAddTestTag() = listOf(
            Arguments.of(2,"TEST-2806","src/test/resources/fileTEST-2806WithoutTag.feature",false),
            Arguments.of(3,"TEST-2806","src/test/resources/fileTEST-2806WithOtherTag.feature",true)
        )

        @JvmStatic
        fun featureFilesAddPreconditionTag() = listOf(
            Arguments.of(4,"TEST-3436","src/test/resources/TEST-3470_withPreconditions_untagged.feature"),
            Arguments.of(4,"TEST-4705","src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature")
        )

        // TODO Add negative test cases
        @JvmStatic
        fun featureFilesTagTest() = listOf(
            Arguments.of(expectedScenario,"TEST-2806","src/test/resources/fileTEST-2806WithoutTag.feature"),
            Arguments.of(expectedScenario,"TEST-2806","src/test/resources/fileTEST-2806WithOtherTag.feature")
        )

        @JvmStatic
        fun featureFilesTagPrecondition() = listOf(
            Arguments.of("TEST-3436","src/test/resources/TEST-3470_withPreconditions_untagged.feature"),
            Arguments.of("TEST-4705","src/test/resources/TEST-4701_withPreconditionsAndOtherTests_untagged.feature")
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
        assertEquals(expectedLine, xRayTagger.findLineWhereScenario(expectedScenario,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesPreconditionsLine")
    fun testFindLineWherePrecondition(expectedPrecondition:String, featureFile: String, expectedLine:Int){
        val featureFileLines = fileManager.readFile(featureFile)
        assertEquals(expectedLine, xRayTagger.findLineWhereScenario(expectedPrecondition,featureFileLines));
    }

    @ParameterizedTest
    @MethodSource("featureFilesAddTestTag")
    fun testAddTestTag(scenarioLine:Int, testID: String, featureFile: String, isPreviousLineTagged: Boolean){
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.addTestTag(scenarioLine, testID, isPreviousLineTagged, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,testID))
    }
    @ParameterizedTest
    @MethodSource("featureFilesAddPreconditionTag")
    fun testAddPreconditionTag(preconditionLine:Int, preconditionID: String, featureFile: String){
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.addPreconditionTag(preconditionLine, preconditionID, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,preconditionID))
    }

    @ParameterizedTest
    @MethodSource("featureFilesTagTest")
    fun testTagTest(scenario: String, testID: String, featureFile: String) {
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.tagTest(scenario, testID, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,testID));
    }

    @ParameterizedTest
    @MethodSource("featureFilesTagPrecondition")
    fun testTagPrecondition(preconditionID: String, featureFile: String) {
        val featureFileLines = fileManager.readFile(featureFile)
        val featureFileLinesTagged = xRayTagger.tagPrecondition(preconditionID, featureFileLines);
        assertTrue(xRayTagger.isFileTagged(featureFileLinesTagged,preconditionID));
    }

    @Test
    fun testAddNewLineThrowsIndexOutOfBoundsException(){
        val featureFileLines = fileManager.readFile("src/test/resources/fileTEST-2806WithoutTag.feature")
        assertFailsWith<IndexOutOfBoundsException>{
            xRayTagger.addNewLine(featureFileLines,100,"TEST-666")
        }
    }

    @Test
    fun testAppendToLineThrowsIndexOutOfBoundsException(){
        val featureFileLines = fileManager.readFile("src/test/resources/fileTEST-2806WithoutTag.feature")
        assertFailsWith<IndexOutOfBoundsException>{
            xRayTagger.appendToLine(featureFileLines,100,"TEST-666")
        }
    }
}

