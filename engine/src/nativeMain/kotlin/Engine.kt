@file:OptIn(ExperimentalForeignApi::class)

// AI-generated: gamepad imports
import engine.color
import engine.toEngine
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
import raylib.GAMEPAD_AXIS_LEFT_X
import raylib.GAMEPAD_AXIS_LEFT_Y
import raylib.GAMEPAD_BUTTON_LEFT_FACE_LEFT
import raylib.GAMEPAD_BUTTON_LEFT_FACE_RIGHT
import raylib.GAMEPAD_BUTTON_LEFT_FACE_UP
import raylib.GAMEPAD_BUTTON_MIDDLE
import raylib.GAMEPAD_BUTTON_RIGHT_FACE_DOWN
import raylib.GetCurrentMonitor
import raylib.GetGamepadAxisMovement
import raylib.GetMonitorHeight
import raylib.GetMonitorWidth
import raylib.GetMousePosition
import raylib.InitAudioDevice
import raylib.InitWindow
import raylib.IsGamepadAvailable
import raylib.IsGamepadButtonDown
import raylib.IsKeyDown
import raylib.IsMouseButtonDown
import raylib.IsSoundPlaying
import raylib.KEY_A
import raylib.KEY_D
import raylib.KEY_E
import raylib.KEY_L
import raylib.KEY_P
import raylib.KEY_S
import raylib.KEY_W
import raylib.MOUSE_BUTTON_LEFT
import raylib.MOUSE_BUTTON_RIGHT
import raylib.PlaySound
import raylib.SetTargetFPS
import raylib.SetWindowPosition
import raylib.SetWindowSize

object Engine {
    var WINDOW_WIDTH: Int = 800
    var WINDOW_HEIGHT: Int = 600
    const val TARGET_FPS = 30
    private val RAYWHITE = color(245, 245, 245)

    fun init() {
//        SetConfigFlags(FLAG_WINDOW_UNDECORATED)
        InitWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Platformer 101")
        InitAudioDevice()
        SetTargetFPS(TARGET_FPS)
        val display = GetCurrentMonitor()
        WINDOW_WIDTH = GetMonitorWidth(display)
        WINDOW_HEIGHT = GetMonitorHeight(display)
        SetWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        SetWindowPosition(0, 0)
    }

    fun update() {
        GameState.wasPressed = GameState.isPressed

        GameState.isPressed = buildList {
            if (IsMouseButtonDown(MOUSE_BUTTON_LEFT.toInt())) add(Input.Mouse1)
            if (IsMouseButtonDown(MOUSE_BUTTON_RIGHT.toInt())) add(Input.Mouse2)
            if (IsKeyDown(KEY_S.toInt())) add(Input.KeyboardS)
            if (IsKeyDown(KEY_L.toInt())) add(Input.KeyboardL)
            if (IsKeyDown(KEY_P.toInt())) add(Input.KeyboardP)
            if (IsKeyDown(KEY_E.toInt())) add(Input.KeyboardE)
            if (IsKeyDown(KEY_W.toInt())) add(Input.KeyboardW)
            if (IsKeyDown(KEY_A.toInt())) add(Input.KeyboardA)
            if (IsKeyDown(KEY_D.toInt())) add(Input.KeyboardD)
            // AI-generated: gamepad input polling
            if (IsGamepadAvailable(0)) {
                if (IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_LEFT.toInt()) || GetGamepadAxisMovement(
                        0,
                        GAMEPAD_AXIS_LEFT_X.toInt()
                    ) < -0.5f
                ) add(Input.ControllerLeft)
                if (IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_RIGHT.toInt()) || GetGamepadAxisMovement(
                        0,
                        GAMEPAD_AXIS_LEFT_X.toInt()
                    ) > 0.5f
                ) add(Input.ControllerRight)
                if (IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_UP.toInt()) || GetGamepadAxisMovement(
                        0,
                        GAMEPAD_AXIS_LEFT_Y.toInt()
                    ) < -0.5f || IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_DOWN.toInt())
                ) add(Input.ControllerUp)
                if (IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE.toInt())) {
                    throw CloseGameException()
                }

            }
        }

        val mousePosition = GetMousePosition()
        GameState.mousePositionX = mousePosition.useContents { x.toInt() }
        GameState.mousePositionY = mousePosition.useContents { y.toInt() }
    }

    fun render() {
        BeginDrawing()
        ClearBackground(RAYWHITE)
        GameState.sounds.forEach {
            if (!IsSoundPlaying(it.toEngine())) {
                PlaySound(it.toEngine());
            }
        }
        GameState.sounds.clear()

        // Render Background
        (0..WINDOW_WIDTH.div(GameState.tileSize)).forEach { xOffset ->
            (0..WINDOW_HEIGHT.div(GameState.tileSize)).forEach { yOffset ->
                Render.drawSprite(
                    sprite = Sprite.sprites["Background"]!!,
                    inputXOffset = GameState.cameraOffsetX,
                    inputYOffset = GameState.backgroundOffsetY,
                    outputPositionX = xOffset * GameState.tileSize.toFloat(),
                    outputPositionY = (yOffset * GameState.tileSize.toFloat()),
                )
            }
        }

        val totalOffsetX = GameState.cameraOffsetX + GameState.playSpaceOffsetX
        when (GameState.sceneType) {
            SceneType.Editor -> {
                GameState.renderables.forEach {
                    Render.drawSprite(
                        sprite = it.currentSprite,
                        outputPositionX = (it.mapEntity.gridPositionX * GameState.tileSize.toFloat()) - totalOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - it.mapEntity.gridPositionY * GameState.tileSize.toFloat() + GameState.playSpaceOffsetY,
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
                        // This is the only difference to above, could probably be simplified with better classes
                        currentFrame = 0,
                    )
                }

                if (GameState.selectedUIElement != null) {
                    Render.drawSprite(
                        sprite = GameState.selectedUIElement!!.sprite,
                        outputPositionX = (GameState.mousePositionX - 32f),
                        outputPositionY = GameState.mousePositionY - 32f,
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
                        tint = color(255, 255, 255, 150),
                    )
                }

                GameState.uiElements.forEach {
                    Render.drawSprite(
                        sprite = it.sprite,
                        outputPositionX = it.outputPositionXTile.toFloat() * GameState.tileSize,
                        outputPositionY = it.outputPositionYTile.toFloat() * GameState.tileSize,
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
                    )
                }
            }

            SceneType.Play -> {
                GameState.renderables.forEach {
                    when (it) {
                        is Animation -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * GameState.tileSize.toFloat()) - totalOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - it.mapEntity.gridPositionY * GameState.tileSize.toFloat() + GameState.playSpaceOffsetY,
                            outputWidth = GameState.tileSize,
                            outputHeight = GameState.tileSize,
                            currentFrame = it.currentFrame,
                        )

                        is Static -> Render.drawSprite(
                            sprite = it.currentSprite,
                            outputPositionX = (it.mapEntity.gridPositionX * GameState.tileSize.toFloat()) - totalOffsetX,
                            outputPositionY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - it.mapEntity.gridPositionY * GameState.tileSize.toFloat() + GameState.playSpaceOffsetY,
                            outputWidth = GameState.tileSize,
                            outputHeight = GameState.tileSize,
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
                        outputPositionX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX - totalOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - playerEntityType.gridPositionY * GameState.tileSize + GameState.playerPositionY + GameState.playSpaceOffsetY,
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
                        currentFrame = GameState.playerCurrentAnimationFrame
                    )

                    val pressedKeys = GameState.isPressed
                    val keyIconSize = GameState.tileSize
                    val gapBetweenKeys = 0
                    val totalWidth =
                        pressedKeys.size * keyIconSize + (pressedKeys.size - 1).coerceAtLeast(0) * gapBetweenKeys

                    val playerWorldX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX
                    val playerWorldY =
                        (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - playerEntityType.gridPositionY * GameState.tileSize + GameState.playerPositionY + GameState.playSpaceOffsetY
                    val startX = playerWorldX + (GameState.tileSize - totalWidth) / 2f

                    pressedKeys.forEachIndexed { index, key ->
                        val x = startX + index * (keyIconSize + gapBetweenKeys) - totalOffsetX
                        val sprite = when (key) {
                            Input.KeyboardW -> Sprite.sprites["Keyboard_W"]!!
                            Input.KeyboardA -> Sprite.sprites["Keyboard_A"]!!
                            Input.KeyboardD -> Sprite.sprites["Keyboard_D"]!!
                            Input.KeyboardS -> Sprite.sprites["Keyboard_S"]!!
                            Input.ControllerLeft -> Sprite.sprites["Switch_Left"]!!
                            Input.ControllerRight -> Sprite.sprites["Switch_Right"]!!
                            Input.ControllerUp -> Sprite.sprites["Switch_Up"]!!
                            else -> null
                        }
                        if (sprite != null) {
                            Render.drawSprite(
                                sprite = sprite,
                                outputWidth = keyIconSize,
                                outputHeight = keyIconSize,
                                outputPositionX = x,
                                outputPositionY = playerWorldY - GameState.tileSize.toFloat(),
                            )
                        }
                    }
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

    class CloseGameException : Exception()
}
