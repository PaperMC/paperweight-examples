plugins {
    java
}

group = "de.verdox.mccreativelab"
version = "1.20.2-R0.1-SNAPSHOT"

dependencies {
    compileOnly(project(":mccreativelab-api"))
    implementation("io.vertx:vertx-core:4.5.0")
}


tasks{
    val copyResult by registering(Copy::class){
        doLast{
            from(jar)
            into(file("../run/plugins"))
        }
    }

    build {
        dependsOn(copyResult);
    }
}

tasks.processResources {
    val apiVersion = rootProject.providers.gradleProperty("mcVersion").get()
        .split(".", "-").take(2).joinToString(".")
    val props = mapOf(
        "version" to project.version,
        "apiversion" to "\"$apiVersion\"",
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}