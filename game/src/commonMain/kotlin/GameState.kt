package game

import game.Game.setPlaySpaceOffset

object GameState {
    var sceneType: SceneType = SceneType.Play
    lateinit var currentMap: String
    var windowHeight: Int = 600
    var windowWidth: Int = 800
    var tileSize: Int = 64
    var playSpaceOffsetX: Int = 0
    var playSpaceOffsetY: Int = 0

    const val SIZE_Y_IN_TILES = 12

    var uiElements: List<UIElement> = listOf(
        UIElement(
            entityType = EntityType.Player,
            sprite = Sprite.sprites["Player_Idle"]!!,
            outputPositionXTile = 2,
            outputPositionYTile = 2,
        ),
        UIElement(
            entityType = EntityType.RockHead,
            sprite = Sprite.sprites["RockHead"]!!,
            outputPositionXTile = 3,
            outputPositionYTile = 2,
        ),
        UIElement(
            entityType = EntityType.Finish,
            sprite = Sprite.sprites["Finish"]!!,
            outputPositionXTile = 4,
            outputPositionYTile = 2,
        ),
        UIElement(
            entityType = EntityType.WoodBox,
            sprite = Sprite.sprites["WoodBox"]!!,
            outputPositionXTile = 5,
            outputPositionYTile = 2,
        ),
        UIElement(
            entityType = EntityType.Strawberry,
            sprite = Sprite.sprites["Strawberry"]!!,
            outputPositionXTile = 6,
            outputPositionYTile = 2,
        ),
        UIElement(
            entityType = EntityType.GrassLeft,
            outputPositionXTile = 1,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.GrassMiddle,
            outputPositionXTile = 2,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.GrassRight,
            outputPositionXTile = 3,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.DirtLeft,
            outputPositionXTile = 4,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.DirtMiddle,
            outputPositionXTile = 5,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.DirtRight,
            outputPositionXTile = 6,
            outputPositionYTile = 3,
        ),
        UIElement(
            entityType = EntityType.RockBox,
            outputPositionXTile = 8,
            outputPositionYTile = 3,
        ),
    )

    var selectedUIElement: UIElement? = null

    var isPressed: List<Input> = emptyList()
    var wasPressed: List<Input> = emptyList()

    var mousePositionX: Int = 0
    var mousePositionY: Int = 0

    var map: MutableList<MapEntity> = mutableListOf()
    var maxXTile: Int = 0
    var minXTile: Int = 0
    var maxYTile: Int = 0
    var minYTile: Int = 0

    var playerPositionXOffsetInTiles: Float = 0.0f
    var playerPositionYOffsetInTiles: Float = 0.0f
    var playerVelocityXInTiles: Float = 0.0f
    var playerVelocityYInTiles: Float = 0.0f
    var playerIsGrounded: Boolean = true
    var playerIsJumping: Boolean = false
    var playerDirection: Int = 1

    var maxJumpVelocity = 37f / 64
    var jumpSpeed = 12f / 64
    var gravity = 6f / 64

    var playerCurrentAnimationFrame: Int = 0

    var backgroundOffsetY: Float = 0.0f

    var cameraOffsetX: Int = 0

    var renderables: MutableList<Renderable> = mutableListOf()

    var sounds: MutableList<Audio.Clip> = mutableListOf()

    fun loadMap(nextMap: String = currentMap, sceneType: SceneType = GameState.sceneType) {
        GameState.sceneType = sceneType
        currentMap = nextMap
        Map.load()
        playerPositionXOffsetInTiles = 0f
        playerPositionYOffsetInTiles = 0f
        playerVelocityXInTiles = 0f
        playerVelocityYInTiles = 0f
        playerIsGrounded = true
        playerIsJumping = false
        playerDirection = 1
        cameraOffsetX = 0
        playerEntity = map.find { it.entity == EntityType.Player }
        if (map.isNotEmpty()) {
            maxXTile = map.maxOf { it.gridPositionX }
            minXTile = map.minOf { it.gridPositionX }
            maxYTile = map.maxOf { it.gridPositionY }
            minYTile = map.minOf { it.gridPositionY }
        }
        initialiseRenderables()
        setPlaySpaceOffset()
    }

    var playerEntity: MapEntity? = null

    val playerWorldX: Float
        get() = playerEntity?.let { (it.gridPositionX + playerPositionXOffsetInTiles) * tileSize } ?: 0.0f
    val playerWorldY: Float
        get() = playerEntity?.let { (playerEntity!!.gridPositionY - playerPositionYOffsetInTiles) * tileSize }
            ?: 0.0f

    fun initialiseRenderables() {
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
                else -> Static(mapEntity, Sprite.sprites[mapEntity.entity.name]!!)
            }
        }.toMutableList()
    }

    fun autoLoadNextMap() {
        val currentMapIndex = Map.maps.indexOfFirst { it == currentMap }
        val nextMap = Map.maps.getOrNull(currentMapIndex + 1)
        if (nextMap != null) {
            loadMap(nextMap)
        }
    }

    fun autoLoadPrevMap() {
        val currentMapIndex = Map.maps.indexOfFirst { it == currentMap }
        val nextMap = Map.maps.getOrNull(currentMapIndex - 1)
        if (nextMap != null) {
            loadMap(nextMap)
        }
    }

    fun playSound(name: String) {
        sounds.add(Audio.audio[name]!!)
    }
}