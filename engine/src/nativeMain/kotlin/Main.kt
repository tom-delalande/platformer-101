@file:OptIn(ExperimentalForeignApi::class)

import engine.Engine
import engine.Engine.executeWithFixedFrameRate
import engine.sprites
import game.Game
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import raylib.CloseWindow
import raylib.UnloadTexture
import raylib.WindowShouldClose


fun main() = runBlocking {
    Engine.init()
    Game.init()

    while (!WindowShouldClose()) {
        executeWithFixedFrameRate {
            Engine.update()
            Game.update()
            Engine.render()
        }
    }

    sprites.forEach {
        UnloadTexture(it.value.texture)
    }
    CloseWindow()
}
