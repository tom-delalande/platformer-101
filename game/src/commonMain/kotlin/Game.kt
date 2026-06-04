package game

import game.GameState.maxXTile
import game.GameState.minXTile
import game.GameState.playSound
import game.GameState.playSpaceOffsetX
import game.GameState.playSpaceOffsetY
import game.GameState.tileSize
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt


object Game {
    fun init(mapUrl: String, sceneType: SceneType) {
        GameState.sceneType = sceneType
        GameState.loadMap(mapUrl)
    }

    fun setWindowProperties(windowHeight: Int, windowWidth: Int) {
        GameState.windowHeight = windowHeight
        GameState.windowWidth = windowWidth
        // Rounded to nearest power of 2
        tileSize = 2.0.pow(log2(GameState.windowWidth / 32.0).roundToInt()).toInt()
        setPlaySpaceOffset()
    }

    fun setPlaySpaceOffset() {
        playSpaceOffsetY = -(3 * tileSize)

        if (GameState.map.isEmpty()) return
        val playSpaceXSizeInTiles = (maxXTile - minXTile)
        playSpaceOffsetX = if (abs(tileSize * playSpaceXSizeInTiles) < GameState.windowWidth) {
            -(GameState.windowWidth / 2 - (tileSize * playSpaceXSizeInTiles) / 2) + minXTile * tileSize
        } else {
            minXTile * tileSize
        }
    }

    fun update() {
        when (GameState.sceneType) {
            SceneType.Editor -> {
                if (Input.Mouse1.isPressed()) {
                    val hit = GameState.uiElements.firstOrNull { ui ->
                        val outputPositionX = ui.outputPositionXTile * tileSize
                        val outputPositionY = ui.outputPositionYTile * tileSize
                        GameState.mousePositionX in outputPositionX..<(outputPositionX + tileSize) &&
                                GameState.mousePositionY in outputPositionY..<(outputPositionY + tileSize)
                    }
                    if (hit != null) {
                        GameState.selectedUIElement = hit
                    } else if (GameState.selectedUIElement != null) {
                        val totalOffsetX = GameState.cameraOffsetX + playSpaceOffsetX
                        val gridX = if (GameState.mousePositionX + totalOffsetX < 0) {
                            (GameState.mousePositionX + totalOffsetX - tileSize) / tileSize
                        } else {
                            (GameState.mousePositionX + totalOffsetX) / tileSize
                        }

                        val validPlaySpaceOffsetY = -(GameState.SIZE_Y_IN_TILES * GameState.tileSize / 2)
                        val gridY =
                            if (GameState.mousePositionY > (GameState.windowHeight / 2 - validPlaySpaceOffsetY)) {
                                ((GameState.windowHeight / 2 - validPlaySpaceOffsetY) - GameState.mousePositionY - tileSize) / tileSize
                            } else {
                                ((GameState.windowHeight / 2 - validPlaySpaceOffsetY) - GameState.mousePositionY) / tileSize
                            }

                        GameState.map.removeAll { it.gridPositionX == gridX && it.gridPositionY == gridY }
                        GameState.map.add(
                            MapEntity(
                                gridPositionX = gridX,
                                gridPositionY = gridY,
                                entity = GameState.selectedUIElement!!.entityType
                            )
                        )
                        Map.save()
                        GameState.initialiseRenderables()
                    }
                }
                if (Input.Mouse2.isPressed()) {
                    val totalOffsetX = GameState.cameraOffsetX + GameState.playSpaceOffsetX
                    val gridX = if (GameState.mousePositionX + totalOffsetX < 0) {
                        (GameState.mousePositionX + totalOffsetX - tileSize) / tileSize
                    } else {
                        (GameState.mousePositionX + totalOffsetX) / tileSize
                    }

                    val validPlaySpaceOffsetY = -(GameState.SIZE_Y_IN_TILES * GameState.tileSize / 2)
                    val gridY = if (GameState.mousePositionY > (GameState.windowHeight / 2 - validPlaySpaceOffsetY)) {
                        ((GameState.windowHeight / 2 - validPlaySpaceOffsetY) - GameState.mousePositionY - tileSize) / tileSize
                    } else {
                        ((GameState.windowHeight / 2 - validPlaySpaceOffsetY) - GameState.mousePositionY) / tileSize
                    }
                    GameState.map.removeAll { it.gridPositionX == gridX && it.gridPositionY == gridY }
                    Map.save()
                    GameState.initialiseRenderables()
                }
                if (Input.KeyboardP.isNewlyPressed()) {
                    GameState.loadMap(sceneType = SceneType.Play)
                    setPlaySpaceOffset()
                }
                if (Input.KeyboardW.isNewlyPressed()) {
                    GameState.autoLoadNextMap()
                }
                if (Input.KeyboardS.isNewlyPressed()) {
                    GameState.autoLoadPrevMap()
                }
                val cameraMoveSpeed = 2
                if (Input.KeyboardD.isPressed()) {
                    GameState.cameraOffsetX += cameraMoveSpeed * tileSize
                }
                if (Input.KeyboardA.isPressed()) {
                    GameState.cameraOffsetX -= cameraMoveSpeed * tileSize
                }
            }

            SceneType.Play -> {
                val speed = 2f / 64
                val maxVelocity = 10f / 64
                val friction = 1f / 64

                if (Input.KeyboardD.isPressed() || Input.SwitchControllerDPadRight.isPressed() || Input.SwitchControllerLJoyStickRight.isPressed()) {
                    GameState.playerVelocityXInTiles = min(GameState.playerVelocityXInTiles + speed, maxVelocity)
                    playSound("Walk")
                }
                if (Input.KeyboardA.isPressed() || Input.SwitchControllerDPadLeft.isPressed() || Input.SwitchControllerLJoyStickLeft.isPressed()) {
                    GameState.playerVelocityXInTiles = max(GameState.playerVelocityXInTiles - speed, -maxVelocity)
                    playSound("Walk")
                }

                GameState.map.toList().map { mapEntity ->
                    Physics.executeIfPlayerIsCollidingWithTile(mapEntity) { _, _, _ ->
                        when (mapEntity.entity) {
                            EntityType.Finish -> {
                                GameState.autoLoadNextMap()
                                playSound("Door")
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
                                playSound("Pickup")
                            }

                            else -> {}
                        }
                    }
                }

                val maxJumpVelocity = 31f / 64
                val jumpSpeed = 10f / 64
                val gravity = 6f / 64
                // AI-generated: added controller input support
                if ((Input.KeyboardW.isPressed() || Input.SwitchControllerA.isPressed()) && GameState.playerIsJumping) {
                    GameState.playerVelocityYInTiles =
                        min(GameState.playerVelocityYInTiles + jumpSpeed, maxJumpVelocity)
                }

                if ((Input.KeyboardW.isNewlyPressed() || Input.SwitchControllerA.isNewlyPressed()) && GameState.playerIsGrounded) {
                    GameState.playerIsJumping = true
                    playSound("Jump")
                    GameState.playerVelocityYInTiles =
                        min(GameState.playerVelocityYInTiles + jumpSpeed, maxJumpVelocity)
                }

                if ((!Input.KeyboardW.isPressed() && !Input.SwitchControllerA.isPressed()) || GameState.playerVelocityYInTiles >= maxJumpVelocity) GameState.playerIsJumping =
                    false


                processTerrainCollisions()

                if (GameState.playerVelocityXInTiles > 0) {
                    GameState.playerDirection = 1
                    GameState.playerVelocityXInTiles = max(GameState.playerVelocityXInTiles - friction, 0f)
                } else if (GameState.playerVelocityXInTiles < 0) {
                    GameState.playerDirection = -1
                    GameState.playerVelocityXInTiles = min(GameState.playerVelocityXInTiles + friction, 0f)
                }
                if (!GameState.playerIsGrounded) {
                    GameState.playerVelocityYInTiles -= gravity
                }
                GameState.playerCurrentAnimationFrame += 1
                GameState.backgroundOffsetY -= 1

                if (Input.KeyboardE.isNewlyPressed()) {
                    GameState.loadMap(sceneType = SceneType.Editor)
                }

                // Scroll screen
                if (GameState.map.isNotEmpty()) {
                    val maxX = maxXTile * tileSize + tileSize + 2 * tileSize
                    val minX = minXTile * tileSize
                    val playerOnScreenPositionX = GameState.playerWorldX - playSpaceOffsetX
                    val diffX = maxX - minX
                    if (playerOnScreenPositionX > GameState.windowWidth / 2 && maxX >= (GameState.windowWidth + GameState.cameraOffsetX) && diffX > GameState.windowWidth) {
                        GameState.cameraOffsetX =
                            (playerOnScreenPositionX - (GameState.windowWidth / 2)).toInt()
                    }
                }

                // Check fall
                if (GameState.playerEntity != null) {
                    if (GameState.playerEntity!!.gridPositionY - GameState.playerPositionYOffsetInTiles < -2) {
                        GameState.playerPositionXOffsetInTiles = GameState.playerEntity?.gridPositionX?.toFloat()!!
                        GameState.playerPositionYOffsetInTiles = GameState.playerEntity?.gridPositionY?.toFloat()!! - 1
                        GameState.cameraOffsetX = 0
                        playSound("Fall")
                    }
                }

                // Check finish level
                if (GameState.map.count { it.entity == EntityType.Strawberry } == 0 && GameState.map.none { it.entity == EntityType.Finish }) {
                    GameState.autoLoadNextMap()
                    playSound("Door")
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
        val terrainEntities = listOf(
            EntityType.Terrain,
            EntityType.WoodBox,
            EntityType.GrassLeft,
            EntityType.GrassMiddle,
            EntityType.GrassRight,
            EntityType.DirtLeft,
            EntityType.DirtMiddle,
            EntityType.DirtRight,

            )
        // AI-gen: separate X movement from Y movement to prevent edge-landing bug
        GameState.playerPositionXOffsetInTiles += GameState.playerVelocityXInTiles

        // AI-gen: handle collisions using separate X then Y resolution
        val playerEntityType = GameState.map.find { it.entity == EntityType.Player }
        if (playerEntityType != null) {
            val terrainBlocks = GameState.map.filter { it.entity in terrainEntities }

            val pWorldX = GameState.playerWorldX
            val pWorldY = GameState.playerWorldY
            // AI-gen: resolve X collisions first (no axis-picking ambiguity)
            for (block in terrainBlocks) {

                val playerRight = pWorldX + tileSize
                val playerTop = pWorldY + tileSize

                val blockLeft = block.gridPositionX * GameState.tileSize
                val blockRight = blockLeft + GameState.tileSize
                val blockBottom = block.gridPositionY * GameState.tileSize
                val blockTop = blockBottom + GameState.tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerTop > blockBottom && pWorldY < blockTop
                ) {
                    val overlapLeft = playerRight - blockLeft
                    val overlapRight = blockRight - pWorldX
                    if (overlapLeft < overlapRight) {
                        GameState.playerPositionXOffsetInTiles =
                            block.gridPositionX - 1.0f - playerEntityType.gridPositionX
                    } else {
                        GameState.playerPositionXOffsetInTiles =
                            block.gridPositionX + 1.0f - playerEntityType.gridPositionX
                    }
                    GameState.playerVelocityXInTiles = 0f
                }
            }

            // AI-gen: now move Y separately
            GameState.playerPositionYOffsetInTiles -= GameState.playerVelocityYInTiles

            // AI-gen: grounded check (snap Y to block top if within tolerance)
            GameState.playerIsGrounded = false
            val groundedTolerance = 2f
            for (block in terrainBlocks) {
                val pWorldX = GameState.playerWorldX
                val pWorldY = GameState.playerWorldY

                val playerRight = pWorldX + GameState.tileSize
                val playerBottom = pWorldY

                val blockLeft = block.gridPositionX * GameState.tileSize
                val blockRight = blockLeft + GameState.tileSize
                val blockTop = block.gridPositionY * GameState.tileSize + GameState.tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerBottom >= blockTop - groundedTolerance && playerBottom <= blockTop + groundedTolerance
                ) {
                    GameState.playerIsGrounded = true
                    GameState.playerPositionYOffsetInTiles =
                        playerEntityType.gridPositionY - (block.gridPositionY + 1.0f)
                    GameState.playerVelocityYInTiles = 0f
                    break
                }
            }

            // AI-gen: resolve Y collisions (only vertical, no more axis-picking)
            for (block in terrainBlocks) {
                val pWorldX = GameState.playerWorldX
                val pWorldY = GameState.playerWorldY

                val playerRight = pWorldX + GameState.tileSize
                val playerTop = pWorldY + GameState.tileSize

                val blockLeft = block.gridPositionX * GameState.tileSize
                val blockRight = blockLeft + GameState.tileSize
                val blockBottom = block.gridPositionY * tileSize
                val blockTop = blockBottom + tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerTop > blockBottom && pWorldY < blockTop
                ) {
                    val overlapTop = blockTop - pWorldY
                    val overlapBottom = playerTop - blockBottom
                    if (overlapTop < overlapBottom) {
                        GameState.playerPositionYOffsetInTiles =
                            playerEntityType.gridPositionY - (block.gridPositionY + 1.0f)
                        GameState.playerVelocityYInTiles = 0f
                        GameState.playerIsGrounded = true
                    } else {
                        GameState.playerPositionYOffsetInTiles =
                            playerEntityType.gridPositionY - (block.gridPositionY - 1.0f)
                        GameState.playerVelocityYInTiles = 0f
                    }
                }
            }
        }
    }
}
