import logic.model

fun gameInit() {
    val content = readTextFile("map.json")
    if (content != null) {
        model = model.copy(map = json.decodeFromString(content))
        println("map.json loaded")
    }
}