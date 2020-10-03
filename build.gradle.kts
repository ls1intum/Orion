import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.26"
    java
    kotlin("jvm") version "1.4.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}

group = "de.tum.www1.artemis.plugin.intellij"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // JSON parsing
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.2.2"
    setPlugins("git4idea", "maven", "Pythonid:202.6397.98", "com.intellij.javafx:1.0.2")
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <p>
            <h2>Improvements and Bugfixes</h2>
            <ul>
                <li>Upgrade to IntelliJ 2020.2.2</li>
                <li>Fix user agent initialisation for new installs (caused crashes or didn't load Artemis)</li>
            </ul>
        </p>""")
}

tasks.publishPlugin {
    setUsername("<your_mail>")
    setToken("<your_token>")
}