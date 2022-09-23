pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenLocal()
    }
}

rootProject.name = "forktest"

include("forktest-api", "forktest-server")
