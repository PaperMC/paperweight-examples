import io.papermc.paperweight.util.Git

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("io.papermc.paperweight.patcher") version "1.1.5"
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") {
        content {
            onlyForConfigurations("paperclip")
        }
    }
    maven("https://maven.quiltmc.org/repository/release/") {
        content {
            onlyForConfigurations("remapper")
        }
    }
}

dependencies {
    remapper("org.quiltmc:tiny-remapper:0.4.1")
    paperclip("io.papermc:paperclip:2.0.0-SNAPSHOT@jar")
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
        Git(layout.projectDirectory)("submodule", "update", "--init").executeOut()
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
                    upstreamDir.set(paperDir.dir("Paper-API"))
                    patchDir.set(layout.projectDirectory.dir("patches/api"))
                    outputDir.set(layout.projectDirectory.dir("ForkTest-API"))
                }
                register("mojangApi") {
                    isBareDirectory.set(true)
                    upstreamDir.set(paperDir.dir("Paper-MojangAPI"))
                    patchDir.set(layout.projectDirectory.dir("patches/mojangapi"))
                    outputDir.set(layout.projectDirectory.dir("ForkTest-MojangAPI"))
                }
                register("server") {
                    upstreamDir.set(paperDir.dir("Paper-Server"))
                    patchDir.set(layout.projectDirectory.dir("patches/server"))
                    outputDir.set(layout.projectDirectory.dir("ForkTest-Server"))
                }
            }
        }
    }
}
