package ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LoginUIKtTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun XRayLoginBox() = runComposeUiTest {
        setContent {  }
    }
}