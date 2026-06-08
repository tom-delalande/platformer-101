import game.Sprite
import kotlinx.cinterop.*
import sdl.*

@OptIn(ExperimentalForeignApi::class)
object Render {
    fun drawSprite(
        sprite: Sprite.Sprite,
        outputPositionX: Float = 0f,
        outputPositionY: Float = 0f,
        outputWidth: Int = sprite.width,
        outputHeight: Int = sprite.height,
        inputXOffset: Int = 0,
        inputYOffset: Float = 0.0f,
        flipHorizontally: Boolean = false,
        currentFrame: Int = 0,
    ) {
        val texture = Assets.fromSprite(sprite)
        memScoped {
            val srcRect = alloc<SDL_FRect>().apply {
                x = (sprite.positionX.toFloat()) + sprite.width * (currentFrame % sprite.numberOfFrames)
                y = sprite.positionY.toFloat()
                w = sprite.width.toFloat()
                h = sprite.height.toFloat()
            }
            val dstRect = alloc<SDL_FRect>().apply {
                x = outputPositionX - (inputXOffset % outputWidth)
                y = outputPositionY - (inputYOffset % outputHeight)
                w = outputWidth.toFloat()
                h = outputHeight.toFloat()
            }
            val flip = if (flipHorizontally) SDL_FLIP_HORIZONTAL else SDL_FLIP_NONE
            SDL_RenderTextureRotated(
                Engine.renderer,
                texture,
                srcRect.ptr,
                dstRect.ptr,
                0.0,
                null,
                flip,
            )
        }
    }
}
