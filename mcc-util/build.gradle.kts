plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.3"
}

group = "de.verdox.mccreativelab"
description = "mcc-util"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}