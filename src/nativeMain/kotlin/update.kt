import engine.engineData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import logic.Input
import logic.MapEntity
import logic.SceneType
import logic.model
import platform.posix.*

private val json = Json { prettyPrint = true }

@OptIn(ExperimentalForeignApi::class)
private fun writeTextFile(path: String, text: String) {
    val file = fopen(path, "w") ?: return
    fputs(text, file)
    fclose(file)
}

@OptIn(ExperimentalForeignApi::class)
private fun readTextFile(path: String): String? {
    val file = fopen(path, "r") ?: return null
    fseek(file, 0, SEEK_END)
    val size = ftell(file)
    if (size <= 0) { fclose(file); return null }
    rewind(file)
    val bytes = ByteArray(size.toInt())
    bytes.usePinned { pinned ->
        fread(pinned.addressOf(0), 1u, size.toULong(), file)
    }
    fclose(file)
    return bytes.decodeToString()
}

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
            val speed = 0.5f
            if (Input.KeyboardD.isPressed()) {
                model.playerPositionX += speed
            }
            if (Input.KeyboardA.isPressed()) {
                model.playerPositionX -= speed
            }
        }
    }
}


fun Input.isNewlyPressed() = model.isPressed.contains(this) && !model.wasPressed.contains(this)
fun Input.isPressed() = model.isPressed.contains(this)
