import com.raylib.kmp.Color
import com.raylib.kmp.ConfigFlags
import com.raylib.kmp.GamepadAxis
import com.raylib.kmp.GamepadButton
import com.raylib.kmp.KeyboardKey
import com.raylib.kmp.MouseButton
import com.raylib.kmp.Ray
import com.raylib.kmp.Raylib
import game.Animation
import game.GameState
import game.Input
import game.SceneType
import game.Sprite
import game.Static
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.delay

object Engine {
    var WINDOW_WIDTH: Int = 800
    var WINDOW_HEIGHT: Int = 600
    const val TARGET_FPS = 30
    private val RAYWHITE = Color(245, 245, 245, 255)

    fun init() {
        val windowed = Platform.getEnv("WINDOWED") == "true"
        if (!windowed) Raylib.setConfigFlags(ConfigFlags.FLAG_WINDOW_UNDECORATED.toUInt())
        Raylib.initWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Platformer 101")
        Raylib.initAudioDevice()
        Raylib.setTargetFPS(TARGET_FPS)
        if (!windowed) {
            val display = Raylib.getCurrentMonitor()
            WINDOW_WIDTH = Raylib.getMonitorWidth(display)
            WINDOW_HEIGHT = Raylib.getMonitorHeight(display)
        }
        Raylib.setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        Raylib.setWindowPosition(0, 0)
//        HideCursor()
    }

    fun update() {
        GameState.wasPressed = GameState.isPressed

        GameState.isPressed = buildList {
            if (Raylib.isMouseButtonDown(MouseButton.MOUSE_BUTTON_LEFT)) add(Input.Mouse1)
            if (Raylib.isMouseButtonDown(MouseButton.MOUSE_BUTTON_RIGHT)) add(Input.Mouse2)
            if (Raylib.isKeyDown(KeyboardKey.KEY_S)) add(Input.KeyboardS)
            if (Raylib.isKeyDown(KeyboardKey.KEY_L)) add(Input.KeyboardL)
            if (Raylib.isKeyDown(KeyboardKey.KEY_P)) add(Input.KeyboardP)
            if (Raylib.isKeyDown(KeyboardKey.KEY_E)) add(Input.KeyboardE)
            if (Raylib.isKeyDown(KeyboardKey.KEY_W)) add(Input.KeyboardW)
            if (Raylib.isKeyDown(KeyboardKey.KEY_A)) add(Input.KeyboardA)
            if (Raylib.isKeyDown(KeyboardKey.KEY_D)) add(Input.KeyboardD)
            if (Raylib.isGamepadAvailable(0)) {
                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_LEFT_FACE_LEFT
                    )
                ) add(Input.SwitchControllerDPadLeft)
                if (Raylib.getGamepadAxisMovement(
                        0,
                        GamepadAxis.GAMEPAD_AXIS_LEFT_X
                    ) < -0.5f
                ) add(Input.SwitchControllerLJoyStickLeft)

                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_LEFT_FACE_RIGHT
                    )
                ) add(Input.SwitchControllerDPadRight)
                if (Raylib.getGamepadAxisMovement(
                        0,
                        GamepadAxis.GAMEPAD_AXIS_LEFT_X
                    ) > 0.5f
                ) add(Input.SwitchControllerLJoyStickRight)

                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_LEFT_FACE_UP
                    )
                ) add(Input.SwitchControllerDPadUp)
                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_LEFT_FACE_DOWN
                    )
                ) add(Input.SwitchControllerDPadDown)
                if (Raylib.getGamepadAxisMovement(
                        0,
                        GamepadAxis.GAMEPAD_AXIS_LEFT_Y
                    ) < -0.5f
                ) add(Input.SwitchControllerLJoyStickUp)
                if (Raylib.getGamepadAxisMovement(
                        0,
                        GamepadAxis.GAMEPAD_AXIS_LEFT_Y
                    ) > 0.5f
                ) add(Input.SwitchControllerLJoyStickDown)
                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_RIGHT_FACE_RIGHT
                    )
                ) add(Input.SwitchControllerA)
                if (Raylib.isGamepadButtonDown(
                        0,
                        GamepadButton.GAMEPAD_BUTTON_RIGHT_FACE_DOWN
                    )
                ) add(Input.SwitchControllerB)

                if (Raylib.isGamepadButtonDown(0, GamepadButton.GAMEPAD_BUTTON_MIDDLE)) {
                    throw CloseGameException()
                }
            }
        }

        val mousePosition = Raylib.getMousePosition()
        GameState.mousePositionX = mousePosition.x.toInt()
        GameState.mousePositionY = mousePosition.y.toInt()
    }

    fun render() {
        Raylib.beginDrawing()
        Raylib.clearBackground(RAYWHITE)
        GameState.sounds.forEach {
            val sound = Assets.fromClip(it)
            if (!Raylib.isSoundPlaying(sound)) {
                Raylib.playSound(sound)
            }
        }
        GameState.sounds.clear()

        // Render Background
        (0..WINDOW_WIDTH.div(GameState.tileSize)).forEach { xOffset ->
            (0..WINDOW_HEIGHT.div(GameState.tileSize)).forEach { yOffset ->
                Render.drawSprite(
                    sprite = Sprite.sprites["Background"]!!,
                    outputWidth = GameState.tileSize,
                    outputHeight = GameState.tileSize,
                    inputXOffset = GameState.cameraOffsetX,
                    inputYOffset = GameState.backgroundOffsetY,
                    outputPositionX = xOffset * GameState.tileSize.toFloat(),
                    outputPositionY = (yOffset * GameState.tileSize.toFloat()),
                )
            }
        }

        val totalOffsetX = GameState.cameraOffsetX + GameState.playSpaceOffsetX
        val validPlaySpaceOffsetY = -(GameState.SIZE_Y_IN_TILES * GameState.tileSize / 2)
        val yOrigin = WINDOW_HEIGHT / 2 - validPlaySpaceOffsetY
        when (GameState.sceneType) {
            SceneType.Editor -> {
                Raylib.drawRectangle(
                    -totalOffsetX,
                    WINDOW_HEIGHT / 2 + validPlaySpaceOffsetY,
                    WINDOW_WIDTH + totalOffsetX,
                    GameState.SIZE_Y_IN_TILES * GameState.tileSize,
                    Color(50, 50, 50, 122)
                )
                Raylib.drawText(GameState.currentMap, 64, 64, 24, Color(0, 0, 0))
                GameState.renderables.forEach {
                    Render.drawSprite(
                        sprite = it.currentSprite,
                        outputPositionX = (it.mapEntity.gridPositionX * GameState.tileSize.toFloat()) - totalOffsetX,
                        outputPositionY = yOrigin - GameState.tileSize - (it.mapEntity.gridPositionY * GameState.tileSize.toFloat()),
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
                        tint = Color(255, 255, 255, 150),
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
                if (GameState.playerEntity != null) {
                    val sprite = when {
                        GameState.playerVelocityYInTiles > 0 -> Sprite.sprites["Player_Jump"]
                        GameState.playerVelocityYInTiles < 0 -> Sprite.sprites["Player_Fall"]
                        GameState.playerVelocityXInTiles != 0f -> Sprite.sprites["Player_Run"]
                        GameState.playerVelocityYInTiles == 0f && GameState.playerVelocityXInTiles == 0f -> Sprite.sprites["Player_Idle"]
                        else -> Sprite.sprites["Player_Idle"]
                    }
                    Render.drawSprite(
                        sprite = sprite!!,
                        flipHorizontally = GameState.playerDirection == -1,
                        outputPositionX = GameState.playerEntity!!.gridPositionX * GameState.tileSize + (GameState.playerPositionXOffsetInTiles * GameState.tileSize) - totalOffsetX,
                        outputPositionY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - GameState.playerWorldY + GameState.playSpaceOffsetY,
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
                        currentFrame = GameState.playerCurrentAnimationFrame
                    )

                    val pressedKeys = GameState.isPressed
                    val keyIconSize = GameState.tileSize
                    val gapBetweenKeys = 0
                    val totalWidth =
                        pressedKeys.size * keyIconSize + (pressedKeys.size - 1).coerceAtLeast(0) * gapBetweenKeys


                    val playerWorldX = GameState.playerWorldX
                    val playerWorldY =
                        (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - GameState.playerWorldY + GameState.playSpaceOffsetY
                    val startX = playerWorldX + (GameState.tileSize - totalWidth) / 2f

                    pressedKeys.forEachIndexed { index, key ->
                        val x = startX + index * (keyIconSize + gapBetweenKeys) - totalOffsetX
                        val sprite = when (key) {
                            Input.KeyboardW -> Sprite.sprites["Keyboard_W"]!!
                            Input.KeyboardA -> Sprite.sprites["Keyboard_A"]!!
                            Input.KeyboardD -> Sprite.sprites["Keyboard_D"]!!
                            Input.KeyboardS -> Sprite.sprites["Keyboard_S"]!!
                            Input.SwitchControllerLJoyStickLeft -> Sprite.sprites[Input.SwitchControllerDPadLeft.name]!!
                            Input.SwitchControllerLJoyStickRight -> Sprite.sprites[Input.SwitchControllerDPadRight.name]!!
                            Input.SwitchControllerDPadRight,
                            Input.SwitchControllerDPadLeft,
                            Input.SwitchControllerA,
                            Input.SwitchControllerB,
                                -> Sprite.sprites[key.name]!!

                            else -> null
                        }
                        if (sprite != null) {
                            Render.drawSprite(
                                sprite = sprite,
                                outputWidth = keyIconSize,
                                outputHeight = keyIconSize,
                                outputPositionX = x,
                                outputPositionY = playerWorldY - 1.5f * GameState.tileSize.toFloat(),
                            )
                        }
                    }
                } else {
                    println("WARN: no player entity set in map")
                }

            }
        }

        // DrawText("Offset X: ${GameState.playSpaceOffsetX}", 64, 64, 24, color(0, 0, 0))

        Raylib.endDrawing()
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
