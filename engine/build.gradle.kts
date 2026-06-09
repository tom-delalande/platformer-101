import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val KonanTarget.familyName: String
    get() = when (family) {
        Family.ANDROID -> "android"
        Family.IOS -> "ios"
        Family.OSX -> "macos"
        Family.LINUX -> "linux"
        Family.MINGW -> "windows"
        Family.TVOS -> "tvos"
        Family.WATCHOS -> "watchos"
    }

val KonanTarget.architectureName: String
    get() = architecture.name.lowercase()

kotlin {
    listOf(
        mingwX64(),
        linuxX64(),
        linuxArm64(),
        macosArm64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.compilations.getByName("main") {
            cinterops {
                val sdl by creating {
                    compilerOpts.add("-I${project.rootDir}/native/include")
                }
                val sdl_image by creating {
                    compilerOpts.add("-I${project.rootDir}/native/include")
                }
                val sdl_ttf by creating {
                    compilerOpts.add("-I${project.rootDir}/native/include")
                }
            }
        }
        target.binaries {
            executable {
                entryPoint = "main"
                runTaskProvider?.configure {
                    workingDir = rootProject.layout.projectDirectory.asFile
                }
                when (target.name) {
                    "macosArm64" -> linkerOpts(
                        "-L/opt/homebrew/lib",
                        "-lSDL3",
                        "-lSDL3_image",
                        "-lSDL3_ttf",
                    )
                    "iosArm64" -> linkerOpts(
                        "-L${project.rootDir}/native/iosArm64/lib",
                        "-lSDL3",
                        "-lSDL3_image",
                        "-lSDL3_ttf",
                        "-lc++",
                        "-framework", "UIKit",
                        "-framework", "AVFoundation",
                        "-framework", "CoreGraphics",
                        "-framework", "CoreMotion",
                        "-framework", "Foundation",
                        "-framework", "GameController",
                        "-framework", "Metal",
                        "-framework", "QuartzCore",
                        "-framework", "AudioToolbox",
                        "-framework", "CoreMedia",
                        "-framework", "CoreVideo",
                        "-framework", "CoreAudio",
                        "-framework", "CoreBluetooth",
                        "-framework", "OpenGLES",
                        "-weak_framework", "CoreHaptics",
                    )
                    "iosSimulatorArm64" -> linkerOpts(
                        "-L${project.rootDir}/native/iosSimulatorArm64/lib",
                        "-lSDL3",
                        "-lSDL3_image",
                        "-lSDL3_ttf",
                        "-lc++",
                        "-framework", "UIKit",
                        "-framework", "AVFoundation",
                        "-framework", "CoreGraphics",
                        "-framework", "CoreMotion",
                        "-framework", "Foundation",
                        "-framework", "GameController",
                        "-framework", "Metal",
                        "-framework", "QuartzCore",
                        "-framework", "AudioToolbox",
                        "-framework", "CoreMedia",
                        "-framework", "CoreVideo",
                        "-framework", "CoreAudio",
                        "-framework", "CoreBluetooth",
                        "-framework", "OpenGLES",
                        "-weak_framework", "CoreHaptics",
                    )
                    else -> linkerOpts(
                        "-L${project.rootDir}/native/${target.name}/lib",
                        "-Wl,--allow-shlib-undefined",
                        "-Wl,--as-needed",
                        "-lSDL3",
                        "-lSDL3_image",
                        "-lSDL3_ttf",
                    )
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        nativeMain.dependencies {
            implementation(project(":game"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        }
    }
}
