plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64()
    linuxArm64()

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

    linuxArm64 {
        val libs = "${project.rootDir}/native/lib"
        compilations["main"].cinterops {
            create("raylib") {
                defFile("src/nativeInterop/cinterop/raylibLinux.def")
                packageName("raylib")
                compilerOpts("-I${project.rootDir}/native/include")
                extraOpts("-libraryPath", libs)
            }
        }
        binaries {
            executable {
                entryPoint = "main"
                linkerOpts(
                    "-L$libs",
                    "-Wl,--allow-shlib-undefined",
                    "-lSDL2",
                    "-lm",
                    "-lpthread",
                    "-ldl"
                )
            }
        }
    }
}
