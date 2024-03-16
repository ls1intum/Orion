import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Provides a property for a key
 */
fun properties(key: String): Provider<String> {
    return providers.gradleProperty(key)
}

/**
 * Provides an enviroment variable
 */
fun environment(key: String) = providers.environmentVariable(key)

// its sadly not possible to put these values in a properties file
plugins {
    id("java")
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.16.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

group = properties("pluginGroup").get()

repositories {
    mavenCentral()
}

dependencies {
    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    pluginName.set(properties("pluginName").get())

    version.set(properties("platformVersion").get())
    // PythonCore: https://plugins.jetbrains.com/plugin/7322-python-community-edition/versions
    // Pythonid: https://plugins.jetbrains.com/plugin/631-python/versions
    plugins.set(listOf("Git4Idea", "PythonCore:233.11799.241", "Pythonid:233.11799.241", "maven", "gradle"))
}

tasks {
    patchPluginXml {

        sinceBuild.set(properties("pluginSinceBuild").get())
        // Orion Plugin version. Needs to be incremented for every new release!
        version.set(properties("pluginVersion").get())
        changeNotes.set(
            """<p>
            <h1>Added a Feedbackmode for Tutors and easy Setup for Local Testing</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Feedbackmode for Students without a button</li>
            </ul>
        </p>"""
        )
    }

    publishPlugin {
        token.set("<your_token>")
    }
}
