package game

import kotlinx.serialization.Serializable

@Serializable
data class MapEntity(
    val gridPositionX: Int,
    val gridPositionY: Int,
    val entity: Entity,
)

enum class SceneType {
    Editor,
    Play
}

data class UIElement(
    val entity: Entity,
    val inputX: Int = 0,
    val inputY: Int = 0,
    val inputWidth: Int,
    val inputHeight: Int,
    val outputPositionX: Int,
    val outputPositionY: Int,
    val outputWidth: Int,
    val outputHeight: Int,
)

@Serializable
enum class Entity {
    Background,
    Terrain,
    Player,
    RockHead,
    Finish,
    WoodBox,
}

