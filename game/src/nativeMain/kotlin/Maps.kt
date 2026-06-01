package game

import kotlinx.serialization.json.Json

object Map {
    val maps = listOf(
        "Assets/Maps/1_1.json",
        "Assets/Maps/1_2.json",
    )

    fun load() {
        val content = Utils.readTextFile(GameState.currentMap)
        if (content != null) {
            GameState.map = Json.decodeFromString(content)
            println("${GameState.currentMap} loaded")
        }
    }

    fun save() {
        Utils.writeTextFile(GameState.currentMap, Json.encodeToString(GameState.map))
        println("${GameState.currentMap} saved")
    }
}