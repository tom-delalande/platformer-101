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
        if (!windowed) Platform.setConfigFlags(Platform.FLAG_WINDOW_UNDECORATED)
        Platform.initWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Platformer 101")
        Platform.initAudioDevice()
        Platform.setTargetFPS(TARGET_FPS)
        if (!windowed) {
            val display = Platform.getCurrentMonitor()
            WINDOW_WIDTH = Platform.getMonitorWidth(display)
            WINDOW_HEIGHT = Platform.getMonitorHeight(display)
        }
        Platform.setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        Platform.setWindowPosition(0, 0)
//        HideCursor()
    }

    fun update() {
        GameState.wasPressed = GameState.isPressed

        GameState.isPressed = buildList {
            if (Platform.isMouseButtonDown(Platform.MOUSE_BUTTON_LEFT)) add(Input.Mouse1)
            if (Platform.isMouseButtonDown(Platform.MOUSE_BUTTON_RIGHT)) add(Input.Mouse2)
            if (Platform.isKeyDown(Platform.KEY_S)) add(Input.KeyboardS)
            if (Platform.isKeyDown(Platform.KEY_L)) add(Input.KeyboardL)
            if (Platform.isKeyDown(Platform.KEY_P)) add(Input.KeyboardP)
            if (Platform.isKeyDown(Platform.KEY_E)) add(Input.KeyboardE)
            if (Platform.isKeyDown(Platform.KEY_W)) add(Input.KeyboardW)
            if (Platform.isKeyDown(Platform.KEY_A)) add(Input.KeyboardA)
            if (Platform.isKeyDown(Platform.KEY_D)) add(Input.KeyboardD)
            if (Platform.isGamepadAvailable(0)) {
                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_LEFT_FACE_LEFT
                    )
                ) add(Input.SwitchControllerDPadLeft)
                if (Platform.getGamepadAxisMovement(
                        0,
                        Platform.GAMEPAD_AXIS_LEFT_X
                    ) < -0.5f
                ) add(Input.SwitchControllerLJoyStickLeft)

                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_LEFT_FACE_RIGHT
                    )
                ) add(Input.SwitchControllerDPadRight)
                if (Platform.getGamepadAxisMovement(
                        0,
                        Platform.GAMEPAD_AXIS_LEFT_X
                    ) > 0.5f
                ) add(Input.SwitchControllerLJoyStickRight)

                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_LEFT_FACE_UP
                    )
                ) add(Input.SwitchControllerDPadUp)
                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_LEFT_FACE_DOWN
                    )
                ) add(Input.SwitchControllerDPadDown)
                if (Platform.getGamepadAxisMovement(
                        0,
                        Platform.GAMEPAD_AXIS_LEFT_Y
                    ) < -0.5f
                ) add(Input.SwitchControllerLJoyStickUp)
                if (Platform.getGamepadAxisMovement(
                        0,
                        Platform.GAMEPAD_AXIS_LEFT_Y
                    ) > 0.5f
                ) add(Input.SwitchControllerLJoyStickDown)
                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_RIGHT_FACE_RIGHT
                    )
                ) add(Input.SwitchControllerA)
                if (Platform.isGamepadButtonDown(
                        0,
                        Platform.GAMEPAD_BUTTON_RIGHT_FACE_DOWN
                    )
                ) add(Input.SwitchControllerB)

                if (Platform.isGamepadButtonDown(0, Platform.GAMEPAD_BUTTON_MIDDLE)) {
                    throw CloseGameException()
                }
            }
        }

        val mousePosition = Platform.getMousePosition()
        GameState.mousePositionX = mousePosition.x.toInt()
        GameState.mousePositionY = mousePosition.y.toInt()
    }

    fun render() {
        Platform.beginDrawing()
        Platform.clearBackground(RAYWHITE)
        GameState.sounds.forEach {
            val sound = Assets.fromClip(it)
            if (!Platform.isSoundPlaying(sound)) {
                Platform.playSound(sound)
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
                Platform.drawRectangle(
                    -totalOffsetX,
                    WINDOW_HEIGHT / 2 + validPlaySpaceOffsetY,
                    WINDOW_WIDTH + totalOffsetX,
                    GameState.SIZE_Y_IN_TILES * GameState.tileSize,
                    Color(50, 50, 50, 122)
                )
                Platform.drawText(GameState.currentMap, 64, 64, 24, Color(0, 0, 0))
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

        Platform.endDrawing()
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
