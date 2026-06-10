@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
        val libs = "${project.rootDir}/native/${name}/lib"
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

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":game"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                implementation("io.github.tom-delalande:raylib-kotlin-multiplatform:+")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain.get())
        }

        val nativeMain by creating {
            dependsOn(commonMain.get())
        }

        val macosArm64Main by getting
        val linuxArm64Main by getting

        macosArm64Main.dependsOn(nativeMain)
        linuxArm64Main.dependsOn(nativeMain)
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