import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.6.5"
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

repositories {
    mavenCentral()
}

dependencies {
    // JSON parsing
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.3"
    setPlugins("git4idea", "maven", "Pythonid:203.5981.155")
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      <p>
            <h2>Added Support f</h2>
            <ul>
                <li>Upgrade to IntelliJ 2020.3</li>
                <li>Upgraded to Gradle 0.6.5</li>
                <li>Upgrade KotlinModule to Version 2.12.6</li>
                <li>Upgrade Gson to Version 2.8.6</li>
                <li>Upgrade Pythonid plugin to version 203.5981.155</li>
            </ul>
        </p>"""
    )
}

tasks.publishPlugin {
    setUsername("<your_mail>")
    setToken("<your_token>")
}