package logic

import kotlinx.serialization.Serializable

var model = Model()

data class Model(
    var sceneType: SceneType = SceneType.Editor,
    var uiElements: List<UIElement> = listOf(
        UIElement(
            sprite = Entity.Terrain,
            inputX = 96,
            inputWidth = 48,
            inputHeight = 48,
            outputPositionX = 64,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            sprite = Entity.Player,
            inputWidth = 32,
            inputHeight = 32,
            outputPositionX = 128,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            sprite = Entity.RockHead,
            inputWidth = 42,
            inputHeight = 42,
            outputPositionX = 192,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
    ),
    var selectedUIElement: UIElement? = null,

    var isPressed: List<Input> = emptyList(),
    var wasPressed: List<Input> = emptyList(),

    var mousePositionX: Int = 0,
    var mousePositionY: Int = 0,

    var map: List<MapEntity> = emptyList(),

    var playerPositionX: Float = 0.0f,
    var playerPositionY: Float = 0.0f,
    var playerVelocityX: Float = 0.0f,
    var playerVelocityY: Float = 0.0f,
)

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
    val sprite: Entity,
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
}

fun Entity.getSpriteData() = when (this) {
    Entity.Terrain -> SpriteData(
        inputX = 96,
        inputY = 0,
        inputWidth = 48,
        inputHeight = 48,
    )
    Entity.Player -> SpriteData(
        inputX = 0,
        inputY = 0,
        inputWidth = 32,
        inputHeight = 32,
    )
    Entity.RockHead -> SpriteData(
        inputX = 0,
        inputY = 0,
        inputWidth = 42,
        inputHeight = 42,
    )

    else -> SpriteData()
}

data class SpriteData(
    val inputX: Int = 0,
    val inputY: Int = 0,
    val inputWidth: Int = 64,
    val inputHeight: Int = 64,
)

enum class Input {
    Mouse1,
    Mouse2,
    KeyboardS,
    KeyboardL,
    KeyboardP,
    KeyboardE,

    KeyboardW,
    KeyboardA,
    KeyboardD,
}