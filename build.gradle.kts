import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String): Provider<String> {
    return providers.gradleProperty(key)
}

fun environment(key: String) = providers.environmentVariable(key)

// its sadly not possible to put these values in a properties file
plugins {
    id("java")
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.intellij") version "1.14.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

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
    plugins.set(listOf("Git4Idea", "maven", "PythonCore:231.8109.144"))
}

tasks {
    patchPluginXml {

        sinceBuild.set(properties("pluginSinceBuild").get())
        // Orion Plugin version. Needs to be incremented for every new release!
        version.set(properties("pluginVersion").get())
        changeNotes.set(
            """<p>
            <h1>Removed Deprecation</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Removed all DSL1 UI-Elements</li>
                <li>Upgraded to DSL2</li>
            </ul>
        </p>"""
        )
    }

    publishPlugin {
        token.set("<your_token>")
    }
}
