plugins {
    application
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.leah"
version = providers.gradleProperty("appVersion").get()

repositories {
    mavenCentral()
}

javafx {
    version = "23.0.2"
    modules("javafx.controls")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.5.0")
    implementation("io.ktor:ktor-client-cio:3.5.0")
    implementation("io.ktor:ktor-client-websockets:3.5.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.leah.macrochatNaCl.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.leah.macrochatNaCl.MainKt"
    }
}