import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

// its sadly not possible to put these values in a properties file
plugins {
    id("java")
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.intellij") version "1.13.3"
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
    if (!properties("platformType").orElse("").equals("")) {
        type.set(properties("platformType").get())
    }
    version.set(properties("platformVersion").get())
    plugins.set(listOf("Git4Idea", "maven", "PythonCore:231.8109.144"))
}

tasks {
    patchPluginXml {
        // Last 2 digits of the year and the major version digit, 211-211.* equals (20)21.1.*
        // See https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild.set(properties("pluginSinceBuild").get())
        // Orion Plugin version. Needs to be incremented for every new release!
        version.set(version)
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
