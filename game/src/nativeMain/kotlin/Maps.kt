package game

import game.Game.setPlaySpaceOffset
import kotlinx.serialization.json.Json

object Map {
    val maps = listOf(
        "Assets/Maps/1_1.json",
        "Assets/Maps/1_2.json",
        "Assets/Maps/1_3.json",
        "Assets/Maps/1_4.json",
        "Assets/Maps/1_5.json",
        "Assets/Maps/1_6.json",
        "Assets/Maps/1_7.json",
        "Assets/Maps/1_8.json",
        "Assets/Maps/1_9.json",
    )

    fun load() {
        val content = Utils.readTextFile(GameState.currentMap)
        if (content != null) {
            GameState.map = Json.decodeFromString(content)
        } else {
            GameState.map = mutableListOf()
        }
        setPlaySpaceOffset()
    }

    fun save() {
        Utils.writeTextFile(GameState.currentMap, Json.encodeToString(GameState.map))
        println("${GameState.currentMap} saved")
    }
}