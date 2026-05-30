val isLinux = System.getProperty("os.name").lowercase().contains("linux")

plugins {
    kotlin("multiplatform") version "2.3.21"
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64()
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

    linuxX64 {
        compilations.getByName("main") {
            cinterops {
                val raylib by creating {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/raylib.def"))
                    packageName("raylib")
                    compilerOpts("-I/opt/homebrew/include")
                }
            }
        }
        if (isLinux) {
            binaries {
                executable {
                    entryPoint = "main"
                    linkerOpts("-lraylib")
                }
            }
        }
    }
}
