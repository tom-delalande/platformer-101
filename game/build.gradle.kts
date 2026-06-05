plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    jvm()
    macosArm64()
    linuxArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        }

        wasmJsMain.dependencies {
        }
    }
}
