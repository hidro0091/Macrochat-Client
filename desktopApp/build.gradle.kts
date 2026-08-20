import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
}
compose.desktop {
    application {
        mainClass = "org.leah.macrochat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.leah.macrochat"
            packageVersion = "1.0.0"
        }
    }
}