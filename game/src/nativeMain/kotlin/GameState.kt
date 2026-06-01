package game

object GameState {
    var sceneType: SceneType = SceneType.Play
    lateinit var currentMap: String
    var windowHeight: Int = 600
    var windowWidth: Int = 800
    var uiElements: List<UIElement> = listOf(
        UIElement(
            entityType = EntityType.Terrain,
            sprite = Sprite.sprites["Terrain"]!!,
            outputPositionX = 64,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entityType = EntityType.Player,
            sprite = Sprite.sprites["Player_Idle"]!!,
            outputPositionX = 128,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entityType = EntityType.RockHead,
            sprite = Sprite.sprites["RockHead"]!!,
            outputPositionX = 192,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entityType = EntityType.Finish,
            sprite = Sprite.sprites["Finish"]!!,
            outputPositionX = 256,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entityType = EntityType.WoodBox,
            sprite = Sprite.sprites["WoodBox"]!!,
            outputPositionX = 320,
            outputPositionY = 0,
            outputWidth = 64,
            outputHeight = 64,
        ),
        UIElement(
            entityType = EntityType.Strawberry,
            sprite = Sprite.sprites["Strawberry"]!!,
            outputPositionX = 384,
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

    var map: MutableList<MapEntity> = mutableListOf()

    var playerPositionX: Float = 0.0f
    var playerPositionY: Float = 0.0f
    var playerVelocityX: Float = 0.0f
    var playerVelocityY: Float = 0.0f
    var playerIsGrounded: Boolean = true
    var playerIsJumping: Boolean = false
    var playerDirection: Int = 1

    var playerCurrentAnimationFrame: Int = 0

    var backgroundOffsetY: Int = 0

    var cameraOffsetX: Int = 0

    const val TILE_SIZE = 64
    var renderables: MutableList<Renderable> = mutableListOf()

    fun loadMap(nextMap: String = currentMap, sceneType: SceneType = GameState.sceneType) {
        GameState.sceneType = sceneType
        currentMap = nextMap
        Map.load()
        playerPositionX = 0f
        playerPositionY = 0f
        playerVelocityX = 0f
        playerVelocityY = 0f
        playerIsGrounded = true
        playerIsJumping = false
        playerDirection = 1
        cameraOffsetX = 0

        renderables = map.mapNotNull { mapEntity ->
            when (mapEntity.entity) {
                EntityType.Strawberry -> {
                    Animation(
                        mapEntity = mapEntity,
                        currentFrame = 0,
                        currentSprite = Sprite.sprites["Strawberry"]!!,
                        onFinish = {
                            it.currentFrame = 0
                        }
                    )
                }

                EntityType.Background -> Static(mapEntity, Sprite.sprites["Background"]!!)
                EntityType.Terrain -> Static(mapEntity, Sprite.sprites["Terrain"]!!)
                EntityType.Player -> when (sceneType) {
                    SceneType.Editor -> Animation(
                        mapEntity = mapEntity,
                        currentFrame = 0,
                        currentSprite = Sprite.sprites["Player_Idle"]!!,
                        onFinish = {
                            it.currentFrame = 0
                        }
                    )
                    SceneType.Play -> null
                }
                EntityType.RockHead -> Static(mapEntity, Sprite.sprites["RockHead"]!!)
                EntityType.Finish -> Static(mapEntity, Sprite.sprites["Finish"]!!)
                EntityType.WoodBox -> Static(mapEntity, Sprite.sprites["WoodBox"]!!)
            }
        }.toMutableList()
    }
}