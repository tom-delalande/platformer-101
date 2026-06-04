package game

import java.io.File

actual fun writeTextFile(path: String, text: String) {
    File(path).writeText(text)
}

actual fun readTextFile(path: String): String? {
    val file = File(path)
    return if (file.exists()) file.readText() else null
}
