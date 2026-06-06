import Engine.executeWithFixedFrameRate
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

        while (!Platform.windowShouldClose()) {
            try {
                executeWithFixedFrameRate {
                    Engine.update()
                    Game.update()
                    Engine.render()
                }
            } catch (_: Engine.CloseGameException) {
                break
            }
        }

        Assets.textures.forEach { Platform.unloadTexture(it.second) }
        Assets.sounds.forEach { Platform.unloadSound(it.second) }
        Platform.closeWindow()
    }
}