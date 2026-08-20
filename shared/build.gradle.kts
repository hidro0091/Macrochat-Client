plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
}

compose.desktop {
    application {
        mainClass = "org.leah.macrochat.MainKt"
    }
}