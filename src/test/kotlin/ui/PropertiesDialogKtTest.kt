package ui

import Constants
import ImporterViewModel
import XRayRESTClient
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import java.io.File
import java.nio.file.Paths
import kotlin.test.BeforeTest

internal class PropertiesDialogKtTest{
    lateinit var importerViewModel:ImporterViewModel

    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+ File.separator+"resources"+ File.separator+"common")
        mockkObject(Constants)
        //every { Constants.PROPERTIES_FILE_PATH} returns ""
        every { Constants.TEST_TAG} returns ""
        every { Constants.PRECONDITION_TAG} returns ""
        every { Constants.PRECONDITION_PREFIX} returns ""
        every { Constants.PROJECT_KEY} returns ""

        val keyValueStorageImpl = KeyValueStorageImpl()
        val xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        val snackbarMessageHandler = SnackbarMessageHandler()
        importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testSomething()= runComposeUiTest {
        setContent{
            PropertiesDialogUI(importerViewModel)
        }
        onNodeWithTag(PROPERTIES_DIALOG_BUTTON).assertExists()
        onNodeWithTag(PROPERTIES_FILE_LOCATION_FIELD).assertDoesNotExist()
        onNodeWithTag(PROPERTIES_DIALOG_BUTTON).performClick()
        onNodeWithTag(PROPERTIES_FILE_LOCATION_FIELD).assertExists()
    }
}
