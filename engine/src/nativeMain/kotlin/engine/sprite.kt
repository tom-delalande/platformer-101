@file:OptIn(ExperimentalForeignApi::class)

package engine

import game.Sprite
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import raylib.LoadTexture
import raylib.Texture2D

data class EngineSprite(
    val texture: CValue<Texture2D>,
    val sprite: Sprite.Sprite,
)

val textures by lazy {
    Sprite.sprites.map {
        it.value to LoadTexture(it.value.texture)
    }
}

fun Sprite.Sprite.toTexture() = textures.find { it.first == this }!!.second