import Engine.executeWithFixedFrameRate
import com.raylib.kmp.Raylib
import game.Game
import game.SceneType
import game.Sprite

object Main {
    suspend fun run() {
        Engine.init()
        Sprite.init()
        val sceneType = when (Platform.getEnv("MODE")) {
            "EDITOR" -> SceneType.Editor
            else -> SceneType.Play
        }

        val mapUrl = when (val map = Platform.getEnv("MAP")) {
            is String -> "Assets/Maps/$map.json"
            else -> "Assets/Maps/1_1.json"
        }

        Game.init(mapUrl, sceneType)
        Game.setWindowProperties(Engine.WINDOW_HEIGHT, Engine.WINDOW_WIDTH)

        while (!Raylib.windowShouldClose()) {
            try {
                    Engine.update()
                    Game.update()
                    Engine.render()
            } catch (_: Engine.CloseGameException) {
                break
            }
        }

        Assets.textures.forEach { Raylib.unloadTexture(it.second) }
        Assets.sounds.forEach { Raylib.unloadSound(it.second) }
        Raylib.closeWindow()
    }
}