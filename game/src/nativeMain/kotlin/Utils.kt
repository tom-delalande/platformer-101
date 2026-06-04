package game

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind

@OptIn(ExperimentalForeignApi::class)
actual fun writeTextFile(path: String, text: String) {
    val file = fopen(path, "w") ?: return
    fputs(text, file)
    fclose(file)
}

@OptIn(ExperimentalForeignApi::class)
actual fun readTextFile(path: String): String? {
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
