# Orion IntelliJ plugin

Open Artemis IDE plugin for the programming exercise integration.
This plugin integrates the [Artemis](https://github.com/ls1intum/Artemis) interactive learning platform into the IntelliJ IDE.
It allows you to directly import programming exercises from Artemis and submit your changes to the build servers.

**Current Version:**  
![GitHub release (latest by date)](https://img.shields.io/github/v/release/ls1intum/Orion)

## Example Usage
![](.github/media/orion_workflow.gif)

## Planned Features
We want to integrate the following features into the plugin:

-   Support for manual assessment  
-   Integration of the [JetBrains Edu Tools](https://plugins.jetbrains.com/plugin/10081-edutools)
-   Support for team exercises
-   Automatic conflict resolution

## Run/debug the plugin

### Development

-   Import as gradle project
-   Select the JBR `>11.0.3` as the project SDK with language level 11
-   Run Gradle task: `runIde`. This will open a new IntelliJ window with the plugin installed.

### Testing of pull requests

1.  #### Download release (.zip)
    
    Download the release file from the pull request's _Checks_ &rarr; _Artifacts_ &rarr; _orion_
    
    The artifact is unavailable during building. The full history of artifacts can be retrieved though the _Actions_ tab
![](.github/media/download_release.png)

2.  #### Install release (.zip)
   
    Install the release file in IntelliJ at _Settings_ &rarr; _Plugins_ &rarr; _Settings_ &rarr; _Install Plugin from Disk..._ &rarr; Select the file
![](.github/media/install_release.png)  
    <details>
    <summary>Installation process as gif</summary>
   
    ![](.github/media/orion_installation.gif)
    </details>

## Publish a new Release

**Before you release any new version, make sure that all version properties in the repository are updated
(`version` in `build.gradle.kts`) and the changelog contains the relevant version information (also in `build.gradle.kts`)**


Follow the steps outlined here:

![](.github/media/github_release.png)

1. Go to the GitHub "_Actions_" tab on the Orion repository
2. Select the "_Release_" workflow
3. Click on "_Run workflow_" and input the new version number to release (e.g. `1.5.0` releases and tags version `v1.5.0`)
4. An admin now has to review and accept the new release by viewing the created workflow run
![](.github/media/release_review.png)
5. After the release is approved, GitHub will automatically build and upload the artifact. It will also create a new draft GitHub release
6. An admin needs to promote the GitHub draft release in order to properly tag and release the latest build on GitHub.
    1. Go to the releases page and open the generated latest draft release of Orion
    2. Update the changelog by copy-pasting the information into the description box
    3. Click on 'Publish release' to finish the process
![](.github/media/draft_release.png)

The latest plugin artifact is now available on both GitHub and via the JetBrains marketplace. 

**It might take some time for the latest version to be seen on the marketplace since JetBrains still has to review 
and approve the changes!**

### Release Process Implementation

**How can I modify the pipeline?**

The release pipeline is fully implemented using GitHub actions. In order to change anything about the process you just
have to edit the `release.yml` in the `.github/workflows` directory.

**Where can I find secrets like the authentication token for the JetBrains repository?**

Admins of the GitHub repository can modify the build environment under  _Settings -> Environments -> prod_. This includes
adding and updating any secrets that should get injected into the environment during the build process.

## Feedback? Questions?
Email: alexander(dot)ungar(at)tum(dot)de
