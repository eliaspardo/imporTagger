import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val ktor_version: String by project
val jvm_version: String by project
// Version 1.0.0 complains about the Kotlin version
val multiplatformSettings_version: String by project
val mockk_version: String by project
val testng_version: String by project
val app_version: String by project

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.0"
    id("com.github.jk1.dependency-license-report") version "2.0"
}

group = "me.elias"
version = "${app_version}"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

licenseReport {
    renderers = arrayOf(com.github.jk1.license.render.JsonReportRenderer()) // You can also use plain text or other formats
    outputDir = "${project.buildDir}/reports/licenses"
}


dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    //implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:2.0.0")
    //@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    //testImplementation(compose.uiTest)
    testImplementation("org.jetbrains.compose.ui:ui-test-junit4:1.2.1")
    testImplementation("io.mockk:mockk:${mockk_version}")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    // Get rid of
    // WARNING: Failed to transform class ImporterViewModel
    //java.lang.IllegalArgumentException: Unsupported class file major version 60
    // https://github.com/mockk/mockk/issues/397
    testRuntimeOnly("net.bytebuddy:byte-buddy:1.10.21")


    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.1.1")
    implementation("com.natpryce:konfig:1.6.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvm_version
}

compose.desktop {
    application {
        mainClass = "AppKt"
        nativeDistributions {
            modules("java.naming")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "XRay Importer"
            packageVersion = "${app_version}"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
        }
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = jvm_version
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = jvm_version
}

task ("printAppVersion"){
    doLast {
        println("${app_version}")
    }
}