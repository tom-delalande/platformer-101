@file:OptIn(ExperimentalForeignApi::class)

import engine.color
import game.Animation
import game.EntityType
import game.GameState
import game.Input
import game.SceneType
import game.Sprite
import game.Static
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import raylib.BeginDrawing
import raylib.ClearBackground
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
import raylib.SetTargetFPS

object Engine {
    const val WINDOW_WIDTH: Int = 800
    const val WINDOW_HEIGHT: Int = 600
    const val TARGET_FPS = 30
    private val RAYWHITE = color(245, 245, 245)

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
                Render.drawSprite(
                    sprite = Sprite.sprites["Background"]!!,
                    inputXOffset = GameState.cameraOffsetX,
                    inputYOffset = GameState.backgroundOffsetY,
                    outputPositionX = xOffset * 64f,
                    outputPositionY = (yOffset * 64f),
                )
            }
        }

        when (GameState.sceneType) {
            SceneType.Editor -> {
                GameState.renderables.forEach {
                    when (it) {
                        is Animation -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * 64f) - GameState.cameraOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.mapEntity.gridPositionY * 64f,
                            outputWidth = 64,
                            outputHeight = 64,
                            currentFrame = it.currentFrame,
                        )

                        is Static -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * 64f) - GameState.cameraOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.mapEntity.gridPositionY * 64f,
                            outputWidth = 64,
                            outputHeight = 64,
                            // This is the only difference to above, could probably be simplified with better classes
                            currentFrame = 0,
                        )
                    }
                }

                if (GameState.selectedUIElement != null) {
                    Render.drawSprite(
                        sprite = GameState.selectedUIElement!!.sprite,
                        outputPositionX = (GameState.mousePositionX - 32f),
                        outputPositionY = GameState.mousePositionY - 32f,
                        outputWidth = 64,
                        outputHeight = 64,
                        tint = color(255, 255, 255, 150),
                    )
                }

                GameState.uiElements.forEach {
                    Render.drawSprite(
                        sprite = it.sprite,
                        outputPositionX = it.outputPositionX.toFloat(),
                        outputPositionY = it.outputPositionY.toFloat(),
                        outputWidth = it.outputWidth,
                        outputHeight = it.outputHeight,
                    )
                }
            }

            SceneType.Play -> {
                GameState.renderables.forEach {
                    when (it) {
                        is Animation -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * 64f) - GameState.cameraOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.mapEntity.gridPositionY * 64f,
                            outputWidth = 64,
                            outputHeight = 64,
                            currentFrame = it.currentFrame,
                        )

                        is Static -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * 64f) - GameState.cameraOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / 64) * 64 - it.mapEntity.gridPositionY * 64f,
                            outputWidth = 64,
                            outputHeight = 64,
                            // This is the only difference to above, could probably be simplified with better classes
                            currentFrame = 0,
                        )
                    }
                }
                val playerEntityType = GameState.map.find { it.entity == EntityType.Player }
                if (playerEntityType != null) {
                    val sprite = when {
                        GameState.playerVelocityY > 0 -> Sprite.sprites["Player_Jump"]
                        GameState.playerVelocityY < 0 -> Sprite.sprites["Player_Fall"]
                        GameState.playerVelocityX != 0f -> Sprite.sprites["Player_Run"]
                        GameState.playerVelocityY == 0f && GameState.playerVelocityX == 0f -> Sprite.sprites["Player_Idle"]
                        else -> Sprite.sprites["Player_Idle"]
                    }
                    Render.drawSprite(
                        sprite = sprite!!,
                        flipHorizontally = GameState.playerDirection == -1,
                        outputPositionX = playerEntityType.gridPositionX * GameState.TILE_SIZE + GameState.playerPositionX - GameState.cameraOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / GameState.TILE_SIZE) * GameState.TILE_SIZE - playerEntityType.gridPositionY * GameState.TILE_SIZE + GameState.playerPositionY,
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

                    val playerWorldX = playerEntityType.gridPositionX * GameState.TILE_SIZE + GameState.playerPositionX
                    val playerWorldY =
                        (WINDOW_HEIGHT / GameState.TILE_SIZE) * GameState.TILE_SIZE - playerEntityType.gridPositionY * GameState.TILE_SIZE + GameState.playerPositionY
                    val startX = playerWorldX + (64 - totalWidth) / 2f

                    pressedKeys.forEachIndexed { index, key ->
                        val x = startX + index * (keyIconSize + gapBetweenKeys) - GameState.cameraOffsetX
                        when (key) {
                            Input.KeyboardW -> Render.drawSprite(
                                sprite = Sprite.sprites["Keyboard_W"]!!,
                                outputWidth = keyIconSize,
                                outputHeight = keyIconSize,
                                outputPositionX = x,
                                outputPositionY = playerWorldY - 64,
                            )

                            Input.KeyboardA -> Render.drawSprite(
                                sprite = Sprite.sprites["Keyboard_A"]!!,
                                outputWidth = keyIconSize,
                                outputHeight = keyIconSize,
                                outputPositionX = x,
                                outputPositionY = playerWorldY - 64,
                            )

                            Input.KeyboardD -> Render.drawSprite(
                                sprite = Sprite.sprites["Keyboard_D"]!!,
                                outputWidth = keyIconSize,
                                outputHeight = keyIconSize,
                                outputPositionX = x,
                                outputPositionY = playerWorldY - 64,
                            )

                            Input.KeyboardS -> Render.drawSprite(
                                sprite = Sprite.sprites["Keyboard_S"]!!,
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
