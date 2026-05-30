@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import logic.Entity
import raylib.Texture2D

var engineData = EngineData()

data class EngineData(
    val sprites: Map<Entity, CValue<Texture2D>> = emptyMap(),
    val windowWidth: Int = 800,
    val windowHeight: Int = 600,
)