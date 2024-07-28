// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import util.KeyValueStorage


fun main() = application {
    val icon = painterResource("icon.png")
    val keyValueStorage = KeyValueStorage()
    val xRayRESTClient = XRayRESTClient(keyValueStorage)
    val importerViewModel = ImporterViewModel(xRayRESTClient,keyValueStorage)
    Window(onCloseRequest = ::exitApplication, title = "XRay Feature File Importer", icon= icon) {
        Column(Modifier.fillMaxWidth(), Arrangement.Center) {
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                XRayLoginBox(
                    importerViewModel.onLoginChanged,
                    importerViewModel.onLoginClick,
                    importerViewModel.onLoginCancelClick,
                    importerViewModel.onLogoutClick,
                    importerViewModel
                )
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                FeatureFileChooserUI(importerViewModel.onFeatureFileChooserClick, importerViewModel.onFeatureFileChooserClose,importerViewModel)
                TestInfoFileChooserUI(importerViewModel.onTestInfoFileChooserClick, importerViewModel.onTestInfoFileChooserClose, importerViewModel)
            }
            if(importerViewModel.testInfoFile.value!=null){
                Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                    Text(text = "TestInfo.json: "+importerViewModel.testInfoFile.value)
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                ImportButton (importerViewModel.onImportClick, importerViewModel.onImportCancelClick, importerViewModel)
            }
            FeatureFileListUI(importerViewModel.onRemoveFile,importerViewModel)
        }
    }
}

@Preview
@Composable
fun Test(){
    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
    Text(text = "TestInfo.json: C:/fasdflkjsdf/fklajsdfsd.json")
    }
}