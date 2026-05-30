@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import logic.SceneType
import logic.model
import raylib.BeginDrawing
import raylib.ClearBackground
import raylib.DrawText
import raylib.DrawTextureRec
import raylib.EndDrawing
import raylib.LoadTexture
import raylib.Rectangle
import raylib.UnloadTexture

private val RAYWHITE = color(245, 245, 245)
private val LIGHTGRAY = color(200, 200, 200)

fun engineInit() {
    engineData = EngineData(
        sprites = mapOf(
            "Background" to LoadTexture("Assets/Pixel Adventure/Background/Blue.png"),
        )
    )

}

fun render() {
    BeginDrawing()
    ClearBackground(RAYWHITE)

    DrawTextureRec(
        texture = engineData.sprites["Background"]!!,
        source = TODO(),
        position = TODO(),
        tint = TODO()
    )
    when (model.sceneType) {
        SceneType.Editor -> {
            DrawText("Hello, Editor!", 190, 200, 20, LIGHTGRAY)
        }
    }
    engineData.sprites.forEach {
        UnloadTexture(it.value)
    }
    EndDrawing()
}

