pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "monumenta-paperfork"

include("monumenta-api", "monumenta-server")
