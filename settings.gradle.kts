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

rootProject.name = "Capsule-Architecture"

include(
    ":capsule-core",
    ":capsule-middleware",
    ":capsule-network",
    ":capsule-navigation-compose",
    ":capsule-navigation-xml",
    ":samples:sample-compose",
    ":samples:sample-xml",
    ":samples:sample-full"
)
