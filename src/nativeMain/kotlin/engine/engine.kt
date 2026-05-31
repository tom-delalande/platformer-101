@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import logic.Entity
import logic.Input
import logic.SceneType
import logic.getSpriteData
import logic.model
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

            Entity.PlayerRun to LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Run (32x32).png"),
            Entity.PlayerJump to LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Jump (32x32).png"),
            Entity.PlayerFall to LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Fall (32x32).png"),
        )
    )
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
                sprite = Entity.Background,
                inputX = 0,
                inputY = 0,
                inputWidth = 64,
                inputHeight = 64,
                outputPositionX = xOffset * 64f,
                outputPositionY = yOffset * 64f,
            )
        }
    }

    when (model.sceneType) {
        SceneType.Editor -> {
            model.map.forEach {
                val spriteData = it.entity.getSpriteData()
                drawSprite(
                    sprite = it.entity,
                    inputX = spriteData.inputX,
                    inputY = spriteData.inputY,
                    inputWidth = spriteData.inputWidth,
                    inputHeight = spriteData.inputHeight,
                    outputPositionX = it.gridPositionX * 64f,
                    outputPositionY = (engineData.windowHeight / 64) * 64 - it.gridPositionY * 64f,
                    outputWidth = 64,
                    outputHeight = 64,
                )
            }

            if (model.selectedUIElement != null) {
                drawSprite(
                    sprite = model.selectedUIElement!!.sprite,
                    inputX = model.selectedUIElement!!.inputX,
                    inputY = model.selectedUIElement!!.inputY,
                    inputWidth = model.selectedUIElement!!.inputWidth,
                    inputHeight = model.selectedUIElement!!.inputHeight,
                    outputPositionX = model.mousePositionX - 32f,
                    outputPositionY = model.mousePositionY - 32f,
                    outputWidth = 64,
                    outputHeight = 64,
                    tint = color(255, 255, 255, 150),
                )
            }

            model.uiElements.forEach {
                drawSprite(
                    sprite = it.sprite,
                    inputX = it.inputX,
                    inputY = it.inputY,
                    inputWidth = it.inputWidth,
                    inputHeight = it.inputHeight,
                    outputPositionX = it.outputPositionX.toFloat(),
                    outputPositionY = it.outputPositionY.toFloat(),
                    outputWidth = it.outputWidth,
                    outputHeight = it.outputHeight,
                )
            }
        }

        SceneType.Play -> {
            model.map.filter { it.entity != Entity.Player }.forEach {
                val spriteData = it.entity.getSpriteData()
                drawSprite(
                    sprite = it.entity,
                    inputX = spriteData.inputX,
                    inputY = spriteData.inputY,
                    inputWidth = spriteData.inputWidth,
                    inputHeight = spriteData.inputHeight,
                    outputPositionX = it.gridPositionX * 64f,
                    outputPositionY = (engineData.windowHeight / 64) * 64 - it.gridPositionY * 64f,
                    outputWidth = 64,
                    outputHeight = 64,
                )
            }
            val playerEntity = model.map.find { it.entity == Entity.Player }
            if (playerEntity != null) {
                var numberOfFrames = 11
                var entity = Entity.Player
                when {
                    model.playerVelocityX != 0f -> {
                        numberOfFrames = 12
                        entity = Entity.PlayerRun
                    }
                    model.playerVelocityY == 0f && model.playerVelocityX == 0f -> {
                        numberOfFrames = 11
                        entity = Entity.Player
                    }
                    model.playerVelocityY >= 0 -> {
                        numberOfFrames = 1
                        entity = Entity.PlayerJump
                    }
                    model.playerVelocityY < 0 -> {
                        numberOfFrames = 1
                        entity = Entity.PlayerFall
                    }
                }
                drawSprite(
                    sprite = entity,
                    inputX = 32 * (model.playerCurrentAnimationFrame % numberOfFrames),
                    inputY = 0,
                    inputWidth = 32,
                    inputHeight = 32,
                    outputPositionX = playerEntity.gridPositionX + model.playerPositionX,
                    outputPositionY = (engineData.windowHeight / 64) * 64 - playerEntity.gridPositionY * 64f + model.playerPositionY,
                    outputWidth = 64,
                    outputHeight = 64,
                )
            } else {
                println("WARN: no player entity set in map")
            }
        }
    }
    EndDrawing()
}


fun drawSprite(
    sprite: Entity,
    inputX: Int = 0,
    inputY: Int = 0,
    inputWidth: Int = 16,
    inputHeight: Int = 16,
    outputPositionX: Float = 0f,
    outputPositionY: Float = 0f,
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
