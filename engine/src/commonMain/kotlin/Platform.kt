@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

import kotlin.jvm.JvmInline


data class Color(val r: Int, val g: Int, val b: Int, val a: Int = 255)
data class Vector2(val x: Float, val y: Float)
data class Rectangle(val x: Float, val y: Float, val width: Float, val height: Float)

@JvmInline
value class Sound(val sound: Any)

@JvmInline
value class Texture(val texture: Any)

expect object Platform {
    val FLAG_WINDOW_UNDECORATED: Int
    val GAMEPAD_AXIS_LEFT_X: Int
    val GAMEPAD_AXIS_LEFT_Y: Int
    val GAMEPAD_BUTTON_LEFT_FACE_DOWN: Int
    val GAMEPAD_BUTTON_LEFT_FACE_LEFT: Int
    val GAMEPAD_BUTTON_LEFT_FACE_RIGHT: Int
    val GAMEPAD_BUTTON_LEFT_FACE_UP: Int
    val GAMEPAD_BUTTON_MIDDLE: Int
    val GAMEPAD_BUTTON_RIGHT_FACE_DOWN: Int
    val GAMEPAD_BUTTON_RIGHT_FACE_RIGHT: Int
    val KEY_A: Int
    val KEY_D: Int
    val KEY_E: Int
    val KEY_L: Int
    val KEY_P: Int
    val KEY_S: Int
    val KEY_W: Int
    val MOUSE_BUTTON_LEFT: Int
    val MOUSE_BUTTON_RIGHT: Int
    fun beginDrawing()
    fun clearBackground(color: Color)
    fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Color)
    fun drawText(text: String?, posX: Int, posY: Int, fontSize: Int, color: Color)
    fun endDrawing()
    fun getCurrentMonitor(): Int
    fun getGamepadAxisMovement(gamepad: Int, axis: Int): Float
    fun getMonitorHeight(monitor: Int): Int
    fun getMonitorWidth(monitor: Int): Int
    fun getMousePosition(): Vector2
    fun initAudioDevice()
    fun initWindow(width: Int, height: Int, title: String?)
    fun isGamepadAvailable(gamepad: Int): Boolean
    fun isGamepadButtonDown(gamepad: Int, button: Int): Boolean
    fun isKeyDown(key: Int): Boolean
    fun isMouseButtonDown(button: Int): Boolean
    fun isSoundPlaying(sound: Sound): Boolean
    fun playSound(sound: Sound)
    fun setConfigFlags(flags: Int)
    fun setTargetFPS(fps: Int)
    fun setWindowPosition(x: Int, y: Int)
    fun setWindowSize(width: Int, height: Int)
    fun drawTexturePro(
        texture: Texture,
        source: Rectangle,
        dest: Rectangle,
        origin: Vector2,
        rotation: Float,
        tint: Color,
    )

    fun windowShouldClose(): Boolean
    fun loadTexture(name: String): Texture
    fun unloadTexture(texture: Texture)
    fun loadSound(name: String): Sound
    fun unloadSound(sound: Sound)
    fun closeWindow()
}