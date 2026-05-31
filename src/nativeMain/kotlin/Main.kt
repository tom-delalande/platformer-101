@file:OptIn(ExperimentalForeignApi::class)

import engine.engineData
import engine.engineInit
import engine.engineUpdate
import engine.render
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.cinterop.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import raylib.*


fun main() = runBlocking {
    InitWindow(800, 450, "Hello Kotlin + Raylib")
    SetTargetFPS(60)
    engineInit()

    while (!WindowShouldClose()) {
        val time = measureTime {
            engineUpdate()
            update()
            render()
        }
        val targetFps = 30
        if (time < (1000 / targetFps).milliseconds) {
            delay((1000 / targetFps).milliseconds - time)
        }
    }

    engineData.sprites.forEach {
        UnloadTexture(it.value)
    }
    CloseWindow()
}
