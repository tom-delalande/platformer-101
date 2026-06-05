@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "UNCHECKED_CAST")
@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import raylib.CloseWindow

actual object Platform {
    actual val GAMEPAD_AXIS_LEFT_X: Int
        get() = raylib.GAMEPAD_AXIS_LEFT_X.toInt()
    actual val GAMEPAD_AXIS_LEFT_Y: Int
        get() = raylib.GAMEPAD_AXIS_LEFT_Y.toInt()
    actual val GAMEPAD_BUTTON_LEFT_FACE_DOWN: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_DOWN.toInt()
    actual val GAMEPAD_BUTTON_LEFT_FACE_LEFT: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_LEFT.toInt()
    actual val GAMEPAD_BUTTON_LEFT_FACE_RIGHT: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_RIGHT.toInt()
    actual val GAMEPAD_BUTTON_LEFT_FACE_UP: Int
        get() = raylib.GAMEPAD_BUTTON_LEFT_FACE_UP.toInt()
    actual val GAMEPAD_BUTTON_MIDDLE: Int
        get() = raylib.GAMEPAD_BUTTON_MIDDLE.toInt()
    actual val GAMEPAD_BUTTON_RIGHT_FACE_DOWN: Int
        get() = raylib.GAMEPAD_BUTTON_RIGHT_FACE_DOWN.toInt()
    actual val GAMEPAD_BUTTON_RIGHT_FACE_RIGHT: Int
        get() = raylib.GAMEPAD_BUTTON_RIGHT_FACE_RIGHT.toInt()
    actual val KEY_A: Int
        get() = raylib.KEY_A.toInt()
    actual val KEY_D: Int
        get() = raylib.KEY_D.toInt()
    actual val KEY_E: Int
        get() = raylib.KEY_E.toInt()
    actual val KEY_L: Int
        get() = raylib.KEY_L.toInt()
    actual val KEY_P: Int
        get() = raylib.KEY_P.toInt()
    actual val KEY_S: Int
        get() = raylib.KEY_S.toInt()
    actual val KEY_W: Int
        get() = raylib.KEY_W.toInt()
    actual val MOUSE_BUTTON_LEFT: Int
        get() = raylib.MOUSE_BUTTON_LEFT.toInt()
    actual val MOUSE_BUTTON_RIGHT: Int
        get() = raylib.MOUSE_BUTTON_RIGHT.toInt()
    actual val FLAG_WINDOW_UNDECORATED: Int
        get() = raylib.FLAG_WINDOW_UNDECORATED.toInt()

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
    actual fun isSoundPlaying(sound: Sound) = raylib.IsSoundPlaying(sound.sound as CValue<raylib.Sound>)
    actual fun playSound(sound: Sound) = raylib.PlaySound(sound.sound as CValue<raylib.Sound>)
    actual fun setConfigFlags(flags: Int) = raylib.SetConfigFlags(flags.toUInt())
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
            texture.texture as CValue<raylib.Texture>,
            source.to(),
            dest.to(),
            origin.to(),
            rotation,
            tint.to()
        )
    }

    actual fun windowShouldClose(): Boolean = raylib.WindowShouldClose()

    actual fun loadTexture(name: String): Texture = Texture(raylib.LoadTexture(name))

    actual fun unloadTexture(texture: Texture) = raylib.UnloadTexture(texture.texture as CValue<raylib.Texture2D>)

    actual fun loadSound(name: String): Sound = Sound(raylib.LoadSound(name))

    actual fun unloadSound(sound: Sound) = raylib.UnloadSound(sound.sound as CValue<raylib.Sound>)

    actual fun closeWindow() = CloseWindow()
}

fun CValue<raylib.Vector2>.to() = this.useContents { Vector2(x, y) }

fun color(r: Int, g: Int, b: Int, a: Int = 255) = cValue<raylib.Color> {
    this.r = r.toUByte()
    this.g = g.toUByte()
    this.b = b.toUByte()
    this.a = a.toUByte()
}

fun Color.to() = color(r, g, b, a)

fun Rectangle.to() = cValue<raylib.Rectangle> {
    this.x = this@to.x
    this.y = this@to.y
    this.width = this@to.width
    this.height = this@to.height
}

fun Vector2.to() = cValue<raylib.Vector2> {
    this.x = this@to.x
    this.y = this@to.y
}
