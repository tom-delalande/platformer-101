package game

import kotlin.collections.Map

object Sprite {
    data class Sprite(
        val texture: String,
        val numberOfFrames: Int = 1,
        val positionX: Int = 0,
        val positionY: Int = 0,
        val width: Int = 64,
        val height: Int = 64,
    )

    var sprites: Map<String, Sprite> = emptyMap()

    fun init() {
        sprites = mapOf(
            "End_Idle" to Sprite(
                texture = "Assets/Pixel Adventure/Items/Checkpoints/End/End (Idle).png",
                numberOfFrames = 1,
                width = 64,
                height = 64,
            ),
            "Keyboard_W" to Sprite(
                texture = "Assets/Inputs/Keyboard_Mouse/Dark/T_W_Key_Dark.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Keyboard_A" to Sprite(
                texture = "Assets/Inputs/Keyboard_Mouse/Dark/T_A_Key_Dark.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Keyboard_S" to Sprite(
                texture = "Assets/Inputs/Keyboard_Mouse/Dark/T_S_Key_Dark.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Keyboard_D" to Sprite(
                texture = "Assets/Inputs/Keyboard_Mouse/Dark/T_D_Key_Dark.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_A" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_A.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_B" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_B.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_Left" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_Dpad_Left.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_Right" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_Dpad_Right.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_Up" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_Dpad_Up.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Switch_Down" to Sprite(
                texture = "Assets/Inputs/SGamepad/Default/T_S_Dpad_Down.png",
                numberOfFrames = 1,
                width = 128,
                height = 128,
            ),
            "Background" to Sprite(
                texture = "Assets/Pixel Adventure/Background/Yellow.png",
                numberOfFrames = 1,
                width = 64,
                height = 64,
            ),
            "Terrain" to Sprite(
                texture = "Assets/Pixel Adventure/Terrain/Terrain (16x16).png",
                numberOfFrames = 1,
                positionX = 98,
                width = 44,
                height = 44,
            ),
            "Player_Idle" to Sprite(
                texture = "Assets/Pixel Adventure/Main Characters/Mask Dude/Idle (32x32).png",
                numberOfFrames = 11,
                width = 32,
                height = 32,
            ),
            "Player_Run" to Sprite(
                texture = "Assets/Pixel Adventure/Main Characters/Mask Dude/Run (32x32).png",
                numberOfFrames = 12,
                width = 32,
                height = 32,
            ),
            "Player_Jump" to Sprite(
                texture = "Assets/Pixel Adventure/Main Characters/Mask Dude/Jump (32x32).png",
                numberOfFrames = 1,
                width = 32,
                height = 32,
            ),
            "Player_Fall" to Sprite(
                texture = "Assets/Pixel Adventure/Main Characters/Mask Dude/Fall (32x32).png",
                numberOfFrames = 1,
                width = 32,
                height = 32,
            ),
            "RockHead" to Sprite(
                texture = "Assets/Pixel Adventure/Traps/Rock Head/Idle.png",
                numberOfFrames = 1,
                width = 42,
                height = 42,
            ),
            "Finish" to Sprite(
                texture = "Assets/Pixel Adventure/Items/Checkpoints/End/End (Idle).png",
                numberOfFrames = 1,
            ),
            "WoodBox" to Sprite(
                texture = "Assets/Pixel Adventure/Terrain/Terrain (16x16).png",
                numberOfFrames = 1,
                positionY = 64,
                width = 48,
                height = 48,
            ),
            "Strawberry" to Sprite(
                texture = "Assets/Pixel Adventure/Items/Fruits/Strawberry.png",
                numberOfFrames = 17,
                width = 32,
                height = 32,
            ),
            "Item_Collected" to Sprite(
                texture = "Assets/Pixel Adventure/Items/Fruits/Collected.png",
                numberOfFrames = 6,
                width = 32,
                height = 32,
            ),
        )
    }
}