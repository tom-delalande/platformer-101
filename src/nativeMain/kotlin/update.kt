import engine.engineData
import kotlin.math.max
import kotlin.math.min
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import logic.Entity
import logic.Input
import logic.MapEntity
import logic.SceneType
import logic.model

val json = Json { prettyPrint = true }

@OptIn(ExperimentalForeignApi::class)
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
                            entity = model.selectedUIElement!!.entity
                        )
                    )
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
            val friction = 1f

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

            // AI-gen: separate X movement from Y movement to prevent edge-landing bug
            model.playerPositionX += model.playerVelocityX

            // AI-gen: handle collisions using separate X then Y resolution
            val playerEntity = model.map.find { it.entity == Entity.Player }
            if (playerEntity != null) {
                val terrainBlocks = model.map.filter { it.entity == Entity.Terrain || it.entity == Entity.WoodBox }
                val tileSize = 64f

                val pWorldX = playerEntity.gridPositionX * tileSize + model.playerPositionX
                val pWorldY = playerEntity.gridPositionY * tileSize - model.playerPositionY
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
                            model.playerPositionX = blockLeft - tileSize - playerEntity.gridPositionX * tileSize
                        } else {
                            model.playerPositionX = blockRight - playerEntity.gridPositionX * tileSize
                        }
                        model.playerVelocityX = 0f
                    }
                }

                // AI-gen: now move Y separately
                model.playerPositionY -= model.playerVelocityY

                // AI-gen: grounded check (snap Y to block top if within tolerance)
                model.playerIsGrounded = false
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
                        model.playerVelocityY = 0f
                        break
                    }
                }

                // AI-gen: resolve Y collisions (only vertical, no more axis-picking)
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
                        val overlapTop = blockTop - pWorldY
                        val overlapBottom = playerTop - blockBottom
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
            // AI-gen: end collision

            if (model.playerVelocityX > 0) {
                model.playerDirection = 1
                model.playerVelocityX = max(model.playerVelocityX - friction, 0f)
            } else if (model.playerVelocityX < 0) {
                model.playerDirection = -1
                model.playerVelocityX = min(model.playerVelocityX + friction, 0f)
            }
            if (!model.playerIsGrounded) {
                model.playerVelocityY -= gravity
            }
            model.playerCurrentAnimationFrame += 1
            model.backgroundOffsetY -= 1

            if (Input.KeyboardE.isNewlyPressed()) {
                model.sceneType = SceneType.Editor
            }
        }
    }
}


fun Input.isNewlyPressed() = model.isPressed.contains(this) && !model.wasPressed.contains(this)
fun Input.isPressed() = model.isPressed.contains(this)
