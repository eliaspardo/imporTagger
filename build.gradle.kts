import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.0"
}

group = "me.elias"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val ktor_version: String by project
val jvm_version: String by project
// Version 1.0.0 complains about the Kotlin version
val multiplatformSettings = "0.7.7"
val mockkVersion = "1.10.0"

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    //implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:2.0.0")
    //@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    //testImplementation(compose.uiTest)
    testImplementation("org.jetbrains.compose.ui:ui-test-junit4:1.2.1")
    testImplementation("io.mockk:mockk:${mockkVersion}")
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
    implementation("com.russhwolf:multiplatform-settings-no-arg:$multiplatformSettings")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.1.1")
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "XRay Importer"
            packageVersion = "1.0.0"
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