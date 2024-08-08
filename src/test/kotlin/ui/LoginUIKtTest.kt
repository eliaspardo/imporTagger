package ui

import ImporterViewModel
import XRAY_CLIENT_ID_FIELD
import XRAY_CLIENT_SECRET_FIELD
import XRayLoginBox
import XRayRESTClient
import androidx.compose.ui.test.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class LoginUIKtTest {

    lateinit var importerViewModelMock:ImporterViewModel
    lateinit var importerViewModel:ImporterViewModel
    val initialXrayClientID = "TestClientID"
    val initialXrayClientSecret = "TestClientSecret"
    val updatedXrayClientID = "UpdatedTestClientID"
    val updatedXrayClientSecret = "UpdatedTestClientSecret"
    @BeforeTest
    fun setup(){
        val keyValueStorageImpl = KeyValueStorageImpl()
        val xRayRESTClient = XRayRESTClient(keyValueStorageImpl)
        val snackbarMessageHandler = SnackbarMessageHandler()
        importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)
        importerViewModelMock = mockk<ImporterViewModel>()
    }

    @AfterTest
    fun tearDown(){
        unmockkAll()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_showsLogoutButtonWhenLoggedIn() = runComposeUiTest {
        every { importerViewModelMock.loginState} returns LoginState.LOGGED_IN
        every { importerViewModelMock.appState} returns AppState.DEFAULT
        every { importerViewModelMock.xrayClientID} returns initialXrayClientID
        every { importerViewModelMock.xrayClientSecret} returns initialXrayClientSecret
        every { importerViewModelMock.isLogoutButtonEnabled()} returns true
        every { importerViewModelMock.onLogoutClick } returns {}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModelMock.onLogoutClick()},importerViewModel=importerViewModelMock)
        }
        onNodeWithText("Logged in as "+initialXrayClientID).assertExists()
        onNodeWithText("Log Out").performClick()
        verify { importerViewModelMock.onLogoutClick }

    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_doesntShowLogoutButtonWhenLoggedOut() = runComposeUiTest {
        every { importerViewModelMock.loginState} returns LoginState.LOGGED_OUT
        every { importerViewModelMock.appState} returns AppState.DEFAULT
        every { importerViewModelMock.xrayClientID} returns initialXrayClientID
        every { importerViewModelMock.xrayClientSecret} returns initialXrayClientSecret
        every { importerViewModelMock.isLoginButtonEnabled()} returns true
        every { importerViewModelMock.onLogoutClick } returns {println("onLogoutClick")}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModelMock.onLogoutClick()},importerViewModel=importerViewModelMock)
        }
        onNodeWithText("Log Out").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_inputtingTextFieldsUpdatesTextFields() = runComposeUiTest {
        setContent {
            XRayLoginBox(onUserNameChanged = importerViewModel.onUserNameChanged, onPasswordChanged = importerViewModel.onPasswordChanged, onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModelMock.onLogoutClick()},importerViewModel=importerViewModel)
        }
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).assertTextEquals("XRay Client ID", updatedXrayClientID)

        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        //onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).assertTextEquals("XRay Client Secret", updatedXrayClientSecret)

        assert(importerViewModel.xrayClientID==updatedXrayClientID)
        assert(importerViewModel.xrayClientSecret==updatedXrayClientSecret)
    }

}