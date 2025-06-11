pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Edge Detection"

include(":app")
include(":opencv")

// Set correct project directory for OpenCV (use proper escaping for Windows paths or use `File(...)`)
project(":opencv").projectDir = file("opencv")
