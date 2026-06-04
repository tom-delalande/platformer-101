package game

sealed interface Renderable {
    val mapEntity: MapEntity
    var currentSprite: Sprite.Sprite
}

data class Animation(
    override val mapEntity: MapEntity,
    override var currentSprite: Sprite.Sprite,
    var currentFrame: Int,
    var onFinish: (Animation) -> Unit,
) : Renderable

data class Static(
    override val mapEntity: MapEntity,
    override var currentSprite: Sprite.Sprite,
) : Renderable