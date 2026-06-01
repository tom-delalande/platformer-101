@file:OptIn(ExperimentalForeignApi::class)

import engine.Sprite
import engine.color
import engine.sprites
import game.Entity
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import raylib.Color
import raylib.DrawTexturePro
import raylib.Rectangle
import raylib.Vector2

object Render {
    // Probably a game function
    fun Entity.toDefaultSprite() = when (this) {
        Entity.Background -> sprites["Background"]!!
        Entity.Terrain -> sprites["Terrain"]!!
        Entity.Player -> sprites["Player_Idle"]!!
        Entity.RockHead -> sprites["RockHead"]!!
        Entity.Finish -> sprites["Finish"]!!
        Entity.WoodBox -> sprites["WoodBox"]!!
    }

    fun drawSprite(
        sprite: Sprite,
        outputPositionX: Float = 0f,
        outputPositionY: Float = 0f,
        outputWidth: Int = sprite.width,
        outputHeight: Int = sprite.height,
        tint: CValue<Color> = color(255, 255, 255),
        inputXOffset: Int = 0,
        inputYOffset: Int = 0,
        flipHorizontally: Boolean = false,
        currentFrame: Int = 0,
    ) {
        DrawTexturePro(
            texture = sprite.texture,
            source = cValue<Rectangle> {
                x = (sprite.positionX.toFloat() + inputXOffset) + sprite.width * (currentFrame % sprite.numberOfFrames)
                y = (sprite.positionY + inputYOffset).toFloat()
                width = if (flipHorizontally) sprite.width.toFloat() * -1 else sprite.width.toFloat()
                height = sprite.height.toFloat()
            },
            dest = cValue<Rectangle> {
                x = outputPositionX
                y = outputPositionY
                width = outputWidth.toFloat()
                height = outputHeight.toFloat()
            },
            origin = cValue<Vector2> {
                x = 0f
                y = 0f
            },
            rotation = 0.0f,
            tint = tint,
        )
    }
}