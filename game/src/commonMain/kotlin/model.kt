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
    val sprite: Sprite.Sprite = Sprite.sprites[entityType.name]!!,
    val outputPositionXTile: Int,
    val outputPositionYTile: Int,
)

@Serializable
enum class EntityType {
    Background,
    Terrain,
    Player,
    RockHead,
    Finish,
    WoodBox,
    RockBox,
    Strawberry,
    GrassLeft,
    GrassMiddle,
    GrassRight,
    DirtLeft,
    DirtMiddle,
    DirtRight,
}

data class Entity(
    val type: EntityType,
    val isDestroying: Boolean,
)