import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.6.5"
    java
    kotlin("jvm") version "1.4.30"
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.3"
    setPlugins("git4idea", "maven", "Pythonid:203.5981.165")
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      <p>
            <h1>Version Upgrade</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Upgrade to IntelliJ 2020.3.2</li>
                <li>Upgrade various plugin dependencies</li>
                <li>Allow access to external webpages (for the integration with self-hosted Artemis instances)</li>
            </ul>
        </p>"""
    )
}

tasks.publishPlugin {
    setUsername("<your_mail>")
    setToken("<your_token>")
}