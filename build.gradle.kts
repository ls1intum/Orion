import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

group = "de.tum.www1.artemis.plugin.intellij"

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
    pluginName.set("orion")
    version.set("2023.2.1")
    plugins.set(listOf("Git4Idea", "PythonCore:232.9559.62", "maven", "gradle"))
}

tasks {
    patchPluginXml {
        // Last 2 digits of the year and the major version digit, 211-211.* equals (20)21.1.*
        // See https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild.set("232")
        // Orion Plugin version. Needs to be incremented for every new release!
        version.set("1.2.5")
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
