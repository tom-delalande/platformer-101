import engine.engineData
import kotlin.math.max
import kotlin.math.min
import kotlinx.serialization.json.Json
import logic.Entity
import logic.Input
import logic.MapEntity
import logic.SceneType
import logic.model

private val json = Json { prettyPrint = true }

fun update() {
    when (model.sceneType) {
        SceneType.Editor -> {
            if (Input.Mouse1.isNewlyPressed()) {
                val hit = model.uiElements.firstOrNull { ui ->
                    model.mousePositionX in ui.outputPositionX..<(ui.outputPositionX + ui.outputWidth) &&
                            model.mousePositionY in ui.outputPositionY..<(ui.outputPositionY + ui.outputHeight)
                }
                if (hit != null) {
                    model = model.copy(selectedUIElement = hit)
                } else if (model.selectedUIElement != null) {
                    val gridX = model.mousePositionX / 64
                    val gridY = (engineData.windowHeight / 64) - (model.mousePositionY / 64)
                    model = model.copy(
                        map = model.map + MapEntity(
                            gridPositionX = gridX,
                            gridPositionY = gridY,
                            entity = model.selectedUIElement!!.sprite
                        )
                    )
                    println(model.map)
                }
            }
            if (Input.Mouse2.isNewlyPressed()) {
                val gridX = model.mousePositionX / 64
                val gridY = (engineData.windowHeight / 64) - (model.mousePositionY / 64)
                model = model.copy(
                    map = model.map.filterNot { it.gridPositionX == gridX && it.gridPositionY == gridY }
                )
            }

            if (Input.KeyboardS.isNewlyPressed()) {
                writeTextFile("map.json", json.encodeToString(model.map))
                println("map.json saved")
            }
            if (Input.KeyboardL.isNewlyPressed()) {
                val content = readTextFile("map.json")
                if (content != null) {
                    model = model.copy(map = json.decodeFromString(content))
                    println("map.json loaded")
                }
            }
            if (Input.KeyboardP.isNewlyPressed()) {
                model.sceneType = SceneType.Play
            }
        }

        SceneType.Play -> {
            val speed = 2f
            val maxVelocity = 10f
            val friction = 0.2f

            if (Input.KeyboardD.isPressed()) {
                model.playerVelocityX = min(model.playerVelocityX + speed, maxVelocity)
            }
            if (Input.KeyboardA.isPressed()) {
                model.playerVelocityX = max(model.playerVelocityX - speed, -maxVelocity)
            }

            val maxJumpVelocity = 30f
            val jumpSpeed = 10f
            val gravity = 6f
            if (Input.KeyboardW.isPressed() && model.playerIsJumping) {
                model.playerVelocityY = min(model.playerVelocityY + jumpSpeed, maxJumpVelocity)
            }

            if (Input.KeyboardW.isNewlyPressed() && model.playerIsGrounded) {
                model.playerIsJumping = true
                model.playerVelocityY = min(model.playerVelocityY + jumpSpeed, maxJumpVelocity)
            }

            if (!Input.KeyboardW.isPressed() || model.playerVelocityY >= maxJumpVelocity) model.playerIsJumping = false

            model.playerPositionX += model.playerVelocityX
            model.playerPositionY -= model.playerVelocityY // y is inverse

            // AI-1: handle collisions and set player is grounded here using model.map (which stores all the coordinates of blocks) and model.playerPositionX and model.playerPositionY
            // Player position is using the pixel value, while the map block use the co-ordiates with bottom left being 0,0
            // Player should collide with and walk on all blocks
            val playerEntity = model.map.find { it.entity == Entity.Player }
            if (playerEntity != null) {
                val terrainBlocks = model.map.filter { it.entity == Entity.Terrain }
                val tileSize = 64f

                model.playerIsGrounded = false

                for (block in terrainBlocks) {
                    val pWorldX = playerEntity.gridPositionX * tileSize + model.playerPositionX
                    val pWorldY = playerEntity.gridPositionY * tileSize - model.playerPositionY

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
                        val overlapBottom = playerTop - blockBottom
                        val overlapTop = blockTop - pWorldY

                        val minOverlapX = min(overlapLeft, overlapRight)
                        val minOverlapY = min(overlapBottom, overlapTop)

                        if (minOverlapX < minOverlapY) {
                            if (overlapLeft < overlapRight) {
                                model.playerPositionX = blockLeft - tileSize - playerEntity.gridPositionX * tileSize
                            } else {
                                model.playerPositionX = blockRight - playerEntity.gridPositionX * tileSize
                            }
                        } else {
                            if (overlapTop < overlapBottom) {
                                model.playerPositionY = playerEntity.gridPositionY * tileSize - blockTop
                                model.playerVelocityY = 0f
                                model.playerIsGrounded = true
                            } else {
                                model.playerPositionY = playerEntity.gridPositionY * tileSize - (blockBottom - tileSize)
                                model.playerVelocityY = 0f
                            }
                        }
                    }
                }

                val groundedTolerance = 2f
                for (block in terrainBlocks) {
                    val pWorldX = playerEntity.gridPositionX * tileSize + model.playerPositionX
                    val pWorldY = playerEntity.gridPositionY * tileSize - model.playerPositionY

                    val playerRight = pWorldX + tileSize
                    val playerBottom = pWorldY

                    val blockLeft = block.gridPositionX * tileSize
                    val blockRight = blockLeft + tileSize
                    val blockTop = block.gridPositionY * tileSize + tileSize

                    if (playerRight > blockLeft && pWorldX < blockRight &&
                        playerBottom >= blockTop - groundedTolerance && playerBottom <= blockTop + groundedTolerance
                    ) {
                        model.playerIsGrounded = true
                        model.playerPositionY = playerEntity.gridPositionY * tileSize - blockTop
                        break
                    }
                }
            }

            if (model.playerVelocityX > 0) {
                model.playerVelocityX = max(model.playerVelocityX - friction, 0f)
            } else {
                model.playerVelocityX = min(model.playerVelocityX + friction, 0f)
            }
            if (!model.playerIsGrounded) {
                model.playerVelocityY -= gravity
            }
            model.playerCurrentAnimationFrame += 1
        }
    }
}


fun Input.isNewlyPressed() = model.isPressed.contains(this) && !model.wasPressed.contains(this)
fun Input.isPressed() = model.isPressed.contains(this)
