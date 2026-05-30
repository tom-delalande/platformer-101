@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import raylib.Texture2D

var engineData = EngineData()

data class EngineData(
    val sprites: Map<String, CValue<Texture2D>> = emptyMap()
)