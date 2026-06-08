pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "platformer-kotlin-raylib"

//includeBuild("../raylib-kotlin-multiplatform")
include(":engine")
include(":game")
