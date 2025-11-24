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
        // JitPack for LibSu
        maven { url = uri("https://jitpack.io") }
        // Google APIs
        maven { url = uri("https://maven.google.com") }
    }
}

rootProject.name = "SmartCleaner"
include(":app")
