
# Orion IntelliJ plugin

Open Artemis IDE plugin for the programming exercise integration.
This plugin integrates the [Artemis](https://github.com/ls1intum/Artemis) interactive learning platform into the IntelliJ IDE.
It allows you to directly import programming exercises from Artemis and submit your changes to the build servers.

**Current version:** 1.1.2

## Example Usage
![](.github/media/orion_workflow.gif)

## Planned Features
We want to integrate the following features into the plugin:

- Support for manual assessment  
- Integration of the  [JetBrains Edu Tools](https://plugins.jetbrains.com/plugin/10081-edutools)
- Support for team exercises
- Automatic conflict resolution

## Run/debug the plugin

- Import as gradle project
- Select the JBR `>11.0.3` as the project SDK with language level 11
- Run Gradle task: `runIde`

## Testing of pull requests

1. **Download release (.zip)**  
  Download the release file from the pull request's Checks &rarr; Artifacts &rarr; orion
![](.github/media/download_release.png)

2. **Install release (.zip)**  
  Install the release file in IntelliJ at Settings &rarr; Plugins &rarr; Settings &rarr; Install Plugin from Disk... &rarr; Select the file
![](.github/media/install_release.png)
   [Installation process as gif](.github/media/orion_installation.gif)

## Feedback? Questions?
Email: alexander(dot)ungar(at)tum(dot)de
