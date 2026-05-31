@file:OptIn(ExperimentalForeignApi::class)

package engine

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import raylib.LoadTexture
import raylib.Texture2D

data class Sprite(
    val texture: CValue<Texture2D>,
    val numberOfFrames: Int = 1,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val width: Int = 64,
    val height: Int = 64,
)

val sprites: Map<String, Sprite> by lazy {
    mapOf(
        "End_Idle" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Items/Checkpoints/End/End (Idle).png"),
            numberOfFrames = 1,
            width = 64,
            height = 64,
        ),
        "Keyboard_W" to Sprite(
            texture = LoadTexture("Assets/Inputs/Keyboard_Mouse/Dark/T_W_Key_Dark.png"),
            numberOfFrames = 1,
            width = 128,
            height = 128,
        ),
        "Keyboard_A" to Sprite(
            texture = LoadTexture("Assets/Inputs/Keyboard_Mouse/Dark/T_A_Key_Dark.png"),
            numberOfFrames = 1,
            width = 128,
            height = 128,
        ),
        "Keyboard_S" to Sprite(
            texture = LoadTexture("Assets/Inputs/Keyboard_Mouse/Dark/T_S_Key_Dark.png"),
            numberOfFrames = 1,
            width = 128,
            height = 128,
        ),
        "Keyboard_D" to Sprite(
            texture = LoadTexture("Assets/Inputs/Keyboard_Mouse/Dark/T_D_Key_Dark.png"),
            numberOfFrames = 1,
            width = 128,
            height = 128,
        ),
    )
}