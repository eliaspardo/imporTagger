package ui

import ImporterViewModel
import LOG_IN_BUTTON
import LOG_OUT_BUTTON
import MockXRayRESTClient
import XRAY_CLIENT_ID_FIELD
import XRAY_CLIENT_SECRET_FIELD
import XRayLoginBox
import XRayRESTClient
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.*
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
    fun testXRayLoginBox_loggedIn_showLogoutButtonAndLoggedInMessage() = runComposeUiTest {
        every { importerViewModelMock.loginState} returns LoginState.LOGGED_IN
        every { importerViewModelMock.appState} returns AppState.DEFAULT
        every { importerViewModelMock.xrayClientID} returns initialXrayClientID
        every { importerViewModelMock.xrayClientSecret} returns initialXrayClientSecret
        every { importerViewModelMock.isLogoutButtonEnabled()} returns true
        every { importerViewModelMock.onLogoutClick } returns {}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick=importerViewModelMock.onLogoutClick,importerViewModel=importerViewModelMock)
        }
        onNodeWithTag(LOG_IN_BUTTON).assertDoesNotExist()
        onNodeWithTag(LOG_OUT_BUTTON).assertExists()
        onNodeWithText("Logged in as "+initialXrayClientID).assertExists()
        onNodeWithText("Log Out").performClick()
        verify { importerViewModelMock.onLogoutClick }

    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_loggedOut_showLogInButtonDoNotShowLogOutButton() = runComposeUiTest {
        every { importerViewModelMock.loginState} returns LoginState.LOGGED_OUT
        every { importerViewModelMock.appState} returns AppState.DEFAULT
        every { importerViewModelMock.xrayClientID} returns initialXrayClientID
        every { importerViewModelMock.xrayClientSecret} returns initialXrayClientSecret
        every { importerViewModelMock.isLoginButtonEnabled()} returns true
        every { importerViewModelMock.onLogoutClick } returns {println("onLogoutClick")}

        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={importerViewModelMock.onLogoutClick()},importerViewModel=importerViewModelMock)
        }
        onNodeWithText(LOG_OUT_BUTTON).assertDoesNotExist()
        onNodeWithTag(LOG_IN_BUTTON).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_loggedOut_inputtingTextFieldsUpdatesTextFieldsAndViewModel() = runComposeUiTest {
        setContent {
            XRayLoginBox(onUserNameChanged = importerViewModel.onUserNameChanged, onPasswordChanged = importerViewModel.onPasswordChanged, onLoginClick={},onLoginCancelClick={},onLogoutClick=importerViewModel.onLogoutClick,importerViewModel=importerViewModel)
        }
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).assertTextEquals("XRay Client ID", updatedXrayClientID)

        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        //onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).assertTextEquals("XRay Client Secret", updatedXrayClientSecret)

        assert(importerViewModel.xrayClientID==updatedXrayClientID)
        assert(importerViewModel.xrayClientSecret==updatedXrayClientSecret)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_loggedOut_logInButtonOnlyEnabledAfterBothUserNameAndPasswordInputted() = runComposeUiTest {
        setContent {
            XRayLoginBox(onUserNameChanged = importerViewModel.onUserNameChanged, onPasswordChanged = importerViewModel.onPasswordChanged, onLoginClick=importerViewModel.onLoginClick,onLoginCancelClick={},onLogoutClick=importerViewModel.onLogoutClick,importerViewModel=importerViewModel)
        }
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        onNodeWithTag(LOG_IN_BUTTON).assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_loggedOut_logInButtonDisabledIfNotBothUserNameAndPasswordInputted() = runComposeUiTest {
        setContent {
            XRayLoginBox(onUserNameChanged = importerViewModel.onUserNameChanged, onPasswordChanged = importerViewModel.onPasswordChanged, onLoginClick=importerViewModel.onLoginClick,onLoginCancelClick={},onLogoutClick=importerViewModel.onLogoutClick,importerViewModel=importerViewModel)
        }
        onNodeWithTag(LOG_IN_BUTTON).assertIsNotEnabled()

        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(LOG_IN_BUTTON).assertIsNotEnabled()

        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextClearance()

        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        onNodeWithTag(LOG_IN_BUTTON).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_logIn_throwsUnknownError() = runComposeUiTest {
        //val keyValueStorageImpl = KeyValueStorageImpl()
        //val snackbarMessageHandler = SnackbarMessageHandler()
        //val MockedXRayRESTClient = MockXRayRESTClient(keyValueStorageImpl)
        //val importerViewModelWithMockedRESTClient = ImporterViewModel(MockedXRayRESTClient,keyValueStorageImpl,snackbarMessageHandler)
        setContent {
            XRayLoginBox(onUserNameChanged = importerViewModel.onUserNameChanged, onPasswordChanged = importerViewModel.onPasswordChanged, onLoginClick=importerViewModel.onLoginClick,onLoginCancelClick={},onLogoutClick=importerViewModel.onLogoutClick,importerViewModel=importerViewModel)
        }

        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        onNodeWithTag(LOG_IN_BUTTON).performClick()
        runBlocking {
            //awaitIdle()
            delay(10000)
            onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).assertTextEquals("XRay Client Secret", "")
        }
    }

}