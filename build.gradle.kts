import io.papermc.paperweight.util.Git

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("io.papermc.paperweight.patcher") version "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(16)
    }

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.emc.gs/nexus/content/groups/aikar/")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.md-5.net/content/repositories/releases/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
}

val paperDir = layout.projectDirectory.dir("work/Paper")
val initSubmodules by tasks.registering {
    outputs.upToDateWhen { false }
    doLast {
        paperDir.asFile.mkdirs()
        Git(paperDir)("submodule", "update", "--init").executeOut()
    }
}

paperweight {
    serverProject.set(project(":ForkTest-Server"))

    upstreams {
        register("paper") {
            upstreamDataTask {
                dependsOn(initSubmodules)
                projectDir.set(paperDir)
            }

            patchTasks {
                register("api") {
                    sourceDir.set(paperDir.dir("Paper-API"))
                    patchDir.set(layout.projectDirectory.dir("patches/api"))
                    outputDir.set(layout.projectDirectory.dir("ForkTest-API"))
                }
                register("server") {
                    sourceDir.set(paperDir.dir("Paper-Server"))
                    patchDir.set(layout.projectDirectory.dir("patches/server"))
                    outputDir.set(layout.projectDirectory.dir("ForkTest-Server"))
                }
            }
        }
    }
}
