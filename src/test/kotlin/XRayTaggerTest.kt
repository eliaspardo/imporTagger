import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class XRayTaggerTest{
    lateinit var xRayTagger:XRayTagger;


    @BeforeTest
    fun setup(){
        xRayTagger = XRayTagger()
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
    }
    @ParameterizedTest
    @MethodSource("featureFilesTestTag")
    fun testIsFileTagged(featureFile: String, testID:String, isTagged: Boolean){
        assertEquals(isTagged,xRayTagger.isFileTagged(File(featureFile),testID));
    }

    @ParameterizedTest
    @MethodSource("featureFilesPreviousLineTagged")
    fun testCheckIfPreviousLineIsTagged(featureFile: String, expectedLine:Int,expected: Boolean){
        assertEquals(expected, xRayTagger.checkIfPreviousLineIsTagged(expectedLine,File(featureFile)));
    }

    @ParameterizedTest
    @MethodSource("featureFilesScenarios")
    fun testGetScenario(featureFile: String, expectedTrimmedScenario:String,expected: Boolean){
        var actualTrimmedScenario = xRayTagger.getScenario(File(featureFile)).replace(" ","").replace("\t", "");
        assertEquals(expected,expectedTrimmedScenario==actualTrimmedScenario)
    }

    @ParameterizedTest
    @MethodSource("featureFilesScenarioLine")
    fun testFindLineWhereScenario(expectedScenario:String, featureFile: String, expectedLine:Int){
        assertEquals(expectedLine, xRayTagger.findLineWhereScenario(expectedScenario,File(featureFile)));
    }

}

