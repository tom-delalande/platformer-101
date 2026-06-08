import cnames.structs.SDL_AudioStream
import cnames.structs.SDL_Gamepad
import cnames.structs.SDL_Renderer
import cnames.structs.SDL_Window
import game.Animation
import game.GameState
import game.Input
import game.SceneType
import game.Sprite
import game.Static
import kotlinx.cinterop.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import sdl.*

@OptIn(ExperimentalForeignApi::class)
object Engine {
    var WINDOW_WIDTH: Int = 800
    var WINDOW_HEIGHT: Int = 600
    const val TARGET_FPS = 30

    lateinit var window: CPointer<SDL_Window>
    lateinit var renderer: CPointer<SDL_Renderer>
    lateinit var audioStream: CPointer<SDL_AudioStream>
    lateinit var walkStream: CPointer<SDL_AudioStream>
    var gamepad: CPointer<SDL_Gamepad>? = null
    var gamepadId: UInt = 0u

    var windowShouldClose = false

    private val AUDIO_FORMAT = SDL_AUDIO_S16
    private const val AUDIO_CHANNELS = 2
    private const val AUDIO_FREQ = 44100

    fun init() {
        SDL_Init(SDL_INIT_VIDEO)
        SDL_SetHint("SDL_HINT_RENDER_SCALE_QUALITY", "0")

        val windowed = getEnv("WINDOWED") == "true"
        val windowFlags = if (!windowed) {
            SDL_WINDOW_FULLSCREEN
        } else {
            SDL_WINDOW_RESIZABLE
        }
        window = SDL_CreateWindow("Platformer 101", WINDOW_WIDTH, WINDOW_HEIGHT, windowFlags)!!

        println("INIT RENDERER")
        renderer = SDL_CreateRenderer(window, null)!!

        if (!windowed) {
            memScoped {
                val w = alloc<IntVar>()
                val h = alloc<IntVar>()
                SDL_GetWindowSize(window, w.ptr, h.ptr)
                WINDOW_WIDTH = w.value
                WINDOW_HEIGHT = h.value
            }
        }

        SDL_InitSubSystem(SDL_INIT_AUDIO)
        memScoped {
            val spec = alloc<SDL_AudioSpec>().apply {
                format = AUDIO_FORMAT
                channels = AUDIO_CHANNELS
                freq = AUDIO_FREQ
            }
            audioStream = SDL_OpenAudioDeviceStream(SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK, spec.ptr, null, null)!!
            walkStream = SDL_OpenAudioDeviceStream(SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK, spec.ptr, null, null)!!
        }
        SDL_ResumeAudioStreamDevice(audioStream)
        SDL_ResumeAudioStreamDevice(walkStream)

        SDL_InitSubSystem(SDL_INIT_GAMEPAD)
        memScoped {
            val count = alloc<IntVar>()
            val gamepads = SDL_GetGamepads(count.ptr)
            if (gamepads != null && count.value > 0) {
                gamepadId = gamepads[0]
                gamepad = SDL_OpenGamepad(gamepadId)
                SDL_free(gamepads)
            }
        }
    }

    fun processEvents() {
        memScoped {
            val event = alloc<SDL_Event>()
            while (SDL_PollEvent(event.ptr)) {
                when (event.type) {
                    SDL_EVENT_QUIT -> windowShouldClose = true
                    SDL_EVENT_GAMEPAD_ADDED -> {
                        if (gamepad == null) {
                            val id = event.gdevice.which
                            gamepad = SDL_OpenGamepad(id)
                            gamepadId = id
                        }
                    }
                    SDL_EVENT_GAMEPAD_REMOVED -> {
                        val id = event.gdevice.which
                        if (id == gamepadId) {
                            gamepad = null
                            gamepadId = 0u
                        }
                    }
                }
            }
        }
    }

    fun update() {
        GameState.wasPressed = GameState.isPressed

        GameState.isPressed = buildList {
            memScoped {
                val mouseX = alloc<FloatVar>()
                val mouseY = alloc<FloatVar>()
                val mouseButtons = SDL_GetMouseState(mouseX.ptr, mouseY.ptr)
                if ((mouseButtons and SDL_BUTTON_LMASK) != 0u) add(Input.Mouse1)
                if ((mouseButtons and SDL_BUTTON_RMASK) != 0u) add(Input.Mouse2)
                GameState.mousePositionX = mouseX.value.toInt()
                GameState.mousePositionY = mouseY.value.toInt()
            }

            val keys = SDL_GetKeyboardState(null) ?: return@buildList
            if (keys[SDL_SCANCODE_S.toInt()].value) add(Input.KeyboardS)
            if (keys[SDL_SCANCODE_L.toInt()].value) add(Input.KeyboardL)
            if (keys[SDL_SCANCODE_P.toInt()].value) add(Input.KeyboardP)
            if (keys[SDL_SCANCODE_E.toInt()].value) add(Input.KeyboardE)
            if (keys[SDL_SCANCODE_W.toInt()].value) add(Input.KeyboardW)
            if (keys[SDL_SCANCODE_A.toInt()].value) add(Input.KeyboardA)
            if (keys[SDL_SCANCODE_D.toInt()].value) add(Input.KeyboardD)

            val pad = gamepad
            if (pad != null && SDL_GamepadConnected(pad)) {
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_DPAD_LEFT)) add(Input.SwitchControllerDPadLeft)
                if (SDL_GetGamepadAxis(pad, SDL_GAMEPAD_AXIS_LEFTX) < 15000) add(Input.SwitchControllerLJoyStickLeft)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_DPAD_RIGHT)) add(Input.SwitchControllerDPadRight)
                if (SDL_GetGamepadAxis(pad, SDL_GAMEPAD_AXIS_LEFTX) > 15000) add(Input.SwitchControllerLJoyStickRight)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_DPAD_UP)) add(Input.SwitchControllerDPadUp)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_DPAD_DOWN)) add(Input.SwitchControllerDPadDown)
                if (SDL_GetGamepadAxis(pad, SDL_GAMEPAD_AXIS_LEFTY) < -15000) add(Input.SwitchControllerLJoyStickUp)
                if (SDL_GetGamepadAxis(pad, SDL_GAMEPAD_AXIS_LEFTY) > 15000) add(Input.SwitchControllerLJoyStickDown)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_EAST)) add(Input.SwitchControllerA)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_SOUTH)) add(Input.SwitchControllerB)
                if (SDL_GetGamepadButton(pad, SDL_GAMEPAD_BUTTON_MISC1)) {
                    throw CloseGameException()
                }
            }
        }
    }

    fun render() {
        SDL_SetRenderDrawColor(renderer, 245.toUByte(), 245.toUByte(), 245.toUByte(), 255.toUByte())
        SDL_RenderClear(renderer)

        GameState.sounds.forEach {
            val s = Assets.fromClip(it)
            if (s.audioBuf != null) {
                SDL_ClearAudioStream(audioStream)
                SDL_PutAudioStreamData(audioStream, s.audioBuf, s.audioLen.toInt())
            }
        }
        GameState.sounds.clear()

        (-1..WINDOW_WIDTH.div(GameState.tileSize) + 2).forEach { xOffset ->
            (-1..WINDOW_HEIGHT.div(GameState.tileSize) + 2).forEach { yOffset ->
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
                memScoped {
                    val rect = alloc<SDL_FRect>().apply {
                        x = (-totalOffsetX).toFloat()
                        y = (WINDOW_HEIGHT / 2 + validPlaySpaceOffsetY).toFloat()
                        w = (WINDOW_WIDTH + totalOffsetX).toFloat()
                        h = (GameState.SIZE_Y_IN_TILES * GameState.tileSize).toFloat()
                    }
                    SDL_SetRenderDrawColor(renderer, 50.toUByte(), 50.toUByte(), 50.toUByte(), 122.toUByte())
                    SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_BLEND)
                    SDL_RenderFillRect(renderer, rect.ptr)
                }

                GameState.renderables.forEach {
                    Render.drawSprite(
                        sprite = it.currentSprite,
                        outputPositionX = (it.mapEntity.gridPositionX * GameState.tileSize.toFloat()) - totalOffsetX,
                        outputPositionY = yOrigin - GameState.tileSize - (it.mapEntity.gridPositionY * GameState.tileSize.toFloat()),
                        outputWidth = GameState.tileSize,
                        outputHeight = GameState.tileSize,
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
                        currentFrame = GameState.playerCurrentAnimationFrame,
                    )

                    val pressedKeys = GameState.isPressed
                    val keyIconSize = GameState.tileSize
                    val gapBetweenKeys = 0
                    val totalWidth = pressedKeys.size * keyIconSize + (pressedKeys.size - 1).coerceAtLeast(0) * gapBetweenKeys

                    val playerWorldX = GameState.playerWorldX
                    val playerWorldY = (WINDOW_HEIGHT / GameState.tileSize) * GameState.tileSize - GameState.playerWorldY + GameState.playSpaceOffsetY
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

        SDL_RenderPresent(renderer)
    }

    suspend fun executeWithFixedFrameRate(block: () -> Unit) {
        val time = measureTime {
            block()
        }
        if (time < (1000 / TARGET_FPS).milliseconds) {
            delay((1000 / TARGET_FPS).milliseconds - time)
        }
    }

    class CloseGameException() : Exception()
}
