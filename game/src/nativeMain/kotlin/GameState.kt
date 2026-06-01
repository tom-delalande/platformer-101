package game

object GameState {
    var sceneType: SceneType = SceneType.Play
    lateinit var currentMap: String
    var uiElements: List<UIElement> = listOf(
        UIElement(
            entity = Entity.Terrain,
            inputX = 96,
            inputWidth = 48,
            inputHeight = 48,
            outputPositionX = 64,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entity = Entity.Player,
            inputWidth = 32,
            inputHeight = 32,
            outputPositionX = 128,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entity = Entity.RockHead,
            inputWidth = 42,
            inputHeight = 42,
            outputPositionX = 192,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entity = Entity.Finish,
            inputWidth = 64,
            inputHeight = 64,
            outputPositionX = 256,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entity = Entity.WoodBox,
            inputX = 0,
            inputY = 64,
            inputWidth = 48,
            inputHeight = 48,
            outputPositionX = 320,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
    )

    var selectedUIElement: UIElement? = null

    var isPressed: List<Input> = emptyList()
    var wasPressed: List<Input> = emptyList()

    var mousePositionX: Int = 0
    var mousePositionY: Int = 0

    var map: List<MapEntity> = emptyList()

    var playerPositionX: Float = 0.0f
    var playerPositionY: Float = 0.0f
    var playerVelocityX: Float = 0.0f
    var playerVelocityY: Float = 0.0f
    var playerIsGrounded: Boolean = true
    var playerIsJumping: Boolean = false
    var playerDirection: Int = 1

    var playerCurrentAnimationFrame: Int = 0

    var backgroundOffsetY: Int = 0

    const val TILE_SIZE = 64
}