import com.raylib.kmp.Color
import com.raylib.kmp.Raylib
import com.raylib.kmp.Rectangle
import com.raylib.kmp.Vector2
import game.Sprite

object Render {
    fun drawSprite(
        sprite: Sprite.Sprite,
        outputPositionX: Float = 0f,
        outputPositionY: Float = 0f,
        outputWidth: Int = sprite.width,
        outputHeight: Int = sprite.height,
        tint: Color = Color(255, 255, 255),
        inputXOffset: Int = 0,
        inputYOffset: Float = 0.0f,
        flipHorizontally: Boolean = false,
        currentFrame: Int = 0,
    ) {
        Raylib.drawTexturePro(
            texture = Assets.fromSprite(sprite),
            srcrec = Rectangle(
                x = (sprite.positionX.toFloat() + inputXOffset) + sprite.width * (currentFrame % sprite.numberOfFrames),
                y = (sprite.positionY + inputYOffset),
                width = if (flipHorizontally) sprite.width.toFloat() * -1 else sprite.width.toFloat(),
                height = sprite.height.toFloat(),
            ),
            dstrec = Rectangle(
                x = outputPositionX,
                y = outputPositionY,
                width = outputWidth.toFloat(),
                height = outputHeight.toFloat(),
            ),
            origin = Vector2(
                x = 0f,
                y = 0f,
            ),
            rotation = 0.0f,
            tint = tint,
        )
    }
}