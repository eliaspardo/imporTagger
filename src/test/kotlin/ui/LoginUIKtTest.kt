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
import androidx.compose.ui.test.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import kotlin.test.BeforeTest

internal class LoginUIKtTest {

    lateinit var importerViewModel:ImporterViewModel
    @BeforeTest
    fun setup(){
        //val keyValueStorageImpl = KeyValueStorageImpl()
        //val xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        //val snackbarMessageHandler = SnackbarMessageHandler()
        //importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)ç
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_showsLogoutButtonWhenLoggedIn() = runComposeUiTest {
        importerViewModel = mockk<ImporterViewModel>()
        every { importerViewModel.loginState} returns LoginState.LOGGED_IN
        every { importerViewModel.appState} returns AppState.DEFAULT
        every { importerViewModel.xrayClientID} returns "Test"
        every { importerViewModel.xrayClientSecret} returns "Test"
        every { importerViewModel.isLogoutButtonEnabled()} returns true
        every { importerViewModel.onLogoutClick } returns {println("onLogoutClick")}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModel.onLogoutClick()},importerViewModel=importerViewModel)
        }
        onNodeWithText("Log Out").performClick()
        verify (exactly = 1){ importerViewModel.onLogoutClick }

        unmockkAll()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_doesntShowLogoutButtonWhenLoggedOut() = runComposeUiTest {
        importerViewModel = mockk<ImporterViewModel>()
        every { importerViewModel.loginState} returns LoginState.LOGGED_OUT
        every { importerViewModel.appState} returns AppState.DEFAULT
        every { importerViewModel.xrayClientID} returns "Test"
        every { importerViewModel.xrayClientSecret} returns "Test"
        every { importerViewModel.isLogoutButtonEnabled()} returns true
        every { importerViewModel.isLoginButtonEnabled()} returns true
        every { importerViewModel.onLogoutClick } returns {println("onLogoutClick")}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModel.onLogoutClick()},importerViewModel=importerViewModel)
        }
        onNodeWithText("Log Out").assertDoesNotExist()
        unmockkAll()
    }

}