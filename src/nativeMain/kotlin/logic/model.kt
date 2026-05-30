package logic

var model = Model(
    sceneType = SceneType.Editor
)

data class Model(
    val sceneType: SceneType,
)

enum class SceneType {
    Editor
}