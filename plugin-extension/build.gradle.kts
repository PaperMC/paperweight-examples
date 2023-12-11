plugins {
    java
}

group = "de.verdox.mccreativelab"
version = "1.20.2-R0.1-SNAPSHOT"

dependencies {
    compileOnly(project(":mccreativelab-api"))
    implementation("io.vertx:vertx-core:4.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks{

    val copyTask = register<Copy>("copyToTestServer") {
    println("Copying plugin jar to testserver")
    from(jar)
    into(file("../run/plugins"))
    }

    build {
        finalizedBy(copyTask)
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