@file:OptIn(ExperimentalForeignApi::class)

import game.Audio
import game.Sprite
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import raylib.LoadSound
import raylib.LoadTexture
import raylib.Texture2D

private val cachedTextures = mutableMapOf<String, CValue<Texture2D>>()

val textures by lazy {
    Sprite.sprites.map {
        it.value to cachedTextures.getOrPut(it.value.texture) { LoadTexture(it.value.texture) }
    }
}

val engineAudio by lazy {
    Audio.audio.map {
        it.value to LoadSound(it.value.file)
    }
}

fun Sprite.Sprite.toTexture() = textures.find { it.first == this }!!.second
fun Audio.Clip.toEngine() = engineAudio.find { it.first == this }!!.second
