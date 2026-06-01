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
        "Background" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Background/Yellow.png"),
            numberOfFrames = 1,
            width = 64,
            height = 64,
        ),
        "Terrain" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Terrain/Terrain (16x16).png"),
            numberOfFrames = 1,
            positionX = 98,
            width = 44,
            height = 44,
        ),
        "Player_Idle" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Idle (32x32).png"),
            numberOfFrames = 11,
            width = 32,
            height = 32,
        ),
        "Player_Run" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Run (32x32).png"),
            numberOfFrames = 12,
            width = 32,
            height = 32,
        ),
        "Player_Jump" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Jump (32x32).png"),
            numberOfFrames = 1,
            width = 32,
            height = 32,
        ),
        "Player_Fall" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Main Characters/Mask Dude/Fall (32x32).png"),
            numberOfFrames = 1,
            width = 32,
            height = 32,
        ),
        "RockHead" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Traps/Rock Head/Idle.png"),
            numberOfFrames = 1,
            width = 42,
            height = 42,
        ),
        "Finish" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Items/Checkpoints/End/End (Idle).png"),
            numberOfFrames = 1,
        ),
        "WoodBox" to Sprite(
            texture = LoadTexture("Assets/Pixel Adventure/Terrain/Terrain (16x16).png"),
            numberOfFrames = 1,
            positionY = 64,
            width = 48,
            height = 48,
        ),
    )
}