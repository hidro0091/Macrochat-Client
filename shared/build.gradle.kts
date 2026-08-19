import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)

                implementation("io.ktor:ktor-client-core:3.5.0")
                implementation("io.ktor:ktor-client-cio:3.5.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.leah.macrochat.MainKt"
    }
}