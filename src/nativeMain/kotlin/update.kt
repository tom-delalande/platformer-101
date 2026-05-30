import logic.SceneType
import logic.model

fun update() {
    when (model.sceneType) {
        SceneType.Editor -> {
            if (model.isMouse1Pressed && !model.wasMouse1Pressed) {
                model.uiElements.forEach {

                }
            }
        }
        SceneType.Play -> TODO()
    }
}
