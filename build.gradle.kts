import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("org.jetbrains.intellij") version "1.1.6"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

group = "de.tum.www1.artemis.plugin.intellij"

repositories {
    mavenCentral()
}

dependencies {
    // JSON parsing
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.2")
    plugins.set(listOf("git4idea", "maven", "PythonCore:212.4746.96"))
}

tasks {
    patchPluginXml {
        // Last 2 digits of the year and the major version digit, 211-211.* equals (20)21.1.*
        // See https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild.set("212")
        untilBuild.set("212.*")
        // Orion Plugin version. Actual Release Versions are determined by GitHub
        // This number is only relevant for non-github releases but should be kept up-to-date
        version.set("1.2.0")
        changeNotes.set(
            """<p>
            <h1>Version Upgrade</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Upgrade to IntelliJ 2021.2</li>
                <li>Add support for assessment in Orion</li>
                <li>Add button to return to exercise</li>
                <li>Add button to open the documentation</li>
                <li>Add dialog to choose commit message</li>
                <li>Add limited support for auxiliary repositories</li>
                <li>Fix a bug that causes the plugin to crash for some programming languages</li>
            </ul>
        </p>"""
        )
    }

    publishPlugin {
        token.set("<your_token>")
    }
}
