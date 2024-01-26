plugins {
    java
}

description = "plugin-extension"
group = "de.verdox.mccreativelab"
version = "1.20.4-R0.1-SNAPSHOT"


java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
dependencies {
    compileOnly(project(":mccreativelab-api"))
    implementation("io.vertx:vertx-core:4.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks{
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    val copyTask = register<Copy>("copyToTestServer") {
    println("Copying plugin jar to testserver")
    from(jar)
    into(file("../run/plugins"))
    }

    build {
        finalizedBy(copyTask)
    }
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

/*publishing {
    publications.create<MavenPublication>("maven").from(components["java"]);
    publications {
        create<MavenPublication>("lib") {
            artifact("/build/libs/$description-$version.jar")
        }
    }
    repositories.maven(repositories.mavenLocal())
}*/

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