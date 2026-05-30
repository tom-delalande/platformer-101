@file:OptIn(ExperimentalForeignApi::class)

import engine.engineInit
import engine.render
import kotlinx.cinterop.*
import raylib.*


fun main() {
    InitWindow(800, 450, "Hello Kotlin + Raylib")
    SetTargetFPS(60)
    engineInit()

    while (!WindowShouldClose()) {
        update()
        render()
    }

    CloseWindow()
}
