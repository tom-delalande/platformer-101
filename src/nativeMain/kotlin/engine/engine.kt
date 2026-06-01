@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import logic.Entity
import logic.Input
import logic.SceneType
import logic.model
import logic.playerWorldX
import logic.playerWorldY
import logic.tileSize
import raylib.BeginDrawing
import raylib.ClearBackground
import raylib.Color
import raylib.DrawTexturePro
import raylib.EndDrawing
import raylib.GetMousePosition
import raylib.IsKeyDown
import raylib.IsMouseButtonPressed
import raylib.KEY_A
import raylib.KEY_D
import raylib.KEY_E
import raylib.KEY_L
import raylib.KEY_P
import raylib.KEY_S
import raylib.KEY_W
import raylib.MOUSE_BUTTON_LEFT
import raylib.MOUSE_BUTTON_RIGHT
import raylib.Rectangle
import raylib.Vector2

private val RAYWHITE = color(245, 245, 245)
private val LIGHTGRAY = color(200, 200, 200)

fun engineInit() {
    engineData = EngineData()
}

fun engineUpdate() {
    model.wasPressed = model.isPressed

    model.isPressed = buildList {
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT.toInt())) add(Input.Mouse1)
        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT.toInt())) add(Input.Mouse2)
        if (IsKeyDown(KEY_S.toInt())) add(Input.KeyboardS)
        if (IsKeyDown(KEY_L.toInt())) add(Input.KeyboardL)
        if (IsKeyDown(KEY_P.toInt())) add(Input.KeyboardP)
        if (IsKeyDown(KEY_E.toInt())) add(Input.KeyboardE)
        if (IsKeyDown(KEY_W.toInt())) add(Input.KeyboardW)
        if (IsKeyDown(KEY_A.toInt())) add(Input.KeyboardA)
        if (IsKeyDown(KEY_D.toInt())) add(Input.KeyboardD)
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
                sprite = sprites["Background"]!!,
                inputYOffset = model.backgroundOffsetY,
                outputPositionX = xOffset * 64f,
                outputPositionY = (yOffset * 64f),
            )
        }
    }

    when (model.sceneType) {
        SceneType.Editor -> {
            model.map.forEach {
                drawSprite(
                    sprite = it.entity.toDefaultSprite(),
                    outputPositionX = it.gridPositionX * 64f,
                    outputPositionY = (engineData.windowHeight / 64) * 64 - it.gridPositionY * 64f,
                    outputWidth = 64,
                    outputHeight = 64,
                )
            }

            if (model.selectedUIElement != null) {
                drawSprite(
                    sprite = model.selectedUIElement!!.entity.toDefaultSprite(),
                    outputPositionX = model.mousePositionX - 32f,
                    outputPositionY = model.mousePositionY - 32f,
                    outputWidth = 64,
                    outputHeight = 64,
                    tint = color(255, 255, 255, 150),
                )
            }

            model.uiElements.forEach {
                drawSprite(
                    sprite = it.entity.toDefaultSprite(),
                    outputPositionX = it.outputPositionX.toFloat(),
                    outputPositionY = it.outputPositionY.toFloat(),
                    outputWidth = it.outputWidth,
                    outputHeight = it.outputHeight,
                )
            }
        }

        SceneType.Play -> {
            model.map.filter { it.entity != Entity.Player }.forEach {
                drawSprite(
                    sprite = it.entity.toDefaultSprite(),
                    outputPositionX = it.gridPositionX * 64f,
                    outputPositionY = (engineData.windowHeight / 64) * 64 - it.gridPositionY * 64f,
                    outputWidth = 64,
                    outputHeight = 64,
                )
            }
            val playerEntity = model.map.find { it.entity == Entity.Player }
            if (playerEntity != null) {
                val sprite = when {
                    model.playerVelocityY > 0 -> sprites["Player_Jump"]
                    model.playerVelocityY < 0 -> sprites["Player_Fall"]
                    model.playerVelocityX != 0f -> sprites["Player_Run"]
                    model.playerVelocityY == 0f && model.playerVelocityX == 0f -> sprites["Player_Idle"]
                    else -> sprites["Player_Idle"]
                }
                drawSprite(
                    sprite = sprite!!,
                    flipHorizontally = model.playerDirection == -1,
                    outputPositionX = playerEntity.gridPositionX * tileSize + model.playerPositionX,
                    outputPositionY = (engineData.windowHeight / tileSize) * tileSize - playerEntity.gridPositionY * tileSize + model.playerPositionY,
                    outputWidth = 64,
                    outputHeight = 64,
                    currentFrame = model.playerCurrentAnimationFrame
                )
            } else {
                println("WARN: no player entity set in map")
            }

            // --- AI-generated: key display above player head ---
            val pressedKeys = model.isPressed
            val keyIconSize = 64
            val gapBetweenKeys = 0
            val totalWidth = pressedKeys.size * keyIconSize + (pressedKeys.size - 1).coerceAtLeast(0) * gapBetweenKeys
            val startX = playerWorldX + (64 - totalWidth) / 2f

            pressedKeys.forEachIndexed { index, key ->
                val x = startX + index * (keyIconSize + gapBetweenKeys)
                when (key) {
                    Input.KeyboardW -> drawSprite(
                        sprite = sprites["Keyboard_W"]!!,
                        outputWidth = keyIconSize,
                        outputHeight = keyIconSize,
                        outputPositionX = x,
                        outputPositionY = playerWorldY - 64,
                    )

                    Input.KeyboardA -> drawSprite(
                        sprite = sprites["Keyboard_A"]!!,
                        outputWidth = keyIconSize,
                        outputHeight = keyIconSize,
                        outputPositionX = x,
                        outputPositionY = playerWorldY - 64,
                    )

                    Input.KeyboardD -> drawSprite(
                        sprite = sprites["Keyboard_D"]!!,
                        outputWidth = keyIconSize,
                        outputHeight = keyIconSize,
                        outputPositionX = x,
                        outputPositionY = playerWorldY - 64,
                    )

                    Input.KeyboardS -> drawSprite(
                        sprite = sprites["Keyboard_S"]!!,
                        outputWidth = keyIconSize,
                        outputHeight = keyIconSize,
                        outputPositionX = x,
                        outputPositionY = playerWorldY - 64,
                    )

                    else -> {}
                }
            }
            // --- end AI-generated ---
        }
    }
    EndDrawing()
}

fun Entity.toDefaultSprite() = when (this) {
    Entity.Background -> sprites["Background"]!!
    Entity.Terrain -> sprites["Terrain"]!!
    Entity.Player -> sprites["Player_Idle"]!!
    Entity.RockHead -> sprites["RockHead"]!!
    Entity.Finish -> sprites["Finish"]!!
    Entity.WoodBox -> sprites["WoodBox"]!!
}

fun drawSprite(
    sprite: Sprite,
    outputPositionX: Float = 0f,
    outputPositionY: Float = 0f,
    outputWidth: Int = sprite.width,
    outputHeight: Int = sprite.height,
    tint: CValue<Color> = color(255, 255, 255),
    inputYOffset: Int = 0,
    flipHorizontally: Boolean = false,
    currentFrame: Int = 0,
) {
    DrawTexturePro(
        texture = sprite.texture,
        source = cValue<Rectangle> {
            x = sprite.positionX.toFloat() + sprite.width * (currentFrame % sprite.numberOfFrames)
            y = (sprite.positionY + inputYOffset).toFloat()
            width = if (flipHorizontally) sprite.width.toFloat() * -1 else sprite.width.toFloat()
            height = sprite.height.toFloat()
        },
        dest = cValue<Rectangle> {
            x = outputPositionX
            y = outputPositionY
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
