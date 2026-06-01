plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64()
    // linuxX64 target added by AI (claude-3.5-sonnet) for Batocera deployment
    linuxX64()

    macosArm64 {
        compilations.getByName("main") {
            cinterops {
                val raylib by creating {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/raylib.def"))
                    packageName("raylib")
                    compilerOpts("-I/opt/homebrew/include")
                }
            }
        }
        binaries {
            executable {
                entryPoint = "main"
                // File-IO working dir: run tasks use the project root so that
                // relative paths like "map.json" and "Assets/" resolve correctly.
                runTaskProvider?.configure {
                    workingDir = rootProject.layout.projectDirectory.asFile
                }
                linkerOpts(
                    "-L/opt/homebrew/lib",
                    "-lraylib",
                    "-framework", "OpenGL",
                    "-framework", "Cocoa",
                    "-framework", "IOKit",
                    "-framework", "CoreFoundation"
                )
            }
        }
    }

    sourceSets {
        nativeMain.dependencies {
            implementation(project(":game"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        }
    }

    // linuxX64 configuration block was AI-generated (claude-3.5-sonnet)
    linuxX64 {
        compilations.getByName("main") {
            cinterops {
                val raylib by creating {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/raylib.def"))
                    packageName("raylib")
                    compilerOpts("-I/usr/include", "-I/usr/local/include")
                }
            }
        }
        binaries {
            executable {
                entryPoint = "main"
                // AI-generated (claude-3.5-sonnet) linker flags for raylib on Linux
                linkerOpts("-L/usr/local/lib", "-lraylib")
                // AI-generated (claude-3.5-sonnet) runTask config for working dir
                runTaskProvider?.configure {
                    workingDir = rootProject.layout.projectDirectory.asFile
                }
            }
        }
    }
}
