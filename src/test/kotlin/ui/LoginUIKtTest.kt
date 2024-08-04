package ui

import ImporterViewModel
import LogoutBox
import XRayLoginBox
import XRayRESTClient
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import kotlin.test.BeforeTest

internal class LoginUIKtTest {

    lateinit var importerViewModel:ImporterViewModel
    @BeforeTest
    fun setup(){
        val keyValueStorageImpl = KeyValueStorageImpl()
        val xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        val snackbarMessageHandler = SnackbarMessageHandler()
        //importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)ç
        importerViewModel = mockk<ImporterViewModel>()
        every { importerViewModel.loginState} returns LoginState.LOGGED_IN
        every { importerViewModel.appState} returns AppState.DEFAULT
        every { importerViewModel.xrayClientID} returns "Test"
        every { importerViewModel.isLogoutButtonEnabled()} returns true
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun XRayLoginBox() = runComposeUiTest {

        setContent {

            XRayLoginBox(onLoginChanged={ username, password ->{}},onLoginClick={},onLoginCancelClick={},onLogoutClick={},importerViewModel=importerViewModel)
            onNodeWithText("Log Out").performClick()
        }
    }

}