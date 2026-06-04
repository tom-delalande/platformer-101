package game

import kotlin.collections.Map

object Audio {
    data class Clip(
        val file: String,
    )

    val audio: Map<String, Clip> by lazy {
        mapOf(
            "Jump" to Clip("Assets/Audio/Jump.wav"),
            "Pickup" to Clip("Assets/Audio/Coin.wav"),
            "Walk" to Clip("Assets/Audio/Walk.wav"),
            "Door" to Clip("Assets/Audio/Door.wav"),
            "Fall" to Clip("Assets/Audio/Hurt.wav"),
        )
    }
}