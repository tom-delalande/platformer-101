import engine.engineData
import kotlin.math.max
import kotlin.math.min
import kotlinx.serialization.json.Json
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
            val speed = 1f
            val maxVelocity = 5f
            val friction = 0.2f
            if (Input.KeyboardD.isPressed()) {
                model.playerVelocityX = max(model.playerVelocityX + speed, maxVelocity)
            }
            if (Input.KeyboardA.isPressed()) {
                model.playerVelocityX = min(model.playerVelocityX - speed, -maxVelocity)
            }


            model.playerPositionX += model.playerVelocityX
            if (model.playerVelocityX > 0) {
                model.playerVelocityX = max(model.playerVelocityX - friction, 0f)
            } else {
                model.playerVelocityX = min(model.playerVelocityX + friction, 0f)
            }
            model.playerCurrentAnimationFrame += 1
        }
    }
}


fun Input.isNewlyPressed() = model.isPressed.contains(this) && !model.wasPressed.contains(this)
fun Input.isPressed() = model.isPressed.contains(this)
