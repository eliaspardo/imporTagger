import ImporterViewModel.loginResponseCode
import ImporterViewModel.loginResponseMessage
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginBox(
    onLoginChanged: (username: String, password:String) -> Unit,
    onLoginClick: () -> Unit
) {
    var isClientSecretVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Text(text = "XRay Login", fontSize = 25.sp)
            OutlinedTextField(
                value = ImporterViewModel.xrayClientID,
                onValueChange = { onLoginChanged(it, ImporterViewModel.xrayClientSecret) },
                label = { Text("XRay Client ID") },
                modifier = Modifier.padding(5.dp)
            )
            OutlinedTextField(
                value = ImporterViewModel.xrayClientSecret,
                onValueChange = { onLoginChanged(ImporterViewModel.xrayClientID, it) },
                label = { Text("XRay Client Secret") },
                modifier = Modifier.padding(5.dp),
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
                isError = ImporterViewModel.isLoginError()
            )
        }
        if (ImporterViewModel.appState != AppState.LOGGING_IN) {
            if (ImporterViewModel.isLoginError()) {
                Text(
                    text = "Login Error. Error Code: "+loginResponseCode+" "+loginResponseMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp)
                )
            }
            Button(onClick = onLoginClick, enabled = ImporterViewModel.isLoginButtonEnabled()) {
                Text("Log In")
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun LogoutBox(onLogoutClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Text(text = "Logged in as ${ImporterViewModel.xrayClientID}", fontSize = 25.sp)
        if(ImporterViewModel.appState!=AppState.LOGGING_OUT) {
            Button(onClick = onLogoutClick, enabled = ImporterViewModel.isLogoutButtonEnabled()) {
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
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    if(ImporterViewModel.loginState==LoginState.LOGGED_OUT||ImporterViewModel.loginState==LoginState.ERROR){
        LoginBox(onLoginChanged, onLoginClick)
    }else{
        LogoutBox(onLogoutClick)
    }
}


