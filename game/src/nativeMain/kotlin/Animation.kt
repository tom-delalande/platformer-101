package game

sealed interface Renderable

data class Animation(
    val mapEntity: MapEntity,
    var currentFrame: Int,
    val currentSprite: Sprite.Sprite,
    val onFinish: (Animation) -> Unit,
) : Renderable

data class Static(
    val mapEntity: MapEntity,
    val currentSprite: Sprite.Sprite,
) : Renderable