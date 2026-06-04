import com.raylib.Raylib
import com.raylib.Raylib as raylib

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Platform {
    actual val GAMEPAD_AXIS_LEFT_X: Int
        get() = raylib.GAMEPAD_AXIS_LEFT_X
    actual val GAMEPAD_AXIS_LEFT_Y: Int
        get() = raylib.GAMEPAD_AXIS_LEFT_Y
    actual val GAMEPAD_BUTTON_LEFT_FACE_DOWN: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_DOWN
    actual val GAMEPAD_BUTTON_LEFT_FACE_LEFT: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_LEFT
    actual val GAMEPAD_BUTTON_LEFT_FACE_RIGHT: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_RIGHT
    actual val GAMEPAD_BUTTON_LEFT_FACE_UP: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_UP
    actual val GAMEPAD_BUTTON_MIDDLE: Int
        get() = raylib.GAMEPAD_BUTTON_MIDDLE
    actual val GAMEPAD_BUTTON_RIGHT_FACE_DOWN: Int
        get() = raylib.GAMEPAD_BUTTON_RIGHT_FACE_DOWN
    actual val GAMEPAD_BUTTON_RIGHT_FACE_RIGHT: Int
        get() = raylib.GAMEPAD_BUTTON_RIGHT_FACE_RIGHT
    actual val KEY_A: Int
        get() = raylib.KEY_A
    actual val KEY_D: Int
        get() = raylib.KEY_D
    actual val KEY_E: Int
        get() = raylib.KEY_E
    actual val KEY_L: Int
        get() = raylib.KEY_L
    actual val KEY_P: Int
        get() = raylib.KEY_P
    actual val KEY_S: Int
        get() = raylib.KEY_S
    actual val KEY_W: Int
        get() = raylib.KEY_W
    actual val MOUSE_BUTTON_LEFT: Int
        get() = raylib.MOUSE_BUTTON_LEFT
    actual val MOUSE_BUTTON_RIGHT: Int
        get() = raylib.MOUSE_BUTTON_RIGHT
    actual val FLAG_WINDOW_UNDECORATED: Int
        get() = raylib.FLAG_WINDOW_UNDECORATED

    actual fun beginDrawing() = raylib.BeginDrawing()
    actual fun clearBackground(color: Color) = raylib.ClearBackground(color.to())
    actual fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Color) =
        raylib.DrawRectangle(x, y, width, height, color.to())

    actual fun drawText(text: String?, posX: Int, posY: Int, fontSize: Int, color: Color) =
        raylib.DrawText(text, posX, posY, fontSize, color.to())

    actual fun endDrawing() = raylib.EndDrawing()
    actual fun getCurrentMonitor() = raylib.GetCurrentMonitor()
    actual fun getGamepadAxisMovement(gamepad: Int, axis: Int): Float = raylib.GetGamepadAxisMovement(gamepad, axis)
    actual fun getMonitorHeight(monitor: Int): Int = raylib.GetMonitorHeight(monitor)
    actual fun getMonitorWidth(monitor: Int): Int = raylib.GetMonitorWidth(monitor)
    actual fun getMousePosition(): Vector2 = raylib.GetMousePosition().to()
    actual fun initAudioDevice() = raylib.InitAudioDevice()
    actual fun initWindow(width: Int, height: Int, title: String?) = raylib.InitWindow(width, height, title)
    actual fun isGamepadAvailable(gamepad: Int) = raylib.IsGamepadAvailable(gamepad)
    actual fun isGamepadButtonDown(button: Int) = raylib.IsMouseButtonDown(button)
    actual fun isKeyDown(key: Int): Boolean = raylib.IsKeyDown(key)
    actual fun isMouseButtonDown(button: Int): Boolean = raylib.IsMouseButtonDown(button)
    actual fun isSoundPlaying(sound: Sound) = raylib.IsSoundPlaying(sound.sound as raylib.Sound)
    actual fun playSound(sound: Sound) = raylib.PlaySound(sound.sound as raylib.Sound)
    actual fun setConfigFlags(flags: Int) = raylib.SetConfigFlags(flags)
    actual fun setTargetFPS(fps: Int) = raylib.SetTargetFPS(fps)
    actual fun setWindowPosition(x: Int, y: Int) = raylib.SetWindowPosition(x, y)
    actual fun setWindowSize(width: Int, height: Int) = raylib.SetWindowSize(width, height)

    actual fun drawTexturePro(
        texture: Texture,
        source: Rectangle,
        dest: Rectangle,
        origin: Vector2,
        rotation: Float,
        tint: Color,
    ) {
        raylib.DrawTexturePro(
            texture.texture as raylib.Texture,
            source.to(),
            dest.to(),
            origin.to(),
            rotation,
            tint.to()
        )
    }

    actual fun windowShouldClose(): Boolean = raylib.WindowShouldClose()

    actual fun loadTexture(name: String): Texture = Texture(raylib.LoadTexture(name))

    actual fun unloadTexture(texture: Texture) = raylib.UnloadTexture(texture.texture as raylib.Texture)

    actual fun loadSound(name: String): Sound = Sound(raylib.LoadSound(name))

    actual fun unloadSound(sound: Sound) = raylib.UnloadSound(sound.sound as raylib.Sound)

    actual fun closeWindow() = raylib.CloseWindow()

    fun raylib.Vector2.to() = Vector2(x(), y())
    fun Vector2.to() = (raylib.Vector2()).x(x).y(y)
    fun Color.to(): Raylib.Color = (Raylib.Color()).r(r.toByte()).g(g.toByte()).b(b.toByte()).a(a.toByte())
    fun Rectangle.to() = (raylib.Rectangle()).x(x).y(y).width(width).height(height)
}