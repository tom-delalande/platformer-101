@file:OptIn(ExperimentalForeignApi::class)

import game.Game
import game.SceneType
import game.Sprite
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import platform.posix.chdir
import sdl.*

fun platformerMain(argc: Int, argv: CArrayPointer<CPointerVar<ByteVar>>?): Int {
    runBlocking {
        Engine.init()

        val basePath = SDL_GetBasePath()
        if (basePath != null) {
            chdir(basePath.toKString())
            SDL_free(basePath)
        }

        Sprite.init()
        val sceneType = when (getEnv("MODE")) {
            "EDITOR" -> SceneType.Editor
            else -> SceneType.Play
        }

        val mapUrl = when (val map = getEnv("MAP")) {
            is String -> "Assets/Maps/$map.json"
            else -> "Assets/Maps/1_1.json"
        }

        Game.init(mapUrl, sceneType)
        Game.setWindowProperties(Engine.WINDOW_HEIGHT, Engine.WINDOW_WIDTH)

        while (!Engine.windowShouldClose) {
            try {
                Engine.executeWithFixedFrameRate {
                    Engine.processEvents()
                    Engine.update()
                    Game.update()
                    Engine.render()
                }
            } catch (_: Engine.CloseGameException) {
                break
            }
        }

        Assets.textures.forEach { SDL_DestroyTexture(it.second) }
        Assets.sounds.forEach {
            if (it.second.audioBuf != null) SDL_free(it.second.audioBuf)
        }
        SDL_DestroyAudioStream(Engine.audioStream)
        SDL_DestroyRenderer(Engine.renderer)
        SDL_DestroyWindow(Engine.window)
        SDL_Quit()
    }
    return 0
}

fun main() {
    SDL_RunApp(0, null, staticCFunction(::platformerMain), null)
}
