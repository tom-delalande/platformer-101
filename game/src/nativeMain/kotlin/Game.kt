package game

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
        if (GameState.map.isEmpty()) return

        val maxYTile = GameState.map.maxOf { it.gridPositionY }
        val minYTile = GameState.map.minOf { it.gridPositionY }
        val playSpaceYSizeInTiles = (maxYTile - minYTile)
        playSpaceOffsetY = -(GameState.windowHeight / 2 - (tileSize * playSpaceYSizeInTiles) / 1.5).toInt()

        val maxXTile = GameState.map.maxOf { it.gridPositionX }
        val minXTile = GameState.map.minOf { it.gridPositionX }
        val playSpaceXSizeInTiles = (maxXTile - minXTile)
        if (abs(tileSize * playSpaceXSizeInTiles) < GameState.windowWidth) {
            playSpaceOffsetX = -(GameState.windowWidth / 2 - (tileSize * playSpaceXSizeInTiles) / 2)
        } else {
            playSpaceOffsetX = 0
        }
    }

    fun update() {
        when (GameState.sceneType) {
            SceneType.Editor -> {
                if (Input.Mouse1.isPressed()) {
                    println("mouse pressed")
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
                        val gridX = ((GameState.mousePositionX + totalOffsetX) / tileSize).let {
                            if (it < 0) {
                                it - 1
                            } else {
                                it
                            }
                        }
                        val gridY =
                            ((GameState.windowHeight - GameState.mousePositionY + tileSize + playSpaceOffsetY) / tileSize)
                        if (GameState.map.none { it.gridPositionX == gridX && it.gridPositionY == gridY }) {
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
                }
                if (Input.Mouse2.isPressed()) {
                    val totalOffsetX = GameState.cameraOffsetX + GameState.playSpaceOffsetX
                    val gridX = ((GameState.mousePositionX + totalOffsetX) / GameState.tileSize).let {
                        if (it < 0) {
                            it - 1
                        } else {
                            it
                        }
                    }
                    val gridY =
                        (GameState.windowHeight - GameState.mousePositionY + GameState.tileSize + playSpaceOffsetY) / GameState.tileSize
                    GameState.map.removeAll { it.gridPositionX == gridX && it.gridPositionY == gridY }
                    Map.save()
                    GameState.initialiseRenderables()
                }

                if (Input.KeyboardS.isNewlyPressed()) {
                    Map.save()
                }
                if (Input.KeyboardL.isNewlyPressed()) {
                    Map.load()
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
                val cameraMoveSpeed = 30
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

                // AI-generated: added controller input support
                if (Input.KeyboardD.isPressed() || Input.ControllerRight.isPressed()) {
                    GameState.playerVelocityX = min(GameState.playerVelocityX + speed, maxVelocity)
                    playSound("Walk")
                }
                if (Input.KeyboardA.isPressed() || Input.ControllerLeft.isPressed()) {
                    GameState.playerVelocityX = max(GameState.playerVelocityX - speed, -maxVelocity)
                    playSound("Walk")
                }

                GameState.map.toList().map { mapEntity ->
                    Physics.executeIfPlayerIsCollidingWithTile(mapEntity) { _, _, _ ->
                        when (mapEntity.entity) {
                            EntityType.Finish -> {
                                GameState.autoLoadNextMap()
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

                val maxJumpVelocity = 31f
                val jumpSpeed = 10f
                val gravity = 6f
                // AI-generated: added controller input support
                if ((Input.KeyboardW.isPressed() || Input.ControllerUp.isPressed()) && GameState.playerIsJumping) {
                    GameState.playerVelocityY = min(GameState.playerVelocityY + jumpSpeed, maxJumpVelocity)
                }

                if ((Input.KeyboardW.isNewlyPressed() || Input.ControllerUp.isNewlyPressed()) && GameState.playerIsGrounded) {
                    GameState.playerIsJumping = true
                    playSound("Jump")
                    GameState.playerVelocityY = min(GameState.playerVelocityY + jumpSpeed, maxJumpVelocity)
                }

                if ((!Input.KeyboardW.isPressed() && !Input.ControllerUp.isPressed()) || GameState.playerVelocityY >= maxJumpVelocity) GameState.playerIsJumping =
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
                if (GameState.map.isNotEmpty()) {
                    val maxX = GameState.map.maxOf { it.gridPositionX } * GameState.tileSize
                    if (GameState.playerPositionX > GameState.windowWidth / 2 && maxX >= GameState.windowWidth + GameState.cameraOffsetX - tileSize) {
                        GameState.cameraOffsetX = (GameState.playerPositionX - (GameState.windowWidth / 2)).toInt()
                    }
                }

                // Check finish level
                if (GameState.map.count { it.entity == EntityType.Strawberry } == 0) {
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
        // AI-gen: separate X movement from Y movement to prevent edge-landing bug
        GameState.playerPositionX += GameState.playerVelocityX

        // AI-gen: handle collisions using separate X then Y resolution
        val playerEntityType = GameState.map.find { it.entity == EntityType.Player }
        if (playerEntityType != null) {
            val terrainBlocks =
                GameState.map.filter { it.entity == EntityType.Terrain || it.entity == EntityType.WoodBox }

            val pWorldX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX
            val pWorldY = playerEntityType.gridPositionY * GameState.tileSize - GameState.playerPositionY
            // AI-gen: resolve X collisions first (no axis-picking ambiguity)
            for (block in terrainBlocks) {

                val playerRight = pWorldX + GameState.tileSize
                val playerTop = pWorldY + GameState.tileSize

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
                        GameState.playerPositionX =
                            blockLeft - GameState.tileSize - playerEntityType.gridPositionX * GameState.tileSize.toFloat()
                    } else {
                        GameState.playerPositionX =
                            blockRight - playerEntityType.gridPositionX * GameState.tileSize.toFloat()
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
                val pWorldX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX
                val pWorldY = playerEntityType.gridPositionY * GameState.tileSize - GameState.playerPositionY

                val playerRight = pWorldX + GameState.tileSize
                val playerBottom = pWorldY

                val blockLeft = block.gridPositionX * GameState.tileSize
                val blockRight = blockLeft + GameState.tileSize
                val blockTop = block.gridPositionY * GameState.tileSize + GameState.tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerBottom >= blockTop - groundedTolerance && playerBottom <= blockTop + groundedTolerance
                ) {
                    GameState.playerIsGrounded = true
                    GameState.playerPositionY = playerEntityType.gridPositionY * GameState.tileSize.toFloat() - blockTop
                    GameState.playerVelocityY = 0f
                    break
                }
            }

            // AI-gen: resolve Y collisions (only vertical, no more axis-picking)
            for (block in terrainBlocks) {
                val pWorldX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX
                val pWorldY = playerEntityType.gridPositionY * GameState.tileSize - GameState.playerPositionY

                val playerRight = pWorldX + GameState.tileSize
                val playerTop = pWorldY + GameState.tileSize

                val blockLeft = block.gridPositionX * GameState.tileSize
                val blockRight = blockLeft + GameState.tileSize
                val blockBottom = block.gridPositionY * GameState.tileSize
                val blockTop = blockBottom + GameState.tileSize

                if (playerRight > blockLeft && pWorldX < blockRight &&
                    playerTop > blockBottom && pWorldY < blockTop
                ) {
                    val overlapTop = blockTop - pWorldY
                    val overlapBottom = playerTop - blockBottom
                    if (overlapTop < overlapBottom) {
                        GameState.playerPositionY =
                            playerEntityType.gridPositionY * GameState.tileSize.toFloat() - blockTop
                        GameState.playerVelocityY = 0f
                        GameState.playerIsGrounded = true
                    } else {
                        GameState.playerPositionY =
                            playerEntityType.gridPositionY * GameState.tileSize - (blockBottom - GameState.tileSize.toFloat())
                        GameState.playerVelocityY = 0f
                    }
                }
            }
        }
    }
}
