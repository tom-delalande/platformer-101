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
        inputYOffset: Int = 0,
        flipHorizontally: Boolean = false,
        currentFrame: Int = 0,
    ) {
        Platform.drawTexturePro(
            texture = Assets.fromSprite(sprite),
            source = Rectangle(
                x = (sprite.positionX.toFloat() + inputXOffset) + sprite.width * (currentFrame % sprite.numberOfFrames),
                y = (sprite.positionY + inputYOffset).toFloat(),
                width = if (flipHorizontally) sprite.width.toFloat() * -1 else sprite.width.toFloat(),
                height = sprite.height.toFloat(),
            ),
            dest = Rectangle(
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