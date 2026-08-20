plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.uiToolingPreview)

    implementation("org.jetbrains.compose.material3:material3:1.9.0")

    implementation("io.ktor:ktor-client-core:3.5.0")
    implementation("io.ktor:ktor-client-cio:3.5.0")
    implementation("io.ktor:ktor-client-websockets:3.5.0")
    implementation("io.github.panpf.sketch4:sketch-compose:4.6.0")
    implementation("io.github.panpf.sketch4:sketch-animated-gif:4.6.0")
    implementation("org.jetbrains.compose.components:components-resources:1.11.1")
    implementation("io.github.panpf.sketch4:sketch-compose-resources:4.6.0")
    implementation("io.github.panpf.sketch4:sketch-extensions-compose-resources:4.6.0")
}
compose.desktop {
    application {
        mainClass = "org.leah.macrochat.MainKt"

        nativeDistributions {
            packageName = "Macrochat"
            packageVersion = "1.1.0"
        }
    }
}

compose.resources {
    packageOfResClass = "org.leah.macrochat.resources"
    generateResClass = always
}