@file:OptIn(ExperimentalForeignApi::class)

import engine.Sprite
import engine.color
import engine.sprites
import game.Entity
import game.GameState
import game.Input
import game.SceneType
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import raylib.BeginDrawing
import raylib.ClearBackground
import raylib.Color
import raylib.DrawTexturePro
import raylib.EndDrawing
import raylib.GetMousePosition
import raylib.InitWindow
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
import raylib.SetTargetFPS
import raylib.Vector2

private val RAYWHITE = color(245, 245, 245)

object Engine {
    const val WINDOW_WIDTH: Int = 800
    const val WINDOW_HEIGHT: Int = 600
    const val TARGET_FPS = 30

    fun init() {
        InitWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Hello Kotlin + Raylib")
        SetTargetFPS(TARGET_FPS)
    }

    fun update() {
        GameState.wasPressed = GameState.isPressed

        GameState.isPressed = buildList {
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
        GameState.mousePositionX = mousePosition.useContents { x.toInt() }
        GameState.mousePositionY = mousePosition.useContents { y.toInt() }
    }

    fun render() {
        BeginDrawing()
        ClearBackground(RAYWHITE)

        // Render Background
        (0..WINDOW_WIDTH.div(64)).forEach { xOffset ->
            (0..WINDOW_HEIGHT.div(64)).forEach { yOffset ->
                drawSprite(
                    sprite = sprites["Background"]!!,
                    inputXOffset = GameState.cameraOffsetX,
                    inputYOffset = GameState.backgroundOffsetY,
                    outputPositionX = xOffset * 64f,
                    outputPositionY = (yOffset * 64f),
                )
            }
        }

        when (GameState.sceneType) {
            SceneType.Editor -> {
                GameState.map.forEach {
                    drawSprite(
                        sprite = it.entity.toDefaultSprite(),
                        outputPositionX = (it.gridPositionX * 64f) - GameState.cameraOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.gridPositionY * 64f,
                        outputWidth = 64,
                        outputHeight = 64,
                    )
                }

                if (GameState.selectedUIElement != null) {
                    drawSprite(
                        sprite = GameState.selectedUIElement!!.entity.toDefaultSprite(),
                        outputPositionX = (GameState.mousePositionX - 32f),
                        outputPositionY = GameState.mousePositionY - 32f,
                        outputWidth = 64,
                        outputHeight = 64,
                        tint = color(255, 255, 255, 150),
                    )
                }

                GameState.uiElements.forEach {
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
                GameState.map.filter { it.entity != Entity.Player }.forEach {
                    drawSprite(
                        sprite = it.entity.toDefaultSprite(),
                        outputPositionX = (it.gridPositionX * 64f) - GameState.cameraOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.gridPositionY * 64f,
                        outputWidth = 64,
                        outputHeight = 64,
                    )
                }
                val playerEntity = GameState.map.find { it.entity == Entity.Player }
                if (playerEntity != null) {
                    val sprite = when {
                        GameState.playerVelocityY > 0 -> sprites["Player_Jump"]
                        GameState.playerVelocityY < 0 -> sprites["Player_Fall"]
                        GameState.playerVelocityX != 0f -> sprites["Player_Run"]
                        GameState.playerVelocityY == 0f && GameState.playerVelocityX == 0f -> sprites["Player_Idle"]
                        else -> sprites["Player_Idle"]
                    }
                    drawSprite(
                        sprite = sprite!!,
                        flipHorizontally = GameState.playerDirection == -1,
                        outputPositionX = playerEntity.gridPositionX * GameState.TILE_SIZE + GameState.playerPositionX - GameState.cameraOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / GameState.TILE_SIZE) * GameState.TILE_SIZE - playerEntity.gridPositionY * GameState.TILE_SIZE + GameState.playerPositionY,
                        outputWidth = 64,
                        outputHeight = 64,
                        currentFrame = GameState.playerCurrentAnimationFrame
                    )

                    // --- AI-generated: key display above player head ---
                    val pressedKeys = GameState.isPressed
                    val keyIconSize = 64
                    val gapBetweenKeys = 0
                    val totalWidth =
                        pressedKeys.size * keyIconSize + (pressedKeys.size - 1).coerceAtLeast(0) * gapBetweenKeys

                    val playerWorldX = playerEntity.gridPositionX * GameState.TILE_SIZE + GameState.playerPositionX
                    val playerWorldY =
                        (WINDOW_HEIGHT / GameState.TILE_SIZE) * GameState.TILE_SIZE - playerEntity.gridPositionY * GameState.TILE_SIZE + GameState.playerPositionY
                    val startX = playerWorldX + (64 - totalWidth) / 2f

                    pressedKeys.forEachIndexed { index, key ->
                        val x = startX + index * (keyIconSize + gapBetweenKeys) - GameState.cameraOffsetX
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
                } else {
                    println("WARN: no player entity set in map")
                }

            }
        }
        EndDrawing()
    }

    suspend fun executeWithFixedFrameRate(block: () -> Unit) {
        val time = measureTime {
            block()
        }
        if (time < (1000 / TARGET_FPS).milliseconds) {
            delay((1000 / TARGET_FPS).milliseconds - time)
        }
    }
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
    inputXOffset: Int = 0,
    inputYOffset: Int = 0,
    flipHorizontally: Boolean = false,
    currentFrame: Int = 0,
) {
    DrawTexturePro(
        texture = sprite.texture,
        source = cValue<Rectangle> {
            x = (sprite.positionX.toFloat() + inputXOffset) + sprite.width * (currentFrame % sprite.numberOfFrames)
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
