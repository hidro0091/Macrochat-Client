plugins {
    application
    kotlin("jvm") version "2.4.10"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.leah"
version = "2.0.0"

repositories {
    mavenCentral()
}

javafx {
    version = "26.0.2"
    modules("javafx.controls")
}

kotlin {
    jvmToolchain(26)
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.5.0")
    implementation("io.ktor:ktor-client-cio:3.5.0")
    implementation("io.ktor:ktor-client-websockets:3.5.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.leah.macrochatNaCl.MainKt")
}