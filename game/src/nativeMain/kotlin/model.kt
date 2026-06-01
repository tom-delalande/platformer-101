package game

import kotlinx.serialization.Serializable

@Serializable
data class MapEntity(
    val gridPositionX: Int,
    val gridPositionY: Int,
    val entity: EntityType,
)

enum class SceneType {
    Editor,
    Play
}

data class UIElement(
    val entityType: EntityType,
    val sprite: Sprite.Sprite,
    val outputPositionX: Int,
    val outputPositionY: Int,
    val outputWidth: Int,
    val outputHeight: Int,
)

@Serializable
enum class EntityType {
    Background,
    Terrain,
    Player,
    RockHead,
    Finish,
    WoodBox,
    Strawberry,
}

data class Entity(
    val type: EntityType,
    val isDestroying: Boolean,
)