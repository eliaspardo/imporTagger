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
        @JvmStatic
        fun featureFiles() = listOf(
            Arguments.of("fileTEST-2806WithoutTag.feature", "TEST-2806", false),
            Arguments.of("fileTEST-2806WithOtherTag.feature", "TEST-2806", false),
            Arguments.of("fileTEST-2806WithTag.feature", "TEST-2807", false),
            Arguments.of("fileTEST-2806WithTag.feature", "TEST-2806", true)
        )
    }
    @ParameterizedTest
    @MethodSource("featureFiles")
    fun testIsFileTagged(featureFile: String, testID:String, isTagged: Boolean){
        assertEquals(isTagged,xRayTagger.isFileTagged(File(featureFile),testID));
    }

    @ParameterizedTest
    @MethodSource("featureFiles")
    fun testGetScenario(featureFile: String, testID:String, isTagged: Boolean){
        var expectedTrimmedScenario = "Scenario: XMS Automation - Xtadium - Config - Configuration creation successfull".replace(" ","").replace("\t", "")
        var actualTrimmedScenario = xRayTagger.getScenario(File(featureFile)).replace(" ","").replace("\t", "");
        assertEquals(expectedTrimmedScenario,actualTrimmedScenario)
    }

}

