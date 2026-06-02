@file:OptIn(ExperimentalForeignApi::class)

import Engine.executeWithFixedFrameRate
import engine.textures
import game.Game
import game.SceneType
import game.Sprite
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix.getenv
import raylib.CloseWindow
import raylib.UnloadTexture
import raylib.WindowShouldClose


fun main() = runBlocking {
    Engine.init()
    Sprite.init()
    val sceneType = when (getenv("MODE")?.toKString()) {
        "EDITOR" -> SceneType.Editor
        else -> SceneType.Play
    }

    val mapUrl = when (val map = getenv("MAP")?.toKString()) {
        is String -> "Assets/Maps/$map.json"
        else -> "Assets/Maps/1_1.json"
    }

    Game.init(mapUrl, sceneType, Engine.WINDOW_HEIGHT, Engine.WINDOW_WIDTH)

    while (!WindowShouldClose()) {
        executeWithFixedFrameRate {
            Engine.update()
            Game.update()
            Engine.render()
        }
    }

    textures.forEach {
        UnloadTexture(it.second)
    }
    CloseWindow()
}
