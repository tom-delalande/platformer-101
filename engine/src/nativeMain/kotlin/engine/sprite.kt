@file:OptIn(ExperimentalForeignApi::class)

package engine

import game.Audio
import game.Sprite
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import raylib.LoadSound
import raylib.LoadTexture
import raylib.LoadWave
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

val engineAudio by lazy {
    Audio.audio.map {
        it.value to LoadSound(it.value.file)
    }
}

fun Sprite.Sprite.toTexture() = textures.find { it.first == this }!!.second
fun Audio.Clip.toEngine() = engineAudio.find { it.first == this }!!.second
