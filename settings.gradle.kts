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

rootProject.name = "filament-hmi"
include(":app")


val inferredSdkRoot = System.getenv("ANDROID_SDK_ROOT")
    ?: System.getenv("ANDROID_HOME")
    ?: "/workspace/android-sdk"

val localPropertiesFile = file("local.properties")
if (!localPropertiesFile.exists()) {
    val sdkDirFile = file(inferredSdkRoot)
    if (sdkDirFile.exists()) {
        localPropertiesFile.writeText("sdk.dir=${sdkDirFile.absolutePath}\n")
    }
}
