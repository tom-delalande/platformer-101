@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import logic.Entity
import logic.SceneType
import logic.model
import raylib.BeginDrawing
import raylib.ClearBackground
import raylib.Color
import raylib.DrawTexturePro
import raylib.EndDrawing
import raylib.GetMousePosition
import raylib.IsMouseButtonPressed
import raylib.LoadTexture
import raylib.MOUSE_BUTTON_LEFT
import raylib.MOUSE_BUTTON_RIGHT
import raylib.Rectangle
import raylib.Vector2

private val RAYWHITE = color(245, 245, 245)
private val LIGHTGRAY = color(200, 200, 200)

fun engineInit() {
    engineData = EngineData(
        sprites = mapOf(
            Entity.Background to LoadTexture("Assets/Pixel Adventure/Background/Blue.png"),
            Entity.Terrain to LoadTexture("Assets/Pixel Adventure/Terrain/Terrain (16x16).png"),
            Entity.Player to LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Idle (32x32).png"),
            Entity.RockHead to LoadTexture("Assets/Pixel Adventure/Traps/Rock Head/Idle.png"),
        )
    )
}

fun engineUpdate() {
    if (model.isMouse1Pressed) {
        model.wasMouse1Pressed = true
    }

    if (model.isMouse2Pressed) {
        model.wasMouse2Pressed = true
    }
    if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT.toInt())) {
        model.isMouse1Pressed = true
    }
    if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT.toInt())) {
        model.isMouse2Pressed = true
    }

    val mousePosition = GetMousePosition()
    model.mousePositionX = mousePosition.useContents { x.toInt() }
    model.mousePositionY = mousePosition.useContents { y.toInt() }
}

fun render() {
    BeginDrawing()
    ClearBackground(RAYWHITE)

    // Render Background
    (0..engineData.windowWidth.div(64)).forEach { xOffset ->
        (0..engineData.windowHeight.div(64)).forEach { yOffset ->
            drawSprite(
                sprite = Entity.Background,
                inputX = 0,
                inputY = 0,
                inputWidth = 64,
                inputHeight = 64,
                outputPositionX = xOffset * 64,
                outputPositionY = yOffset * 64,
            )
        }
    }

    model.uiElements.forEach {
        drawSprite(
            sprite = it.sprite,
            inputX = it.inputX,
            inputY = it.inputY,
            inputWidth = it.inputWidth,
            inputHeight = it.inputHeight,
            outputPositionX = it.outputPositionX,
            outputPositionY = it.outputPositionY,
            outputWidth = it.outputWidth,
            outputHeight = it.outputHeight,
        )
    }

    when (model.sceneType) {
        SceneType.Editor -> {
        }

        SceneType.Play -> TODO()
    }
    EndDrawing()
}


fun drawSprite(
    sprite: Entity,
    inputX: Int = 0,
    inputY: Int = 0,
    inputWidth: Int = 16,
    inputHeight: Int = 16,
    outputPositionX: Int = 0,
    outputPositionY: Int = 0,
    outputWidth: Int = inputWidth,
    outputHeight: Int = inputHeight,
    tint: CValue<Color> = color(255, 255, 255),
) {
    DrawTexturePro(
        texture = engineData.sprites[sprite]!!,
        source = cValue<Rectangle> {
            x = inputX.toFloat()
            y = inputY.toFloat()
            width = inputWidth.toFloat()
            height = inputHeight.toFloat()
        },
        dest = cValue<Rectangle> {
            x = outputPositionX.toFloat()
            y = outputPositionY.toFloat()
            width = outputWidth.toFloat()
            height = outputHeight.toFloat()
        },
        origin = cValue<Vector2> {
            x = 0f
            y = 0f
        },
        rotation = 0.0f,
        tint = tint,
    )
}
