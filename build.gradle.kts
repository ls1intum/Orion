import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("org.jetbrains.intellij") version "0.7.3"
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
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2021.1.1"
    setPlugins("git4idea", "maven", "PythonCore:211.6693.119")
}

tasks {
    patchPluginXml {
        // Last 2 digits of the year and the major version digit, 211-211.* equals (20)21.1.*
        // See https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild("211")
        untilBuild("211.*")
        // Orion Plugin version. Actual Release Versions are determined by GitHub
        // This number is only relevant for non-github releases but should be kept up-to-date
        version("1.1.3")
        changeNotes(
            """<p>
            <h1>Version Upgrade</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Upgrade to IntelliJ 2021.1.1</li>
                <li>Add button to return to exercise</li>
                <li>Add dialog to choose commit message</li>
                <li>Refactoring and minor improvements</li>
            </ul>
        </p>"""
        )
    }

    publishPlugin {
        setUsername("<your_mail>")
        setToken("<your_token>")
    }
}
