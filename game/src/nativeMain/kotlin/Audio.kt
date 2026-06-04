package game

import kotlin.collections.Map

object Audio {
    data class Clip(
        val file: String,
    )

    val audio: Map<String, Clip> by lazy {
        mapOf(
            "Jump" to Clip("Assets/Audio/jump.wav"),
            "Pickup" to Clip("Assets/Audio/Pickup Coin3.wav"),
            "Walk" to Clip("Assets/Audio/walk 2 medium.wav"),
            "Door" to Clip("Assets/Audio/door open.wav"),
            "Fall" to Clip("Assets/Audio/fall.wav"),
        )
    }
}