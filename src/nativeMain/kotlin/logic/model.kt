package logic

var model = Model()

data class Model(
    val sceneType: SceneType = SceneType.Editor,
    val uiElements: List<UIElement> = listOf(
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
    val selectedElement: UIElement? = null,

    var isMouse1Pressed: Boolean = false,
    var isMouse2Pressed: Boolean = false,

    var wasMouse1Pressed: Boolean = false,
    var wasMouse2Pressed: Boolean = false,

    var mousePositionX: Int = 0,
    var mousePositionY: Int = 0,
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

enum class Entity {
    Background,
    Terrain,
    Player,
    RockHead,
}