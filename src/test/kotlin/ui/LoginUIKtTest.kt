package ui

import LOG_IN_CANCEL_BUTTON
import ImporterViewModel
import LOG_IN_CIRCULAR_PROGRESS_INDICATOR
import LOG_IN_BUTTON
import LOG_OUT_BUTTON
import LoginState
import XRAY_CLIENT_ID_FIELD
import XRAY_CLIENT_SECRET_FIELD
import XRayLoginBox
import XRayRESTClient
import androidx.compose.ui.test.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.*
import networking.createHTTPClient
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import snackbar.SnackbarMessageHandler
import util.KeyValueStorageImpl
import java.io.File
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class LoginUIKtTest {

    lateinit var importerViewModelMock:ImporterViewModel
    lateinit var importerViewModel:ImporterViewModel
    val initialXrayClientID = "TestClientID"
    val initialXrayClientSecret = "TestClientSecret"
    val updatedXrayClientID = "UpdatedTestClientID"
    val updatedXrayClientSecret = "UpdatedTestClientSecret"
    val httpClient = createHTTPClient()

    @BeforeTest
    fun setup(){
        System.setProperty("compose.application.resources.dir", Paths.get("").toAbsolutePath().toString()+ File.separator+"resources"+ File.separator+"common")
        val keyValueStorageImpl = KeyValueStorageImpl()
        val xRayRESTClient = XRayRESTClient(httpClient,keyValueStorageImpl)
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
    fun testXRayLoginBox_logInError_clearsSecret() = runComposeUiTest {
        setContent {
            XRayLoginBox(
                importerViewModel.onUserNameChanged,
                importerViewModel.onPasswordChanged,
                importerViewModel.onLoginClick,
                importerViewModel.onLoginCancelClick,
                importerViewModel.onLogoutClick,
                importerViewModel
            )
        }
        onNodeWithTag(XRAY_CLIENT_ID_FIELD).performTextInput(updatedXrayClientID)
        onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput(updatedXrayClientSecret)
        onNodeWithTag(LOG_IN_BUTTON).performClick()
        runBlocking {
            // TODO - This should be using idling resources or something similar
            //awaitIdle()
            delay(5000)
            assertEquals(importerViewModel.loginState, LoginState.ERROR)
            assert(importerViewModel.xrayClientSecret=="")
            // TODO - For some reason the secret is not cleared in the UI. Forcing a keystroke updates the field
            onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).performTextInput("t")
            onNodeWithTag(XRAY_CLIENT_SECRET_FIELD).assertTextEquals("XRay Client Secret", "•")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testXRayLoginBox_cancelButtonAndProgressIndicatorPresent_whileLoggingIn() = runComposeUiTest {
        every { importerViewModelMock.appState} returns AppState.LOGGING_IN
        every { importerViewModelMock.loginState} returns LoginState.DEFAULT
        every { importerViewModelMock.xrayClientID} returns initialXrayClientID
        every { importerViewModelMock.xrayClientSecret} returns initialXrayClientSecret
        setContent {
            XRayLoginBox(onUserNameChanged = {}, onPasswordChanged = {},onLoginClick={},onLoginCancelClick={},onLogoutClick={},importerViewModel=importerViewModelMock)
        }
        onNodeWithTag(LOG_IN_CANCEL_BUTTON).assertExists()
        onNodeWithTag(LOG_IN_CIRCULAR_PROGRESS_INDICATOR)
    }
}