package game

import kotlin.math.max
import kotlin.math.min
import kotlinx.serialization.json.Json


object Game {
    fun init(mapUrl: String, sceneType: SceneType) {
        GameState.sceneType = sceneType
        GameState.currentMap = mapUrl
        Map.load()
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
                        val gridX = GameState.mousePositionX / 64
                        val gridY = GameState.mousePositionY / 64
                        GameState.map += MapEntity(
                            gridPositionX = gridX,
                            gridPositionY = gridY,
                            entity = GameState.selectedUIElement!!.entity
                        )
                    }
                }
                if (Input.Mouse2.isNewlyPressed()) {
                    val gridX = GameState.mousePositionX / 64
                    val gridY = GameState.mousePositionY / 64
                    GameState.map = GameState.map.filterNot { it.gridPositionX == gridX && it.gridPositionY == gridY }
                }

                if (Input.KeyboardS.isNewlyPressed()) {
                    Map.save()
                }
                if (Input.KeyboardL.isNewlyPressed()) {
                    Map.load()
                }
                if (Input.KeyboardP.isNewlyPressed()) {
                    GameState.sceneType = SceneType.Play
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

                GameState.map.map { mapEntity ->
                    Physics.executeIfPlayerIsCollidingWithTile(mapEntity) { _, _, _ ->
                        when (mapEntity.entity) {
                            Entity.Finish -> {
                                val currentMapIndex = Map.maps.indexOfFirst { it == GameState.currentMap }
                                val nextMap = Map.maps.getOrNull(currentMapIndex + 1)
                                if (nextMap != null) {
                                    GameState.currentMap = nextMap
                                    Map.load()
                                    GameState.playerPositionX = 0f
                                    GameState.playerPositionY = 0f
                                    GameState.playerVelocityX = 0f
                                    GameState.playerVelocityY = 0f
                                    GameState.playerIsGrounded = true
                                    GameState.playerIsJumping = false
                                    GameState.playerDirection = 1
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
            }

        }
    }

    private fun processTerrainCollisions() {
        // AI-gen: separate X movement from Y movement to prevent edge-landing bug
        GameState.playerPositionX += GameState.playerVelocityX

        // AI-gen: handle collisions using separate X then Y resolution
        val playerEntity = GameState.map.find { it.entity == Entity.Player }
        if (playerEntity != null) {
            val terrainBlocks =
                GameState.map.filter { it.entity == Entity.Terrain || it.entity == Entity.WoodBox }
            val tileSize = 64f

            val pWorldX = playerEntity.gridPositionX * tileSize + GameState.playerPositionX
            val pWorldY = playerEntity.gridPositionY * tileSize - GameState.playerPositionY
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
                        GameState.playerPositionX = blockLeft - tileSize - playerEntity.gridPositionX * tileSize
                    } else {
                        GameState.playerPositionX = blockRight - playerEntity.gridPositionX * tileSize
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
                val pWorldX = playerEntity.gridPositionX * tileSize + GameState.playerPositionX
                val pWorldY = playerEntity.gridPositionY * tileSize - GameState.playerPositionY

                val playerRight = pWorldX + tileSize
                val playerBottom = pWorldY

                val blockLeft = block.gridPositionX * tileSize
                val blockRight = blockLeft + tileSize
                val blockTop = block.gridPositionY * tileSize + tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerBottom >= blockTop - groundedTolerance && playerBottom <= blockTop + groundedTolerance
                ) {
                    GameState.playerIsGrounded = true
                    GameState.playerPositionY = playerEntity.gridPositionY * tileSize - blockTop
                    GameState.playerVelocityY = 0f
                    break
                }
            }

            // AI-gen: resolve Y collisions (only vertical, no more axis-picking)
            for (block in terrainBlocks) {
                val pWorldX = playerEntity.gridPositionX * tileSize + GameState.playerPositionX
                val pWorldY = playerEntity.gridPositionY * tileSize - GameState.playerPositionY

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
                        GameState.playerPositionY = playerEntity.gridPositionY * tileSize - blockTop
                        GameState.playerVelocityY = 0f
                        GameState.playerIsGrounded = true
                    } else {
                        GameState.playerPositionY =
                            playerEntity.gridPositionY * tileSize - (blockBottom - tileSize)
                        GameState.playerVelocityY = 0f
                    }
                }
            }
        }
    }
}
