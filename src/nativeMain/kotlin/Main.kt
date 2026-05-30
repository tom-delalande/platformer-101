@file:OptIn(ExperimentalForeignApi::class)

import engine.engineData
import engine.engineInit
import engine.engineUpdate
import engine.render
import kotlinx.cinterop.*
import raylib.*


fun main() {
    InitWindow(800, 450, "Hello Kotlin + Raylib")
    SetTargetFPS(60)
    engineInit()

    while (!WindowShouldClose()) {
        engineUpdate()
        update()
        render()
    }

    engineData.sprites.forEach {
        UnloadTexture(it.value)
    }
    CloseWindow()
}
