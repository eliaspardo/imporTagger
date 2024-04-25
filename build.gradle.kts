import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    //kotlin("jvm") version "1.8.10"
    //kotlin("jvm") version "1.9.10"
    //id("org.jetbrains.compose") version "1.1.0"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "me.elias"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val ktor_version: String by project

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    //implementation("cafe.adriel.voyager:voyager-navigator:1.0.0-rc06")
    //implementation("com.darkrockstudios:mpfilepicker:2.0.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.5.3")
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    //kotlinOptions.jvmTarget = "11"
    kotlinOptions.jvmTarget = "16"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "XRay Importer"
            packageVersion = "1.0.0"
        }
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    //jvmTarget = "1.8"
    jvmTarget = "16"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    //jvmTarget = "1.8"
    jvmTarget = "16"
}