@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

// ── JS bridge externals ──────────────────────────────────
@JsFun("(w, h, t) => globalThis.platform_initWindow(w, h, t)")
external fun platform_initWindow(w: Int, h: Int, t: String)

@JsFun("() => globalThis.platform_windowShouldClose()")
external fun platform_shouldClose(): Boolean

@JsFun("() => globalThis.platform_closeWindow()")
external fun platform_closeWindow()

@JsFun("(w, h) => globalThis.platform_setWindowSize(w, h)")
external fun platform_setWindowSize(w: Int, h: Int)

@JsFun("(x, y) => globalThis.platform_setWindowPosition(x, y)")
external fun platform_setWindowPosition(x: Int, y: Int)

@JsFun("(f) => globalThis.platform_setConfigFlags(f)")
external fun platform_setConfigFlags(f: Int)

@JsFun("(f) => globalThis.platform_setTargetFPS(f)")
external fun platform_setTargetFPS(f: Int)

@JsFun("() => globalThis.platform_beginDrawing()")
external fun platform_beginDrawing()

@JsFun("() => globalThis.platform_endDrawing()")
external fun platform_endDrawing()

@JsFun("(r,g,b,a) => globalThis.platform_clearBackground(r,g,b,a)")
external fun platform_clearBackground(r: Int, g: Int, b: Int, a: Int)

@JsFun("(x,y,w,h,r,g,b,a) => globalThis.platform_drawRectangle(x,y,w,h,r,g,b,a)")
external fun platform_drawRectangle(x: Int, y: Int, w: Int, h: Int, r: Int, g: Int, b: Int, a: Int)

@JsFun("(t,x,y,f,r,g,b,a) => globalThis.platform_drawText(t,x,y,f,r,g,b,a)")
external fun platform_drawText(t: String?, x: Int, y: Int, f: Int, r: Int, g: Int, b: Int, a: Int)

@JsFun("(i,sx,sy,sw,sh,dx,dy,dw,dh,ox,oy,rt,tr,tg,tb,ta) => globalThis.platform_drawTexturePro(i,sx,sy,sw,sh,dx,dy,dw,dh,ox,oy,rt,tr,tg,tb,ta)")
external fun platform_drawTexturePro(
    texId: Int,
    srcX: Float, srcY: Float, srcW: Float, srcH: Float,
    dstX: Float, dstY: Float, dstW: Float, dstH: Float,
    originX: Float, originY: Float, rotation: Float,
    tintR: Int, tintG: Int, tintB: Int, tintA: Int,
)

@JsFun("(p) => globalThis.platform_loadTexture(p)")
external fun platform_loadTexture(p: String): Int

@JsFun("(id) => globalThis.platform_unloadTexture(id)")
external fun platform_unloadTexture(id: Int)

@JsFun("() => globalThis.platform_initAudio()")
external fun platform_initAudio()

@JsFun("(p) => globalThis.platform_loadSound(p)")
external fun platform_loadSound(p: String): Int

@JsFun("(id) => globalThis.platform_unloadSound(id)")
external fun platform_unloadSound(id: Int)

@JsFun("(id) => globalThis.platform_playSound(id)")
external fun platform_playSound(id: Int)

@JsFun("(id) => globalThis.platform_isSoundPlaying(id)")
external fun platform_isSoundPlaying(id: Int): Boolean

@JsFun("(k) => globalThis.platform_isKeyDown(k)")
external fun platform_isKeyDown(k: Int): Boolean

@JsFun("(b) => globalThis.platform_isMouseButtonDown(b)")
external fun platform_isMouseButtonDown(b: Int): Boolean

@JsFun("() => globalThis.platform_getMouseX()")
external fun platform_getMouseX(): Int

@JsFun("() => globalThis.platform_getMouseY()")
external fun platform_getMouseY(): Int

@JsFun("(g) => globalThis.platform_isGamepadAvailable(g)")
external fun platform_isGamepadAvailable(g: Int): Boolean

@JsFun("(g,b) => globalThis.platform_isGamepadButtonDown(g,b)")
external fun platform_isGamepadButtonDown(g: Int, b: Int): Boolean

@JsFun("(g,a) => globalThis.platform_getGamepadAxis(g,a)")
external fun platform_getGamepadAxis(g: Int, a: Int): Float

@JsFun("() => globalThis.platform_getCurrentMonitor()")
external fun platform_getCurrentMonitor(): Int

@JsFun("(m) => globalThis.platform_getMonitorWidth(m)")
external fun platform_getMonitorWidth(m: Int): Int

@JsFun("(m) => globalThis.platform_getMonitorHeight(m)")
external fun platform_getMonitorHeight(m: Int): Int

@JsFun("(n) => globalThis.platform_getenv(n)")
external fun platform_getenv(n: String): String?

// ── Platform implementation ─────────────────────────────
actual object Platform {
    actual val FLAG_WINDOW_UNDECORATED: Int get() = 0

    actual val GAMEPAD_AXIS_LEFT_X: Int get() = 0
    actual val GAMEPAD_AXIS_LEFT_Y: Int get() = 1

    actual val GAMEPAD_BUTTON_LEFT_FACE_DOWN: Int get() = 5
    actual val GAMEPAD_BUTTON_LEFT_FACE_LEFT: Int get() = 6
    actual val GAMEPAD_BUTTON_LEFT_FACE_RIGHT: Int get() = 7
    actual val GAMEPAD_BUTTON_LEFT_FACE_UP: Int get() = 4
    actual val GAMEPAD_BUTTON_MIDDLE: Int get() = 8
    actual val GAMEPAD_BUTTON_RIGHT_FACE_DOWN: Int get() = 0
    actual val GAMEPAD_BUTTON_RIGHT_FACE_RIGHT: Int get() = 1

    actual val KEY_A: Int get() = 65
    actual val KEY_D: Int get() = 68
    actual val KEY_E: Int get() = 69
    actual val KEY_L: Int get() = 76
    actual val KEY_P: Int get() = 80
    actual val KEY_S: Int get() = 83
    actual val KEY_W: Int get() = 87

    actual val MOUSE_BUTTON_LEFT: Int get() = 0
    actual val MOUSE_BUTTON_RIGHT: Int get() = 1

    actual fun initWindow(width: Int, height: Int, title: String?) =
        platform_initWindow(width, height, title ?: "")

    actual fun windowShouldClose(): Boolean = platform_shouldClose()

    actual fun closeWindow() = platform_closeWindow()

    actual fun setWindowSize(width: Int, height: Int) =
        platform_setWindowSize(width, height)

    actual fun setWindowPosition(x: Int, y: Int) =
        platform_setWindowPosition(x, y)

    actual fun setConfigFlags(flags: Int) = platform_setConfigFlags(flags)

    actual fun setTargetFPS(fps: Int) = platform_setTargetFPS(fps)

    actual fun beginDrawing() = platform_beginDrawing()

    actual fun endDrawing() = platform_endDrawing()

    actual fun clearBackground(color: Color) =
        platform_clearBackground(color.r, color.g, color.b, color.a)

    actual fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Color) =
        platform_drawRectangle(x, y, width, height, color.r, color.g, color.b, color.a)

    actual fun drawText(text: String?, posX: Int, posY: Int, fontSize: Int, color: Color) =
        platform_drawText(text, posX, posY, fontSize, color.r, color.g, color.b, color.a)

    actual fun drawTexturePro(
        texture: Texture,
        source: Rectangle,
        dest: Rectangle,
        origin: Vector2,
        rotation: Float,
        tint: Color,
    ) = platform_drawTexturePro(
        texture.texture as Int,
        source.x, source.y, source.width, source.height,
        dest.x, dest.y, dest.width, dest.height,
        origin.x, origin.y, rotation,
        tint.r, tint.g, tint.b, tint.a,
    )

    actual fun loadTexture(name: String): Texture = Texture(platform_loadTexture(name))

    actual fun unloadTexture(texture: Texture) =
        platform_unloadTexture(texture.texture as Int)

    actual fun initAudioDevice() = platform_initAudio()

    actual fun loadSound(name: String): Sound = Sound(platform_loadSound(name))

    actual fun unloadSound(sound: Sound) = platform_unloadSound(sound.sound as Int)

    actual fun playSound(sound: Sound) = platform_playSound(sound.sound as Int)

    actual fun isSoundPlaying(sound: Sound): Boolean =
        platform_isSoundPlaying(sound.sound as Int)

    actual fun isKeyDown(key: Int): Boolean = platform_isKeyDown(key)

    actual fun isMouseButtonDown(button: Int): Boolean = platform_isMouseButtonDown(button)

    actual fun getMousePosition(): Vector2 =
        Vector2(platform_getMouseX().toFloat(), platform_getMouseY().toFloat())

    actual fun isGamepadAvailable(gamepad: Int): Boolean =
        platform_isGamepadAvailable(gamepad)

    actual fun isGamepadButtonDown(gamepad: Int, button: Int): Boolean =
        platform_isGamepadButtonDown(gamepad, button)

    actual fun getGamepadAxisMovement(gamepad: Int, axis: Int): Float =
        platform_getGamepadAxis(gamepad, axis)

    actual fun getCurrentMonitor(): Int = platform_getCurrentMonitor()

    actual fun getMonitorHeight(monitor: Int): Int = platform_getMonitorHeight(monitor)

    actual fun getMonitorWidth(monitor: Int): Int = platform_getMonitorWidth(monitor)

    actual fun getEnv(name: String): String? = platform_getenv(name)
}
