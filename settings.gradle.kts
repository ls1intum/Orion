pluginManagement {
    repositories {
        gradlePluginPortal()
        maven{
            setUrl("https://jetbrains.bintray.com/intellij-plugin-service")
        }
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
        mavenCentral()
    }
}

rootProject.name = "orion"

