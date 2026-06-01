@file:OptIn(ExperimentalForeignApi::class)

import engine.color
import engine.toTexture
import game.Sprite
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import raylib.Color
import raylib.DrawTexturePro
import raylib.Rectangle
import raylib.Vector2

object Render {
    fun drawSprite(
        sprite: Sprite.Sprite,
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
            texture = sprite.toTexture(),
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