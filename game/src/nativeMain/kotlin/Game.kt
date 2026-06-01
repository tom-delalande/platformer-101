package game

import kotlin.math.max
import kotlin.math.min


object Game {
    fun init(mapUrl: String, sceneType: SceneType, windowHeight: Int, windowWidth: Int) {
        GameState.sceneType = sceneType
        GameState.windowHeight = windowHeight
        GameState.windowWidth = windowWidth
        GameState.loadMap(mapUrl)
    }

    fun update() {
        when (GameState.sceneType) {
            SceneType.Editor -> {
                if (Input.Mouse1.isNewlyPressed()) {
                    val hit = GameState.uiElements.firstOrNull { ui ->
                        GameState.mousePositionX in ui.outputPositionX..<(ui.outputPositionX + ui.outputWidth) &&
                                GameState.mousePositionY in ui.outputPositionY..<(ui.outputPositionY + ui.outputHeight)
                    }
                    if (hit != null) {
                        GameState.selectedUIElement = hit
                    } else if (GameState.selectedUIElement != null) {
                        val gridX = ((GameState.mousePositionX + GameState.cameraOffsetX) / GameState.TILE_SIZE).let {
                            if (it < 0) {
                                it - 1
                            } else {
                                it
                            }
                        }
                        val gridY =
                            (GameState.windowHeight - GameState.mousePositionY + GameState.TILE_SIZE) / GameState.TILE_SIZE
                        GameState.map += MapEntity(
                            gridPositionX = gridX,
                            gridPositionY = gridY,
                            entity = GameState.selectedUIElement!!.entityType
                        )
                    }
                }
                if (Input.Mouse2.isNewlyPressed()) {
                    val gridX = ((GameState.mousePositionX + GameState.cameraOffsetX) / GameState.TILE_SIZE).let {
                        if (it < 0) {
                            it - 1
                        } else {
                            it
                        }
                    }
                    val gridY =
                        (GameState.windowHeight - GameState.mousePositionY + GameState.TILE_SIZE) / GameState.TILE_SIZE
                    GameState.map = GameState.map.filterNot { it.gridPositionX == gridX && it.gridPositionY == gridY }.toMutableList()
                }

                if (Input.KeyboardS.isNewlyPressed()) {
                    Map.save()
                }
                if (Input.KeyboardL.isNewlyPressed()) {
                    Map.load()
                }
                if (Input.KeyboardP.isNewlyPressed()) {
                    GameState.loadMap(sceneType = SceneType.Play)
                }
                val cameraMoveSpeed = 10
                if (Input.KeyboardD.isPressed()) {
                    GameState.cameraOffsetX += cameraMoveSpeed
                }
                if (Input.KeyboardA.isPressed()) {
                    GameState.cameraOffsetX -= cameraMoveSpeed
                }
            }

            SceneType.Play -> {
                val speed = 2f
                val maxVelocity = 10f
                val friction = 1f

                if (Input.KeyboardD.isPressed()) {
                    GameState.playerVelocityX = min(GameState.playerVelocityX + speed, maxVelocity)
                }
                if (Input.KeyboardA.isPressed()) {
                    GameState.playerVelocityX = max(GameState.playerVelocityX - speed, -maxVelocity)
                }

                GameState.map.toList().map { mapEntity ->
                    Physics.executeIfPlayerIsCollidingWithTile(mapEntity) { _, _, _ ->
                        when (mapEntity.entity) {
                            EntityType.Finish -> {
                                val currentMapIndex = Map.maps.indexOfFirst { it == GameState.currentMap }
                                val nextMap = Map.maps.getOrNull(currentMapIndex + 1)
                                if (nextMap != null) {
                                    GameState.loadMap(nextMap)
                                }
                            }

                            EntityType.Strawberry -> {
                                val renderable = GameState.renderables.first { it.mapEntity == mapEntity }
                                if (renderable !is Animation) return@executeIfPlayerIsCollidingWithTile
                                // THIS IS A BAD WAY TO DO THIS, THESE NEED TO BE TRACKED IN THE ACTUAL ENTITY CLASS
                                // NOT USING SPRITES TO GUESS THE STATE
                                if (renderable.currentSprite == Sprite.sprites["Item_Collected"]!!) return@executeIfPlayerIsCollidingWithTile
                                renderable.currentSprite = Sprite.sprites["Item_Collected"]!!
                                renderable.currentFrame = 0
                                renderable.onFinish = {
                                    GameState.map.remove(mapEntity)
                                    GameState.renderables.remove(renderable)
                                }
                            }

                            else -> {}
                        }
                    }
                }

                val maxJumpVelocity = 30f
                val jumpSpeed = 10f
                val gravity = 6f
                if (Input.KeyboardW.isPressed() && GameState.playerIsJumping) {
                    GameState.playerVelocityY = min(GameState.playerVelocityY + jumpSpeed, maxJumpVelocity)
                }

                if (Input.KeyboardW.isNewlyPressed() && GameState.playerIsGrounded) {
                    GameState.playerIsJumping = true
                    GameState.playerVelocityY = min(GameState.playerVelocityY + jumpSpeed, maxJumpVelocity)
                }

                if (!Input.KeyboardW.isPressed() || GameState.playerVelocityY >= maxJumpVelocity) GameState.playerIsJumping =
                    false


                processTerrainCollisions()

                if (GameState.playerVelocityX > 0) {
                    GameState.playerDirection = 1
                    GameState.playerVelocityX = max(GameState.playerVelocityX - friction, 0f)
                } else if (GameState.playerVelocityX < 0) {
                    GameState.playerDirection = -1
                    GameState.playerVelocityX = min(GameState.playerVelocityX + friction, 0f)
                }
                if (!GameState.playerIsGrounded) {
                    GameState.playerVelocityY -= gravity
                }
                GameState.playerCurrentAnimationFrame += 1
                GameState.backgroundOffsetY -= 1

                if (Input.KeyboardE.isNewlyPressed()) {
                    GameState.sceneType = SceneType.Editor
                }

                // Scroll screen
                if (GameState.playerPositionX > GameState.windowWidth / 2) {
                    GameState.cameraOffsetX = (GameState.playerPositionX - (GameState.windowWidth / 2)).toInt()
                }

            }
        }
        // Animate entities
        GameState.renderables.toList().forEach {
            if (it is Animation) {
                if (it.currentFrame > it.currentSprite.numberOfFrames) it.onFinish(it)
                it.currentFrame += 1
            }
        }
    }

    private fun processTerrainCollisions() {
        // AI-gen: separate X movement from Y movement to prevent edge-landing bug
        GameState.playerPositionX += GameState.playerVelocityX

        // AI-gen: handle collisions using separate X then Y resolution
        val playerEntityType = GameState.map.find { it.entity == EntityType.Player }
        if (playerEntityType != null) {
            val terrainBlocks =
                GameState.map.filter { it.entity == EntityType.Terrain || it.entity == EntityType.WoodBox }
            val tileSize = 64f

            val pWorldX = playerEntityType.gridPositionX * tileSize + GameState.playerPositionX
            val pWorldY = playerEntityType.gridPositionY * tileSize - GameState.playerPositionY
            // AI-gen: resolve X collisions first (no axis-picking ambiguity)
            for (block in terrainBlocks) {

                val playerRight = pWorldX + tileSize
                val playerTop = pWorldY + tileSize

                val blockLeft = block.gridPositionX * tileSize
                val blockRight = blockLeft + tileSize
                val blockBottom = block.gridPositionY * tileSize
                val blockTop = blockBottom + tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerTop > blockBottom && pWorldY < blockTop
                ) {
                    val overlapLeft = playerRight - blockLeft
                    val overlapRight = blockRight - pWorldX
                    if (overlapLeft < overlapRight) {
                        GameState.playerPositionX = blockLeft - tileSize - playerEntityType.gridPositionX * tileSize
                    } else {
                        GameState.playerPositionX = blockRight - playerEntityType.gridPositionX * tileSize
                    }
                    GameState.playerVelocityX = 0f
                }
            }

            // AI-gen: now move Y separately
            GameState.playerPositionY -= GameState.playerVelocityY

            // AI-gen: grounded check (snap Y to block top if within tolerance)
            GameState.playerIsGrounded = false
            val groundedTolerance = 2f
            for (block in terrainBlocks) {
                val pWorldX = playerEntityType.gridPositionX * tileSize + GameState.playerPositionX
                val pWorldY = playerEntityType.gridPositionY * tileSize - GameState.playerPositionY

                val playerRight = pWorldX + tileSize
                val playerBottom = pWorldY

                val blockLeft = block.gridPositionX * tileSize
                val blockRight = blockLeft + tileSize
                val blockTop = block.gridPositionY * tileSize + tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerBottom >= blockTop - groundedTolerance && playerBottom <= blockTop + groundedTolerance
                ) {
                    GameState.playerIsGrounded = true
                    GameState.playerPositionY = playerEntityType.gridPositionY * tileSize - blockTop
                    GameState.playerVelocityY = 0f
                    break
                }
            }

            // AI-gen: resolve Y collisions (only vertical, no more axis-picking)
            for (block in terrainBlocks) {
                val pWorldX = playerEntityType.gridPositionX * tileSize + GameState.playerPositionX
                val pWorldY = playerEntityType.gridPositionY * tileSize - GameState.playerPositionY

                val playerRight = pWorldX + tileSize
                val playerTop = pWorldY + tileSize

                val blockLeft = block.gridPositionX * tileSize
                val blockRight = blockLeft + tileSize
                val blockBottom = block.gridPositionY * tileSize
                val blockTop = blockBottom + tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerTop > blockBottom && pWorldY < blockTop
                ) {
                    val overlapTop = blockTop - pWorldY
                    val overlapBottom = playerTop - blockBottom
                    if (overlapTop < overlapBottom) {
                        GameState.playerPositionY = playerEntityType.gridPositionY * tileSize - blockTop
                        GameState.playerVelocityY = 0f
                        GameState.playerIsGrounded = true
                    } else {
                        GameState.playerPositionY =
                            playerEntityType.gridPositionY * tileSize - (blockBottom - tileSize)
                        GameState.playerVelocityY = 0f
                    }
                }
            }
        }
    }
}
