package game

import game.Game.setPlaySpaceOffset
import kotlinx.serialization.json.Json
import platform.posix.ILL_BADSTK

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
        "Assets/Maps/2_1.json",
        "Assets/Maps/2_2.json",
        "Assets/Maps/2_3.json",
        "Assets/Maps/2_4.json",
        "Assets/Maps/2_5.json",
        "Assets/Maps/2_6.json",
        "Assets/Maps/2_7.json",
        "Assets/Maps/2_8.json",
        "Assets/Maps/2_9.json",
        "Assets/Maps/3_1.json",
        "Assets/Maps/3_2.json",
        "Assets/Maps/3_3.json",
        "Assets/Maps/3_4.json",
        "Assets/Maps/3_5.json",
        "Assets/Maps/3_6.json",
        "Assets/Maps/3_7.json",
        "Assets/Maps/3_8.json",
        "Assets/Maps/3_9.json",
    )

    fun load() {
        val content = Utils.readTextFile(GameState.currentMap)
        if (content != null) {
            GameState.map = Json.decodeFromString(content)
        } else {
            GameState.map = mutableListOf()
        }
    }

    fun save() {
        Utils.writeTextFile(GameState.currentMap, Json.encodeToString(GameState.map))
        println("${GameState.currentMap} saved")
    }
}