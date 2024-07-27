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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginBox(
    onLoginChanged: (username: String, password:String) -> Unit,
    onLoginClick: (coroutineScope: CoroutineScope) -> Unit,
    importerViewModel: ImporterViewModel
) {
    var isClientSecretVisible by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Text(text = "XRay Login", fontSize = 25.sp)
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = importerViewModel.xrayClientID,
                onValueChange = { onLoginChanged(it, importerViewModel.xrayClientSecret) },
                label = { Text("XRay Client ID") },
                modifier = Modifier.padding(5.dp)
                .onKeyEvent {
                    if (it.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Next)
                        // TODO Tab goes to next field, but writes tab, removing it here, not very elegant
                        // TODO When doing Alt+Tab the last character is deleted
                        onLoginChanged(importerViewModel.xrayClientID.substring(0, maxOf(importerViewModel.xrayClientID.length - 1,0)), importerViewModel.xrayClientSecret)
                        true
                    } else {
                        false
                    }
                }
            )
            OutlinedTextField(
                value = importerViewModel.xrayClientSecret,
                onValueChange = { onLoginChanged(importerViewModel.xrayClientID, it) },
                label = { Text("XRay Client Secret") },
                modifier = Modifier.padding(5.dp)
                .onKeyEvent {
                    if (it.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Next)
                        onLoginChanged(importerViewModel.xrayClientID, importerViewModel.xrayClientSecret.substring(0, maxOf(importerViewModel.xrayClientSecret.length - 1,0)))
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
                isError = importerViewModel.isLoginError()
            )
        }
        if (importerViewModel.appState != AppState.LOGGING_IN) {
            if (importerViewModel.isLoginError()) {
                Text(
                    text = "Login Error. Error Code: "+importerViewModel.loginResponseCode+" "+importerViewModel.loginResponseMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp)
                )
            }
            /*Button(onClick = onLoginClick, enabled = importerViewModel.isLoginButtonEnabled()) {
                Text("Log In")
            }*/

            Button(onClick = { onLoginClick(coroutineScope) }, enabled = importerViewModel.isLoginButtonEnabled()) {
                Text("Log In")
            }
        } else {
            CircularProgressIndicator()
            // TODO Cancel coroutines
            Button(onClick = {importerViewModel.onLoginCancelClick}, enabled = true){
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
    ){
        Text(text = "Logged in as ${importerViewModel.xrayClientID}", fontSize = 25.sp)
        if(importerViewModel.appState!=AppState.LOGGING_OUT) {
            Button(onClick = onLogoutClick, enabled = importerViewModel.isLogoutButtonEnabled()) {
                Text("Log Out")
            }
        }else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun XRayLoginBox(
    onLoginChanged: (username: String, password: String) -> Unit,
    onLoginClick: (coroutineScope: CoroutineScope) -> Unit,
    onLogoutClick: () -> Unit,
    importerViewModel: ImporterViewModel
) {
    if(importerViewModel.loginState==LoginState.DEFAULT||importerViewModel.loginState==LoginState.ERROR){
        LoginBox(onLoginChanged, onLoginClick,importerViewModel)
    }else{
        LogoutBox(onLogoutClick,importerViewModel)
    }
}


