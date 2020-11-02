import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.5.0"
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
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON parsing
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.2.3"
    setPlugins("git4idea", "maven", "Pythonid:202.6397.98")
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      <p>
            <h2>Improvements and Bugfixes</h2>
            <ul>
                <li>Upgrade to IntelliJ 2020.2.3</li>
                <li>Migration to JCEF runtime. Dependency on JavaFx run time plugin is no longer needed </li>
                <li>Fix user agent initialisation for new installs (caused crashes or didn't load Artemis)</li>
                <li>Fix a crash caused by the old JavaFx runtime </li>
                <li>Fix Artemis tool window UI mangling when moved to bottom </li>
                <li>Partially fix the back button not working issue </li>
                <li>General improvements in plugin stability </li>
            </ul>
        </p>"""
    )
}

tasks.publishPlugin {
    setUsername("<your_mail>")
    setToken("<your_token>")
}