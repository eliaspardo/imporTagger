// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "XRay Feature File Importer") {
        Column(Modifier.fillMaxWidth(), Arrangement.Center) {
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                XRayLoginBox(
                    ImporterViewModel.onLoginChanged,
                    ImporterViewModel.onLoginClick,
                    ImporterViewModel.onLogoutClick
                )
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                FeatureFileChooserUI(ImporterViewModel.onFeatureFileChooserClick, ImporterViewModel.onFeatureFileChooserClose)
                TestInfoFileChooserUI(ImporterViewModel.onTestInfoFileChooserClick, ImporterViewModel.onTestInfoFileChooserClose)
            }
            if(ImporterViewModel.testInfoFile.value!=null){
                Text(text = "TestInfo.json: "+ImporterViewModel.testInfoFile.value)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                ImportButton { ImporterViewModel.onImportClick }
            }
            FeatureFileListUI(ImporterViewModel.onRemoveFile)
        }
    }
}