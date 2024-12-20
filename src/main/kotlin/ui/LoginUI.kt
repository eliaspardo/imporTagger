import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


const val XRAY_CLIENT_ID_FIELD = "xRayClientIDField"
const val XRAY_CLIENT_SECRET_FIELD = "xRayClientSecretField"
const val LOG_OUT_BUTTON = "logOutButton"
const val LOG_IN_BUTTON = "logInButton"
const val LOG_IN_CANCEL_BUTTON = "cancelLoginButton"
const val LOG_IN_CIRCULAR_PROGRESS_INDICATOR = "loginCircularProgressIndicator"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginBox(
    onUserNameChanged: (username: String) -> Unit,
    onPasswordChanged: (username: String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginCancelClick: () -> Unit,
    importerViewModel: ImporterViewModel
) {
    var isClientSecretVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = importerViewModel.xrayClientID,
                onValueChange = { onUserNameChanged(it) },
                label = { Text("XRay Client ID") },
                modifier = Modifier.padding(5.dp).testTag(XRAY_CLIENT_ID_FIELD)
                .onKeyEvent {
                    // Handling of tab navigation
                    if (it.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Next)
                        // TODO Tab goes to next field, but writes tab, removing it here, not very elegant
                        // TODO When doing Alt+Tab the last character is deleted
                        onUserNameChanged(importerViewModel.xrayClientID.substring(0, maxOf(importerViewModel.xrayClientID.length - 1,0)))
                        true
                    } else {
                        false
                    }
                }
            )
            OutlinedTextField(
                value = importerViewModel.xrayClientSecret,
                onValueChange = { onPasswordChanged(it) },
                label = { Text("XRay Client Secret") },
                modifier = Modifier.padding(5.dp).testTag(XRAY_CLIENT_SECRET_FIELD)
                .onKeyEvent {
                    // Handling of tab navigation
                    if (it.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Next)
                        onPasswordChanged(importerViewModel.xrayClientSecret.substring(0, maxOf(importerViewModel.xrayClientSecret.length - 1,0)))
                        true
                    } else {
                        false
                    }
                },
                visualTransformation =
                if (isClientSecretVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconToggleButton(checked = isClientSecretVisible, onCheckedChange = { isClientSecretVisible = it }) {
                        Icon(
                            imageVector = if (isClientSecretVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            }, "Show Password"
                        )
                    }
                },
                isError = importerViewModel.loginState==LoginState.ERROR
            )
        }
        if (importerViewModel.appState != AppState.LOGGING_IN) {
            Button(onClick = onLoginClick, enabled = importerViewModel.isLoginButtonEnabled(), modifier = Modifier.testTag(LOG_IN_BUTTON)) {
                Text("Log In")
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.testTag(LOG_IN_CIRCULAR_PROGRESS_INDICATOR))
            Button(onClick = onLoginCancelClick, enabled = true, modifier = Modifier.testTag(LOG_IN_CANCEL_BUTTON)){
                Text("Cancel")
            }
        }
    }
}

@Composable
fun LogoutBox(onLogoutClick: () -> Unit,importerViewModel: ImporterViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = "Logged in as ${importerViewModel.xrayClientID}", fontSize = 25.sp)
        if (importerViewModel.appState != AppState.LOGGING_OUT) {
            Button(onClick = onLogoutClick, enabled = importerViewModel.isLogoutButtonEnabled(), modifier = Modifier.testTag(LOG_OUT_BUTTON)) {
                Text("Log Out")
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun XRayLoginBox(
    onUserNameChanged: (username: String) -> Unit,
    onPasswordChanged: (username: String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginCancelClick: () -> Unit,
    onLogoutClick: () -> Unit,
    importerViewModel: ImporterViewModel
) {
    if(importerViewModel.loginState==LoginState.LOGGED_IN){
        LogoutBox(onLogoutClick,importerViewModel)
    }else{
        LoginBox(onUserNameChanged, onPasswordChanged, onLoginClick, onLoginCancelClick, importerViewModel)
    }
}

