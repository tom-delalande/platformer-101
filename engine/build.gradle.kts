plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    jvm()
    macosArm64()
    linuxArm64()
    linuxX64()
    mingwX64()

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

    linuxArm64 {
        val libs = "${project.rootDir}/native/linuxArm64/lib"
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
                    "-Wl,--as-needed",
                    "-lm",
                    "-lpthread",
                    "-ldl",
                    "-latomic"
                )
            }
        }
    }

    linuxX64 {
        val libs = "${project.rootDir}/native/linuxX64/lib"
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
                    "-Wl,--as-needed",
                    "-lm",
                    "-lpthread",
                    "-ldl",
                    "-latomic"
                )
            }
        }
    }

    mingwX64 {
        val libs = "${project.rootDir}/native/mingwX64/lib"
        compilations["main"].cinterops {
            create("raylib") {
                defFile("src/nativeInterop/cinterop/raylibWin.def")
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
                    "-lraylib",
                    "-lSDL2",
                    "-lwinmm",
                    "-lgdi32",
                    "-lopengl32",
                    "-static"
                )
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":game"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        }

        jvmMain.dependencies {
            implementation("uk.co.electronstudio.jaylib:jaylib:6.0.+")
        }
    }
}

val runJvm by tasks.registering(JavaExec::class) {
    group = "application"
    description = "Run the JVM application"
    dependsOn("compileKotlinJvm")
    jvmArgs("-XstartOnFirstThread")
    environment("SDL_VIDEO_DRIVER", "cocoa")
    classpath = files(
        tasks.named("compileKotlinJvm").map { it.outputs.files },
        configurations.named("jvmRuntimeClasspath")
    )
    mainClass.set("MainKt")
    workingDir = rootProject.layout.projectDirectory.asFile
}
